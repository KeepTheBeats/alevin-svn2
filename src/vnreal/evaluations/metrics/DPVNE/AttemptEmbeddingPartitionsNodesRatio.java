package vnreal.evaluations.metrics.DPVNE;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import vnreal.algorithms.DPVNE.ClusterHead;
import vnreal.algorithms.DPVNE.MetricsCounter;
import vnreal.evaluations.metrics.EvaluationMetric;
import vnreal.network.NetworkStack;
import vnreal.network.virtual.VirtualNetwork;

public class AttemptEmbeddingPartitionsNodesRatio implements EvaluationMetric<NetworkStack> {

	final MetricsCounter counter;
	
	public AttemptEmbeddingPartitionsNodesRatio(MetricsCounter counter) {
		this.counter = counter;
	}
	
	public double calculate(NetworkStack stack) {
		int sumSNodes = 0;
		
		Map<ClusterHead, List<VirtualNetwork>> map = counter.numberOfVNRsAttemptsEmbeddedPerPartition;

		for (Entry<ClusterHead, List<VirtualNetwork>> e : map.entrySet()) {
			sumSNodes += ((((double) e.getKey().cluster.getVertexCount()) * 100.0d)
					/ (double) stack.getSubstrate().getVertexCount())
					* (double) e.getValue().size();
		}
		
		double result = (sumSNodes / ((double) (stack.size() - 1)));
		return result;
	}
	
	public String toString() {
		return this.getClass().getSimpleName();
	}
	
}
