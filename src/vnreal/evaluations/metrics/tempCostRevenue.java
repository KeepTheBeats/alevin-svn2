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

import java.util.Iterator;
import java.util.Map;

import vnreal.constraints.demands.AbstractDemand;
import vnreal.constraints.demands.BandwidthDemand;
import vnreal.constraints.demands.CpuDemand;
import vnreal.evaluations.utils.VnrUtils;
import vnreal.mapping.Mapping;
import vnreal.network.NetworkStack;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

/**
 * Class to obtain the cost-revenue relationship. Cost and revenue are defined 
 * in their respective classes.
 * 
 * The cost-revenue relationship measures the proportion of cost spent in the substrate 
 * network taking into account the revenue that has been mapped. The lower cost-revenue, the 
 * better mapping has been performed. 
 * 
 * @author Juan Felipe Botero
 * @since 2011-03-08
 *
 */

public class tempCostRevenue implements EvaluationMetric<NetworkStack> {
	boolean isPathSplitting;
	double costMapped = 0;

	public tempCostRevenue(boolean isPsAlgorithm) {
		this.isPathSplitting = isPsAlgorithm;
	}

	@Override
	public double calculate(NetworkStack stack) {
		Map<Integer, Boolean> isMappedVnr = VnrUtils
				.calculateMappedVnr(stack);
		double mappedRevenue = 0;
		int mapped = 0;
		double costRev = 0;
		for (VirtualNetwork net : stack.getVirtuals()) {
			if (isMappedVnr.get(net)){
				mapped++;
				costMapped = 0;
				mappedRevenue += calculateVnetRevenue(net);
				costRev += (costMapped/mappedRevenue);					
			}
		}
		
		return (costRev/mapped);
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "CostRevenue";
	}
	private double calculateVnetRevenue(VirtualNetwork vNet) {
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
					if (!isPathSplitting) {
						total_demBW += ((BandwidthDemand) dem)
								.getDemandedBandwidth();
						break; // continue with next link
					} else {
						if (dem.getMappings().isEmpty()) {
							total_demBW += ((BandwidthDemand) dem)
									.getDemandedBandwidth();
							break;
						}
					}
				}

			}
			for (AbstractDemand dem : tmpl) {
				if (dem instanceof BandwidthDemand) {
					for (Mapping map : dem.getMappings()){
						costMapped += ((BandwidthDemand) map.getDemand()).getDemandedBandwidth();
					}
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
			for (AbstractDemand dem : tmps) {
				if (dem instanceof CpuDemand) {
					for (Mapping map : dem.getMappings()){
						costMapped += ((CpuDemand) map.getDemand()).getDemandedCycles();
					}
				}
			}
		}
		return (total_demBW + total_demCPU);
	}

}
