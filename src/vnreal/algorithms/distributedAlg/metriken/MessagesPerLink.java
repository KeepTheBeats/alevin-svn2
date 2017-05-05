package vnreal.algorithms.distributedAlg.metriken;

import vnreal.algorithms.distributedAlg.DistributedAlgorithm;
import vnreal.evaluations.metrics.EvaluationMetric;
import vnreal.network.NetworkStack;

public class MessagesPerLink implements EvaluationMetric<NetworkStack> {
	public double calculate(NetworkStack stack) {
		return DistributedAlgorithm.usedLinksForMessages / stack.getSubstrate().getEdges().size();
	}
	
	public String toString() {
		return "MessagesPerLink";
	}

}
