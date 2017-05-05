/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2010-2011, The VNREAL Project Team.
 * 
 * This work has been funded by the European FP7
 * Network of Excellence "Euro-NF" (grant agreement no. 216366)
 * through the Specific Joint Developments and Experiments Project
 * "Virtual Network Resource Embedding Algorithms" (VNREAL). 
 *
 * The VNREAL Project Team consists of members from:
 * - University of Wuerzburg, Germany
 * - Universitat Politecnica de Catalunya, Spain
 * - University of Passau, Germany
 * See the file AUTHORS for details and contact information.
 * 
 * This file is part of ALEVIN (ALgorithms for Embedding VIrtual Networks).
 *
 * ALEVIN is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License Version 3 or later
 * (the "GPL"), or the GNU Lesser General Public License Version 3 or later
 * (the "LGPL") as published by the Free Software Foundation.
 *
 * ALEVIN is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * or the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License and
 * GNU Lesser General Public License along with ALEVIN; see the file
 * COPYING. If not, see <http://www.gnu.org/licenses/>.
 *
 * ***** END LICENSE BLOCK ***** */
package vnreal.algorithms.utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import vnreal.constraints.demands.AbstractDemand;
import vnreal.constraints.demands.BandwidthDemand;
import vnreal.constraints.demands.CpuDemand;
import vnreal.constraints.resources.AbstractResource;
import vnreal.constraints.resources.BandwidthResource;
import vnreal.constraints.resources.CpuResource;
import vnreal.mapping.Mapping;
import vnreal.network.Network;
import vnreal.network.NetworkStack;
import vnreal.network.Node;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

/**
 * Class with some util functions that are called from one or more virtual
 * network embedding algorithm
 * 
 * @author Juan Felipe Botero
 */

public class MiscelFunctions {

	/**
	 * Round the double d to the with c values after the comma
	 * 
	 * @param d
	 * @param c
	 * @return
	 */
	public static double round(double d, int decimalPlace) {
		BigDecimal bd = new BigDecimal(d);
		bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
		return bd.doubleValue();
	}

	/**
	 * Process the results of the LP solver to create an answer in a HashedMap
	 * Structure with the substrate and virtual link and the LP solver answer.
	 * 
	 * @param solverResult
	 *            Result of the solver
	 * @param value
	 *            variable of the solver that will be processed
	 * @return Hashed map structure with organized solver results
	 * 
	 */
	public static Map<List<String>, Double> processSolverResult(
			Map<String, Double> solverResult, String value) {
		Map<List<String>, Double> newSolverResult = new LinkedHashMap<List<String>, Double>();

		String word, subword;
		StringTokenizer elements, subElements;
		List<String> values, subValues = null;

		for (Iterator<String> cad = solverResult.keySet().iterator(); cad
				.hasNext();) {
			String ntmp = cad.next();
			Double vtmp = solverResult.get(ntmp);

			elements = new StringTokenizer(ntmp, value);
			values = new LinkedList<String>();

			while (elements.hasMoreTokens()) {
				word = elements.nextToken();
				subElements = new StringTokenizer(word, ",");
				subValues = new LinkedList<String>();
				while (subElements.hasMoreTokens()) {
					subword = subElements.nextToken();
					subValues.add(subword);
				}
				values.addAll(subValues);
			}
			newSolverResult.put(values, vtmp);
		}
		return newSolverResult;
	}

	/**
	 * 
	 * @param vNet
	 *            Virtual Network Request
	 * @return revenue of vNet
	 */
	public static double calculateVnetRevenue(VirtualNetwork vNet) {
		double total_demBW = 0;
		double total_demCPU = 0;
		Iterable<VirtualLink> tmpLinks;
		Iterable<VirtualNode> tmpNodes;
		tmpLinks = vNet.getEdges();
		tmpNodes = vNet.getVertices();
		for (Iterator<VirtualLink> tmpLink = tmpLinks.iterator(); tmpLink
				.hasNext();) {
			VirtualLink tmpl = tmpLink.next();
			for (AbstractDemand dem : tmpl) {
				if (dem instanceof BandwidthDemand) {
					total_demBW += ((BandwidthDemand) dem)
							.getDemandedBandwidth();
					break; // continue with next link
				}
			}
		}
		for (Iterator<VirtualNode> tmpNode = tmpNodes.iterator(); tmpNode
				.hasNext();) {
			VirtualNode tmps = tmpNode.next();
			for (AbstractDemand dem : tmps) {
				if (dem instanceof CpuDemand) {
					total_demCPU += ((CpuDemand) dem).getDemandedCycles();
					break; // continue with next node
				}
			}
		}
		return (total_demBW + total_demCPU);
	}

	/**
	 * Method to sort the set of virtual networks taking into account the
	 * revenues
	 * 
	 * @param stack
	 *            set of VNRs and substrate net
	 * @return ordered VNR stack by revenues
	 */
	public static NetworkStack sortByRevenues(NetworkStack stack) {
		List<VirtualNetwork> virtualNetworks = stack.getVirtuals();
		Collections.sort(virtualNetworks, new Comparator<VirtualNetwork>() {
			public int compare(VirtualNetwork v1, VirtualNetwork v2) {
				Double revenue = calculateVnetRevenue(v2) - calculateVnetRevenue(v1); // Decreasing order!
				return revenue.intValue();
			}
		});
		return new NetworkStack(stack.getSubstrate(), virtualNetworks);
	}

	/**
	 * 
	 * @param <K>
	 * @param map
	 * @return Ordered Map structure
	 */
	public static <K,V extends Comparable<V>> Map<K, V> sortByValue(Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o2, Map.Entry<K, V> o1) {
				return o1.getValue().compareTo(o2.getValue());
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	/**
	 * 
	 * @param net
	 * @param node
	 * @return available resources (or demands) of one node
	 */
	public static double getAr(Network<?, ?, ?> net, Node<?> node) {
		double incBw = 0, nodeAr = 0;

		if (net instanceof VirtualNetwork) {
			if (node instanceof VirtualNode) {
				for (AbstractDemand dem : ((VirtualNode) node))
					if (dem instanceof CpuDemand)
						nodeAr = ((CpuDemand) dem).getDemandedCycles();

				for (VirtualLink incLink : ((VirtualNetwork) net)
						.getOutEdges((VirtualNode) node))
					for (AbstractDemand dem : ((VirtualLink) incLink))
						if (dem instanceof BandwidthDemand)
							incBw += ((BandwidthDemand) dem)
									.getDemandedBandwidth();
			}
		}
		if (net instanceof SubstrateNetwork) {
			if (node instanceof SubstrateNode) {
				for (AbstractResource res : ((SubstrateNode) node))
					if (res instanceof CpuResource) {
						nodeAr = ((CpuResource) res).getAvailableCycles();
					}

				for (SubstrateLink incLink : ((SubstrateNetwork) net)
						.getOutEdges((SubstrateNode) node))
					for (AbstractResource res : incLink)
						if (res instanceof BandwidthResource) {
							incBw += ((BandwidthResource) res)
									.getAvailableBandwidth();
						}
			}
		}
		return (nodeAr * incBw);
	}

	/**
	 * 
	 * @param nodeSet
	 * @param node
	 * @param nodeAr
	 * @return the NodeRank (0) of node
	 */
	@SuppressWarnings("unchecked")
	public static double calculateNr_0_pJF(Collection<?> nodeSet, Node<?> node,
			Map<Node<?>, Double> nodeAr) {
		double sumAr = 0;
		for (Node<?> tempSnode : ((Collection<Node<?>>) nodeSet))
			sumAr += nodeAr.get(tempSnode);

		if (sumAr != 0) {
			return (nodeAr.get(node) / sumAr);
		} else {
			return 1;
		}

	}

	/**
	 * 
	 * @param nodeNR_i
	 * @param nodeNR_i_1
	 * @param nodeSet
	 * @return ||NodeRank(i+1) - NodeRank(i)||
	 */
	@SuppressWarnings("unchecked")
	public static double calculateNR_norm(Map<Node<?>, Double> nodeNR_i,
			Map<Node<?>, Double> nodeNR_i_1, Collection<?> nodeSet) {
		double sumNR = 0;
		for (Node<?> tempNode : ((Collection<Node<?>>) nodeSet))
			sumNR += Math.pow((nodeNR_i_1.get(tempNode) - nodeNR_i
					.get(tempNode)), 2);

		return Math.sqrt(sumNR);
	}

	/**
	 * 
	 * @param nodeNR_i
	 * @param p_J_u_v
	 * @param p_F_u_v
	 * @param nodeSet
	 * @return NodeRank (i+1) of the set of nodes from the NodeRank(i)
	 */
	@SuppressWarnings("unchecked")
	public static Map<Node<?>, Double> calculateNR_i_1(
			Map<Node<?>, Double> nodeNR_i, Map<NodexNode, Double> p_J_u_v,
			Map<NodexNode, Double> p_F_u_v, Collection<?> nodeSet) {
		Map<Node<?>, Double> nodeNR_i_1 = new LinkedHashMap<Node<?>, Double>();
		final double p_u_j = 0.15;
		final double p_u_f = 0.85;
		double nodeNR_i_1_value;
		for (Node<?> node1 : ((Collection<Node<?>>) nodeSet)) {
			nodeNR_i_1_value = 0;
			for (Iterator<NodexNode> tempNodePair = p_J_u_v.keySet().iterator(); tempNodePair
					.hasNext();) {
				NodexNode currNodexNode = tempNodePair.next();
				if (currNodexNode.getNode2().equals(node1))
					nodeNR_i_1_value += p_J_u_v.get(currNodexNode) * p_u_j
							* nodeNR_i.get(currNodexNode.getNode1());
			}

			for (Iterator<NodexNode> tempNodePair = p_F_u_v.keySet().iterator(); tempNodePair
					.hasNext();) {
				NodexNode currNodexNode = tempNodePair.next();
				if (currNodexNode.getNode2().equals(node1))
					nodeNR_i_1_value += p_F_u_v.get(currNodexNode) * p_u_f
							* nodeNR_i.get(currNodexNode.getNode1());
			}
			nodeNR_i_1.put(node1, nodeNR_i_1_value);
		}
		return nodeNR_i_1;
	}

	/**
	 * 
	 * @param vNode
	 *            virtual node
	 * @param sNode
	 *            substrate node
	 * @param distance
	 * @return true if vNode is separated from sNode by a value lower than
	 *         "distance"
	 */
	public static boolean nodeDistance(VirtualNode vNode, SubstrateNode sNode,
			int distance) {
		double dis;
		dis = Math.pow(sNode.getCoordinateX() - vNode.getCoordinateX(), 2)
				+ Math.pow(sNode.getCoordinateY() - vNode.getCoordinateY(), 2);
		if (Math.sqrt(dis) <= distance) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Function in charge of calculating the node ranking of a particular
	 * network (substrate or virtual network). see Algorithm 1 in: X. Cheng, S.
	 * Su, Z. Zhang, H. Wang, F. Yang, Y. Luo, J. Wang, Virtual network
	 * embedding through topology-aware node ranking, SIGCOMM Comput. Commun.
	 * Rev. 41 (2011) 38�47.
	 * 
	 * @param net
	 * @return the optimal NR
	 */
	public static Map<Node<?>, Double> create_NR(Network<?, ?, ?> net,
			double epsilon) {
		Map<Node<?>, Double> nodeAr = new LinkedHashMap<Node<?>, Double>();
		Map<Node<?>, Double> nodeNR_i = new LinkedHashMap<Node<?>, Double>();
		Map<Node<?>, Double> nodeNR_i_1 = new LinkedHashMap<Node<?>, Double>();
		Map<NodexNode, Double> p_J_u_v = new LinkedHashMap<NodexNode, Double>();
		Map<NodexNode, Double> p_F_u_v = new LinkedHashMap<NodexNode, Double>();
		NodexNode tempNodePair;
		Map<Node<?>, List<Node<?>>> nodeNeighbors = new LinkedHashMap<Node<?>, List<Node<?>>>();
		List<Node<?>> tempNodeNeighbors = new LinkedList<Node<?>>();
		double delta = epsilon + 1;

		for (Node<?> node : net.getVertices())
			nodeAr.put(node, getAr(net, node));

		for (Node<?> node : net.getVertices()) {
			nodeNR_i.put(node, calculateNr_0_pJF(net.getVertices(), node,
					nodeAr));
			tempNodeNeighbors.clear();

			if (net instanceof SubstrateNetwork)
				for (SubstrateLink sLink : ((SubstrateNetwork) net)
						.getOutEdges((SubstrateNode) node))
					tempNodeNeighbors.add(((SubstrateNetwork) net)
							.getDest(sLink));

			if (net instanceof VirtualNetwork)
				for (VirtualLink vLink : ((VirtualNetwork) net)
						.getOutEdges((VirtualNode) node))
					tempNodeNeighbors
							.add(((VirtualNetwork) net).getDest(vLink));

			nodeNeighbors.put(node, tempNodeNeighbors);
			for (Node<?> node2 : net.getVertices()) {
				if (!node.equals(node2)) {
					tempNodePair = new NodexNode(node, node2);
					p_J_u_v.put(tempNodePair, calculateNr_0_pJF(net
							.getVertices(), node2, nodeAr));
					if (tempNodeNeighbors.contains(node2))
						p_F_u_v.put(tempNodePair, calculateNr_0_pJF(
								tempNodeNeighbors, node2, nodeAr));
				}
			}
		}
		while (delta > epsilon) {
			nodeNR_i_1 = calculateNR_i_1(nodeNR_i, p_J_u_v, p_F_u_v, net
					.getVertices());
			delta = calculateNR_norm(nodeNR_i, nodeNR_i_1, net.getVertices());
			nodeNR_i = nodeNR_i_1;
		}
		return nodeNR_i_1;
	}

	/**
	 * 
	 * @param vNode
	 *            virtual node
	 * @param dem
	 *            demand of the virtual node
	 * @param filtratedsNodes
	 *            set of all nodes to extract the feasible substrate node
	 *            candidates
	 * @param dist
	 *            parameter to assure that the candidate nodes will be separated
	 *            of virtual node by a distance of, at maximum, equal to dist.
	 * @return The set of substrate nodes accomplishing a demand and a distance
	 *         dist from the vnode
	 */
	public static List<SubstrateNode> findFulfillingNodes(VirtualNode vNode,
			List<SubstrateNode> filtratedsNodes, int dist,
			Map<VirtualNode, SubstrateNode> nodeMapping) {
		List<SubstrateNode> nodes = new LinkedList<SubstrateNode>();
		for (SubstrateNode n : filtratedsNodes) {
			if (NodeLinkAssignation.isMappable(vNode, n)
					&& nodeDistance(vNode, n, dist)
					&& !nodeMapping.containsValue(n)) {
				nodes.add(n);
			}
		}
		return nodes;
	}

	/**
	 * 
	 * @param vNode
	 *            virtual node
	 * @param dem
	 *            demand of the virtual node
	 * @param filtratedsNodes
	 *            set of all nodes to extract the feasible substrate node
	 *            candidates
	 * @param dist
	 *            parameter to assure that the candidate nodes will be separated
	 *            of virtual node by a distance of, at maximum, equal to dist.
	 * @return The set of substrate nodes accomplishing a demand and a distance
	 *         dist from the vnode
	 */
	public static List<SubstrateNode> findFulfillingNodes(VirtualNode vNode,
			List<SubstrateNode> filtratedsNodes, int dist) {
		List<SubstrateNode> nodes = new LinkedList<SubstrateNode>();
		for (SubstrateNode n : filtratedsNodes) {
			if (NodeLinkAssignation.isMappable(vNode, n)
					&& nodeDistance(vNode, n, dist)) {
				nodes.add(n);
			}
		}
		return nodes;
	}

	/**
	 * 
	 * @param vNode
	 *            virtual node
	 * @param dem
	 *            demand of the virtual node
	 * @param filtratedsNodes
	 *            set of all nodes to extract the feasible substrate node
	 *            candidates
	 * @param dist
	 *            parameter to assure that the candidate nodes will be separated
	 *            of virtual node by a distance of, at maximum, equal to dist.
	 * @return The set of substrate nodes accomplishing a demand and a distance
	 *         dist from the vnode
	 */
	public static List<SubstrateNode> findFulfillingNodes(VirtualNode vNode,
			List<SubstrateNode> filtratedsNodes) {
		List<SubstrateNode> nodes = new LinkedList<SubstrateNode>();
		for (SubstrateNode n : filtratedsNodes) {
			if (NodeLinkAssignation.isMappable(vNode, n)) {
				nodes.add(n);
			}
		}
		return nodes;
	}

	/**
	 * 
	 * @param vNode
	 *            virtual node
	 * @param dem
	 *            demand of the virtual node
	 * @param filtratedsNodes
	 *            set of all nodes to extract the feasible substrate node
	 *            candidates
	 * 
	 * @return The set of substrate nodes accomplishing a demand
	 */
	public static List<SubstrateNode> findFulfillingNodes(VirtualNode vNode,
			List<SubstrateNode> filtratedsNodes,
			Map<VirtualNode, SubstrateNode> nodeMapping) {
		List<SubstrateNode> nodes = new LinkedList<SubstrateNode>();
		for (SubstrateNode n : filtratedsNodes) {
			if (NodeLinkAssignation.isMappable(vNode, n)
					&& !nodeMapping.containsValue(n)) {
				nodes.add(n);
			}
		}
		return nodes;
	}


	/** unregister mapping of a virtual node that could not be mapped */
	public static void clearVnodeMappings(VirtualNetwork vNet, VirtualNode vNode) {

		for (AbstractDemand dem : vNode.get()) {
			List<Mapping> mappingsCopy = new ArrayList<Mapping>();
			if (!dem.getMappings().isEmpty()) {
				mappingsCopy.addAll(dem.getMappings());
				for (Mapping m : mappingsCopy)
					m.getDemand().free(m.getResource());
			}
		}

		for (VirtualLink link : vNet.getOutEdges(vNode)) {
			for (AbstractDemand dem : link.get()) {
				List<Mapping> mappingsCopy = new ArrayList<Mapping>();
				if (!dem.getMappings().isEmpty()) {
					mappingsCopy.addAll(dem.getMappings());
					for (Mapping m : mappingsCopy)
						m.getDemand().free(m.getResource());
				}
			}
			for (AbstractDemand dem : link.getHiddenHopDemands()) {
				List<Mapping> mappingsCopy = new ArrayList<Mapping>();
				if (!dem.getMappings().isEmpty()) {
					mappingsCopy.addAll(dem.getMappings());
					for (Mapping m : mappingsCopy)
						m.getDemand().free(m.getResource());
				}
			}
		}
		for (VirtualLink link : vNet.getInEdges(vNode)) {
			for (AbstractDemand dem : link.get()) {
				List<Mapping> mappingsCopy = new ArrayList<Mapping>();
				if (!dem.getMappings().isEmpty()) {
					mappingsCopy.addAll(dem.getMappings());
					for (Mapping m : mappingsCopy)
						m.getDemand().free(m.getResource());
				}
			}
			for (AbstractDemand dem : link.getHiddenHopDemands()) {
				List<Mapping> mappingsCopy = new ArrayList<Mapping>();
				if (!dem.getMappings().isEmpty()) {
					mappingsCopy.addAll(dem.getMappings());
					for (Mapping m : mappingsCopy)
						m.getDemand().free(m.getResource());
				}
			}
		}
	}

	public static boolean hasLinkMappings(VirtualLink vLink) {
		for (AbstractDemand dem : vLink.get())
			if (!dem.getMappings().isEmpty())
				return true;

		return false;
	}

	public static List<SubstrateNode> getUnmappedNodes(
			Collection<SubstrateNode> subNodes) {
		List<SubstrateNode> unmappedNodes = new LinkedList<SubstrateNode>();
		boolean hasMappings = false;
		for (SubstrateNode n : subNodes) {
			hasMappings = false;
			for (AbstractResource res : n) {
				if (!res.getMappings().isEmpty()) {
					hasMappings = true;
					break;
				}
			}
			if (!hasMappings)
				unmappedNodes.add(n);

		}
		return unmappedNodes;
	}

	public static List<SubstrateLink> getUnmappedLinks(
			Collection<SubstrateLink> subLinks) {
		List<SubstrateLink> unmappedLinks = new LinkedList<SubstrateLink>();
		boolean hasMappings = false;
		for (SubstrateLink l : subLinks) {
			hasMappings = false;
			for (AbstractResource res : l) {
				if (!res.getMappings().isEmpty()) {
					hasMappings = true;
					break;
				}
			}
			if (!hasMappings)
				unmappedLinks.add(l);

		}
		return unmappedLinks;
	}


	/**
	 * Method to see if the node that is going to be mapped in the substrate
	 * network have links in the virtual network with some virtual node that has
	 * been already mapped to it.
	 * 
	 * @param vNet
	 * @param vNode
	 * @param sNode
	 * @return
	 */
	public static boolean linksWithItself(VirtualNetwork vNet,
			VirtualNode vNode, SubstrateNode sNode,
			Map<VirtualNode, SubstrateNode> nodeMapping) {
		if (nodeMapping.containsValue(sNode)) {
			for (Iterator<VirtualNode> it = nodeMapping.keySet().iterator(); it
					.hasNext();) {
				VirtualNode tmpVnode = it.next();
				if (nodeMapping.get(tmpVnode).equals(sNode)) {
					for (VirtualLink tmpVlink : vNet.getOutEdges(tmpVnode)) {
						if (vNet.getDest(tmpVlink).equals(vNode))
							return true;
					}
					for (VirtualLink tmpVlink : vNet.getInEdges(tmpVnode)) {
						if (vNet.getSource(tmpVlink).equals(vNode))
							return true;
					}
				}
			}
		} else {
			return false;
		}
		return false;
	}

	public static List<SubstrateNode> findFulfillingNodesEA(VirtualNode vNode,
			List<SubstrateNode> filtratedsNodes, int dist,
			Map<VirtualNode, SubstrateNode> nodeMapping,
			VirtualNode mappedParent, List<SubstrateNode> orderedCandidates,
			VirtualNetwork vNet) {
		List<SubstrateNode> mappedNodes = new LinkedList<SubstrateNode>();
		List<SubstrateNode> unMappedNodes = new LinkedList<SubstrateNode>();
		List<SubstrateNode> nodes = new LinkedList<SubstrateNode>();
		boolean isDest = isDestination(vNode, mappedParent, vNet);

		if (isDest) {
			for (VirtualLink tmpVlink : vNet.getOutEdges(mappedParent)) {
				VirtualNode tmpNode = vNet.getDest(tmpVlink);
				for (AbstractDemand dem : tmpNode) {
					if (dem instanceof CpuDemand) {
						if (!dem.getMappings().isEmpty()
								&& !linksWithItself(vNet, vNode, nodeMapping
										.get(tmpNode), nodeMapping)) {
							mappedNodes.add(nodeMapping.get(tmpNode));
						}
					}
				}
			}
		} else {
			for (VirtualLink tmpVlink : vNet.getInEdges(mappedParent)) {
				VirtualNode tmpNode = vNet.getSource(tmpVlink);
				for (AbstractDemand dem : tmpNode) {
					if (dem instanceof CpuDemand) {
						if (!dem.getMappings().isEmpty()
								&& !linksWithItself(vNet, vNode, nodeMapping
										.get(tmpNode), nodeMapping)) {
							mappedNodes.add(nodeMapping.get(tmpNode));
						}
					}
				}
			}
		}
		for (SubstrateNode n : orderedCandidates) {
			if (NodeLinkAssignation.isMappable(vNode, n)
					&& nodeDistance(vNode, n, dist) && mappedNodes.contains(n)) {
				nodes.add(n);
			}
		}

		for (SubstrateNode n : orderedCandidates) {
			if (NodeLinkAssignation.isMappable(vNode, n)
					&& nodeDistance(vNode, n, dist)
					&& filtratedsNodes.contains(n) && !nodes.contains(n)
					&& !linksWithItself(vNet, vNode, n, nodeMapping)) {
				for (AbstractResource res : n) {
					if (res instanceof CpuResource) {
						if (!res.getMappings().isEmpty()) {
							// nodes.add(n);
						} else {
							unMappedNodes.add(n);
						}
						break;
					}
				}
			}
		}
		nodes.addAll(unMappedNodes);
		return nodes;
	}

	public static List<SubstrateNode> findFulfillingNodesEA(VirtualNode vNode,
			List<SubstrateNode> filtratedsNodes,
			Map<VirtualNode, SubstrateNode> nodeMapping,
			VirtualNode mappedParent, List<SubstrateNode> orderedCandidates,
			VirtualNetwork vNet) {
		List<SubstrateNode> mappedNodes = new LinkedList<SubstrateNode>();
		List<SubstrateNode> unMappedNodes = new LinkedList<SubstrateNode>();
		List<SubstrateNode> nodes = new LinkedList<SubstrateNode>();
		boolean isDest = isDestination(vNode, mappedParent, vNet);

		if (isDest) {
			for (VirtualLink tmpVlink : vNet.getOutEdges(mappedParent)) {
				VirtualNode tmpNode = vNet.getDest(tmpVlink);
				for (AbstractDemand dem : tmpNode) {
					if (dem instanceof CpuDemand) {
						if (!dem.getMappings().isEmpty()
								&& NodeLinkAssignation.isMappable(vNode,
										nodeMapping.get(tmpNode))
								&& !linksWithItself(vNet, vNode, nodeMapping
										.get(tmpNode), nodeMapping)) {
							mappedNodes.add(nodeMapping.get(tmpNode));
						}
					}
				}
			}
		} else {
			for (VirtualLink tmpVlink : vNet.getInEdges(mappedParent)) {
				VirtualNode tmpNode = vNet.getSource(tmpVlink);
				for (AbstractDemand dem : tmpNode) {
					if (dem instanceof CpuDemand) {
						if (!dem.getMappings().isEmpty()
								&& NodeLinkAssignation.isMappable(vNode,
										nodeMapping.get(tmpNode))
								&& !linksWithItself(vNet, vNode, nodeMapping
										.get(tmpNode), nodeMapping)) {
							mappedNodes.add(nodeMapping.get(tmpNode));
						}
					}
				}
			}
		}
		for (SubstrateNode n : orderedCandidates) {
			if (NodeLinkAssignation.isMappable(vNode, n)
					&& mappedNodes.contains(n) && filtratedsNodes.contains(n)) {
				nodes.add(n);
			}
		}

		for (SubstrateNode n : orderedCandidates) {
			if (NodeLinkAssignation.isMappable(vNode, n)
					&& filtratedsNodes.contains(n) && !nodes.contains(n)
					&& !linksWithItself(vNet, vNode, n, nodeMapping)) {
				for (AbstractResource res : n) {
					if (res instanceof CpuResource) {
						if (!res.getMappings().isEmpty()) {
							nodes.add(n);
						} else {
							unMappedNodes.add(n);
						}
						break;
					}
				}
			}
		}
		nodes.addAll(unMappedNodes);
		return nodes;
	}

	private static boolean isDestination(VirtualNode child, VirtualNode parent,
			VirtualNetwork vNet) {

		for (VirtualLink tmpVlink : vNet.getOutEdges(parent)) {
			if (vNet.getDest(tmpVlink).equals(child))
				return true;
		}

		for (VirtualLink tmpVlink : vNet.getInEdges(parent)) {
			if (vNet.getSource(tmpVlink).equals(child))
				return false;
		}
		return false;
	}

	public static double getRemaRes(SubstrateNetwork sNet) {
		double nodeRemaRes = 0;
		double linkRemaRes = 0;
		for (SubstrateLink tmpSLink : sNet.getEdges())
			for (AbstractResource res : tmpSLink)
				if (res instanceof BandwidthResource)
					linkRemaRes += ((BandwidthResource) res)
							.getAvailableBandwidth();

		for (SubstrateNode tmpSNode : sNet.getVertices())
			for (AbstractResource res : tmpSNode)
				if (res instanceof CpuResource)
					nodeRemaRes += ((CpuResource) res).getAvailableCycles();

		return (nodeRemaRes + linkRemaRes);
	}

}
