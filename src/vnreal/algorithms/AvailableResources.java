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
package vnreal.algorithms;

/**
 * This is the class of the virtual embedding algorithm that mixes 
 * the Greedy Available resources phase in node mapping and k shortest 
 * paths in link mapping
 * 
 * 
 * This node mapping algorithm was proposed in:
 *  
 * -  Minlan Yu, Yung Yi, Jennifer Rexford, and Mung Chiang. Rethinking
 *    virtual network embedding: Substrate support for path splitting and 
 *    migration. ACM SIGCOMM CCR, 38(2):17–29, April 2008.
 *    
 * Note: It is not the main algorithm proposed in the paper, but rather
 * a baseline algorithm with a simple greedy approach.
 * 
 * @author Juan Felipe Botero
 * @author Lisset Diaz
 * @since 2010-10-25
 */

import vnreal.algorithms.linkmapping.PathSplittingVirtualLinkMapping;
import vnreal.algorithms.linkmapping.kShortestPathLinkMapping;
import vnreal.algorithms.nodemapping.AvailableResourcesNodeMapping;
import vnreal.algorithms.utils.MiscelFunctions;
import vnreal.network.NetworkStack;

public class AvailableResources extends GenericMappingAlgorithm {
	
	// Default values
	private static int DEFAULT_DIST = -1; // No distance calculation
	private static boolean DEFAULT_OVERLOAD = false; // No node overload
	private static boolean DEFAULT_PS = false; // No path splitting
	private static double DEFAULT_WCPU = 1;
	private static double DEFAULT_WBW = 1;
	private static int DEFAULT_KSP = 1;
	private static boolean DEFAULT_EPPSTEIN = false;
	
	/**
	 * Constructor of the algorithm
	 * 
	 * @param AlgorithmParameter Accepts settings for: <br />
	 * "distance" (int) - The maximum distance within which to search for substrate nodes
	 * (set to a negative value to disable) <br />
	 * "overload" (boolean) - Indicate, whether a substrate node may host more than one
	 * node of each VirtualNetwork <br />
	 * "PathSplitting" (boolean) - Indicate, whether link mapping should allow virtual
	 * links to be split over several paths <br />
	 * "weightCpu" (double) - The weight of CPU resources <br />
	 * "weightBw" (double) - The weight of BW resources <br />
	 * "kShortestPath" (int) - The number k of shortest paths to compute  
	 * 
	 */
	public AvailableResources(AlgorithmParameter param) {
		int distance = param.getInteger("distance", DEFAULT_DIST);
		boolean nodeOverload = param.getBoolean("overload", DEFAULT_OVERLOAD);
		this.nodeMappingAlgorithm = new AvailableResourcesNodeMapping(distance, nodeOverload);
		
		if (param.getBoolean("PathSplitting", DEFAULT_PS)) {
			double weightCpu = param.getDouble("weightCpu", DEFAULT_WCPU);
			double weightBw = param.getDouble("weightBw", DEFAULT_WBW);
			this.linkMappingAlgorithm = new PathSplittingVirtualLinkMapping(weightCpu, weightBw);
		} else {		
			int k = param.getInteger("kShortestPath", DEFAULT_KSP);
			boolean eppstein = param.getBoolean("eppstein", DEFAULT_EPPSTEIN);
			this.linkMappingAlgorithm = new kShortestPathLinkMapping(k, eppstein);
		}

	}
	
	
	@Override
	public void setStack(NetworkStack stack) {
		this.ns = MiscelFunctions.sortByRevenues(stack);
	}
}
