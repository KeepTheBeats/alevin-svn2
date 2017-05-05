package vnreal.algorithms.DPVNE.singlenetworkmapping.factory;

import vnreal.algorithms.DPVNE.singlenetworkmapping.CoordinatedAlgorithmkSP;
import vnreal.algorithms.singlenetworkmapping.SingleNetworkMappingAlgorithm;

public class CoordinatedAlgorithmkSPFactory implements EmbeddingAlgorithmFactory {
	
	@Override
	public SingleNetworkMappingAlgorithm createInstance() {
		return new CoordinatedAlgorithmkSP();
	}

}
