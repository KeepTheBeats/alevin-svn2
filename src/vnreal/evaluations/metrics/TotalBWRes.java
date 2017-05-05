package vnreal.evaluations.metrics;

import vnreal.constraints.resources.AbstractResource;
import vnreal.constraints.resources.BandwidthResource;
import vnreal.network.NetworkStack;
import vnreal.network.substrate.SubstrateLink;

/**
 * Calculates the total bandwidth available in the substrate network.
 * Useful for generated random networks.
 * 
 * @author Andreas Fischer
 * @since 2014-10-20
 *
 */
public class TotalBWRes implements EvaluationMetric<NetworkStack> {

	@Override
	public double calculate(NetworkStack stack) {
		double totalBW = 0;
		for (SubstrateLink link : stack.getSubstrate().getEdges()) {
			AbstractResource res = link.get(BandwidthResource.class);
			if (res != null) {
				totalBW += ((BandwidthResource) res).getBandwidth();
			}
		}
		return totalBW;
	}

	@Override
	public String toString() {
		return "Total bandwidth resources";
	}

}
