package vnreal.algorithms.DPVNE.singlenetworkmapping.factory;

import vnreal.algorithms.DPVNE.singlenetworkmapping.GARSP;
import vnreal.algorithms.singlenetworkmapping.SingleNetworkMappingAlgorithm;

public class GARSPFactory implements EmbeddingAlgorithmFactory {
	
	@Override
	public SingleNetworkMappingAlgorithm createInstance() {
		return new GARSP();
	}

}
