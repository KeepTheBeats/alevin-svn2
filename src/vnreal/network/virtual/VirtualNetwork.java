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
package vnreal.network.virtual;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections15.Factory;

import vnreal.constraints.demands.AbstractDemand;
import vnreal.mapping.Mapping;
import vnreal.network.Network;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import edu.uci.ics.jung.graph.util.Pair;

/**
 * A virtual network built upon the physical substrate.
 * 
 * @author Michael Duelli
 */
@SuppressWarnings("serial")
public class VirtualNetwork extends
		Network<AbstractDemand, VirtualNode, VirtualLink> {
	/** The layer resp. virtual network id which start from 0. */
	private String name = null;

	public VirtualNetwork(int layer, boolean autoUnregisterConstraints, boolean directed) {
		super(autoUnregisterConstraints, directed);
		this.layer = layer;
	}
	
	public VirtualNetwork(int layer, boolean autoUnregisterConstraints) {
		super(autoUnregisterConstraints);
		this.layer = layer;
	}

	public VirtualNetwork(int layer) {
		this(layer, true);
	}

	@Override
	public boolean addVertex(VirtualNode vertex) {
		if (vertex.getLayer() == getLayer())
			return super.addVertex(vertex);
		else
			return false;
	}

	@Override
	public boolean addEdge(VirtualLink edge, VirtualNode v, VirtualNode w) {
		if (edge.getLayer() == getLayer() && v.getLayer() == getLayer()
				&& w.getLayer() == getLayer())
			return super.addEdge(edge, new Pair<VirtualNode>(v, w));
		else
			return false;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getLabel() {
		return "Virtual Network (" + layer + ")";
	}

	@Override
	public int getLayer() {
		return layer;
	}

	@Override
	public Factory<VirtualLink> getEdgeFactory() {
		return new Factory<VirtualLink>() {
			@Override
			public VirtualLink create() {
				return new VirtualLink(layer);
			}
		};
	}

	@Override
	public String toString() {
		String result = "NODES:\n";
		for (VirtualNode n : getVertices()) {
			result += "id:" + n.getId() + " name:" + n.getName() + ")\n";
			for (AbstractDemand d : n.get()) {
				result += "  " + d.toString() + "\n";
			}
		}

		result += "\nEDGES:\n";
		for (VirtualLink l : getEdges()) {
			result += "id:" + l.getId() + " name:" + l.getName() + "  (" + getSource(l).getId() + "-->"
					+ getDest(l).getId() + ")\n";
			for (AbstractDemand d : l.get()) {
				result += "  " + d.toString() + "\n";
			}
		}

		return result;
	}

	@Override
	public VirtualNetwork getInstance(boolean autoUnregister) {
		return new VirtualNetwork(getLayer(), autoUnregister);
	}

	public VirtualNetwork getCopy(boolean autoUnregister, boolean deepCopy) {
		VirtualNetwork result = new VirtualNetwork(layer, autoUnregister);
		getCopy(deepCopy, result);
		return result;
	}
	
	/*
	 * Create a copy of a VirtualNetwork. The layer information is taken from
	 * the provided network in case of a deep copy.
	 */
	public void getCopy(boolean deepCopy, VirtualNetwork result) {
		
		HashMap<VirtualNode, VirtualNode> map = new HashMap<VirtualNode, VirtualNode>();
		
		for (VirtualNode vnode : getVertices()) {
			if (deepCopy) {
				VirtualNode copy = vnode.getCopy(true, new VirtualNode(result.layer));
				result.addVertex(copy);
				map.put(vnode, copy);
			} else {
				result.addVertex(vnode);
			}
		}
		
		for (VirtualLink vlink : getEdges()) {
			VirtualNode source = getSource(vlink);
			VirtualNode dest = getDest(vlink);
			if (deepCopy) {
				result.addEdge(vlink.getCopy(true, new VirtualLink(result.layer)),
						map.get(source), map.get(dest));
			} else {
				result.addEdge(vlink, source, dest);
			}
		}

	}
	
	public SubstrateNetwork convertTopology(boolean autounregister) {
		SubstrateNetwork copyVNetwork = new SubstrateNetwork(autounregister);
		HashMap<VirtualNode, SubstrateNode> map = new HashMap<VirtualNode, SubstrateNode>();
		
		LinkedList<VirtualLink> originalLinks = new LinkedList<VirtualLink>(getEdges());
		VirtualNode tmpSNode, tmpDNode;
		VirtualLink tmpSLink;
		for (Iterator<VirtualNode> tempSubsNode = getVertices().iterator(); tempSubsNode
				.hasNext();) {
			tmpSNode = tempSubsNode.next();
			SubstrateNode c = new SubstrateNode();
			map.put(tmpSNode, c);
			copyVNetwork.addVertex(c);
		}
		for (Iterator<VirtualLink> tempItSubLink = originalLinks.iterator(); tempItSubLink
				.hasNext();) {
			tmpSLink = tempItSubLink.next();
			SubstrateLink c = new SubstrateLink();
			tmpSNode = getSource(tmpSLink);
			tmpDNode = getDest(tmpSLink);
			copyVNetwork.addEdge(c, map.get(tmpSNode), map.get(tmpDNode));
		}

		return copyVNetwork;
	}
	
	public void generateDuplicateEdges() {

		LinkedList<VirtualNode> nodes = new LinkedList<VirtualNode>(getVertices());
		for (VirtualNode n : nodes) {
			for (VirtualLink outedge : new LinkedList<VirtualLink>(getOutEdges(n))) {
				if (!outedge.getName().endsWith("_dup")) {
					boolean done = false;
					VirtualNode second = getOpposite(n, outedge);
					for (VirtualLink secondoutedge : new LinkedList<VirtualLink>(getOutEdges(second))) {
						
						VirtualNode secondopposite = getOpposite(second, secondoutedge);
						if (secondopposite == n) {
							if (!done) {
								// there already is a backlink
								secondoutedge.removeAll();
								for (AbstractDemand r : outedge) {
									secondoutedge.add(r.getCopy(secondoutedge));
								}
								secondoutedge.setName(outedge.getName() + "_dup");
	
								done = true;
							} else {
								removeEdge(secondoutedge);
							}
						}
					}
					
					if (!done) {
						VirtualLink newEdge = outedge.getCopy(true);
						newEdge.setName(outedge.getName() + "_dup");
						addEdge(newEdge, second, n);
					}
				}
				
			}


		}
	}
	
	
	public void clearVnrMappings() {
		for (VirtualNode node : getVertices()) {
			for (AbstractDemand dem : node.get()) {
				List<Mapping> mappingsCopy = new ArrayList<Mapping>();
				mappingsCopy.addAll(dem.getMappings());
				for (Mapping m : mappingsCopy)
					m.getDemand().free(m.getResource());
			}
		}
		for (VirtualLink link : getEdges()) {
			for (AbstractDemand dem : link.get()) {
				List<Mapping> mappingsCopy = new ArrayList<Mapping>();
				mappingsCopy.addAll(dem.getMappings());
				for (Mapping m : mappingsCopy)
					m.getDemand().free(m.getResource());
			}
			for (AbstractDemand dem : link.getHiddenHopDemands()) {
				List<Mapping> mappingsCopy = new ArrayList<Mapping>();
				mappingsCopy.addAll(dem.getMappings());
				for (Mapping m : mappingsCopy)
					m.getDemand().free(m.getResource());
			}
		}
	}
	
	
	public LinkedList<String> removeDuplicateEdges() {
		LinkedList<String> result = new LinkedList<String>();
		for (VirtualNode sn : new LinkedList<VirtualNode>(getVertices())) {
			for (VirtualLink l1 : new LinkedList<VirtualLink>(getOutEdges(sn))) {
				VirtualNode opp = getOpposite(sn, l1);
				for (VirtualLink l2 : getOutEdges(opp)) {
					if (getOpposite(opp, l2) == sn) {
						removeEdge(l1);
						result.add(l1.getName());
						break;
					}
				}
			}
		}
		return result;
	}

}
