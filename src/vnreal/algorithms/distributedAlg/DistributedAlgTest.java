package vnreal.algorithms.distributedAlg;

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

import java.util.LinkedList;
import java.util.List;

import mulavito.algorithms.IAlgorithm;
import tests.scenarios.AbstractScenarioTest;
import vnreal.algorithms.AlgorithmParameter;
import vnreal.algorithms.distributedAlg.metriken.AverageMsgsReceivedPerNode;
import vnreal.algorithms.distributedAlg.metriken.AverageSentMsgsPerNode;
import vnreal.algorithms.distributedAlg.metriken.BFMessageCounter;
import vnreal.algorithms.distributedAlg.metriken.BFRunCounter;
import vnreal.algorithms.distributedAlg.metriken.ClusterCounter;
import vnreal.algorithms.distributedAlg.metriken.MessagesPerLink;
import vnreal.algorithms.distributedAlg.metriken.NodesUsedSolelyForForwarding;
import vnreal.algorithms.distributedAlg.metriken.NormalMessageCounter;
import vnreal.algorithms.distributedAlg.metriken.NotifyMessageCounter;
import vnreal.evaluations.metrics.EvaluationMetric;
import vnreal.network.NetworkStack;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.virtual.VirtualNetwork;


public final class DistributedAlgTest extends AbstractScenarioTest {
	
	public DistributedAlgTest(String name) {
		super(name, false);
	}
	
	@Override
	public LinkedList<AlgorithmParameter> getAlgorithmParams() {
		LinkedList<AlgorithmParameter> result = new LinkedList<AlgorithmParameter>();
		result.add(null);
		return result;
	}
	
	@Override
	public LinkedList<EvaluationMetric<NetworkStack>> getMetrics(double elapsedTime) {
		LinkedList<EvaluationMetric<NetworkStack>> result = super.getMetrics(elapsedTime);

//		result.add(new AvgCommPathLength(counter));
//		result.add(new AvgNumMsgsPerCommPath(counter));

		result.add(new AverageMsgsReceivedPerNode());
		result.add(new AverageSentMsgsPerNode());
		result.add(new BFMessageCounter());
		result.add(new BFRunCounter());
		result.add(new ClusterCounter());
		result.add(new MessagesPerLink());
		result.add(new NodesUsedSolelyForForwarding());
		result.add(new NormalMessageCounter());
		result.add(new NotifyMessageCounter());
		
		return result;
	}

	@Override
	public NetworkStack map(AlgorithmParameter param, SubstrateNetwork substrate, List<VirtualNetwork> virtuals) {
		NetworkStack stack = new NetworkStack(substrate, virtuals);
		IAlgorithm algo =
			new DistributedAlgorithm(stack);
		
		algo.performEvaluation();
		return stack;
	}

}
