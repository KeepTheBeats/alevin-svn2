package vnreal.algorithms.DPVNE;

import vnreal.algorithms.DPVNE.singlenetworkmapping.factory.AdvSubgraphFactory;
import vnreal.algorithms.DPVNE.singlenetworkmapping.factory.EmbeddingAlgorithmFactory;

public final class DPVNEAdvancedSubgraphMetisTest extends DistributedMetisTest {

	public DPVNEAdvancedSubgraphMetisTest(DPVNEAlgorithmParameters params, String name) {
		super(params, name, false);
	}

	@Override
	public EmbeddingAlgorithmFactory getFactory() {
		return new AdvSubgraphFactory();
	}
	
}
