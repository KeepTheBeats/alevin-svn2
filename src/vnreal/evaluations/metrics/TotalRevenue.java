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

import java.util.LinkedList;

import vnreal.constraints.demands.AbstractDemand;
import vnreal.constraints.demands.BandwidthDemand;
import vnreal.constraints.demands.CpuDemand;
import vnreal.network.NetworkEntity;
import vnreal.network.NetworkStack;
import vnreal.network.substrate.SubstrateNetwork;

/**
 * Class to obtain the total revenue that could be obtained of the stack of
 * virtual networks requests.
 * 
 * This revenue sums the revenue of the VNRs that were successfully mapped and
 * the revenue of those that were not mapped.
 * 
 * @author Juan Felipe Botero
 * @since 2011-03-08
 * 
 */

public class TotalRevenue implements EvaluationMetric<NetworkStack> {
	private boolean isPathSplitting;

	public TotalRevenue(boolean isPsAlgorithm) {
		this.isPathSplitting = isPsAlgorithm;
	}

	@Override
	public double calculate(NetworkStack stack) {
		
		SubstrateNetwork sNet = stack.getSubstrate();
		
		LinkedList<NetworkEntity<AbstractDemand>> vNodes = MappedRevenue.getMappedVirtualEntities(sNet.getVertices());
		LinkedList<NetworkEntity<AbstractDemand>> vLinks = MappedRevenue.getMappedVirtualEntities(sNet.getEdges());
		
		return calculateVnetRevenue(vNodes, vLinks);
	}

	private double calculateVnetRevenue(LinkedList<NetworkEntity<AbstractDemand>> vNodes, LinkedList<NetworkEntity<AbstractDemand>> vLinks) {
		double total_demBW = 0;
		double total_demCPU = 0;
		
		for (NetworkEntity<AbstractDemand> owner : vLinks) {
			for (AbstractDemand dem : owner.get()) {
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
		}
		for (NetworkEntity<AbstractDemand> owner : vNodes) {
			for (AbstractDemand dem : owner.get()) {
				if (dem instanceof CpuDemand) {
					total_demCPU += ((CpuDemand) dem).getDemandedCycles();
					break; // continue with next node
				}
			}
		}
		return (total_demBW + total_demCPU);
	}

	@Override
	public String toString() {
		return "TotalRevenue";
	}
}
