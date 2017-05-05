package vnreal.algorithms.DPVNE;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import vnreal.constraints.demands.AbstractDemand;
import vnreal.constraints.demands.BandwidthDemand;
import vnreal.constraints.resources.AbstractResource;
import vnreal.constraints.resources.BandwidthResource;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;

/**
 * This is the core class of the algorithm.
 * It defines the behavior of how request messages are sent/virtual network requests
 * are forwarded.
 */
public class DelegateRequestMessage extends Message {

	final VirtualNetwork vNetwork;
	
	private volatile EmbeddingResultMessage receivedAnswer = null;
	final Queue<ClusterHead> stoppedDelegationNodes;

	
	public DelegateRequestMessage(
			ClusterHead source,
			VirtualNetwork vNetwork,
			Queue<ClusterHead> stoppedDelegationNodes) {

		super(source);
		this.vNetwork = vNetwork;
		this.stoppedDelegationNodes = stoppedDelegationNodes;
	}
	
	public void setReceivedAnswer(EmbeddingResultMessage answer) {
		if (DistributedMappingAlgorithm.DEBUG) {
			System.out.println("setReceivedAnswer " + answer);
		}
		this.receivedAnswer = answer;
	}
	
	public EmbeddingResultMessage getReceivedAnswer() {
		return receivedAnswer;
	}

	public VirtualNetwork getVirtualNetwork() {
		return vNetwork;
	}

	@Override
	public void run() {
		LinkedList<UpdateEntry> embedResult = null;
		
		if (onThisClusterHead.isDelegationNode()) {
			// ############   DELEGATION NODES:

			if (DistributedMappingAlgorithm.DEBUG) {
				System.out.println(this + ": isDelegationNode");
			}

			embedResult = delegate();
			if (embedResult != null) {
				if (DistributedMappingAlgorithm.DEBUG) {
					System.out.println("   " + this + ": delegated to own subclusters");
				}
			} else {
				if (DistributedMappingAlgorithm.DEBUG) {
					System.out.println("   " + this + ": could not delegate to own subclusters");
				}
				embedResult = embedItHere();
				if (embedResult == null) {
					if (DistributedMappingAlgorithm.DEBUG) {
						System.out.println("   " + this + ": could not embed into own cluster, trying parent nodes ...");
					}
					embedResult = delegateToParentNodes(this);
				}
			}

		} else {
			// ############   NON-DELEGATION NODES:

			onThisClusterHead.isActive = false;

			if (DistributedMappingAlgorithm.DEBUG) {
				System.out.println(this + ": !isDelegationNode");
			}

			if (onThisClusterHead.getDelegationNodes() == null) {
				// ######   NODES BELOW DELEGATION NODES
				if (DistributedMappingAlgorithm.DEBUG) {
					System.out.println("   " + this + ": lower-level node (i don't know any delegation nodes)");
				}

				embedResult = embedItHere();

			} else {
				// ######   NODES ABOVE DELEGATION NODES

				embedResult = delegateToADirectChild();

				if (embedResult == null) {
					embedResult = embedItHere();
				}

				if (embedResult == null && getSource() != onThisClusterHead.getParent()) {
					embedResult = delegateToParentNodes(this);
				}

			}
			
			onThisClusterHead.isActive = true;
		}

		if (DistributedMappingAlgorithm.DEBUG) {
			System.out.println("   " + this + ": success: " + (embedResult != null) + ". Notifying sender ...");
		}

		if (getSource().isMainNode) {
			if (DistributedMappingAlgorithm.DEBUG) {
				if (!stoppedDelegationNodes.isEmpty()) {
					System.out.println("   " + this + ": starting all delegation nodes that were stopped previously.");
				}
			}
			while (!stoppedDelegationNodes.isEmpty()) {
				onThisClusterHead.send(
						new StartMessage(onThisClusterHead),
						stoppedDelegationNodes.poll());
			}
		}
		
		EmbeddingResultMessage result = new EmbeddingResultMessage(onThisClusterHead, this, embedResult);
		onThisClusterHead.send(result, getSource());
	}
	
	
	LinkedList<UpdateEntry> delegateToADirectChild() {
		if (DistributedMappingAlgorithm.DEBUG) {
			System.out.println("   " + this + ": upper-level node (i know about all delegation nodes)");
		}

		Collection<ClusterHead> tmp = onThisClusterHead.getChildren();

		if (tmp != null) {
			List<ClusterHead> children = new LinkedList<ClusterHead>(tmp);

			Collections.shuffle(children);
			for (ClusterHead child : children) {
				if (child != getSource()) {

					//TODO: isLocallyUnLocked or !isLocallyFullyLocked?
					if (child.isDelegationNode() || onThisClusterHead.isLocallyUnLocked(child)) {

						if (child.isDelegationNode()) {
							if (DistributedMappingAlgorithm.DEBUG) {
								System.out.println("   " + this + ": child: isDelegationNode -> sending stop message");
							}

							StopMessage myMsg = new StopMessage(onThisClusterHead, getVirtualNetwork(), stoppedDelegationNodes);
							onThisClusterHead.send(myMsg, child);
							EmbeddingResultMessage answer = myMsg.getReceivedAnswer();
							if (answer != null && answer.succeded()) {
								return answer.getEmbeddingResult();
							} else {
								stoppedDelegationNodes.add(child);
							}
						} else {
							if (DistributedMappingAlgorithm.DEBUG) {
								System.out.println("   " + this + ": child (" + child + ", " + onThisClusterHead + "): !isDelegationNode -> delegating");
							}

							onThisClusterHead.setLocalFullLock(child, true);
							
							DelegateRequestMessage myMsg = new DelegateRequestMessage(onThisClusterHead, getVirtualNetwork(), stoppedDelegationNodes);
							onThisClusterHead.send(myMsg, child);
							EmbeddingResultMessage answer = myMsg.getReceivedAnswer();
							
							onThisClusterHead.setLocalFullLock(child, false);
							
							if (answer != null && answer.succeded()) {
								return answer.getEmbeddingResult();
							}
						}
					}
				}
			}

		}
		
		return null;
	}

	/**
	 * Tries to delegate requests to another clusterheads 
	 * @return Collection of UpdateEntries
	 */
	LinkedList<UpdateEntry> delegate() {
		assert(onThisClusterHead.isDelegationNode());

		Collection<ClusterHead> embeddingCandidates = onThisClusterHead.findClusterCandidates(getVirtualNetwork(), onThisClusterHead);
		if (DistributedMappingAlgorithm.DEBUG) {
			System.out.println("embeddingCandidates: " + embeddingCandidates.size());
		}

		EmbeddingResultMessage embedded = null;
		for (ClusterHead candiCluster : embeddingCandidates) {

			if (onThisClusterHead.isLocallyUnLocked(candiCluster)) {
				if (DistributedMappingAlgorithm.DEBUG) {
					System.out.println("   " + this + ": delegate: sending DelegateRequestMessage to " + candiCluster + "...");
				}

				onThisClusterHead.setLocalFullLock(candiCluster, true);
				DelegateRequestMessage msg = new DelegateRequestMessage(onThisClusterHead, getVirtualNetwork(), stoppedDelegationNodes);
				onThisClusterHead.send(msg, candiCluster);

				embedded = msg.getReceivedAnswer();

				if (DistributedMappingAlgorithm.DEBUG) {
					System.out.println("   " + this + ": delegate: got an answer and unlocked sublocktree. succeded: " + (embedded != null && embedded.getEmbeddingResult() != null) + "  " + msg);
				}

				onThisClusterHead.setLocalFullLock(candiCluster, false);
			}


			if (embedded != null && embedded.succeded()) {
				break;
			}
		}

		return (embedded != null ? embedded.getEmbeddingResult() : null);
	}
	
	/**
	 * This Method tries to embed the network by himself (on this clusterhead)
	 * @return Result of the embedding
	 */
	LinkedList<UpdateEntry> embedItHere() {
		if (DistributedMappingAlgorithm.DEBUG) {
			System.out.println(this + ": trying to embed it myself ...");
		}
		
		
		double res = 0.0d;
		if(onThisClusterHead instanceof FullKnowledgeClusterHead) {
			for (SubstrateLink sl : onThisClusterHead.cluster.getEdges()) {
				for (AbstractResource r : sl) {
					if (r instanceof BandwidthResource) {
						res += ((BandwidthResource) r).getAvailableBandwidth();
					}
				}
			}
//		} else if (onThisClusterHead instanceof PartialKnowledgeClusterHead){
//			PartialKnowledgeClusterHead pkn = (PartialKnowledgeClusterHead) onThisClusterHead;
//			res = pkn.getNetInfo().getSumBandwidth();
		}
		
		
		double dem = 0.0d;
		for (VirtualLink vl : getVirtualNetwork().getEdges()) {
			for (AbstractDemand d : vl) {
				if (d instanceof BandwidthDemand) {
					dem += ((BandwidthDemand) d).getDemandedBandwidth();
				}
			}
		}
		
		double result = res / dem;
		
		LinkedList<UpdateEntry> embedResult = onThisClusterHead.embed(getVirtualNetwork(), this);
		
		if (DistributedMappingAlgorithm.DEBUG) {
			if (embedResult != null)
				System.out.println(this + ": tried embedding it myself. result: " + (embedResult != null));
		}
		
		onThisClusterHead.counter.attemptEmbedded(onThisClusterHead, vNetwork, result);
		if (embedResult != null) {
			onThisClusterHead.updateLocalState(embedResult);
			
			onThisClusterHead.counter.successfullyEmbedded(onThisClusterHead, vNetwork, result);
		}
		
		
			
		return embedResult;
	}

	LinkedList<UpdateEntry> delegateToParentNodes(DelegateRequestMessage msg) {
		ClusterHead parent = onThisClusterHead.getParent();

		if (parent == null) {
			if (DistributedMappingAlgorithm.DEBUG) {
				System.out.println("   " + this + ": delegateToParent: parent == null!");
			}
			return null;
		} else {

			if (DistributedMappingAlgorithm.DEBUG) {
				System.out.println("   " + this + ": delegateToParent: parent != null");
			}
			
			onThisClusterHead.setLocalFullLock(parent, true);

			DelegateRequestMessage delegationMsg = new DelegateRequestMessage(onThisClusterHead, getVirtualNetwork(), stoppedDelegationNodes);
			onThisClusterHead.send(delegationMsg, parent);
			EmbeddingResultMessage result = delegationMsg.getReceivedAnswer();

			if (DistributedMappingAlgorithm.DEBUG) {
				System.out.println(this + ": done.");
			}
			
			onThisClusterHead.setLocalFullLock(parent, false);

			return (result.succeded() ? result.getEmbeddingResult() : null);
		}
	}
	
	@Override
	public String toString() {
		if (onThisClusterHead == null) {
			return "-1_-1";
		}
		return onThisClusterHead + "_" + onThisClusterHead.getLevel();
	}

}
