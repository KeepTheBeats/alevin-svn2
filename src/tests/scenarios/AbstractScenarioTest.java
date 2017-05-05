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
package tests.scenarios;

import java.util.LinkedList;

import vnreal.algorithms.utils.SubgraphBasicVN.Utils;
import vnreal.evaluations.metrics.AcceptedVnrRatio;
import vnreal.evaluations.metrics.AvActiveLinkStress;
import vnreal.evaluations.metrics.AvActiveNodeStress;
import vnreal.evaluations.metrics.AvAllPathLength;
import vnreal.evaluations.metrics.AvLinkStress;
import vnreal.evaluations.metrics.AvNodeStress;
import vnreal.evaluations.metrics.AvPathLength;
import vnreal.evaluations.metrics.Cost;
import vnreal.evaluations.metrics.CostRevenue;
import vnreal.evaluations.metrics.EvaluationMetric;
import vnreal.evaluations.metrics.LinkCostPerVnr;
import vnreal.evaluations.metrics.LinkUtilization;
import vnreal.evaluations.metrics.MappedRevenue;
import vnreal.evaluations.metrics.MaxLinkStress;
import vnreal.evaluations.metrics.MaxNodeStress;
import vnreal.evaluations.metrics.MaxPathLength;
import vnreal.evaluations.metrics.NodeUtilization;
import vnreal.evaluations.metrics.NumberOfEmbeddedVNets;
import vnreal.evaluations.metrics.RejectedNetworksNumber;
import vnreal.evaluations.metrics.RemainingLinkResource;
import vnreal.evaluations.metrics.RevenueCost;
import vnreal.evaluations.metrics.RunningNodes;
import vnreal.evaluations.metrics.Runtime;
import vnreal.evaluations.metrics.SolelyForwardingHops;
import vnreal.evaluations.metrics.runningTimePerMappedVN;
import vnreal.network.NetworkStack;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.virtual.VirtualNetwork;

public abstract class AbstractScenarioTest implements ScenarioTestInterface<SubstrateNetwork, VirtualNetwork, NetworkStack> {
	
	private final String name;
	
	/** # of runs with different random seeds if algorithm is non-deterministic */
	public int numRunsPerScenario = 1;
	
	public int timeoutSNSize = -1;
	
	public final boolean isPathSplittingAlgorithm;
	
	protected AbstractScenarioTest(String name, boolean isPathSplittingAlgorithm) {
		this.name = name;
		this.isPathSplittingAlgorithm = isPathSplittingAlgorithm;
	}
	
	public boolean isPathSplittingAlgorithm() {
		return isPathSplittingAlgorithm;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public void unmap(SubstrateNetwork sNet, VirtualNetwork vNet) {
		Utils.clearVnrMappings(sNet, vNet.getLayer());
	}
	
	@Override
	public LinkedList<EvaluationMetric<NetworkStack>> getMetrics(double elapsedTimeMS) {
		LinkedList<EvaluationMetric<NetworkStack>> result = new LinkedList<EvaluationMetric<NetworkStack>>();
		
		result.add(new AcceptedVnrRatio());
		result.add(new RejectedNetworksNumber());

		result.add(new AvAllPathLength());
		result.add(new AvPathLength());

		result.add(new AvActiveLinkStress());
		result.add(new AvActiveNodeStress());
		result.add(new AvLinkStress());
		result.add(new AvNodeStress());
		result.add(new MaxLinkStress());
		result.add(new MaxNodeStress());
		result.add(new MaxPathLength());

		result.add(new Cost());
		result.add(new CostRevenue(isPathSplittingAlgorithm()));
		result.add(new RevenueCost(isPathSplittingAlgorithm()));
		result.add(new MappedRevenue(isPathSplittingAlgorithm()));

//		result.add(new EnergyConsumption());
		result.add(new RunningNodes());

		result.add(new LinkCostPerVnr());
		result.add(new LinkUtilization());
		result.add(new NodeUtilization());
		result.add(new RemainingLinkResource());
		result.add(new SolelyForwardingHops());
		
		result.add(new Runtime(elapsedTimeMS));
		result.add(new runningTimePerMappedVN(elapsedTimeMS));
		result.add(new NumberOfEmbeddedVNets());
//		result.add(memoryUsage1);
//		result.add(memoryUsage2);
		
		return result;
	}

}
