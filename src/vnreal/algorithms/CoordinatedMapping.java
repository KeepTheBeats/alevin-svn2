package vnreal.algorithms;

import vnreal.algorithms.linkmapping.PathSplittingVirtualLinkMapping;
import vnreal.algorithms.linkmapping.RoundingPathSplittingLinkMapping;
import vnreal.algorithms.linkmapping.kShortestPathLinkMapping;
import vnreal.algorithms.nodemapping.CoordinatedVirtualNodeMapping;
import vnreal.algorithms.utils.MiscelFunctions;
import vnreal.network.NetworkStack;

public class CoordinatedMapping extends GenericMappingAlgorithm {
	
	// Default values
	private static int DEFAULT_DIST = -1; // No distance calculation
	private static boolean DEFAULT_OVERLOAD = false; // No node overload
	private static boolean DEFAULT_RANDOMIZE = false; // No randomization
	private static boolean DEFAULT_ROUND = false; // No randomization
	private static boolean DEFAULT_PS = false; // No path splitting
	private static double DEFAULT_WCPU = 1;
	private static double DEFAULT_WBW = 1;
	private static int DEFAULT_KSP = 1;
	private static boolean DEFAULT_EPPSTEIN = true;
	
	public CoordinatedMapping(AlgorithmParameter param) {
		
		int distance = param.getInteger("distance", DEFAULT_DIST);
		double weightCpu = param.getDouble("weightCpu", DEFAULT_WCPU);
		double weightBw = param.getDouble("weightBw", DEFAULT_WBW);
		boolean randomize = param.getBoolean("randomize", DEFAULT_RANDOMIZE);
		boolean nodeOverload = param.getBoolean("overload", DEFAULT_OVERLOAD);
		
		this.nodeMappingAlgorithm = new CoordinatedVirtualNodeMapping(distance,
				weightCpu, weightBw, randomize, nodeOverload);
		
		if (param.getBoolean("PathSplitting", DEFAULT_PS)) {
			if (param.getBoolean("rounding", DEFAULT_ROUND)) {
				int k = param.getInteger("kShortestPath", DEFAULT_KSP);
				this.linkMappingAlgorithm = new RoundingPathSplittingLinkMapping(
						weightCpu, weightBw, k, randomize);
			} else {
				this.linkMappingAlgorithm = new PathSplittingVirtualLinkMapping(weightCpu, weightBw); 
			}
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
