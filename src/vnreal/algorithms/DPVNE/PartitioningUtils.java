package vnreal.algorithms.DPVNE;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;

public class PartitioningUtils {
	
	private PartitioningUtils() {
	}
	
	public static LinkedList<Partition> findAllPartitions(SubstrateNetwork cluster, SubstrateNetwork wholeNetwork) {
		
		LinkedList<Partition> result = new LinkedList<Partition>();
		Collection<SubstrateNode> remainingNodes = cluster.getVertices();
		
		int seenNodes = 0;
		for (Iterator<SubstrateNode> nodeset = remainingNodes.iterator(); nodeset.hasNext(); nodeset = remainingNodes.iterator()) {
			SubstrateNetwork currentCluster = dfs(cluster, nodeset.next());
			result.add(new Partition(currentCluster));
			
			Collection<SubstrateNode> currentClusterNodes = currentCluster.getVertices();
			seenNodes += currentClusterNodes.size();
			if (seenNodes == cluster.getVertexCount()) {
				break;
			}
			
			remainingNodes = vnreal.algorithms.utils.SubgraphBasicVN.Utils.minus(remainingNodes, currentClusterNodes);
		}
		
		if (wholeNetwork != null) {
			for (Partition s1 : result) {

				for (Partition s2 : result) {

					for (SubstrateNode n1 : s1.getSubstrateNetwork().getVertices()) {
						for (SubstrateNode n2 : s2.getSubstrateNetwork().getVertices()) {
							Collection<SubstrateLink> edges = wholeNetwork.findEdgeSet(n1, n2);
							if (edges != null && !edges.isEmpty()) {
								for (SubstrateLink e : edges) {
									if (wholeNetwork.getEndpoints(e).getFirst() == n1) {
										s1.getDirectedEdgesToNeighbors().add(new PartitionConnection(n1.getName(), s1, n2.getName(), s2, e));
									} else {
										s2.getDirectedEdgesToNeighbors().add(new PartitionConnection(n2.getName(), s2, n1.getName(), s1, e));
									}

								}
							}
						}
					}

				}
			}
		}
		
		return result;
	}
	
	public static SubstrateNetwork dfs(SubstrateNetwork sNetwork, SubstrateNode sn) {
		SubstrateNetwork result = sNetwork.getInstance(false);
		dfs(sNetwork, sn, result);
		return result;
	}
	
	private static void dfs(SubstrateNetwork sNetwork, SubstrateNode sn, SubstrateNetwork result) {
		
		Collection<SubstrateLink> resultedges = result.getEdges();
		
		if (!result.getVertices().contains(sn)) {
			result.addVertex(sn);
			for (SubstrateLink sl : sNetwork.getInEdges(sn)) {
				if (!resultedges.contains(sl)) {
					SubstrateNode opp = sNetwork.getOpposite(sn, sl);
					dfs(sNetwork, opp, result);
					result.addEdge(sl, opp, sn);
				}
			}
			for (SubstrateLink sl : sNetwork.getOutEdges(sn)) {
				if (!resultedges.contains(sl)) {
					SubstrateNode opp = sNetwork.getOpposite(sn, sl);
					dfs(sNetwork, opp, result);
					result.addEdge(sl, sn, opp);
				}
			}
		}
	}
	
	
//	public static Collection<VirtualNetwork> findAllClusters(VirtualNetwork cluster) {
//		
//		   Collection<VirtualNetwork> result = new LinkedList<VirtualNetwork>();
//		   Collection<VirtualNode> remainingNodes = cluster.getVertices();
//		   
//		   int seenNodes = 0;
//		   for (Iterator<VirtualNode> nodeset = remainingNodes.iterator(); nodeset.hasNext(); ) {
//			   VirtualNetwork currentCluster = dfs(cluster, nodeset.next());
//			   result.add(currentCluster);
//			   
//			   Collection<VirtualNode> currentClusterNodes = currentCluster.getVertices();
//			   seenNodes += currentClusterNodes.size();
//			   if (seenNodes == cluster.getVertexCount()) {
//				   break;
//			   }
//			   
//			   remainingNodes = vnreal.algorithms.utils.SubgraphBasicVN.Utils.minus(remainingNodes, currentClusterNodes);
//			   nodeset = remainingNodes.iterator();
//		   }
//		   
//		   return result;
//	}
//	
//	public static VirtualNetwork dfs(VirtualNetwork sNetwork, VirtualNode sn) {
//		
//		VirtualNetwork result = sNetwork.getInstance(false);
//		dfs(sNetwork, sn, result);
//		return result;
//	}
//	
//	private static void dfs(VirtualNetwork sNetwork, VirtualNode sn, VirtualNetwork result) {
//		
//		Collection<VirtualNode> resultnodes = result.getVertices();
//		
//		if (!resultnodes.contains(sn)) {
//			result.addVertex(sn);
//			for (VirtualLink sl : sNetwork.getInEdges(sn)) {
//				VirtualNode opp = sNetwork.getOpposite(sn, sl);
//				dfs(sNetwork, opp, result);
//				result.addEdge(sl, opp, sn);
//			}
//			for (VirtualLink sl : sNetwork.getOutEdges(sn)) {
//				VirtualNode opp = sNetwork.getOpposite(sn, sl);
//				dfs(sNetwork, opp, result);
//				result.addEdge(sl, sn, opp);
//			}
//		}
//	}

}
