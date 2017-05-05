package vnreal.algorithms.DPVNE;

import vnreal.algorithms.DPVNE.singlenetworkmapping.factory.EmbeddingAlgorithmFactory;
import vnreal.algorithms.DPVNE.singlenetworkmapping.factory.GARPSFactory;

public final class DPVNEGARPSTest extends DistributedMetisTest {

	public DPVNEGARPSTest(DPVNEAlgorithmParameters params, String name) {
		super(params, name, true);
	}

	@Override
	public EmbeddingAlgorithmFactory getFactory() {
		return new GARPSFactory();
	}
	
}
