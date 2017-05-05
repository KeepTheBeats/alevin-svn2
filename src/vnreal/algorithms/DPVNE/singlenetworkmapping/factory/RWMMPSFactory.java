package vnreal.algorithms.DPVNE.singlenetworkmapping.factory;

import vnreal.algorithms.DPVNE.singlenetworkmapping.RWMMPS;
import vnreal.algorithms.singlenetworkmapping.SingleNetworkMappingAlgorithm;

public class RWMMPSFactory implements EmbeddingAlgorithmFactory {
	
	@Override
	public SingleNetworkMappingAlgorithm createInstance() {
		return new RWMMPS();
	}

}
