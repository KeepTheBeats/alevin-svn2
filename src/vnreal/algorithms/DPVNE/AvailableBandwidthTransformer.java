package vnreal.algorithms.DPVNE;

import org.apache.commons.collections15.Transformer;

import vnreal.constraints.resources.AbstractResource;
import vnreal.constraints.resources.CpuResource;
import vnreal.network.substrate.SubstrateLink;

public class AvailableBandwidthTransformer implements Transformer<SubstrateLink, Double> {

	private final double biggestBandwidth;
	private final double alpha;
	
	public AvailableBandwidthTransformer(double biggestBandwidth, double alpha) {
		this.biggestBandwidth = biggestBandwidth;
		this.alpha = alpha;
	}
	
	@Override
	public Double transform(SubstrateLink arg0) {
		boolean found = false;
		double currentBandwidth = 0.0d;
		
		for (AbstractResource r : arg0) {
			if (r instanceof CpuResource) {
				currentBandwidth = ((CpuResource) r).getAvailableCycles();
				found = true;
				break;
			}
		}
		if (!found) {
			throw new AssertionError();
		}
		
		return (alpha * ((biggestBandwidth - currentBandwidth) / (biggestBandwidth)));
	}
	
}
