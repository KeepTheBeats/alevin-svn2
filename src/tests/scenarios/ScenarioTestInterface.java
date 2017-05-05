package tests.scenarios;

import java.util.LinkedList;
import java.util.List;

import vnreal.algorithms.AlgorithmParameter;
import vnreal.evaluations.metrics.EvaluationMetric;

public interface ScenarioTestInterface<S, V, R> {

	public R map(AlgorithmParameter param, S substrate, List<V> virtuals);
	public void unmap(S substrate, V virtual);
	
	public String getName();
	public LinkedList<AlgorithmParameter> getAlgorithmParams();
	
	public List<EvaluationMetric<R>> getMetrics(double elapsedTimeMS);

}
