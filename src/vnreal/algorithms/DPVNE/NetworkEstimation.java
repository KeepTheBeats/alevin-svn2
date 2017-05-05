package vnreal.algorithms.DPVNE;

import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.virtual.VirtualNetwork;

public interface NetworkEstimation {
	
	public double estimate(SubstrateNetwork sNetwork, VirtualNetwork vNetwork);
	
}
