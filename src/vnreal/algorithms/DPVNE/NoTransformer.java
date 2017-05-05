package vnreal.algorithms.DPVNE;

import org.apache.commons.collections15.Transformer;

import vnreal.network.substrate.SubstrateLink;

public class NoTransformer implements Transformer<SubstrateLink, Double> {

	@Override
	public Double transform(SubstrateLink arg0) {
		return 1.0d;
	}
	
}
