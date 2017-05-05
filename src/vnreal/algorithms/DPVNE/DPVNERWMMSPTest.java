package vnreal.algorithms.DPVNE;

import vnreal.algorithms.DPVNE.singlenetworkmapping.factory.EmbeddingAlgorithmFactory;
import vnreal.algorithms.DPVNE.singlenetworkmapping.factory.RWMMSPFactory;

public final class DPVNERWMMSPTest extends DistributedMetisTest {

	public DPVNERWMMSPTest(DPVNEAlgorithmParameters params, String name) {
		super(params, name, false);
	}

	@Override
	public EmbeddingAlgorithmFactory getFactory() {
		return new RWMMSPFactory();
	}
	
}
