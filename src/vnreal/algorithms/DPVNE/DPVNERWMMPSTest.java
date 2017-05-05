package vnreal.algorithms.DPVNE;

import vnreal.algorithms.DPVNE.singlenetworkmapping.factory.EmbeddingAlgorithmFactory;
import vnreal.algorithms.DPVNE.singlenetworkmapping.factory.RWMMPSFactory;

public final class DPVNERWMMPSTest extends DistributedMetisTest {

	public DPVNERWMMPSTest(DPVNEAlgorithmParameters params, String name) {
		super(params, name, true);
	}

	@Override
	public EmbeddingAlgorithmFactory getFactory() {
		return new RWMMPSFactory();
	}
	
}
