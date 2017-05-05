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
package vnreal.evaluations.metrics;

/**
 * Class to obtain the revenue that is mapped over the stack of virtual networks requests. 
 * 
 * The revenue of realizing the embedding of one virtual network request 
 * over a substrate network is defined by the next equation (Latex format)
 * (Notation in http://www3.informatik.uni-wuerzburg.de/research/projects/vnreal/wiki/doku.php?id=private:nomenclature):
 * 
 * Rev(G^k(V^k,A^k))= \sum_{i^k \in V^k} ND_{cpu}(i^k) + \sum_{(i^k,j^k) \in A^k} LD_{bw}(i^k,j^k)  
 * 
 * where ND_{cpu}(i^k) is the demand of CPU in the node i^k and LD_bw(i^k,j^k) is the link demand of virtual link (i^k,j^k) belonging to the VNR k.
 * 
 * 
 * The revenue, of a VNR, can be defined as the weighted sum of its demands. The total revenue is 
 * the sum of the revenue of all successfully mapped VNRs.
 * 
 * @author Juan Felipe Botero
 * @since 2011-03-08
 *
 */

import java.util.Collection;
import java.util.LinkedList;

import vnreal.constraints.demands.AbstractDemand;
import vnreal.constraints.demands.BandwidthDemand;
import vnreal.constraints.demands.CpuDemand;
import vnreal.constraints.resources.AbstractResource;
import vnreal.mapping.Mapping;
import vnreal.network.NetworkEntity;
import vnreal.network.NetworkStack;
import vnreal.network.substrate.SubstrateNetwork;

public class MappedRevenue implements EvaluationMetric<NetworkStack> {
	boolean isPathSplitting;

	public MappedRevenue() {
		this(false);
	}
	
	public MappedRevenue(boolean isPsAlgorithm) {
		this.isPathSplitting = isPsAlgorithm;
	}

	@Override
	public double calculate(NetworkStack stack) {
		SubstrateNetwork sNet = stack.getSubstrate();
		
		LinkedList<NetworkEntity<AbstractDemand>> vNodes = getMappedVirtualEntities(sNet.getVertices());
		LinkedList<NetworkEntity<AbstractDemand>> vLinks = getMappedVirtualEntities(sNet.getEdges());
		
		return calculateVnetRevenue(vNodes, vLinks);
	}
	
	@SuppressWarnings("unchecked")
	static LinkedList<NetworkEntity<AbstractDemand>> getMappedVirtualEntities(Collection<? extends NetworkEntity<AbstractResource>> entities) {
		LinkedList<NetworkEntity<AbstractDemand>> result = new LinkedList<NetworkEntity<AbstractDemand>>();
		
		for (NetworkEntity<AbstractResource> e : entities) {
			for (AbstractResource res : e.get()) {
				for (Mapping m : res.getMappings()) {
					if (m.getDemand().getOwner() != null) {
						NetworkEntity<AbstractDemand> owner = (NetworkEntity<AbstractDemand>) m.getDemand().getOwner();
						if (!result.contains(owner)) {
							result.add(owner);
						}
					}
				}
			}
		}
		
		return result;
	}

	private double calculateVnetRevenue(LinkedList<NetworkEntity<AbstractDemand>> vNodes, LinkedList<NetworkEntity<AbstractDemand>> vLinks) {
		double total_demBW = 0.0d;
		double total_demCPU = 0.0d;
		
		for (NetworkEntity<AbstractDemand> owner : vLinks) {
			for (AbstractDemand dem : owner.get()) {
				if (dem instanceof BandwidthDemand) {
					if (!isPathSplitting) {
						total_demBW += ((BandwidthDemand) dem)
								.getDemandedBandwidth();
					} else {
						if (dem.getMappings().isEmpty()) {
							total_demBW += ((BandwidthDemand) dem)
									.getDemandedBandwidth();
							break;
						}
					}
				}

			}
		}
		for (NetworkEntity<AbstractDemand> owner : vNodes) {
			for (AbstractDemand dem : owner.get()) {
				if (dem instanceof CpuDemand) {
					total_demCPU += ((CpuDemand) dem).getDemandedCycles();
				}
			}
		}
		return (total_demBW + total_demCPU);
	}

	@Override
	public String toString() {
		return "MappedRevenue";
	}

}
