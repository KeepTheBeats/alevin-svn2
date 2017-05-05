package vnreal.algorithms.distributedAlg.metriken;

import vnreal.algorithms.distributedAlg.DistributedAlgorithm;
import vnreal.evaluations.metrics.EvaluationMetric;
import vnreal.network.NetworkStack;

public class BFMessageCounter implements EvaluationMetric<NetworkStack> {
	public double calculate(NetworkStack stack) {
		return (double) (DistributedAlgorithm.numberOfBFMessages);
	}
	
	public String toString() {
		return "NumberOfBellmanFordMessages";
	}

}
