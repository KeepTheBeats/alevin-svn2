package vnreal.algorithms.DPVNE;

import tests.scenarios.AlgorithmParameters;
import vnreal.algorithms.utils.SubgraphBasicVN.Utils;

public class DPVNEAlgorithmParameters extends AlgorithmParameters {

	public double[] omega =  { 10d };

	public int[] delegationNodesLevelArray = { 0 };
	public int[] fullKnowledgeNodesLevelArray = { 0 };

	public int[] partitions = { 2 };
	public int[] maxLevels = { -1 };
	public int minPartitionSize = 5;

	public String toString(String prefix) {
		return Utils.toString(prefix, this, "\n", ":");
	}

	public String getSuffix(String prefix) {
		return Utils.toString(prefix, this);
	}

}
