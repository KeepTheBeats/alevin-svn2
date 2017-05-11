package vnreal.algorithms.DPVNE;




import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;

import tests.AlgorithmEvaluation;
import tests.DistributionParameter;
import tests.generators.constraints.ConstraintsGenerator;
import tests.generators.constraints.RandomDemandGenerator;
import tests.generators.constraints.RandomDemandGenerator.RandomDemandGeneratorParameter;
import tests.generators.constraints.RandomResourceGenerator;
import tests.generators.constraints.RandomResourceGenerator.RandomResourceGeneratorParameter;
import tests.generators.network.BarabasiAlbertNetworkGenerator;
import tests.generators.network.BarabasiAlbertNetworkGenerator.BarabasiAlbertNetworkGeneratorParameters;
import tests.scenarios.AbstractScenarioTest;
import vnreal.constraints.demands.AbstractDemand;
import vnreal.constraints.resources.AbstractResource;
import vnreal.core.Scenario;
import vnreal.gui.GUI;
import vnreal.gui.utils.FileFilters;
import vnreal.io.XMLExporter;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

import tests.*;

public class Main {
	
	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
		
		final String PROJECT_NAME = "[myalevin]";
//		AlgorithmEvaluation.DEBUG = true;
		
		// ################## substrate networks ##################
		
		// ## waxman substrate networks -->
//		final Integer[] numSNodesArray = { 50 };
//		final double waxman_salpha = 0.3;
//		final double waxman_sbeta = 0.3;
//		
//		final WaxmanNetworkGeneratorParameters sNetGeneratorParams = new WaxmanNetworkGeneratorParameters(numSNodesArray, waxman_salpha, waxman_sbeta, true);
//		final WaxmanNetworkGenerator<AbstractResource, SubstrateNode, SubstrateLink, SubstrateNetwork> sNetGenerator = WaxmanNetworkGenerator.createSubstrateNetworkInstance();
		// <--
		
		final int Barabasi_sEdgesToAttach = 2;
//		final Integer[] Barabasi_sNumTimeStepsArray = { 30, 50, 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000 };
		final Integer[] Barabasi_sNumTimeStepsArray = { 100 };
		
		final BarabasiAlbertNetworkGeneratorParameters sNetGeneratorParams = new BarabasiAlbertNetworkGeneratorParameters(Barabasi_sEdgesToAttach, Barabasi_sNumTimeStepsArray);
		final BarabasiAlbertNetworkGenerator<AbstractResource, SubstrateNode, SubstrateLink, SubstrateNetwork> sNetGenerator = BarabasiAlbertNetworkGenerator.createSubstrateNetworkInstance();
		

		// ## random resource generator -->
		final boolean generateIDResources = false;
		final int minCPUResource = 1;
		final int maxCPUResource = 100;
		final int minBandwidthResource = 1;
		final int maxBandwidthResource = 100;
		final int minDelayResource = -1;
		final int maxDelayResource = -1;
		final ConstraintsGenerator<SubstrateNetwork> sNetConstraintsGenerator = new RandomResourceGenerator(new RandomResourceGeneratorParameter(generateIDResources, null, -1, -1, minCPUResource, maxCPUResource, -1, -1, minBandwidthResource, maxBandwidthResource, minDelayResource, maxDelayResource, null));
		// <--
		
		
		
//		final Integer[] numVNetsArray = { 1 };
//		final Integer[] numVNodesPerVNetArray = { 10 };
//		final double waxman_graph_valpha = 0.3d;
//		final double waxman_graph_vbeta = 0.3d;
//
//		final WaxmanNetworkGeneratorParameters vNetGeneratorParams = new WaxmanNetworkGeneratorParameters(numVNodesPerVNetArray, waxman_graph_valpha, waxman_graph_vbeta, true);
//		final NetworkGenerator<AbstractDemand, VirtualNode, VirtualLink, VirtualNetwork> vNetGenerator = WaxmanNetworkGenerator.createVirtualNetworkInstance();
		
		final Integer[] numVNetsArray = { 5 };
		final int Barabasi_vEdgesToAttach = 2;
		final Integer[] Barabasi_vNumTimeStepsArray = { 10 };
		final BarabasiAlbertNetworkGeneratorParameters vNetGeneratorParams = new BarabasiAlbertNetworkGeneratorParameters(Barabasi_vEdgesToAttach, Barabasi_vNumTimeStepsArray);
		final BarabasiAlbertNetworkGenerator<AbstractDemand, VirtualNode, VirtualLink, VirtualNetwork> vNetGenerator = BarabasiAlbertNetworkGenerator.createVirtualNetworkInstance();
		
		
//		## demand generator -->
		final int minCPUDemand = 1;
		final int maxCPUDemand = 50;
		final int minBandwidthDemand = 1;
		final int maxBandwidthDemand = 50;
		final int minDelayDemand = -1;
		final int maxDelayDemand = -1;
		final RandomDemandGenerator vNetConstraintsGenerator = new RandomDemandGenerator(new RandomDemandGeneratorParameter(
				minCPUDemand, maxCPUDemand, minBandwidthDemand, maxBandwidthDemand, minDelayDemand, maxDelayDemand, 0, null));
//		<--

		// ################## algorithm ##################
		
		DPVNEAlgorithmParameters algoParam = new DPVNEAlgorithmParameters();
		LinkedList<AbstractScenarioTest> algorithms = new LinkedList<AbstractScenarioTest>();
		DPVNEAdvancedSubgraphMetisTest DPVNEAdvSubgraph = new DPVNEAdvancedSubgraphMetisTest(algoParam, "DPVNE_AdvSubgraph");
		algorithms.add(DPVNEAdvSubgraph);
//		algorithms.add(new DPVNEBFSCoordinatedVNETest(algoParam, "DPVNE_BFSCoord"));
//		algorithms.add(new DPVNEDViNEPSTest(algoParam, "DPVNE_DViNEPS"));
//		DPVNEDViNESPTest DPVNEDViNESP = new DPVNEDViNESPTest(algoParam, "DPVNE_DViNESP");
//		algorithms.add(DPVNEDViNESP);
//		algorithms.add(new DPVNEExactMIPVNETest(algoParam, "DPVNE_Exact"));
//		algorithms.add(new DPVNEGARPSTest(algoParam, "DPVNE_GARPS"));
//		DPVNEGARSPTest DPVNEGARSP = new DPVNEGARSPTest(algoParam, "DPVNE_GARSP");
//		algorithms.add(DPVNEGARSP);
//		algorithms.add(new DPVNERWMMPSTest(algoParam, "DPVNE_RWMMPS"));
//		DPVNERWMMSPTest DPVNERWMMSP = new DPVNERWMMSPTest(algoParam, "DPVNE_RWMMSP");
//		algorithms.add(DPVNERWMMSP);
		
//		AdvancedSubgraphTest AdvancedSubgraph = new AdvancedSubgraphTest("AdvSubgraph");
//		algorithms.add(AdvancedSubgraph);
////		algorithms.add(new BFSCoordinatedVNETest("BFSCoord"));
////		algorithms.add(new DViNEPSTest("DViNEPS"));
//		DViNESPTest DViNESP = new DViNESPTest("DViNESP");
//		algorithms.add(DViNESP);
////		algorithms.add(new ExactMIPVNETest("ExactMIP"));
////		algorithms.add(new GARPSTest("GARPS"));
//		GARSPTest GARSP = new GARSPTest("GARSP");
//		algorithms.add(GARSP);
////		algorithms.add(new RWMMPSTest("RWMMPS"));
//		RWMMSPTest RWMMSP = new RWMMSPTest("RWMMSP");
//		algorithms.add(RWMMSP);
////		DistributedAlgTest Houidi = new DistributedAlgTest("Houidi");
////		Houidi.timeoutSNSize = 200;
////		algorithms.add(Houidi);
		
		
		final boolean generateDuplicateEdges = true;
		final int numScenarios = 1;
		final long maxRuntimeInSeconds = -1;
//		final DistributionParameter distributionParameter = new DistributionParameter(1000, 4d/100d, 1d/1000d);
//		final DistributionParameter distributionParameter = new DistributionParameter(100, 4d/100d, 1d/100d);
//		final DistributionParameter distributionParameter = new DistributionParameter(5, 1d/100d, 1d/10d);
		final DistributionParameter distributionParameter = null;
		
		final boolean export = false;
		
		
		AlgorithmEvaluation eval = new AlgorithmEvaluation(sNetGenerator, vNetGenerator, generateDuplicateEdges);
		
		
		
		// ################## run ##################
		int namenum = 50;
		while(namenum<2501){
			try {
				eval.executeTests(String.valueOf(namenum), export, numScenarios, maxRuntimeInSeconds,
						distributionParameter,
						sNetGeneratorParams, sNetConstraintsGenerator,
						vNetGeneratorParams, numVNetsArray, vNetConstraintsGenerator,
						algorithms);
	
			} catch(Throwable e) {
				throw(e);
			}
			namenum=namenum+50;
		}
		
		/*Scenario scenario = GUI.getInstance().getScenario();
		if (scenario.getNetworkStack() == null) {
			JOptionPane.showMessageDialog(
					GUI.getInstance(),
					"No scenario opened, no export possible.");
			return;
		}

		File f =this.chooseFile("Scenario Export", FileFilters.xmlFilter);
		if (f == null) {
			return;
		}

		String fileName = "/home/ubuntu/mine/123.xml";
		//if (!fileName.endsWith(".xml")) {
		//	fileName = fileName + ".xml";
		//}

		try {
			XMLExporter.exportStack(fileName, scenario.getNetworkStack());
		} catch (Exception ex) {
			// TODO why this catch-all for XML export?
			ex.printStackTrace();
		}*/
	}
	

}
