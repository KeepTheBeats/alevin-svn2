package vnreal.core;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import vnreal.algorithms.AbstractAlgorithm;
import vnreal.core.oldFramework.TestRun;
import vnreal.evaluations.metrics.EvaluationMetric;
import vnreal.network.NetworkStack;

/**
 * This class represents a single run of an experiment. 
 * @author Andreas Fischer
 * @since 2015-06-01
 *
 */
public class Run {
	private final Scenario scen;
	private AbstractAlgorithm algo;
	private final LinkedHashMap<String, Map<String, String>> params;
	private HashMap<String, Double> results;
	private boolean success = true; 
	
	public Run(final Scenario scen, final LinkedHashMap<String, Map<String, String>> params) {
		this.scen = scen;
		this.params = params;
		this.results = new HashMap<String, Double>();
	}
	
	public Scenario getScenario() {
		return this.scen;
	}
	
	public AbstractAlgorithm getAlgorithm() {
		return this.algo;
	}
	
	public void setAlgorithm(AbstractAlgorithm algo) {
		this.algo = algo;
	}
	
	public LinkedHashMap<String, Map<String, String>> getParams() {
		return params;
	}

	public Map<String, Double> getResults() {
		return this.results;
	}
	
	/*
	 * Let the algorithm process the scenario
	 */
	public void process() {
		algo.setStack(scen.getNetworkStack());
		try {
			algo.performEvaluation();
		} catch (Throwable th) {
			/* It is ok in this case to catch *everything* (i.e., "Throwable"),
			 * as any problem---exception or error---occurring in the algorithm
			 * indicates abnormal behavior, in which case the run should abort.
			 * If the problem can be handled, it has to be handled by the algorithm
			 * itself. If the problem can not be handled, the execution of the
			 * simulation should not be stopped due to programming errors in the
			 * algorithm.
			 */
			System.out.println(th.getMessage());
			this.success = false;
		}
	}
	
	public void evaluate(List<EvaluationMetric<NetworkStack>> metrics) {
		for (EvaluationMetric<NetworkStack> metric : metrics) {
			if (this.success) {
				results.put(metric.getClass().getSimpleName(), metric.calculate(scen.getNetworkStack()));
			} else {
				results.put(metric.getClass().getSimpleName(), Double.NaN);
			}
		}
	}
	
	public TestRun toOldFormat() {
		TestRun tr = new TestRun();
		tr.setAlgo(this.algo);
		tr.setScenario(this.scen);
		for (String generator : this.params.keySet()) {
			Map<String, String> genparams = this.params.get(generator);
			for (String param : genparams.keySet()) {
				tr.addParameter(generator + "_" + param, genparams.get(param));
			}
		}
		
		for (String metric : this.results.keySet()) {
			tr.addResult(metric, this.results.get(metric));
		}
		
		return tr;
	}
}
