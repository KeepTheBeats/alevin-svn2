package vnreal.algorithms.DPVNE;

import java.util.Collection;
import java.util.LinkedList;
import vnreal.algorithms.DPVNE.singlenetworkmapping.factory.EmbeddingAlgorithmFactory;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;

public class HierarchicalPartitioning {
	
	int pCounter = 0;
	final int fullKnowledgeNodesLevel;
	final int delegationNodesLevel;
	final int minPartitionSize;
	final PartitioningAlgorithm partitioningAlgorithm;
//	final PartialLinkmappingAlgorithm partialLinkmappingAlgorithm;
	final EmbeddingAlgorithmFactory embeddingAlgorithmFactory;
	final NetworkEstimation estimationAlgorithm;
	int maxLevel;
	
//	public static HashMap<SubstrateNetwork, LockTree> cache = new HashMap<SubstrateNetwork, LockTree>();
	
	public HierarchicalPartitioning(
			int fullKnowledgeNodesLevel,
			int delegationNodesLevel,
			int minPartitionSize,
			int maxLevel,
			PartitioningAlgorithm partitioningAlgorithm,
//			PartialLinkmappingAlgorithm partialLinkmappingAlgorithm,
			EmbeddingAlgorithmFactory embeddingAlgorithmFactory,
			NetworkEstimation estimationAlgorithm) {
		
		this.fullKnowledgeNodesLevel = fullKnowledgeNodesLevel;
		this.delegationNodesLevel = delegationNodesLevel;
		this.minPartitionSize = minPartitionSize;
		this.partitioningAlgorithm = partitioningAlgorithm;
//		this.partialLinkmappingAlgorithm = partialLinkmappingAlgorithm;
		this.embeddingAlgorithmFactory = embeddingAlgorithmFactory;
		this.estimationAlgorithm = estimationAlgorithm;
		this.maxLevel = maxLevel;
	}
	
	public EmbeddingAlgorithmFactory getEmbeddingAlgorithmFactory() {
		return embeddingAlgorithmFactory;
	}

	public NetworkEstimation getEstimationAlgorithm() {
		return estimationAlgorithm;
	}
	
	/**
	 * Creates the rootNode of the LockTree
	 * 
	 * @param cluster SubstrateNetwork
	 * @return Locktree with the Clusterheads
	 */
	public LockTree getPartitionsTree(SubstrateNetwork cluster) {
		
//		if (cache.get(cluster) != null) {
//			return cache.get(cluster);
//		}
		
		SubstrateNetwork newCluster = cluster.getCopy(false, true);
//		newCluster.generateDuplicateEdges();
		SubstrateNode clusterhead = Utils.findClusterhead(newCluster);
		Collection<ClusterHead> delegationNodes = new LinkedList<ClusterHead>();
		
		ClusterHead newClusterHead = null;
		if (fullKnowledgeNodesLevel <= 0) {
			newClusterHead = new FullKnowledgeClusterHead(
					newCluster,
					false,
					0,
					(delegationNodesLevel == 0),
					clusterhead,
					delegationNodes,
					embeddingAlgorithmFactory.createInstance(),
					estimationAlgorithm);
//		} else {
//			newClusterHead = new PartialKnowledgeClusterHead(
//					cluster,
//					(delegationNodesLevel == 0),
//					0,
//					clusterhead,
//					delegationNodes,
//					estimationAlgorithm,
//					partitioningAlgorithm,
//					partialLinkmappingAlgorithm,
//					embeddingAlgorithmFactory.createInstance(),
//					null);
		}
		
		LockTree root = new LockTree("p" + pCounter++, null, newClusterHead);
		
		if (delegationNodesLevel == 0) {
			delegationNodes.add(newClusterHead);
		}
		
		int d = 0;
		if (maxLevel != 0)
			d = getPartitionsTree(root, 1, delegationNodes, newCluster, 0);
		root.setDepth(d);
		
		cloneSubLockTrees(root);
		
//		cache.put(cluster, root);
		
//		removeDuplicateEdges(root, cluster.getEdges());
		
		return root;
	}
	
	void removeDuplicateEdges(LockTree current, Collection<SubstrateLink> orig) {
		if (current.getChildren() != null) {
			for (LockTree c : current.getChildren()) {
				SubstrateNetwork cc = current.getClusterHead().cluster;
				for (SubstrateLink l : new LinkedList<SubstrateLink>(cc.getEdges())) {
					boolean found = false;
					for (SubstrateLink o : orig) {
						if (o.getName().equals(l.getName())) {
							found = true;
							break;
						}
					}
					if (!found) {
						cc.removeEdge(l);
					}
				}
				removeDuplicateEdges(c, orig);
			}
		}
	}
	
	protected static void cloneSubLockTrees(LockTree current) {
		current.getClusterHead().setSubLockTree(current.cloneUnLockedSubTree());
		if (current.getChildren() != null) {
			for (LockTree c : current.getChildren()) {
				cloneSubLockTrees(c);
			}
		}
	}
	
	/**
	 * Creates whole Locktree below root
	 * @param parent Parent Tree
	 * @param level Level of Tree
	 * @param delegationNodes the DelegationNodes
	 * @param parentCluster SubstratNetwork of Parent
	 */
	private int getPartitionsTree(LockTree parent, int level, Collection<ClusterHead> delegationNodes,
			SubstrateNetwork parentCluster, int depth) {
		int maxdepth = depth;
		if (parentCluster.getVertexCount() < minPartitionSize) {
			return maxdepth;
		}
		
		Collection<Partition> partitions = partitioningAlgorithm.getPartitions(parentCluster);
		if (partitions != null && partitions.size() > 1) {
			LinkedList<LockTree> children = new LinkedList<LockTree>();
			for (Partition partition : partitions) {
				
				SubstrateNetwork partitionNet = partition.getSubstrateNetwork();
				SubstrateNode clusterhead = Utils.findClusterhead(partitionNet);
				
				ClusterHead clusterHead = null;
				
				if (fullKnowledgeNodesLevel <= level) {
					clusterHead = new FullKnowledgeClusterHead(
							partitionNet,
							false,
							level,
							(delegationNodesLevel == level),
							clusterhead,
							(delegationNodesLevel <= level ? null : delegationNodes),
							embeddingAlgorithmFactory.createInstance(),
							estimationAlgorithm);
					
					//we only need to add Information if the parent is PK
//					if(parent.getClusterHead() instanceof PartialKnowledgeClusterHead) {
//						PartialKnowledgeClusterHead pkn = (PartialKnowledgeClusterHead) parent.getClusterHead();
//						pkn.addChildInfo(clusterHead, new NetworkInformation(partitionNet.getCopy(false, true), partition.getDirectedEdgesToNeighbors()));
//					}
//				} else {
//					clusterHead = new PartialKnowledgeClusterHead(
//							partitionNet.getCopy(false, true),
//							(delegationNodesLevel == level),
//							level,
//							clusterhead,
//							(delegationNodesLevel <= level ? null : delegationNodes),
//							estimationAlgorithm,
//							partitioningAlgorithm,
//							partialLinkmappingAlgorithm,
//							embeddingAlgorithmFactory.createInstance(),
//							partition.getDirectedEdgesToNeighbors());
//					
//					//If we have a PKNode we simply add our the info to the Parent (has to be a PK Node to)
//					((PartialKnowledgeClusterHead)parent.getClusterHead()).addChildInfo(clusterHead, ((PartialKnowledgeClusterHead)clusterHead).getNetInfo().getCopy());
				}
				
				LockTree child = new LockTree("p" + (pCounter++), parent, clusterHead);
				
				if (delegationNodesLevel == level) {
					delegationNodes.add(clusterHead);
				}
				
				// in reality, the next step would be done by "clusterhead".
				if (maxLevel == -1 || level < maxLevel) {
					int d = getPartitionsTree(child, level+1, delegationNodes, partition.getSubstrateNetwork(), depth+1);
					if (d > maxdepth) {
						maxdepth = d;
					}
				}
				
				children.add(child);
			}
			parent.setChildren(children);
		}
		
		return maxdepth;
	}
	
}
