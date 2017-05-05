package vnreal.algorithms.distributedAlg.metriken;

import vnreal.algorithms.distributedAlg.DistributedAlgorithm;
import vnreal.evaluations.metrics.EvaluationMetric;
import vnreal.network.NetworkStack;

public class AverageSentMsgsPerNode implements EvaluationMetric<NetworkStack> {
	public double calculate(NetworkStack stack) {
		return DistributedAlgorithm.getMsgsSentPerNode();
	}
	
	public String toString() {
		return "AverageMsgsSentPerNode";
	}

}
