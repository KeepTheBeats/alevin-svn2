package tests;

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;

import tests.generators.constraints.ConstraintsGenerator;
import tests.generators.constraints.RandomDemandGenerator;
import tests.generators.constraints.RandomDemandGenerator.RandomDemandGeneratorParameter;
import tests.generators.constraints.RandomResourceGenerator;
import tests.generators.constraints.RandomResourceGenerator.RandomResourceGeneratorParameter;
import tests.generators.network.BarabasiAlbertNetworkGenerator;
import tests.generators.network.BarabasiAlbertNetworkGenerator.BarabasiAlbertNetworkGeneratorParameters;
import tests.scenarios.AbstractScenarioTest;
import tests.scenarios.AdvancedSubgraphTest;
import vnreal.constraints.demands.AbstractDemand;
import vnreal.constraints.resources.AbstractResource;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

public class ScenarioQueue {
	
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
		final Integer[] Barabasi_sNumTimeStepsArray = { 1000 };
		
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
		
		final Integer[] numVNetsArray = { 1 };
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
		
//		AlgorithmParameter algoParam = new AlgorithmParameter();
		LinkedList<AbstractScenarioTest> algorithms = new LinkedList<AbstractScenarioTest>();
		
		AdvancedSubgraphTest AdvancedSubgraph = new AdvancedSubgraphTest("AdvSubgraph");
		algorithms.add(AdvancedSubgraph);
		
		
		final boolean generateDuplicateEdges = true;
		final int numScenarios = 1;
		final long maxRuntimeInSeconds = -1;
		final DistributionParameter distributionParameter = new DistributionParameter(1000, 4d/100d, 1d/1000d);
//		final DistributionParameter distributionParameter = new DistributionParameter(100, 4d/100d, 1d/100d);
//		final DistributionParameter distributionParameter = new DistributionParameter(5, 1d/100d, 1d/10d);
//		final DistributionParameter distributionParameter = null;
		
		final boolean export = false;
		
		
		AlgorithmEvaluation eval = new AlgorithmEvaluation(sNetGenerator, vNetGenerator, generateDuplicateEdges);
		
		
		
		// ################## run ##################
		try {
			eval.executeTests(PROJECT_NAME, export, numScenarios, maxRuntimeInSeconds,
					distributionParameter,
					sNetGeneratorParams, sNetConstraintsGenerator,
					vNetGeneratorParams, numVNetsArray, vNetConstraintsGenerator,
					algorithms);

//			String subject = PROJECT_NAME + " success -- simulation terminated!";
//			String content = subject;
//			AlgorithmEvaluation.execMailNotificationTool(subject, content);		
		} catch(Throwable e) {
//			String subject = PROJECT_NAME + " EXCEPTION was thrown";
//			String content = subject;
//			AlgorithmEvaluation.execMailNotificationTool(subject, content);
			throw(e);
		}
	}

}
