package vnreal.evaluations.metrics.DPVNE;

import vnreal.algorithms.DPVNE.MetricsCounter;
import vnreal.evaluations.metrics.EvaluationMetric;
import vnreal.network.NetworkStack;


public class NumberOfMessages implements EvaluationMetric<NetworkStack> {

	final MetricsCounter counter;
	
	public NumberOfMessages(MetricsCounter counter) {
		this.counter = counter;
	}
	
	public double calculate(NetworkStack stack) {
		return counter.numberOfMessages;
	}
	
	public String toString() {
		return this.getClass().getSimpleName();
	}
	
}
