package vnreal.algorithms.DPVNE;

import java.util.Collection;
import java.util.LinkedList;

import vnreal.algorithms.singlenetworkmapping.SingleNetworkMappingAlgorithm;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualNetwork;

public abstract class ClusterHead {
	
	public final SubstrateNetwork cluster;
	public boolean isActive = true;
	int level;
	public MetricsCounter counter = null;
	public final boolean isMainNode;
	final boolean isDelegationNode;
	final SubstrateNode clusterHeadNode;
	final SingleNetworkMappingAlgorithm mappingAlgorithm;
	final NetworkEstimation estimationAlgorithm;

	LockTree subLockTree;
	final Collection<ClusterHead> delegationNodes;
	
	public final Collection<UpdateEntry> appliedUpdates = new LinkedList<UpdateEntry>();	
	
	/**
	 * Constructor
	 * @param cluster SubstrateNetwork 
	 * @param isMainNode True if is Main Node
	 * @param isDelegationNode True if DelegationNode
	 * @param clusterHeadNode ??
	 * @param delegationNodes Collection of DelagationNodes
	 * @param estimationAlgorithm Used estimationAlgo
	 */
	public ClusterHead(
			SubstrateNetwork cluster,
			boolean isMainNode,
			int level,
			boolean isDelegationNode, SubstrateNode clusterHeadNode,
			Collection<ClusterHead> delegationNodes,
			SingleNetworkMappingAlgorithm mappingAlgorithm,
			NetworkEstimation estimationAlgorithm) {
		
		this.cluster = cluster;
		this.level = level;
		this.isMainNode = isMainNode;
		this.isDelegationNode = isDelegationNode;
		this.clusterHeadNode = clusterHeadNode;
		this.delegationNodes = delegationNodes;
		this.mappingAlgorithm = mappingAlgorithm;
		this.estimationAlgorithm = estimationAlgorithm;
	}
	
	public int getLevel() {
		return level;
	}
	
	public void setSubLockTree(LockTree subLockTree) {
		this.subLockTree = subLockTree;
	}
	
	public LockTree getSubLockTree() {
		return this.subLockTree;
	}
	
	/**
	 * Returns the delegation nodes below
	 * @return Collection of delegations nodes below
	 */
	public Collection<ClusterHead> getDelegationNodes() {
		return delegationNodes;
	}
	
	public ClusterHead getParent() {
		if (subLockTree == null || subLockTree.getParent() == null) {
			return null;
		}
		return subLockTree.getParent().getClusterHead();
	}
	
	public Collection<ClusterHead> getChildren() {
		if (subLockTree == null || subLockTree.getChildren() == null) {
			return null;
		}
		LinkedList<ClusterHead> result = new LinkedList<ClusterHead>();
		
		for (LockTree c : subLockTree.getChildren()) {
			result.add(c.getClusterHead());
		}
		
		return result;
	}

	public boolean isDelegationNode() {
		return isDelegationNode;
	}

	public SubstrateNode getClusterHeadNode() {
		return clusterHeadNode;
	}
	
	public void updateLocalState(
			Collection<UpdateEntry> vNet) {
		
		assert (!isMainNode);
		
		if (vNet != null) {
			subLockTree.propagateUpdates(vNet);
		}
	}
	
	/**
	 * Sets the lock-state for the clusterhead in the locktree
	 * @param clusterHead The clusterhead which should bu (un)locked
	 * @param lock true if it should be locked, else false for unlock
	 */
	public void setLocalFullLock(ClusterHead clusterHead, boolean lock) {
		assert(!isMainNode && !clusterHead.isMainNode);
		
		LockTree c = subLockTree.getSubLockTreeFor(clusterHead);
		c.setFullLock(lock);
	}

	/**
	 * This method checks wether the node is availble for embedding
	 * @param clusterHead Canidate clusterhead
	 * @return true if available  for embedding
	 */
	public boolean isLocallyUnLocked(ClusterHead clusterHead) {
		assert(!isMainNode && !clusterHead.isMainNode);
		
		return subLockTree.getSubLockTreeFor(clusterHead).isUnLocked();
	}

	public boolean isLocallyPartiallyLocked(ClusterHead clusterHead) {
		assert(!isMainNode && !clusterHead.isMainNode);
		
		return subLockTree.getSubLockTreeFor(clusterHead).isPartiallyLocked();
	}
	
	public boolean isLocallyFullyLocked(ClusterHead clusterHead) {
		assert(!isMainNode && !clusterHead.isMainNode);
		
		return subLockTree.getSubLockTreeFor(clusterHead).isFullyLocked();
	}
	
	/**
	 * This method applies all updates 
	 * @param updates Collection of Update-Entries
	 */
	public abstract void applyUpdates(Collection<UpdateEntry> updates);
	
	/**
	 * This Method looks up whether an child can embed the Network and returns a list of possible ClusterHeads
	 * 
	 * @param vNet The virtual net
	 * @param orig The first clusterhead which was asked 
	 * @return A sorted List of Possible ClusterHaeds
	 */
	public abstract Collection<ClusterHead> findClusterCandidates(
			VirtualNetwork vNet, ClusterHead orig);
	

	public void send(StartMessage msg, ClusterHead destination) {
		assert(!isMainNode && !destination.isMainNode && destination.isDelegationNode);
		
		counter.sent(this, destination, msg);
		
		addLocalUpdatesFor(msg, destination);
		destination.receive(msg);
	}
	
	public void send(StopMessage msg, ClusterHead destination) {
		assert(!isMainNode && !destination.isMainNode && destination.isDelegationNode);
		
		counter.sent(this, destination, msg);
		
		addLocalUpdatesFor(msg, destination);
		destination.receive(msg);
	}
	
	public void send(DelegateRequestMessage msg, ClusterHead destination) {
		
		counter.sent(this, destination, msg);
		
		addLocalUpdatesFor(msg, destination);
		
		destination.receive(msg);
	}

	public void send(EmbeddingResultMessage msg, ClusterHead destination) {
		counter.sent(this, destination, msg);
		
		if (!destination.isMainNode) {
			addLocalUpdatesFor(msg, destination);
		}
		
		destination.receive(msg);
	}
	
//	public void send(RollbackResultMessage msg, ClusterHead destination) {
//		
//		counter.sent(this, destination, msg);
//		
//		if (!destination.isMainNode) {
//			addLocalUpdatesFor(msg, destination);
//		}
//		
//		destination.receive(msg);
//	}
//	
//	public void send(RollbackMessage msg, ClusterHead destination) {
//		
//		counter.sent(this, destination, msg);
//		
//		if (!destination.isMainNode) {
//			addLocalUpdatesFor(msg, destination);
//		}
//		
//		destination.receive(msg);
//	}
	
	
	public synchronized void receive(StartMessage msg) {
		assert(isDelegationNode);
		
		counter.received(msg.getSource(), this, msg);
		
		if (DistributedMappingAlgorithm.DEBUG) {
			System.out.println(this + " received: " + msg);
		}
		
		msg.exec(this);
	}
	
	public synchronized void receive(EmbeddingResultMessage msg) {

		counter.received(msg.getSource(), this, msg);
		
		if (DistributedMappingAlgorithm.DEBUG) {
			System.out.println(this + " received: EmbeddingResultMessage " + msg.thisIsAnAnswerTo);
			System.out.println(Thread.activeCount() + " threads running. ");
		}
		
		if (isMainNode) {
			applyUpdates(msg.getEmbeddingResult());
		}
		
		msg.exec(this);
	}
	
//	public synchronized void receive(RollbackResultMessage msg) {
//
//		counter.received(msg.getSource(), this, msg);
//		
//		if (DistributedMappingAlgorithm.DEBUG) {
//			System.out.println(this + " received: RollBackResultMessage " + msg.thisIsAnAnswerTo);
//			System.out.println(Thread.activeCount() + " threads running. ");
//		}
//		
//		msg.exec(this);
//	}
//	
//	public synchronized void receive(RollbackMessage msg) {
//
//		counter.received(msg.getSource(), this, msg);
//		
//		if (DistributedMappingAlgorithm.DEBUG) {
//			System.out.println(this + " received: RollBackMessage ");
//			System.out.println(Thread.activeCount() + " threads running. ");
//		}
//		
//		msg.exec(this);
//	}
	
	public synchronized void receive(StopMessage msg) {
		assert(!isMainNode && isDelegationNode);
		
		counter.received(msg.getSource(), this, msg);
		
		if (DistributedMappingAlgorithm.DEBUG) {
			System.out.println(this + " received (" + this + "): " + msg);
		}
		
		msg.exec(this);
	}
	
	public synchronized void receive(DelegateRequestMessage msg) {
		assert(!isMainNode);

		counter.received(msg.getSource(), this, msg);
		
		if (DistributedMappingAlgorithm.DEBUG) {
			System.out.println(this + " received: " + msg);
		}

		msg.exec(this);
	}
	
	
	void addLocalUpdatesFor(Message msg, ClusterHead destination) {
		LockTree c = subLockTree.getSubLockTreeFor(destination);

		msg.addUpdates(c.getUpdates());
		c.clearUpdates();
	}


//	/**
//	 * Checks that a Substrate-Node/-Link is contained in local cluster 
//	 * 
//	 * @param sname  Name of SubstrateLink or SubstrateNode
//	 * @return true if node contained in the local cluster
//	 */
//	public boolean coversClusterEntity(String sname) {
//		for (SubstrateNode sn : cluster.getVertices()) {
//			if (sn.getName().equals(sname)) {
//				return true;
//			}
//		}
//
//		for (SubstrateLink sl : cluster.getEdges()) {
//			if (sl.getName().equals(sname)) {
//				return true;
//			}
//		}
//
//		return false;
//	}
	
	@Override
	public String toString() {
		return (isMainNode ? "MAIN" : (subLockTree == null ? "null" : subLockTree.name));
	}
	
	public void setCounter(MetricsCounter counter) {
		this.counter = counter;
	}
	
	/**
	 * This method embeds the virtual Network
	 * @param vNet The Virtual Net
	 * @param msg Message
	 * @return UpdateEntry
	 */
	public abstract LinkedList<UpdateEntry> embed(VirtualNetwork vNet,
			Message msg);
	
//	/**
//	 * This method unmaps a VirtualNetwork
//	 * 
//	 * @param vNet {@link VirtualNetwork} which will be unmapped
//	 * @return {@link Collection} of {@link UpdateEntry}
//	 */
//	public abstract Collection<UpdateEntry> rollBack(VirtualNetwork vNet);
	
}
