package vnreal.core.oldFramework;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import tests.generators.network.AbstractNetworkStackGenerator;
import tests.generators.network.WaxmanNetworkStackGenerator;
import tests.generators.seed.AbstractSeedGenerator;
import tests.generators.seed.StandardSeedGenerator;
import tests.io.TestParamImporter;
import vnreal.algorithms.AbstractAlgorithm;
import vnreal.algorithms.AlgorithmParameter;
import vnreal.core.Consts;
import vnreal.core.Registry;
import vnreal.evaluations.metrics.EvaluationMetric;
import vnreal.generators.demands.AbstractDemandGenerator;
import vnreal.generators.resources.AbstractResourceGenerator;
import vnreal.io.DOTExporter;
import vnreal.io.XMLExporter;
import vnreal.network.NetworkStack;

public class TestOrchestrator {
	public static void execute(String testfile) {
		
		File paramsfile = new File(testfile);
		String filename = paramsfile.getName();
		String resultname = filename.substring(0, filename.lastIndexOf('.'));
		TestParamImporter imp = null;
		
		try {
			imp = new TestParamImporter(paramsfile.getAbsolutePath());
		} catch (IOException e) {
			System.out.println("Error while parsing test parameters! Aborting!");
			System.out.println(e);
			System.exit(1);
		}
		
		AbstractSeedGenerator seed = new StandardSeedGenerator();
		
		TestGenerator gen = new TestGenerator(imp.getRuns());
		gen.setParams(imp.getParams());
		
		TestSeries series = gen.generateTests();
		
		XMLExporter exporter = new XMLExporter(Consts.RESULTS_DIR + Consts.FILE_SEPARATOR + resultname + ".xml",
												(imp.getSaveNetworks().equals("XML")) );
		
		DOTExporter dexp = null;
		if (imp.getSaveNetworks().equals("DOT")) {
			String basedir = Consts.RESULTS_DIR + Consts.FILE_SEPARATOR + resultname;
			new File(basedir).mkdirs(); // Create directory
			dexp = new DOTExporter();
			dexp.init(basedir, null);
		}
		
		Registry reg = Registry.getInstance();
		
		// Instantiate the algorithm
		AbstractAlgorithm algorithm = null;
		String algo = imp.getAlgo();
		if (algo == null) {
			System.out.println("Could not parse Algorithm! Aborting!");
			System.out.println("Recognized algorithms are:");
			System.out.println(reg.listAlgorithms());
			System.exit(1);
		}
		AlgorithmParameter params = imp.getAlgoParams();
		try {
			// Each individual TestRun needs its own instance of the algorithm
			for (TestRun tr : series.getAllTestRuns()) {
				algorithm = reg.getAlgorithm(algo, params);
				tr.setAlgo(algorithm);
			}
		} catch (ClassNotFoundException | NullPointerException e) {
			System.out.println("Algorithm not found! Aborting!");
			System.out.println("Recognized algorithms are:");
			System.out.println(reg.listAlgorithms());
			System.exit(1);
		}
		
		// Instantiate resource generators
		List<AbstractResourceGenerator<?>> resGenerators = new ArrayList<AbstractResourceGenerator<?>>();
		List<String> resGens = imp.getResGens();
		if (resGens == null) {
			System.out.println("Warning: No resource generators specified!");
			System.out.println("Inserting NullResourceGenerator to avoid crash.");
			resGens = new ArrayList<String>();
			resGens.add("NullResourceGenerator");
			System.out.println("Recognized generators are:");
			System.out.println(reg.listResGens());
		} else {
			for (String generator : resGens) {
				try {
					resGenerators.add(reg.getResourceGenerator(generator));
				} catch (ClassNotFoundException | NullPointerException e) {
					System.out.println("Could not find class: " + generator + " - skipping!");
					System.out.println("Recognized resource generators are:");
					System.out.println(reg.listResGens());
				}
			}
		}

		
		// Instantiate demand generators
		List<AbstractDemandGenerator<?>> demGenerators = new ArrayList<AbstractDemandGenerator<?>>();
		List<String> demGens = imp.getDemGens();
		if (demGens == null) {
			System.out.println("Warning: No demand generators specified!");
			System.out.println("Inserting NullDemandGenerator to avoid crash.");
			demGens = new ArrayList<String>();
			demGens.add("NullDemandGenerator");
			System.out.println("Recognized generators are:");
			System.out.println(reg.listDemGens());
		} else {
			for (String generator : demGens) {
				try {
					demGenerators.add(reg.getDemandGenerator(generator));
				} catch (ClassNotFoundException | NullPointerException e) {
					System.out.println("Could not find class: " + generator + " - skipping!");
					System.out.println("Recognized demand generators are:");
					System.out.println(reg.listDemGens());
				}
			}
		}
		
		// Instantiate network generator
		AbstractNetworkStackGenerator ngen = null;
		String generator = imp.getNetGen();
		if (generator == null) {
			System.out.println("Network generator not found!");
			System.out.println("Recognized algorithms are:");
			System.out.println(reg.listNetGens());
			System.out.println("Using default network generator: WaxmanNetworkStackGenerator");
			ngen = new WaxmanNetworkStackGenerator();
		} else {
			try {
				ngen = reg.getNetworkGenerator(generator);
			} catch (ClassNotFoundException | NullPointerException e) {
				System.out.println("Could not parse network generator! Aborting!");
				System.out.println("Recognized generators are:");
				System.out.println(reg.listNetGens());
				System.exit(1);
			}
		}
		
		// Instantiate metrics
		List<EvaluationMetric<NetworkStack>> metrics = new ArrayList<EvaluationMetric<NetworkStack>>();
		List<String> ms = imp.getMetrics();
		if (ms == null) {
			System.out.println("Warning: No metrics specified!");
			System.out.println("Recognized metrics are:");
			System.out.println(reg.listMetrics());
		} else {
			for (String metric : ms ) {
				try {
					metrics.add(reg.getMetric(metric));
				} catch (ClassNotFoundException | NullPointerException e) {
					System.out.println("Could not find class: " + metric + " - skipping!");
					System.out.println("Recognized metrics are:");
					System.out.println(reg.listMetrics());
				}
			}
		}
		
		// Finally: Create the TestRunner and start the Tests
		TestRunner run = new TestRunner(
				seed,
				ngen,
				false,	// Should networks be bidirectional?
				exporter, dexp, // Exporters
				resGenerators, demGenerators,
				metrics);
		
		run.runAllTest(series.getAllTestRuns(), 1);

	}
}
