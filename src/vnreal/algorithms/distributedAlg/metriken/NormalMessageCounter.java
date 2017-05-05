package vnreal.algorithms.distributedAlg.metriken;

import vnreal.algorithms.distributedAlg.DistributedAlgorithm;
import vnreal.evaluations.metrics.EvaluationMetric;
import vnreal.network.NetworkStack;

public class NormalMessageCounter implements EvaluationMetric<NetworkStack> {
	public double calculate(NetworkStack stack) {
		return DistributedAlgorithm.numberOfMessages;
	}
	
	public String toString() {
		return "NumberOfNormalMessages";
	}
}
