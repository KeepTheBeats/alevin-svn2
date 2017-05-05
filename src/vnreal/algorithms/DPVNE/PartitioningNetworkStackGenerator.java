package vnreal.algorithms.DPVNE;
//package tests.scenarios;
//
//import java.util.Collections;
//import java.util.LinkedList;
//import java.util.List;
//
//import vnreal.network.NetworkStack;
//import vnreal.network.partitioning.Partition;
//import vnreal.network.partitioning.PartitioningAlgorithm;
//import vnreal.network.substrate.SubstrateNetwork;
//import vnreal.network.virtual.VirtualNetwork;
//
//
//public class PartitioningNetworkStackGenerator extends NetworkStackGenerator {
//
//	PartitioningAlgorithm partitioning;
//	double load;
//	
//	/**
//	 * @param partitioning
////	 * @param load -- 0.0 - 1.0
//	 */
//	public PartitioningNetworkStackGenerator(PartitioningAlgorithm partitioning, double load) {
//		this.partitioning = partitioning;
//		this.load = load;
//	}
//	
//	@Override
//	public NetworkStack generate(NetworkGeneratorParameter<NetworkGeneratorParameters> data, int numScenario) {
//		NetworkStack result = null;
//
//		SubstrateNetwork sNetwork = generateSNetwork(data, numScenario);
//		result = new NetworkStack(sNetwork, generateVNetworks(data, numScenario));
//
//		return result;
//	}
//	
//	public List<VirtualNetwork> generateVNetworks(SubstrateNetwork snet, NetworkGeneratorParameter<NetworkGeneratorParameters> data, int numScenario) {
//		LinkedList<Partition> partitions = new LinkedList<Partition>(partitioning.getPartitions(snet));
//		LinkedList<VirtualNetwork> result = new LinkedList<VirtualNetwork>();
//		Collections.shuffle(partitions);
//
//		int size = 0;
//		for (Partition p : new LinkedList<Partition>(partitions)) {
//			SubstrateNetwork ps = p.getSubstrateNetwork();
//			
//			if (((double) (size + ps.getVertexCount())) / snet.getVertexCount() <= load) {
//				result.add(ps.convertTopology());
//				size += ps.getVertexCount();
//			}
//		}
//		return result;
//	}
//
//}
