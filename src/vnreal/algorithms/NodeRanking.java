package vnreal.algorithms;

import vnreal.algorithms.linkmapping.PathSplittingVirtualLinkMapping;
import vnreal.algorithms.linkmapping.kShortestPathLinkMapping;
import vnreal.algorithms.nodemapping.NodeRankingBasedAlgorithm;
import vnreal.algorithms.utils.MiscelFunctions;
import vnreal.network.NetworkStack;

/**
 * This is the class of the virtual embedding algorithm that calculates the coordinated VNE
 * based on node ranking
 * 
 * This algorithm was proposed in:
 * 
 * X. Cheng, S. Su, Z. Zhang, H. Wang, F. Yang, Y. Luo, J. Wang, Virtual network embedding 
 * through topology-aware node ranking, SIGCOMM Comput. Commun. Rev. 41 (2011) 38-47.
 * 
 * @author Juan Felipe Botero, Andreas Fischer
 * @since 2011-02-10
 */
public class NodeRanking extends GenericMappingAlgorithm {
	
	// Default values
	private static int DEFAULT_DIST = -1; // No distance calculation
	private static boolean DEFAULT_OVERLOAD = false; // No node overload
	private static boolean DEFAULT_PS = false; // No path splitting
	private static double DEFAULT_WCPU = 1;
	private static double DEFAULT_WBW = 1;
	private static double DEFAULT_EPSILON = 0.0001;
	private static int DEFAULT_KSP = 1;
	private static boolean DEFAULT_EPPSTEIN = true;
	
	public NodeRanking(AlgorithmParameter param) {
		boolean nodeOverload = param.getBoolean("overload", DEFAULT_OVERLOAD);
		int distance = param.getInteger("distance", DEFAULT_DIST);
		double epsilon = param.getDouble("epsilon", DEFAULT_EPSILON);

		this.nodeMappingAlgorithm = new NodeRankingBasedAlgorithm(nodeOverload, epsilon, distance);
		
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
