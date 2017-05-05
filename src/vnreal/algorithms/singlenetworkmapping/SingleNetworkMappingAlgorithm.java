package vnreal.algorithms.singlenetworkmapping;

import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.virtual.VirtualNetwork;

public abstract class SingleNetworkMappingAlgorithm {

	private final boolean isPathSplittingAlgorithm;
	
	public SingleNetworkMappingAlgorithm(boolean isPathSplittingAlgorithm) {
		this.isPathSplittingAlgorithm = isPathSplittingAlgorithm;
	}
	
	public abstract boolean mapNetwork(SubstrateNetwork network, VirtualNetwork vNetwork);
	
	public boolean isPathSplittingAlgorithm() {
		return isPathSplittingAlgorithm;
	}

}
