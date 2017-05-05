package vnreal.algorithms.DPVNE;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import mulavito.algorithms.AbstractAlgorithmStatus;
import vnreal.algorithms.AbstractAlgorithm;
import vnreal.network.Network;
import vnreal.network.NetworkStack;
import vnreal.network.virtual.VirtualNetwork;

public class DistributedMappingAlgorithm extends AbstractAlgorithm {
	
	
	public static boolean DEBUG = false;
	
	private final NetworkStack stack;
	private Iterator<VirtualNetwork> curIt;
	private Iterator<? extends Network<?, ?, ?>> curNetIt;
	
	public LockTree rootClusterhead;
	
	public Collection<ClusterHead> nodesEmbeddingItself = null;
	
	MetricsCounter counter = null;
	private long runtimeWithoutPartitioning = 0l;
	
	public DistributedMappingAlgorithm(
			NetworkStack stack) {
		
		this.stack = stack;
		this.curNetIt = stack.iterator();
		
		nodesEmbeddingItself = new LinkedList<ClusterHead>();
	}
	
	public long getRuntimeWithoutPartitioning() {
		return runtimeWithoutPartitioning;
	}
	
	@Override
	public List<AbstractAlgorithmStatus> getStati() {
		return null;
	}
	
	public void setCounter(MetricsCounter counter) {
		this.counter = counter;
	}

	@Override
	protected boolean preRun() {
		return true;
	}
	
	@Override
	protected void postRun() {
	}

	@Override
	protected void evaluate() {
		FullKnowledgeClusterHead main = new FullKnowledgeClusterHead(
				stack.getSubstrate(), true, -1, false, null, null, null, null);
		main.setCounter(counter);
		
		ClusterHead[] delegationNodes =
				rootClusterhead.getClusterHead().getDelegationNodes().toArray(new ClusterHead[0]);
		if (delegationNodes.length == 0) {
			System.err.println("WARNING: no delegation nodes set. Evaluation cancelled.");
			return;
		}
		
		Random r = new Random();
		
		LinkedList<DelegateRequestMessage> requests = new LinkedList<DelegateRequestMessage>();
		Queue<ClusterHead> stoppedDelegationNodes = new LinkedList<ClusterHead>();
		
		long time = System.currentTimeMillis();
		while (hasNext()) {
			VirtualNetwork vNetwork = getNext();

			DelegateRequestMessage msg = new DelegateRequestMessage(main, vNetwork, stoppedDelegationNodes);
			requests.add(msg);
			
			if (DEBUG) {
				System.out.println("\nnext request");
			}
			delegationNodes[r.nextInt(delegationNodes.length)].receive(msg);
		}

		for (DelegateRequestMessage msg : requests) {
			msg.getReceivedAnswer();
		}
		runtimeWithoutPartitioning = System.currentTimeMillis() - time;
		if (DEBUG) {
			System.out.println("execution terminated.");
		}
		
	}

	@SuppressWarnings("unchecked")
	protected boolean hasNext() {
		if (curIt == null || !curIt.hasNext()) {
			if (curNetIt.hasNext()) {
				curNetIt.next();

				curIt = (Iterator<VirtualNetwork>) curNetIt;
				return hasNext();
			} else
				return false;
		} else
			return true;
	}

	protected VirtualNetwork getNext() {
		if (!hasNext())
			return null;
		else {
			return curIt.next();
		}
	}
	
}
