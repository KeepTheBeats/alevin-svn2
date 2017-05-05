package vnreal.algorithms.DPVNE.singlenetworkmapping.factory;

import vnreal.algorithms.DPVNE.singlenetworkmapping.RWMMSP;
import vnreal.algorithms.singlenetworkmapping.SingleNetworkMappingAlgorithm;

public class RWMMSPFactory implements EmbeddingAlgorithmFactory {
	
	@Override
	public SingleNetworkMappingAlgorithm createInstance() {
		return new RWMMSP();
	}

}
