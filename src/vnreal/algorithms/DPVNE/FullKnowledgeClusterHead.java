package vnreal.algorithms.DPVNE;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import vnreal.algorithms.singlenetworkmapping.SingleNetworkMappingAlgorithm;
import vnreal.algorithms.utils.SubgraphBasicVN.Utils;
import vnreal.constraints.demands.AbstractDemand;
import vnreal.constraints.demands.BandwidthDemand;
import vnreal.constraints.demands.CpuDemand;
import vnreal.constraints.resources.AbstractResource;
import vnreal.constraints.resources.PowerResource;
import vnreal.mapping.Mapping;
import vnreal.network.NetworkEntity;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

public class FullKnowledgeClusterHead extends ClusterHead {

	/**
	 * Creates a new FullKnowledgeClusterHead
	 * @param cluster SubstrateNetwork
	 * @param isMainNode True if is Main Node
	 * @param isDelegationNode True if DelegationNode
	 * @param clusterHeadNode ??
	 * @param delegationNodes Collection of DelagationNodes
	 * @param mappingAlgorithm Used Mappingalgorithm
	 * @param estimationAlgorithm  Used estimationAlgo
	 */
	public FullKnowledgeClusterHead(
			SubstrateNetwork cluster,
			boolean isMainNode,
			int level,
			boolean isDelegationNode,
			SubstrateNode clusterHeadNode,
			Collection<ClusterHead> delegationNodes,
			SingleNetworkMappingAlgorithm mappingAlgorithm,
			NetworkEstimation estimationAlgorithm) {

		super(cluster, isMainNode, level, isDelegationNode, clusterHeadNode, delegationNodes, mappingAlgorithm, estimationAlgorithm);
	}

	public SubstrateNetwork getCluster() {
		return cluster;
	}

	/**
	 * This method applies all updates 
	 * @param updates Collection of Update-Entries
	 */
	public void applyUpdates(Collection<UpdateEntry> updates) {
		
		if (DistributedMappingAlgorithm.DEBUG) {
			System.out.println("applyUpdates: " + isMainNode + " " + this + "  " + (updates == null ? "null" : updates.size()));
		}
		
		if (updates != null) {
			
			for (UpdateEntry e : updates) {
				
				if (!appliedUpdates.contains(e)) {
					
					NetworkEntity<AbstractResource> ne = null;
					if (e.isNode) {
						ne = findSNode(cluster, e.sEntityName);
					} else {
						ne = findSLink(cluster, e.sEntityName);
					}
					
					if (ne != null) {
						
						assign(ne, e);
//						if (!a && isMainNode) {
//							throw new AssertionError("SOMETHING WAS NOT EMBEDDED CORRECTLY: " 
//									+ ne + " " +e.demand+ " " + e.sEntityName + "\n\n" + cluster);
//						}
					} else if (isMainNode) {
						throw new AssertionError(cluster + " " + e.layer);
					}

					appliedUpdates.add(e);
				}	
			}
		}
		
	}
	
	@Override
	public Collection<ClusterHead> findClusterCandidates(final VirtualNetwork vNetwork, ClusterHead orig) {

		LinkedList<FullKnowledgeClusterHead> result = new LinkedList<FullKnowledgeClusterHead>();
		LinkedList<LockTree> queue = new LinkedList<LockTree>();
		
		if (!subLockTree.isFullyLocked()) {
			queue.add(subLockTree);
		}

		while (!queue.isEmpty()) {
			LockTree current = queue.pop();
			if (current.getChildren() != null) {
				for (LockTree t : current.getChildren()) {
					//TODO: Here we got Information of Children without sending a message!
					if (estimationAlgorithm.estimate(
							((FullKnowledgeClusterHead) t.getClusterHead()).getCluster(), vNetwork) >= 1.0d) {
						if (t.isUnLocked()) {
							queue.addLast(t);
						}
					}
				}
			}
			if (current.getClusterHead() != orig) {
				result.add((FullKnowledgeClusterHead) current.getClusterHead());
			}
		}

		Collections.sort(result, new Comparator<FullKnowledgeClusterHead>() {
			@Override
			public int compare(FullKnowledgeClusterHead o1, FullKnowledgeClusterHead o2) {
				
				double i1 = estimationAlgorithm.estimate(o1.getCluster(), vNetwork);
				double i2 = estimationAlgorithm.estimate(o2.getCluster(), vNetwork);
				
				if (i1 < i2) {
					return -1;
				}
				if (i1 > i2) {
					return 1;
				}
				
				return 0;
			}
		});

		return new LinkedList<ClusterHead>(result);
	}
	
    public double ratioActiveServers(Collection<SubstrateNode> sNodes) {
        double activeServers = 0;
        for (SubstrateNode n : sNodes) {
            PowerResource er = Utils.getEnergyResource(n);
            if (er.isUsed()) {
                ++activeServers;
            }
        }

        return activeServers / sNodes.size();
    }
    
    public double ratioInactiveServers(Collection<SubstrateNode> sNodes) {
        double inactiveServers = 0;
        for (SubstrateNode n : sNodes) {
            PowerResource er = Utils.getEnergyResource(n);
            if (!er.isUsed()) {
                ++inactiveServers;
            }
        }

        return inactiveServers / sNodes.size();
    }

	@Override
	public LinkedList<UpdateEntry> embed(VirtualNetwork vNet,
			Message msg) {
		
		if (DistributedMappingAlgorithm.DEBUG) {
			System.out.print("embedding (" + this + ") ... ");
		}
		
		SubstrateNetwork newcluster = cluster.getCopy(false, true);
		VirtualNetwork vnetcopy = vNet.getCopy(false, true);
		if (mappingAlgorithm.mapNetwork(newcluster, vnetcopy)) {

			if (DistributedMappingAlgorithm.DEBUG) {
				System.out.println("success");
			}
			
			LinkedList<UpdateEntry> newupdates = toUpdate(vnetcopy);
			applyUpdates(newupdates);
			
			return newupdates;
		}

		if (DistributedMappingAlgorithm.DEBUG) {
			System.out.println("fail");
		}
		
		return null;
	}
	
	public LinkedList<UpdateEntry> toUpdate(VirtualNetwork vNet) {

		LinkedList<UpdateEntry> updates = new LinkedList<UpdateEntry>();

		for (VirtualNode v : vNet.getVertices()) {
			for (AbstractDemand d : v.get()) {
				double bw = 0d, cpu = 0d;
				if (d instanceof BandwidthDemand) {
					bw = ((BandwidthDemand) d).getDemandedBandwidth();
				}
				if (d instanceof CpuDemand) {
					cpu = ((CpuDemand) d).getDemandedCycles();
				}
				if (!d.getMappings().isEmpty()) {
					for (Mapping m : d.getMappings()) {
						updates.add(new UpdateEntry(true, vNet.getLayer(), m.getResource().getOwner().getName(), d));
					}
				}
			}
		}
		
		for (VirtualLink v : vNet.getEdges()) {
			for (AbstractDemand d : v.get()) {
				double bw = 0d, cpu = 0d;
				if (d instanceof BandwidthDemand) {
					bw = ((BandwidthDemand) d).getDemandedBandwidth();
				}
				if (d instanceof CpuDemand) {
					cpu = ((CpuDemand) d).getDemandedCycles();
				}
				if (!d.getMappings().isEmpty()) {
					for (Mapping m : d.getMappings()) {
						updates.add(new UpdateEntry(false, vNet.getLayer(), m.getResource().getOwner().getName(), d));
					}
				}
			}
		}
		
//		for (SubstrateNode sNode : cluster.getVertices()) {
//			for (AbstractResource res : sNode) {
//				for (Mapping m : res.getMappings()) {
//					if ((m.getDemand().getOwner()).getLayer() == vNet.getLayer()) { 
//						updates.add(new UpdateEntry(true, vNet.getLayer(), res.getOwner().getName(), res.getName(), m.getDemand()));
//					}
//				}
//			}
//
//		}
//		for (SubstrateLink sLink : cluster.getEdges()) {
//			for (AbstractResource res : sLink) {
//				for (Mapping m : res.getMappings()) {
//					if (((VirtualLink) m.getDemand().getOwner()).getLayer() == vNet.getLayer()) {
//						updates.add(new UpdateEntry(false, vNet.getLayer(), res.getOwner().getName(), res.getName(), m.getDemand()));
//					}
//				}
//			}
//
//		}

		return updates;
	}
	
	static NetworkEntity<AbstractResource> findSLink(SubstrateNetwork snet, String sEntityName) {
		for (SubstrateLink sl : snet.getEdges()) {
			if (sl.getName().equals(sEntityName)) {
				return sl;
			}
		}
		
		return null;
	}
	
	static NetworkEntity<AbstractResource> findSNode(SubstrateNetwork snet, String sEntityName) {
		for (SubstrateNode sl : snet.getVertices()) {
			if (sl.getName().equals(sEntityName)) {
				return sl;
			}
		}
		
		return null;
	}
	
	static NetworkEntity<AbstractDemand> findVNode(VirtualNetwork snet, String sEntityName) {
		for (VirtualNode sl : snet.getVertices()) {
			if (sl.getName().equals(sEntityName)) {
				return sl;
			}
		}
		
		return null;
	}
	
	static NetworkEntity<AbstractDemand> findVLink(VirtualNetwork snet, String sEntityName) {
		for (VirtualLink sl : snet.getEdges()) {
			if (sl.getName().equals(sEntityName)) {
				return sl;
			}
		}
		
		return null;
	}
	
	public static void assign(NetworkEntity<AbstractResource> sl, UpdateEntry e) {

		for (AbstractResource r : sl) {
			if (r.accepts(e.demand) && r.fulfills(e.demand) && e.demand.occupy(r)) {
				return;
			}
//			if (r instanceof CpuResource) {
//				CpuResource re = ((CpuResource) r);
//				re.setOccupiedCycles(re.getOccupiedCycles() + e.cpu);
//			}
//			if (r instanceof BandwidthResource) {
//				BandwidthResource re = ((BandwidthResource) r);
//				re.setOccupiedBandwidth(re.getOccupiedBandwidth() + e.bw);
//			}
		}
	}
	
	/**
	 * This method returns the requested virtual Cluster/Link demand Resource defined by the class
	 * @param sname Name of the Link/Node
	 * @return NetworkEntity<AbstractDemand>
	 */
	public static NetworkEntity<AbstractDemand> findVirtualEntity(String vname,
			VirtualNetwork vNet) {
		for (VirtualNode sn : vNet.getVertices()) {
			if (sn.getName().equals(vname)) {
				return sn;
			}
		}

		for (VirtualLink sl : vNet.getEdges()) {
			if (sl.getName().equals(vname)) {
				return sl;
			}
		}

		return null;
	}

//	@Override
//	public Collection<UpdateEntry> rollBack(VirtualNetwork vNet) {
////		Collection<UpdateEntry> result = new ArrayList<UpdateEntry>();
////		
////		//We are here FullyKnowledge, so we need to unmap the vNets
////		//1. if we have children, the unmap down to them
////		Collection<LockTree> children =  subLockTree.getChildren();
////		if(children != null) {
////			for(LockTree t : children) {
////				//For every children 
////				RollbackMessage rbm = new RollbackMessage(this, vNet);
////				this.send(rbm, t.getClusterHead());
////				//We don't need the answer, because we need to unmap it for ourselves
////			}
////		}
////		
////		//2. Unmap on our Cluster
////		//TODO: Make this more efficient 
////		ArrayList<UpdateEntry> undoEntries = new ArrayList<UpdateEntry>();
////		//Iterate over the list of VNodes
////		for(VirtualNode vn : vNet.getVertices()) {
////			//Search in the list of appliedUpdates for matches
////			for(UpdateEntry ue : appliedUpdates) {
////				if(ue.getvEntity().equals(vn.getName()))
////					//Add it to the undolist
////					undoEntries.add(ue);
////			}
////		}
////		//if we have no nodes mapped, there can't be any Links
////		if(undoEntries.isEmpty())
////			return null;
////		
////		//Iterate over the list of VLinks
////		for(VirtualLink vl : vNet.getEdges()) {
////			//Search in the list of appliedUpdates for matches
////			for(UpdateEntry ue : appliedUpdates) {
////				if(ue.getvEntity().equals(vl.getName()))
////					//Add it to the undolist
////					undoEntries.add(ue);
////			}
////		}
////		
////		//we now have the complete list of all Entries to undo
////		for(UpdateEntry ue : undoEntries) {
////			unmap(ue, vNet);
////			appliedUpdates.remove(ue);
////			result.add(ue);
////		}
////		
////		return result;
//		throw new Error();
//	}

//	/**
//	 * This unmaps the given Demand and Resource
//	 * 
//	 * @param ue The {@link UpdateEntry}
//	 * @param vNet The Corresponding {@link VirtualNetwork}
//	 */
//	private void unmap(UpdateEntry ue, VirtualNetwork vNet) {
//		//Find vEntity and sEntity 
//		NetworkEntity<AbstractResource> sEntity = cluster.getEntitieByName(ue.getsEntity());
//		NetworkEntity<AbstractDemand> vEntity = vNet.getEntitieByName(ue.getvEntity());
//		
//		if(sEntity == null || vEntity == null) {
//			throw new Error("One entity was not found! Can't be!");
//		}
//		
//		//Walk through all Demands
//		for(AbstractDemand ad : vEntity.get()) {
//			//Walk through all Resources
//			for(AbstractResource ar : sEntity.get()) {
//				//if it accepts
//				if(ar.accepts(ad)) {
//					//Free the Resource
//					ad.free(ar);
//				}
//			}
//		}
//	}

	
}
