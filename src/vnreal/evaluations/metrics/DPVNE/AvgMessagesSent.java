package vnreal.evaluations.metrics.DPVNE;

import vnreal.algorithms.DPVNE.MetricsCounter;
import vnreal.evaluations.metrics.EvaluationMetric;
import vnreal.network.NetworkStack;


public class AvgMessagesSent implements EvaluationMetric<NetworkStack> {
	
	final MetricsCounter counter;
		
	public AvgMessagesSent(MetricsCounter counter) {
		this.counter = counter;
	}
	
	public double calculate(NetworkStack stack) {
		int sum = 0;
		for (Integer i : counter.numberOfMessagesSentPerNode.values()) {
			sum += i;
		}
		
		return (sum / (double) stack.getSubstrate().getVertexCount());
	}
	
	public String toString() {
		return this.getClass().getSimpleName();
	}

}
