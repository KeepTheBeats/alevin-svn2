package vnreal.core;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Set;


import tests.generators.network.AbstractNetworkStackGenerator;
import vnreal.algorithms.AbstractAlgorithm;
import vnreal.algorithms.AlgorithmParameter;
import vnreal.evaluations.metrics.EvaluationMetric;
import vnreal.generators.demands.AbstractDemandGenerator;
import vnreal.generators.resources.AbstractResourceGenerator;
import vnreal.generators.scenariochain.IChainElement;
import vnreal.io.IResultExporter;
import vnreal.network.NetworkStack;

/**
 * This class serves as a registry which can be used by other classes to load
 * particular VNE algorithms or set up particular metrics, just by their class
 * name. In particular, the registry maintains a list of all implemented
 * algorithms and metrics within a HashMap, indexed by the class simple name.
 * Users can add additional algorithms and metrics by specifying the folders
 * to search.
 * 
 * @author Michael Till Beck
 * @author Andreas Fischer
 * @since Dec 2014
 *
 */
public class Registry {
	
	HashMap<String, Class<EvaluationMetric>> metrics = new HashMap<String, Class<EvaluationMetric>>();
	HashMap<String, Class<AbstractAlgorithm>> algorithms = new HashMap<String, Class<AbstractAlgorithm>>();
	HashMap<String, Class<AbstractResourceGenerator>> resGens = new HashMap<String, Class<AbstractResourceGenerator>>();
	HashMap<String, Class<AbstractDemandGenerator>> demGens = new HashMap<String, Class<AbstractDemandGenerator>>();
	HashMap<String, Class<AbstractNetworkStackGenerator>> netGens = new HashMap<String, Class<AbstractNetworkStackGenerator>>();
	HashMap<String, Class<IChainElement>> scenGenChainElems = new HashMap<String, Class<IChainElement>>();
	HashMap<String, Class<IResultExporter>> exporters = new HashMap<String, Class<IResultExporter>>();



	private static Registry singleton = null;
	
	
	/**
	 * Private constructor to avoid having more than one object.
	 */
	private Registry() {
		
		// By default: Load all algorithms and metrics found in the classpath.
		for (String child : System.getProperty("java.class.path").split(File.pathSeparator)) {
			loadMetrics(new File(child));
			loadAlgorithms(new File(child));
			loadResGens(new File(child));
			loadDemGens(new File(child));
			loadNetGens(new File(child));
			loadScenGenChainElems(new File(child));
			loadExporters(new File(child));
		}
		
	}
	
	/**
	 * Get a new reference to the ALEVIN registry. If it doesn't exist, a new
	 * registry will be created.
	 * 
	 * @return The registry
	 */
	public static Registry getInstance() {
		if (singleton == null)
			singleton = new Registry();
		return singleton;
	}
	
	/**
	 * Load all metrics from a particular folder.
	 * 
	 * @param metricsFolder The folder containing the package structure.
	 * NB: If a metric is part of package
	 * <code>com.example.metrics</code>
	 * and the entire project is located in <code>$PROJECTFOLDER</code>
	 * then <code>metricsFolder</code> should point to
	 * <code>$PROJECTFOLDER</code>, NOT
	 * <code>$PROJECTFOLDER/com/example/metrics</code>
	 */
	public void loadMetrics(File metricsFolder) {
		for (Class<EvaluationMetric> m : ClassLoader.getClasses(metricsFolder, "", EvaluationMetric.class)) {
			metrics.put(m.getSimpleName(), m);
		}
	}
	
	/**
	 * Load all algorithms from a particular folder.
	 * 
	 * @param algorithmsFolder The folder containing the package structure.
	 * NB: If an algorithm is part of package
	 * <code>com.example.algorithms</code>
	 * and the entire project is located in <code>$PROJECTFOLDER</code>
	 * then <code>algorithmsFolder</code> should point to
	 * <code>$PROJECTFOLDER</code>, NOT
	 * <code>$PROJECTFOLDER/com/example/algorithms</code>
	 */
	public void loadAlgorithms(File algorithmsFolder) {
		for (Class<AbstractAlgorithm> a : ClassLoader.getClasses(algorithmsFolder, "", AbstractAlgorithm.class)) {
			algorithms.put(a.getSimpleName(), a);
		}
	}
	
	public void loadResGens(File resGensFolder) {
		for (Class<AbstractResourceGenerator> rg : ClassLoader.getClasses(resGensFolder, "", AbstractResourceGenerator.class)) {
			resGens.put(rg.getSimpleName(), rg);
		}
	}
	
	public void loadDemGens(File demGensFolder) {
		for (Class<AbstractDemandGenerator> dg : ClassLoader.getClasses(demGensFolder, "", AbstractDemandGenerator.class)) {
			demGens.put(dg.getSimpleName(), dg);
		}
	}
	
	public void loadNetGens(File netGensFolder) {
		for (Class<AbstractNetworkStackGenerator> ng : ClassLoader.getClasses(netGensFolder, "", AbstractNetworkStackGenerator.class)) {
			netGens.put(ng.getSimpleName(), ng);
		}
	}
	
	public void loadScenGenChainElems(File scenGensFolder) {
		for (Class<IChainElement> sgcelem : ClassLoader.getClasses(scenGensFolder, "", IChainElement.class)) {
			scenGenChainElems.put(sgcelem.getSimpleName(), sgcelem);
		}
	}
	
	public void loadExporters(File exportersFolder) {
		for (Class<IResultExporter> exporter : ClassLoader.getClasses(exportersFolder, "", IResultExporter.class)) {
			exporters.put(exporter.getSimpleName(), exporter);
		}
	}
	
	/**
	 * Get an instance of a particular algorithm by its simple name.
	 * 
	 * @param algo The simple name of the algorithm class
	 * @param param A set of parameters used to instantiate the algorithm
	 * @return A ready-to-use VNE algorithm instance
	 * @throws ClassNotFoundException if the given simple name does not match
	 * any registered algorithm.
	 */
	public AbstractAlgorithm getAlgorithm(String algo, AlgorithmParameter param) throws ClassNotFoundException {
		for (Constructor<?> c : algorithms.get(algo).getDeclaredConstructors()) {
			if (c.getParameterTypes().length == 1
					&& c.getParameterTypes()[0].getClass() == AlgorithmParameter.class.getClass()) {
				
				try {
					return (AbstractAlgorithm) c.newInstance(param);
				} catch (InstantiationException e) {
					throw new ClassNotFoundException("", e);
				} catch (IllegalAccessException e) {
					throw new ClassNotFoundException("", e);
				} catch (IllegalArgumentException e) {
					throw new ClassNotFoundException("", e);
				} catch (InvocationTargetException e) {
					throw new ClassNotFoundException("", e);
				}
			}
		}
		
		throw new ClassNotFoundException();
	}
	
	/**
	 * Get an instance of a particular metric by its simple name
	 * @param metric The simple name of the metric class
	 * @return A ready to use VNE metric instance
	 * @throws ClassNotFoundException if the given simple name does not match
	 * any registered metric.
	 */
	public EvaluationMetric<NetworkStack> getMetric(String metric) throws ClassNotFoundException {
		for (Constructor<?> c : metrics.get(metric).getDeclaredConstructors()) {
			if (c.getParameterTypes().length == 0) {
				
				try {
					return (EvaluationMetric<NetworkStack>) c.newInstance();
				} catch (InstantiationException e) {
					throw new ClassNotFoundException("", e);
				} catch (IllegalAccessException e) {
					throw new ClassNotFoundException("", e);
				} catch (IllegalArgumentException e) {
					throw new ClassNotFoundException("", e);
				} catch (InvocationTargetException e) {
					throw new ClassNotFoundException("", e);
				}
			}
		}
		
		throw new ClassNotFoundException(metric);
	}
	
	public AbstractResourceGenerator<?> getResourceGenerator(String generator) throws ClassNotFoundException {
		for (Constructor<?> c : resGens.get(generator).getDeclaredConstructors()) {
			if (c.getParameterTypes().length == 0) {
				
				try {
					return (AbstractResourceGenerator<?>) c.newInstance();
				} catch (InstantiationException e) {
					throw new ClassNotFoundException("", e);
				} catch (IllegalAccessException e) {
					throw new ClassNotFoundException("", e);
				} catch (IllegalArgumentException e) {
					throw new ClassNotFoundException("", e);
				} catch (InvocationTargetException e) {
					throw new ClassNotFoundException("", e);
				}
			}
		}
		
		throw new ClassNotFoundException(generator);
	}
	
	public AbstractDemandGenerator<?> getDemandGenerator(String generator) throws ClassNotFoundException {
		for (Constructor<?> c : demGens.get(generator).getDeclaredConstructors()) {
			if (c.getParameterTypes().length == 0) {
				
				try {
					return (AbstractDemandGenerator<?>) c.newInstance();
				} catch (InstantiationException e) {
					throw new ClassNotFoundException("", e);
				} catch (IllegalAccessException e) {
					throw new ClassNotFoundException("", e);
				} catch (IllegalArgumentException e) {
					throw new ClassNotFoundException("", e);
				} catch (InvocationTargetException e) {
					throw new ClassNotFoundException("", e);
				}
			}
		}
		
		throw new ClassNotFoundException(generator);
	}
	
	public AbstractNetworkStackGenerator getNetworkGenerator(String generator) throws ClassNotFoundException {
		for (Constructor<?> c : netGens.get(generator).getDeclaredConstructors()) {
			if (c.getParameterTypes().length == 0) {
				
				try {
					return (AbstractNetworkStackGenerator) c.newInstance();
				} catch (InstantiationException e) {
					throw new ClassNotFoundException("", e);
				} catch (IllegalAccessException e) {
					throw new ClassNotFoundException("", e);
				} catch (IllegalArgumentException e) {
					throw new ClassNotFoundException("", e);
				} catch (InvocationTargetException e) {
					throw new ClassNotFoundException("", e);
				}
			}
		}
		
		throw new ClassNotFoundException(generator);
	}
	
	public IChainElement getScenarioGeneratorChainElement(String elem) throws ClassNotFoundException {
		for (Constructor<?> c : scenGenChainElems.get(elem).getDeclaredConstructors()) {
			if (c.getParameterTypes().length == 0) {
				
				try {
					return (IChainElement) c.newInstance();
				} catch (InstantiationException e) {
					throw new ClassNotFoundException("", e);
				} catch (IllegalAccessException e) {
					throw new ClassNotFoundException("", e);
				} catch (IllegalArgumentException e) {
					throw new ClassNotFoundException("", e);
				} catch (InvocationTargetException e) {
					throw new ClassNotFoundException("", e);
				}
			}
		}
		
		throw new ClassNotFoundException(elem);
	}
	
	public IResultExporter getExporter(String exporter) throws ClassNotFoundException {
		for (Constructor<?> c : exporters.get(exporter).getDeclaredConstructors()) {
			if (c.getParameterTypes().length == 0) {
				
				try {
					return (IResultExporter) c.newInstance();
				} catch (InstantiationException e) {
					throw new ClassNotFoundException("", e);
				} catch (IllegalAccessException e) {
					throw new ClassNotFoundException("", e);
				} catch (IllegalArgumentException e) {
					throw new ClassNotFoundException("", e);
				} catch (InvocationTargetException e) {
					throw new ClassNotFoundException("", e);
				}
			}
		}
		
		throw new ClassNotFoundException(exporter);
	}
	
	/**
	 * Get a list of all registered metrics.
	 * 
	 * @return A list of metrics that can be retrieved via <code>getMetric()</code>.
	 */
	public Set<String> listMetrics() {
		return metrics.keySet();
	}
	
	/**
	 * Get a list of all registered algorithms.
	 * 
	 * @return A list of algorithms that can be retrieved via <code>getAlgorithm()</code>.
	 */
	public Set<String> listAlgorithms() {
		return algorithms.keySet();
	}
	
	public Set<String> listResGens() {
		return resGens.keySet();
	}
	
	public Set<String> listDemGens() {
		return demGens.keySet();
	}
	
	public Set<String> listNetGens() {
		return netGens.keySet();
	}

}
