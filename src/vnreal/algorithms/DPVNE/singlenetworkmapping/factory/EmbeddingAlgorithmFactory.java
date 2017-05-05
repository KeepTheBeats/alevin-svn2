package vnreal.algorithms.DPVNE.singlenetworkmapping.factory;

import vnreal.algorithms.singlenetworkmapping.SingleNetworkMappingAlgorithm;

public interface EmbeddingAlgorithmFactory {
	
	public abstract SingleNetworkMappingAlgorithm createInstance();
	
}
