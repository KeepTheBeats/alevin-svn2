package vnreal.algorithms.DPVNE;

import vnreal.algorithms.DPVNE.singlenetworkmapping.factory.CoordinatedAlgorithmkSPFactory;
import vnreal.algorithms.DPVNE.singlenetworkmapping.factory.EmbeddingAlgorithmFactory;

public final class DPVNEDViNESPTest extends DistributedMetisTest {

	public DPVNEDViNESPTest(DPVNEAlgorithmParameters params, String name) {
		super(params, name, false);
	}

	@Override
	public EmbeddingAlgorithmFactory getFactory() {
		return new CoordinatedAlgorithmkSPFactory();
	}
	
}
