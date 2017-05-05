package vnreal.algorithms.distributedAlg.metriken;

import vnreal.algorithms.distributedAlg.DistributedAlgorithm;
import vnreal.evaluations.metrics.EvaluationMetric;
import vnreal.network.NetworkStack;

public class NodesUsedSolelyForForwarding implements EvaluationMetric<NetworkStack> {
	public double calculate(NetworkStack stack) {
		return DistributedAlgorithm.nodesUsedSolelyForForwarding;
	}
	
	public String toString() {
		return "NodesUsedSolelyForForwarding";
	}

}
