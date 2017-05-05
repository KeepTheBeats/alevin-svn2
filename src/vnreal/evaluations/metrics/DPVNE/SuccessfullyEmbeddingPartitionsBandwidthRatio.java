package vnreal.evaluations.metrics.DPVNE;

import java.util.List;

import vnreal.algorithms.DPVNE.MetricsCounter;
import vnreal.algorithms.DPVNE.MetricsCounter.BandwidthRatio;
import vnreal.evaluations.metrics.EvaluationMetric;
import vnreal.network.NetworkStack;


public class SuccessfullyEmbeddingPartitionsBandwidthRatio implements EvaluationMetric<NetworkStack> {

	final MetricsCounter counter;
	
	public SuccessfullyEmbeddingPartitionsBandwidthRatio(MetricsCounter counter) {
		this.counter = counter;
	}
	
	public double calculate(NetworkStack stack) {
		double sum = 0;
		
		List<BandwidthRatio> list = counter.successfullyBandwidthRatio;

		for (BandwidthRatio e : list) {
			sum += e.ratio;
		}
		
		if (list.size() == 0)
			return 0.0d;
		
		double result = (sum / ((double) (list.size())));
		return result;
	}
	
	public String toString() {
		return this.getClass().getSimpleName();
	}
	
}
