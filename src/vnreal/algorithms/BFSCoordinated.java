package vnreal.algorithms;

import vnreal.algorithms.linkmapping.FoolLinkMapping;
import vnreal.algorithms.nodemapping.BFSNRCoordinatedMapping;
import vnreal.algorithms.nodemapping.BFSNRnodeMappingPathSplitting;
import vnreal.algorithms.utils.MiscelFunctions;
import vnreal.network.NetworkStack;

/**
 * This is the class of the virtual embedding algorithm that calculates the coordinated VNE
 * based on node ranking and BFS virtual network tree 
 * 
 * This algorithm was proposed in:
 * 
 * X. Cheng, S. Su, Z. Zhang, H. Wang, F. Yang, Y. Luo, J. Wang, Virtual network embedding 
 * through topology-aware node ranking, SIGCOMM Comput. Commun. Rev. 41 (2011) 38-47.
 * 
 * @author Juan Felipe Botero, Andreas Fischer
 * @since 2011-02-10
 */
public class BFSCoordinated extends GenericMappingAlgorithm {

	// Default values
	private static int DEFAULT_DIST = -1; // No distance calculation
	private static boolean DEFAULT_OVERLOAD = false; // No node overload
	private static boolean DEFAULT_PS = false; // No path splitting
	private static double DEFAULT_WCPU = 1;
	private static double DEFAULT_WBW = 1;
	private static int DEFAULT_MAXHOPS = 100;
	
	public BFSCoordinated(AlgorithmParameter param) {
		
		int distance = param.getInteger("distance", DEFAULT_DIST);
		boolean nodeOverload = param.getBoolean("overload", DEFAULT_OVERLOAD);
		int maxHops = param.getInteger("maxHops", DEFAULT_MAXHOPS);
		
		if (param.getBoolean("PathSplitting", DEFAULT_PS)) {
			double weightCpu = param.getDouble("weightCpu", DEFAULT_WCPU);
			double weightBw = param.getDouble("weightBw", DEFAULT_WBW);
			this.nodeMappingAlgorithm = new BFSNRnodeMappingPathSplitting(
					weightCpu, weightBw, nodeOverload, 0.0001, distance, maxHops);
		} else {
			this.nodeMappingAlgorithm = new BFSNRCoordinatedMapping(nodeOverload,
					0.0001, distance, maxHops);
		}
		this.linkMappingAlgorithm = new FoolLinkMapping();
	}
	
	
	@Override
	public void setStack(NetworkStack stack) {
		this.ns = MiscelFunctions.sortByRevenues(stack);
	}
}
