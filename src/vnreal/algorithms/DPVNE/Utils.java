package vnreal.algorithms.DPVNE;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.shortestpath.MyDijkstraShortestPath;
import vnreal.constraints.demands.AbstractDemand;
import vnreal.constraints.demands.BandwidthDemand;
import vnreal.constraints.resources.AbstractResource;
import vnreal.constraints.resources.BandwidthResource;
import vnreal.constraints.resources.CpuResource;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualLink;

public class Utils {

	private Utils() {}
	
	public static int getHopCount(
			SubstrateNetwork sNetwork,
			SubstrateNode n1, SubstrateNode n2,
			Transformer<SubstrateLink, Double> t) {
		
		return findShortestPath(sNetwork, n1, n2, t).size();
	}
	
	public static LinkedList<SubstrateLink> getLongestShortestPath(SubstrateNetwork sNetwork, Transformer<SubstrateLink, Double> t) {
		Map<SubstrateNode, Collection<LinkedList<SubstrateLink>>> shortestPaths = Utils.findAllShortestPaths(sNetwork, t);
		
		LinkedList<SubstrateLink> longestShortest = null;
		for (Entry<SubstrateNode, Collection<LinkedList<SubstrateLink>>> current : shortestPaths.entrySet()) {
			for (LinkedList<SubstrateLink> path : current.getValue()) {
				if (longestShortest == null || path.size() > longestShortest.size()) {
					longestShortest = path;
				}
			}
		}
		
		return longestShortest;
	}
	
	public static void getScenarioNames(HashMap<String, String> scenarioNames) {
		for (Entry<String, String> scenario1 : scenarioNames.entrySet()) {
			String[] split1 = scenario1.getKey().split("_");
			for (String split1entry : split1) {
				String[] kv1 = split1entry.split(":");
				boolean addsplit1entry = false;
				
				for (Entry<String, String> scenario2 : scenarioNames.entrySet()) {
					if (scenario1.getKey().equals(scenario2.getKey()))
						continue;
					
					String[] split2 = scenario2.getKey().split("_");
					boolean foundHere = false;
					for (String split2entry : split2) {
						String[] kv2 = split2entry.split(":");
						if (kv2[0].equals(kv1[0])) {
							foundHere = true;
							if (kv1.length > 1 && kv2.length > 1 && !kv2[1].equals(kv1[1])) {
								addsplit1entry = true;
								break;
							}
						}
					}
					
					if (!foundHere)
						addsplit1entry = true;
					if (addsplit1entry)
						break;
				}
				
				if (addsplit1entry) {
					if (scenarioNames.get(scenario1.getKey()) == null) {
						scenarioNames.put(scenario1.getKey(), split1entry);
					} else {
						scenarioNames.put(scenario1.getKey(), scenario1.getValue() + "_" + split1entry);
					}
				}
			}
			
			if (scenarioNames.get(scenario1.getKey()) == null)
				scenarioNames.put(scenario1.getKey(), "");
		}
	}
	
	public static BandwidthResource getBandwidthResource(SubstrateLink sl) {
		for (AbstractResource r : sl.get()) {
			if (r instanceof BandwidthResource) {
				return (BandwidthResource) r;
			}
		}

		return null;
	}
	
	public static BandwidthDemand getBandwidthDemand(VirtualLink vl) {
		for (AbstractDemand r : vl.get()) {
			if (r instanceof BandwidthDemand) {
				return (BandwidthDemand) r;
			}
		}

		return null;
	}
	
	/**
	 * Returns the SubstrateNode with most CPU Ressources
	 * @param cluster SubstrateNetwork
	 * @return Node with most CPUResources
	 */
	public static SubstrateNode findClusterhead(SubstrateNetwork cluster) {
		SubstrateNode result = null;
		double cpuMax = 0.0d;
		
		for (SubstrateNode sn : cluster.getVertices()) {
			if (result == null) {
				result = sn;
			} else {
				for (AbstractResource r : sn) {
					if (r instanceof CpuResource) {
						CpuResource c = (CpuResource) r;
						double cpu = c.getAvailableCycles();
						if (cpu > cpuMax) {
							cpuMax = cpu;
							result = sn;
						}
					}
				}
			}
		}
		
		return result;
	}
	
	public static SubstrateNode findNode(SubstrateNetwork cluster, String name) {
		
		for (SubstrateNode sn : cluster.getVertices()) {
			if (sn.getName().equals(name)) {
				return sn;
			}
		}
		
		return null;
	}

	
	public static double findMaxAvailBandwidth(SubstrateNetwork cluster) {
		double max = 0.0d;
		for (SubstrateLink sl : cluster.getEdges()) {
			for (AbstractResource r : sl) {
				if (r instanceof BandwidthResource) {
					BandwidthResource br = (BandwidthResource) r;
					if (br.getAvailableBandwidth() > max) {
						max = br.getAvailableBandwidth();
						break;
					}
				}
			}
		}
		return max;
	}
	
	public static LinkedList<SubstrateLink> findShortestPath(
			SubstrateNetwork sNetwork,
			SubstrateNode n1, SubstrateNode n2,
			Transformer<SubstrateLink, Double> t) {

		if (n1.getId() == n2.getId()) {
			return new LinkedList<SubstrateLink>();
		}

		MyDijkstraShortestPath<SubstrateNode, SubstrateLink> dijkstra =
				new MyDijkstraShortestPath<SubstrateNode, SubstrateLink>(
						sNetwork, t, true);

		LinkedList<SubstrateLink> path = dijkstra.getPath(n1, n2, -1);

		return path;
	}
	
	public static Map<SubstrateNode, Collection<LinkedList<SubstrateLink>>> findAllShortestPaths(
			SubstrateNetwork sNetwork,
			Transformer<SubstrateLink, Double> t) {
		
		Map<SubstrateNode, Collection<LinkedList<SubstrateLink>>> result =
				new HashMap<SubstrateNode, Collection<LinkedList<SubstrateLink>>>();
		
		for (SubstrateNode n : sNetwork.getVertices()) {
			result.put(n, findAllShortestPaths(sNetwork, n, t));
		}
		
		return result;
	}
	
	public static Collection<LinkedList<SubstrateLink>> findAllShortestPaths(
			SubstrateNetwork sNetwork,
			SubstrateNode n,
			Transformer<SubstrateLink, Double> t) {
		
		Collection<LinkedList<SubstrateLink>> result = new LinkedList<LinkedList<SubstrateLink>>();
		
		for (SubstrateNode n2 : sNetwork.getVertices()) {
			if (n != n2) {
				LinkedList<SubstrateLink> current =
						findShortestPath(sNetwork, n, n2, t);
				
				if (current != null) {
					result.add(current);
				}
			}
		}
		
		return result;
	}
	
	public static int getBetweenness(SubstrateNetwork sNetwork, SubstrateNode n, Transformer<SubstrateLink, Double> t) {
		Collection<LinkedList<SubstrateLink>> paths = getShortestPathsANodeGoesThrough(sNetwork, n, t);
		
		return paths.size();
	}
	
	public static int getBetweenness(SubstrateNetwork sNetwork, SubstrateLink l, Transformer<SubstrateLink, Double> t) {
		Collection<LinkedList<SubstrateLink>> paths = getShortestPathsALinkGoesThrough(sNetwork, l, t);
		
		return paths.size();
	}
	
	public static Collection<LinkedList<SubstrateLink>> getShortestPathsANodeGoesThrough(
			SubstrateNetwork sNetwork,
			SubstrateNode n,
			Transformer<SubstrateLink, Double> t) {
		
		Collection<LinkedList<SubstrateLink>> result = new LinkedList<LinkedList<SubstrateLink>>();
		
		Map<SubstrateNode, Collection<LinkedList<SubstrateLink>>> all =
				Utils.findAllShortestPaths(sNetwork, t);
		
		for (Entry<SubstrateNode, Collection<LinkedList<SubstrateLink>>> e : all.entrySet()) {
			if (e.getKey() != n) {
				for (LinkedList<SubstrateLink> path : e.getValue()) {
					
					if (sNetwork.getEndpoints(path.getLast()).getSecond() != n) {

						for (SubstrateLink sl : path) {
							if (path.getLast() != sl) {
								if (sNetwork.getEndpoints(sl).getSecond() == n) {
									result.add(path);
									break;
								}
							}
						}
					}
				}
			}
		}
		
		return result;
	}
	
	public static Collection<LinkedList<SubstrateLink>> getShortestPathsALinkGoesThrough(
			SubstrateNetwork sNetwork,
			SubstrateLink l,
			Transformer<SubstrateLink, Double> t) {
		
		Collection<LinkedList<SubstrateLink>> result = new LinkedList<LinkedList<SubstrateLink>>();
		
		Map<SubstrateNode, Collection<LinkedList<SubstrateLink>>> all =
				Utils.findAllShortestPaths(sNetwork, t);
		
		for (Entry<SubstrateNode, Collection<LinkedList<SubstrateLink>>> e : all.entrySet()) {
			for (LinkedList<SubstrateLink> path : e.getValue()) {
				
				if (path.contains(l)) {
					result.add(path);
					break;
				}
			}
		}
		
		return result;
	}
	
}
