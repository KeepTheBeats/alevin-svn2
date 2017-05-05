package vnreal.algorithms.distributedAlg.metriken;

import vnreal.algorithms.distributedAlg.DistributedAlgorithm;
import vnreal.evaluations.metrics.EvaluationMetric;
import vnreal.network.NetworkStack;

public class ClusterCounter implements EvaluationMetric<NetworkStack> {
	public double calculate(NetworkStack stack) {
		return DistributedAlgorithm.clusterCounter;
	}
	
	public String toString() {
		return "ClusterCounter";
	}

}
