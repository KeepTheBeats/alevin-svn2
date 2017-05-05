package vnreal.algorithms.DPVNE.singlenetworkmapping.factory;

import vnreal.algorithms.DPVNE.singlenetworkmapping.CoordinatedMappingPS;
import vnreal.algorithms.singlenetworkmapping.SingleNetworkMappingAlgorithm;

public class CoordinatedMappingPSFactory implements EmbeddingAlgorithmFactory {
	
	@Override
	public SingleNetworkMappingAlgorithm createInstance() {
		return new CoordinatedMappingPS();
	}

}
