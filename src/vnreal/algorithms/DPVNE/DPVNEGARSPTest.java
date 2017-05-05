package vnreal.algorithms.DPVNE;

import vnreal.algorithms.DPVNE.singlenetworkmapping.factory.EmbeddingAlgorithmFactory;
import vnreal.algorithms.DPVNE.singlenetworkmapping.factory.GARSPFactory;

public final class DPVNEGARSPTest extends DistributedMetisTest {

	public DPVNEGARSPTest(DPVNEAlgorithmParameters params, String name) {
		super(params, name, false);
	}

	@Override
	public EmbeddingAlgorithmFactory getFactory() {
		return new GARSPFactory();
	}
	
}
