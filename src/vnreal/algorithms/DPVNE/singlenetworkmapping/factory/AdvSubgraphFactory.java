package vnreal.algorithms.DPVNE.singlenetworkmapping.factory;

import vnreal.algorithms.isomorphism.AdvancedSubgraphIsomorphismAlgorithm;
import vnreal.algorithms.singlenetworkmapping.SingleNetworkMappingAlgorithm;

public class AdvSubgraphFactory implements EmbeddingAlgorithmFactory {
	
	@Override
	public SingleNetworkMappingAlgorithm createInstance() {
		return new AdvancedSubgraphIsomorphismAlgorithm();
	}

}
