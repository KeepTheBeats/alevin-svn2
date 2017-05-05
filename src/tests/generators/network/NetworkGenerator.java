package tests.generators.network;

import vnreal.constraints.AbstractConstraint;
import vnreal.network.Link;
import vnreal.network.Network;
import vnreal.network.Node;

public interface NetworkGenerator<T extends AbstractConstraint, V extends Node<T>, E extends Link<T>, N extends Network<T, V, E>> extends NetworkGeneratorInterface<N> {

//	/**
//	 * A method to generate a scenario with the specified parameters
//	 */
//	public abstract NetworkStack generate(NetworkGeneratorParameter<NetworkGeneratorParameters> data, int numScenario) {
//		
//		NetworkStack result = null;
//
//		SubstrateNetwork sNetwork = generateSNetwork(Random random, data);
//		result = new NetworkStack(sNetwork, generateVNetworks(random, data));
//			
//		return result;
//	}
	
}
