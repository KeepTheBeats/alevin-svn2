package vnreal.core;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;


import vnreal.algorithms.AbstractAlgorithm;
import vnreal.algorithms.AlgorithmParameter;
import vnreal.evaluations.metrics.EvaluationMetric;
import vnreal.io.IResultExporter;
import vnreal.io.experiment.AlgorithmType;
import vnreal.io.experiment.Experiment;
import vnreal.io.experiment.ExporterType;
import vnreal.io.experiment.GeneratorElementType;
import vnreal.io.experiment.MetricType;
import vnreal.io.experiment.ParamType;
import vnreal.io.experiment.RNGType;
import vnreal.network.NetworkStack;

public class Orchestrator {
	public static void execute(String xmlfile) throws SAXException, JAXBException, ClassNotFoundException {
		SchemaFactory schemaFactory = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );
	    Schema schema = schemaFactory.newSchema(new File(vnreal.core.Consts.EXPERIMENT_XML));
	    JAXBContext jaxbContext = JAXBContext.newInstance("vnreal.io.experiment");
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		unmarshaller.setSchema(schema);
		Experiment exp = (Experiment) unmarshaller.unmarshal(new File(xmlfile));
		
		Registry reg = Registry.getInstance();
		
		// First, extract the parameters from the Experiment specification 
		LinkedHashMap<String, Map<String, List<String>>> params = getParameters(exp);
				
		// Second, create all possible combinations of parameter values
		List<LinkedHashMap<String, Map<String, String>>> runParams = getRunParams(params);
		
		// Prepare Metrics and Exporters in advance
		List<EvaluationMetric<NetworkStack>> metrics = new LinkedList<EvaluationMetric<NetworkStack>>();
		for (MetricType metric : exp.getEvaluation().getMetric()) {
			metrics.add(reg.getMetric(metric.getName()));
		}
		List<IResultExporter> exportersL = new LinkedList<IResultExporter>();
		Experiment.Exporters exporters = exp.getExporters();
		for (ExporterType exporterT : exporters.getExporter()) {
			
			HashMap<String, String> expParam = new HashMap<String, String>();
			for (ParamType param : exporterT.getParameter()) {
				expParam.put(param.getName(), param.getValue());				
			}
			IResultExporter exporter = reg.getExporter(exporterT.getName());
			exporter.init(exporters.getResultsDir(), expParam);
			exportersL.add(exporter);
		}

				
		for (AlgorithmType algoT : exp.getAlgorithm()) {  // This is currently buggy! At the moment, only the last algorithm is actually employed!
			// Set up the algorithm
			AlgorithmParameter algoParam = new AlgorithmParameter();
			for (ParamType param : algoT.getParameter()) {
				algoParam.put(param.getName(), param.getValue());
			}
			
			// Build and execute the run
			for (LinkedHashMap<String, Map<String, String>> parSet : runParams) {
				ScenarioGeneratorChain generator = new ScenarioGeneratorChain();
				generator.init(parSet);
				Scenario scen = generator.generate();
				generator.reset(); // Need to reset the generator for the next round
				Run run = new Run(scen, parSet);
				
				// Unfortunately, some algorithms currently keep state and thus must
				// be initialized here (instead of once outside the loop)
				AbstractAlgorithm algo = reg.getAlgorithm(algoT.getName(), algoParam);
				run.setAlgorithm(algo);
				
				// Perform the actual embedding
				run.process();
				
				run.evaluate(metrics);
				
				// Finally, export the results
				for (IResultExporter exporter : exportersL)
					exporter.export(run);
			}
		}
		
		// Finalize all exporters
		for (IResultExporter exporter : exportersL)
			exporter.close();
		
	}
	
	/**
	 * This method will extract all parameters from an Experiment specification.
	 * @param exp The Experiment specification
	 * @return An ordered Map (LinkedHashMap) associating generators with their parameters. A single parameter
	 * for any generator can have multiple values.
	 */
	private static LinkedHashMap<String, Map<String, List<String>>> getParameters(Experiment exp) {
		LinkedHashMap<String, Map<String, List<String>>> params = new LinkedHashMap<String, Map<String, List<String>>>();
		
		// The Random Number Generator is a special element - has to be specially extracted here
		
		RNGType rng = exp.getGenerators().getRNG();
		
		Map<String, List<String>> rngSeeds = new HashMap<String, List<String>>();
		String seeds = rng.getRandomSeeds();
		if (seeds == null) {
			seeds = rng.getOrderedSeeds();
			if (seeds == null) {
				rngSeeds.put("Seed", rng.getSeed());
			} else { // The user wants ordered seeds from 0 to the value of "seeds" - 1
				int num = Integer.parseInt(seeds);
				List<String> randSeedList = new LinkedList<String>();
				for (int i = 0; i < num; i++) {
					randSeedList.add("" + i);
				}
				rngSeeds.put("Seed", randSeedList);
			}
		} else { // The user doesn't care about particular seeds, but wants us to generate a bunch of them
			int num = Integer.parseInt(seeds);
			List<String> randSeedList = new LinkedList<String>();
			Random initRNG = new Random();
			for (int i = 0; i < num; i++) {
				randSeedList.add("" + initRNG.nextLong());
			}
			rngSeeds.put("Seed", randSeedList);
		}
		params.put("RNG", rngSeeds);
		
		for (GeneratorElementType generator : exp.getGenerators().getGeneratorElement()) {
			Map<String, List<String>> paramMap = new HashMap<String, List<String>>();
			for (ParamType param : generator.getParameter()) {
				if (paramMap.containsKey(param.getName())) {
					paramMap.get(param.getName()).add(param.getValue());
				} else {
					List<String> values = new LinkedList<String>();
					values.add(param.getValue());
					paramMap.put(param.getName(), values);
				}
			}
			params.put(generator.getName(), paramMap);
		}
		return params;
	}
	
	/**
	 * This method will take the list of parameters from an experiment specification and
	 * create all possible parameter combinations. It returns a list of parameter sets to
	 * be used for individual runs.  
	 * @param params
	 * @return
	 */
	private static List<LinkedHashMap<String, Map<String, String>>> getRunParams(LinkedHashMap<String, Map<String, List<String>>> params) {
		List<LinkedHashMap<String, Map<String, String>>> result = null;
		
		for (String generator : params.keySet())
			for (String parameter : params.get(generator).keySet()) {
				result = iterate(result, generator, parameter, params.get(generator).get(parameter));
			}
			
		return result;
	}
	
	/**
	 * This method 
	 * @param current
	 * @param generator
	 * @param param
	 * @param values
	 * @return
	 */
	private static List<LinkedHashMap<String, Map<String, String>>> iterate(List<LinkedHashMap<String, Map<String, String>>> current,
																			String generator, 
																			String param, List<String> values) {
		List<LinkedHashMap<String, Map<String, String>>> result = new LinkedList<LinkedHashMap<String, Map<String, String>>>();
		
		for (String val : values) {
			if (current == null) {
				LinkedHashMap<String, Map<String, String>> firstEntries = new LinkedHashMap<String, Map<String, String>>();
				HashMap<String, String> paramMap = new HashMap<String, String>();
				paramMap.put(param, val);
				firstEntries.put(generator, paramMap);
				result.add(firstEntries);
			} else {
				for (LinkedHashMap<String, Map<String, String>> map : current) {
					LinkedHashMap<String, Map<String, String>> copy = createCopy(map);
					if (copy.containsKey(generator)) {
						copy.get(generator).put(param, val);
					} else {
						HashMap<String, String> paramMap = new HashMap<String, String>();
						paramMap.put(param, val);
						copy.put(generator, paramMap);
					}
					result.add(copy);
				}
			}
		}
		return result;
	}
	
	/**
	 * Create a deep copy of a parameter collection
	 * @param map The LinkedHashMap to copy
	 * @return A deep copy of the LinkedHashMap
	 */
	private static LinkedHashMap<String, Map<String, String>> createCopy(LinkedHashMap<String, Map<String, String>> map) {
		LinkedHashMap<String, Map<String, String>> result = new LinkedHashMap<String, Map<String, String>>();
		for (Map.Entry<String, Map<String, String>> element : map.entrySet()) {
			HashMap<String, String> paramMap = new HashMap<String, String>();
			for (Map.Entry<String, String> param : element.getValue().entrySet()) {
				paramMap.put(param.getKey(), param.getValue());
			}
			result.put(element.getKey(), paramMap);
		}
		return result;
	}
}
