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
package vnreal.algorithms.DPVNE;

import java.util.LinkedList;
import java.util.List;

import tests.scenarios.AbstractScenarioTest;
import vnreal.algorithms.AlgorithmParameter;
import vnreal.algorithms.CoordinatedMapping;
import vnreal.algorithms.GenericMappingAlgorithm;
import vnreal.hiddenhopmapping.BandwidthCpuHiddenHopMapping;
import vnreal.hiddenhopmapping.IHiddenHopMapping;
import vnreal.network.NetworkStack;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.virtual.VirtualNetwork;

public final class DViNEPSTest extends AbstractScenarioTest {

	public DViNEPSTest(String name) {
		super(name, true);
	}
	
	@Override
	public LinkedList<AlgorithmParameter> getAlgorithmParams() {
		LinkedList<AlgorithmParameter> result = new LinkedList<AlgorithmParameter>();
		result.add(null);
		return result;
	}
	
	@Override
	public NetworkStack map(AlgorithmParameter data, SubstrateNetwork substrate, List<VirtualNetwork> virtuals) {
		NetworkStack stack = new NetworkStack(substrate, virtuals);
		
		LinkedList<IHiddenHopMapping> hhMappings = new LinkedList<IHiddenHopMapping>();
		double hiddenHopsFactor = 0;
		hhMappings.add(new BandwidthCpuHiddenHopMapping(hiddenHopsFactor));

		AlgorithmParameter param = new AlgorithmParameter();
		param.put("weightCpu", "0");
		param.put("weightBw", "0");
		param.put("randomize", "false");
		param.put("overload", "false");
		param.put("distance", "35");
		param.put("PathSplitting", "true");
		GenericMappingAlgorithm algo = new CoordinatedMapping(param);
		algo.setStack(stack);		if (algo instanceof GenericMappingAlgorithm)
			((GenericMappingAlgorithm) algo).setHhMappings(hhMappings);
		
		algo.performEvaluation();
		
		return stack;
	}
	
}
