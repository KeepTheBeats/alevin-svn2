package vnreal.algorithms.DPVNE;

import vnreal.algorithms.AlgorithmParameter;
import vnreal.algorithms.utils.SubgraphBasicVN.Utils;

public class DPVNEAlgorithmParameter extends AlgorithmParameter {
	public final double omega;
	public final int delegationNodesLevel;
	public final int fullKnowledgeNodesLevel;
	public final int partitions;
	public final int minPartitionSize;
	public final int maxLevel;

	public DPVNEAlgorithmParameter(
			double vbandwidthWeight,
			int delegationNodesLevel,
			int fullKnowledgeNodesLevel,
			int partitions,
			int minPartitionSize,
			int maxLevel) {
		
		this.omega = vbandwidthWeight;
		this.delegationNodesLevel = delegationNodesLevel;
		this.fullKnowledgeNodesLevel = fullKnowledgeNodesLevel;
		this.partitions = partitions;
		this.minPartitionSize = minPartitionSize;
		this.maxLevel = maxLevel;
	}
	
	@Override
	public String toString(String prefix) {
		return Utils.toString(prefix, this, "\n", ":");
	}
	
	@Override
	public String getSuffix(String prefix) {
		return Utils.toString(prefix, this);
	}

}
