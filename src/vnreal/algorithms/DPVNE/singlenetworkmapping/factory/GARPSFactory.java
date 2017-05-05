package vnreal.algorithms.DPVNE.singlenetworkmapping.factory;

import vnreal.algorithms.DPVNE.singlenetworkmapping.GARPS;
import vnreal.algorithms.singlenetworkmapping.SingleNetworkMappingAlgorithm;

public class GARPSFactory implements EmbeddingAlgorithmFactory {
	
	@Override
	public SingleNetworkMappingAlgorithm createInstance() {
		return new GARPS();
	}

}
