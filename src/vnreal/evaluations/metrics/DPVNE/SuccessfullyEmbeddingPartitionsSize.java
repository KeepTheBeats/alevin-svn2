package vnreal.evaluations.metrics.DPVNE;


import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import vnreal.algorithms.DPVNE.ClusterHead;
import vnreal.algorithms.DPVNE.MetricsCounter;
import vnreal.evaluations.metrics.EvaluationMetric;
import vnreal.network.NetworkStack;
import vnreal.network.virtual.VirtualNetwork;


public class SuccessfullyEmbeddingPartitionsSize implements EvaluationMetric<NetworkStack> {

	final MetricsCounter counter;
	
	public SuccessfullyEmbeddingPartitionsSize(MetricsCounter counter) {
		this.counter = counter;
	}
	
	public double calculate(NetworkStack stack) {
		double sumSNodes = 0;
		
		Map<ClusterHead, List<VirtualNetwork>> map = counter.numberOfVNRsSuccessfullyEmbeddedPerPartition;

		double num = 0d;
		for (Entry<ClusterHead, List<VirtualNetwork>> e : map.entrySet()) {
			double i = (double) e.getValue().size();
			sumSNodes += ((double) e.getKey().cluster.getVertexCount() * i);
			num += i;
		}
		
		double result = (sumSNodes / num);
		return result;
	}
	
	public String toString() {
		return this.getClass().getSimpleName();
	}
	
}
