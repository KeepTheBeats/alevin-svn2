package vnreal.algorithms.DPVNE;

import vnreal.algorithms.DPVNE.singlenetworkmapping.factory.CoordinatedMappingPSFactory;
import vnreal.algorithms.DPVNE.singlenetworkmapping.factory.EmbeddingAlgorithmFactory;

public final class DPVNEDViNEPSTest extends DistributedMetisTest {

	public DPVNEDViNEPSTest(DPVNEAlgorithmParameters params, String name) {
		super(params, name, true);
	}

	@Override
	public EmbeddingAlgorithmFactory getFactory() {
		return new CoordinatedMappingPSFactory();
	}
	
}
