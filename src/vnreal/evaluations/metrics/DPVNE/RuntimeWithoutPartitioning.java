package vnreal.evaluations.metrics.DPVNE;

import vnreal.evaluations.metrics.EvaluationMetric;
import vnreal.network.NetworkStack;

public class RuntimeWithoutPartitioning implements EvaluationMetric<NetworkStack> {

	double runtime;

	public RuntimeWithoutPartitioning(double time) {
		this.runtime = time;
	}

	@Override
	public double calculate(NetworkStack stack) {
		return runtime;
	}

	@Override
	public String toString() {
		return "RuntimeWithoutPartitioning";
	}

}
