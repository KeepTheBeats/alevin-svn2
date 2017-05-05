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
package vnreal.network.substrate;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.commons.collections15.Factory;

import vnreal.constraints.demands.AbstractDemand;
import vnreal.constraints.resources.AbstractResource;
import vnreal.mapping.Mapping;
import vnreal.network.Network;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

/**
 * The physical substrate underlying all virtual networks.
 * 
 * @author Michael Duelli
 */
@SuppressWarnings("serial")
public class SubstrateNetwork extends
		Network<AbstractResource, SubstrateNode, SubstrateLink> {

	public SubstrateNetwork() {
		this(false);
	}
	
	public SubstrateNetwork(boolean autoUnregisterConstraints) {
		super(autoUnregisterConstraints);
	}
	
	public SubstrateNetwork(boolean autoUnregisterConstraints, boolean directed) {
		super(autoUnregisterConstraints, directed);
	}

	@Override
	public String getLabel() {
		return "Substrate Network";
	}

	@Override
	public int getLayer() {
		return layer;
	}

	@Override
	public Factory<SubstrateLink> getEdgeFactory() {
		return new Factory<SubstrateLink>() {
			@Override
			public SubstrateLink create() {
				return new SubstrateLink();
			}
		};
	}

	@Override
	public String toString() {
		String result = "NODES:\n";
		for (SubstrateNode n : getVertices()) {
			result += "[id:" + n.getId() + " name:" + n.getName() + "\n";
			
			for (AbstractResource r : n.get()) {
				result += "  " + r.getClass().getSimpleName() + "." + r.toString() + "\n";
			}

			LinkedList<AbstractDemand> es = new LinkedList<AbstractDemand>();
			for (AbstractResource r : n) {
				for (Mapping m : r.getMappings()) {
					AbstractDemand e = m.getDemand();
					if (es.contains(e)) {
						continue;
					}
					es.add(e);
					result += "\n    hosting " + (e == null ? "null" : (e.getClass().getSimpleName() + "." + e + " (" + (e.getOwner() == null ? "null" : e.getOwner().getId() + "_" + e.getOwner().getName()) + ")"));
				}
			}
			
			result += "]\n";
		}

		result += "\nEDGES:\n";
		for (SubstrateLink l : getEdges()) {
			result += "[id:" + l.getId() + " name:" + l.getName() + "\n";
			result += l.getId() + "  (" + getSource(l).getName() + "-->"
					+ getDest(l).getName() + ") \n";
			for (AbstractResource r : l.get()) {
				result += "  " + r.getClass().getSimpleName() + "." + r.toString() + "\n";
			}
			
			LinkedList<AbstractDemand> es = new LinkedList<AbstractDemand>();
			for (AbstractResource r : l) {
				for (Mapping m : r.getMappings()) {
					AbstractDemand e = m.getDemand();
					if (es.contains(e)) {
						continue;
					}
					es.add(e);
					result += "\n    hosting " + (e == null ? "null" : (e.getClass().getSimpleName() + "." + e + " (" + (e.getOwner() == null ? "null" : e.getOwner().getId() + "_" + e.getOwner().getName()) + ")"));
				}
			}
			result += "]\n";
		}

		return result;
	}

	@Override
	public SubstrateNetwork getInstance(boolean autoUnregister) {
		return new SubstrateNetwork(autoUnregister);
	}

	public SubstrateNetwork getCopy(boolean autoUnregister, boolean deepCopy) {
		SubstrateNetwork result = new SubstrateNetwork(autoUnregister);
		getCopy(deepCopy, result);
		return result;
	}
	
	public void getCopy(boolean deepCopy, SubstrateNetwork result) {

		HashMap<String, SubstrateNode> map = new HashMap<String, SubstrateNode>();

		LinkedList<SubstrateLink> originalLinks = new LinkedList<SubstrateLink>(
				getEdges());
		SubstrateNode tmpSNode, tmpDNode;
		SubstrateLink tmpSLink;
		for (Iterator<SubstrateNode> tempSubsNode = getVertices().iterator(); tempSubsNode
				.hasNext();) {
			tmpSNode = tempSubsNode.next();
			if (deepCopy) {
				SubstrateNode clone = tmpSNode.getCopy(deepCopy);
				result.addVertex(clone);
				map.put(tmpSNode.getName(), clone);
			} else {
				result.addVertex(tmpSNode);
			}
		}
		for (Iterator<SubstrateLink> tempItSubLink = originalLinks.iterator(); tempItSubLink
				.hasNext();) {
			tmpSLink = tempItSubLink.next();
			tmpSNode = getSource(tmpSLink);
			tmpDNode = getDest(tmpSLink);

			if (deepCopy) {
				result.addEdge(tmpSLink.getCopy(deepCopy),
						map.get(tmpSNode.getName()),
						map.get(tmpDNode.getName()));
			} else {
				result.addEdge(tmpSLink, tmpSNode, tmpDNode);
			}
		}

	}
	
	public void clearMappings() {

		for (SubstrateNode n : getVertices()) {
			for (AbstractResource d : n.get()) {
				for (Mapping m : new LinkedList<Mapping>(d.getMappings())) {
					m.getDemand().free(d);
				}
			}
		}
		for (SubstrateLink l : getEdges()) {
			for (AbstractResource d : l.get()) {
				for (Mapping m : new LinkedList<Mapping>(d.getMappings())) {
					m.getDemand().free(d);
				}
			}
		}

	}
	
	public VirtualNetwork convertTopology() {
		VirtualNetwork copyVNetwork = new VirtualNetwork(this.getLayer());
		HashMap<SubstrateNode, VirtualNode> map = new HashMap<SubstrateNode, VirtualNode>();
		
		LinkedList<SubstrateLink> originalLinks = new LinkedList<SubstrateLink>(getEdges());
		SubstrateNode tmpSNode, tmpDNode;
		SubstrateLink tmpSLink;
		for (Iterator<SubstrateNode> tempSubsNode = getVertices().iterator(); tempSubsNode
				.hasNext();) {
			tmpSNode = tempSubsNode.next();
			VirtualNode c = new VirtualNode(this.getLayer());
			map.put(tmpSNode, c);
			copyVNetwork.addVertex(c);
		}
		for (Iterator<SubstrateLink> tempItSubLink = originalLinks.iterator(); tempItSubLink
				.hasNext();) {
			tmpSLink = tempItSubLink.next();
			VirtualLink c = new VirtualLink(this.getLayer());
			tmpSNode = getSource(tmpSLink);
			tmpDNode = getDest(tmpSLink);
			copyVNetwork.addEdge(c, map.get(tmpSNode), map.get(tmpDNode));
		}

		return copyVNetwork;
	}

	public void generateDuplicateEdges() {
		
		LinkedList<SubstrateNode> nodes = new LinkedList<SubstrateNode>(getVertices());
		for (SubstrateNode n : nodes) {
			for (SubstrateLink outedge : new LinkedList<SubstrateLink>(getOutEdges(n))) {
				if (!outedge.getName().endsWith("_dup")) {
					boolean done = false;
					SubstrateNode second = getOpposite(n, outedge);
					for (SubstrateLink secondoutedge : new LinkedList<SubstrateLink>(getOutEdges(second))) {
						
						SubstrateNode secondopposite = getOpposite(second, secondoutedge);
						if (secondopposite == n) {
							if (!done) {
								// there already is a backlink
								secondoutedge.removeAll();
								for (AbstractResource r : outedge) {
									secondoutedge.add(r.getCopy(secondoutedge, false));
								}
								secondoutedge.setName(outedge.getName() + "_dup");
	
								done = true;
							} else {
								removeEdge(secondoutedge);
							}
						}
					}
					
					if (!done) {
						SubstrateLink newEdge = outedge.getCopy(true);
						newEdge.setName(outedge.getName() + "_dup");
						addEdge(newEdge, second, n);
					}
				}
				
			}


		}
		
	}
	
	public LinkedList<String> removeDuplicateEdges() {
		LinkedList<String> result = new LinkedList<String>();
		for (SubstrateNode sn : new LinkedList<SubstrateNode>(getVertices())) {
			for (SubstrateLink l1 : new LinkedList<SubstrateLink>(getOutEdges(sn))) {
				SubstrateNode opp = getOpposite(sn, l1);
				for (SubstrateLink l2 : getOutEdges(opp)) {
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
