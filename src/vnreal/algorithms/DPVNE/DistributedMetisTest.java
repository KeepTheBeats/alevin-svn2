package vnreal.algorithms.DPVNE;

import vnreal.algorithms.DPVNE.singlenetworkmapping.factory.EmbeddingAlgorithmFactory;

public abstract class DistributedMetisTest extends AlgorithmTest {
	
	public DistributedMetisTest(DPVNEAlgorithmParameters params, String name, boolean isPathSplittingAlgorithm) {
		super(params, name, isPathSplittingAlgorithm);
	}
	
	public HierarchicalPartitioning getHierarchicalPartitioning(DPVNEAlgorithmParameter data) {
		
		NetworkEstimation estimationMethod = new CpuBandwidthNetworkEstimation(data.omega);
		
		return new HierarchicalPartitioning(
				data.fullKnowledgeNodesLevel,
				data.delegationNodesLevel,
				data.minPartitionSize,
				data.maxLevel,
				new MetisPartitioningAlgorithm(data.partitions),
//				null,
				getFactory(),
				estimationMethod);
	}
	
	public abstract EmbeddingAlgorithmFactory getFactory();
	
}
