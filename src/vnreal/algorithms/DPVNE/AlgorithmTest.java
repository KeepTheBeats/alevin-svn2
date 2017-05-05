package vnreal.algorithms.DPVNE;

import java.util.LinkedList;
import java.util.List;

import vnreal.algorithms.AlgorithmParameter;
import vnreal.evaluations.metrics.EvaluationMetric;
import vnreal.evaluations.metrics.DPVNE.AttemptEmbeddingPartitionsNodesRatio;
import vnreal.evaluations.metrics.DPVNE.AttemptsEmbeddingPartitionsBandwidthRatio;
import vnreal.evaluations.metrics.DPVNE.AvgMessagesReceived;
import vnreal.evaluations.metrics.DPVNE.AvgMessagesSent;
import vnreal.evaluations.metrics.DPVNE.NumberOfMessages;
import vnreal.evaluations.metrics.DPVNE.RuntimeWithoutPartitioning;
import vnreal.evaluations.metrics.DPVNE.SuccessfullyEmbeddingPartitionsBandwidthRatio;
import vnreal.evaluations.metrics.DPVNE.SuccessfullyEmbeddingPartitionsLevel;
import vnreal.evaluations.metrics.DPVNE.SuccessfullyEmbeddingPartitionsNodesRatio;
import vnreal.evaluations.metrics.DPVNE.SuccessfullyEmbeddingPartitionsSize;
import vnreal.network.NetworkStack;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.virtual.VirtualNetwork;

public abstract class AlgorithmTest extends AbstractDistributedScenarioTest {

	/* advantages of this algorithm:
	 * - parallel, resilient clustering
	 * - parallel mapping
	 * - algorithm-independent
	 * 
	 * quality of mapping depends on
	 * - clustering
	 *   - algorithm
	 *   - (min) cluster sizes
     * - cluster selection (network estimation)
	 *   - idea regarding resilience: prefer clusters with no/only few active nodes
	 * - mapping algorithm
	 */

	MetricsCounter counter = null;
	
	SubstrateNetwork sNetCache = null;
	AlgorithmParameter paramCache = null;
	LockTree lockTreeCache = null;
	
	
	DistributedMappingAlgorithm algo = null;
	
//	protected static HashMap<SubstrateNetwork, HierarchicalPartitioning> cache = new HashMap<SubstrateNetwork, HierarchicalPartitioning>();
	
	public AlgorithmTest(DPVNEAlgorithmParameters params, String name, boolean isPathSplittingAlgorithm) {
		super(params, name, isPathSplittingAlgorithm);
	}
	
	@Override
	public LinkedList<EvaluationMetric<NetworkStack>> getMetrics(double elapsedTimeMS) {
		LinkedList<EvaluationMetric<NetworkStack>> metrics = super.getMetrics(elapsedTimeMS);
		
		metrics.add(new AvgMessagesSent(counter));
		metrics.add(new AvgMessagesReceived(counter));
		metrics.add(new NumberOfMessages(counter));
		metrics.add(new SuccessfullyEmbeddingPartitionsBandwidthRatio(counter));
		metrics.add(new SuccessfullyEmbeddingPartitionsNodesRatio(counter));
		metrics.add(new SuccessfullyEmbeddingPartitionsSize(counter));
		metrics.add(new SuccessfullyEmbeddingPartitionsLevel(counter));
		metrics.add(new AttemptEmbeddingPartitionsNodesRatio(counter));
		metrics.add(new AttemptsEmbeddingPartitionsBandwidthRatio(counter));
		metrics.add(new RuntimeWithoutPartitioning(algo.getRuntimeWithoutPartitioning()));
		
		return metrics;
	}
	
//	private HashMap<Integer, List<Integer>> getAvgPerLevel(Map<ClusterHead, List<VirtualNetwork>> map) {
//		HashMap<Integer, List<Integer>> tmp = new HashMap<Integer, List<Integer>>();
//		
//		for (Entry<ClusterHead, List<VirtualNetwork>> e : map.entrySet()) {
//			Integer level = getLevel(algo.rootClusterhead, 0, e.getKey());
//			List<Integer> t = tmp.get(level);
//			if (t == null) {
//				t = new LinkedList<Integer>();
//				tmp.put(level, t);
//			}
//			
//			t.add(e.getValue().size());
//		}
//		
//		return tmp;
//	}
//	
//	private int getLevel(LockTree current, int currentLevel, ClusterHead head) {
//		if (current.getClusterHead() == head) {
//			return currentLevel;
//		}
//		if (current.getChildren() != null) {
//			for (LockTree c : current.getChildren()) {
//				int cl = getLevel(c, currentLevel+1, head);
//				if (cl != -1) {
//					return cl;
//				}
//			}
//		}
//		return -1;
//	}
	
	@Override
	public void unmap(SubstrateNetwork sNet, VirtualNetwork vNet) {
		if (this.lockTreeCache != null) {
			this.lockTreeCache.propagateUnmapping(vNet);
		}
	}
	
	@Override
	public NetworkStack map(AlgorithmParameter data, SubstrateNetwork substrate, List<VirtualNetwork> virtuals) {
		
		if ((paramCache == null || paramCache != data)
				|| (sNetCache == null || sNetCache != substrate)) {
			
			counter = new MetricsCounter();
			
			paramCache = data;
			sNetCache = substrate;
			
			
			System.out.print("partitioning ...");
			HierarchicalPartitioning hierarchicalPartitioning = getHierarchicalPartitioning((DPVNEAlgorithmParameter) data);
			lockTreeCache = hierarchicalPartitioning.getPartitionsTree(substrate);
			
			lockTreeCache.setCounterRecursively(counter);
			System.out.println(" done.");
		} else {
			System.out.println("re-using cached partitioning result");
		}
		
		NetworkStack stack = new NetworkStack(substrate, virtuals);
		algo = new DistributedMappingAlgorithm(stack);
		algo.rootClusterhead = lockTreeCache;
		algo.setCounter(counter);
		
		algo.performEvaluation();
		
		return stack;
	}
	
	public abstract HierarchicalPartitioning getHierarchicalPartitioning(DPVNEAlgorithmParameter data);
}
