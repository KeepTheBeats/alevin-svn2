package vnreal.evaluations.metrics;

import vnreal.constraints.demands.BandwidthDemand;
import vnreal.network.NetworkStack;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;

/**
 * Calculates the total bandwidth demanded in all virtual networks.
 * Useful for generated random networks.
 * 
 * @author Andreas Fischer
 * @since 2014-10-20
 *
 */
public class TotalBWDem implements EvaluationMetric<NetworkStack> {

	@Override
	public double calculate(NetworkStack stack) {
		double totalBW = 0;
		for (VirtualNetwork net : stack.getVirtuals()) {
			for (VirtualLink link : net.getEdges()) {
				BandwidthDemand dem = (BandwidthDemand) link.get(BandwidthDemand.class);
				if (dem != null) {
					totalBW += dem.getDemandedBandwidth();
				}
			}
		}
		return totalBW;
	}

	@Override
	public String toString() {
		return "Total bandwidth demand";
	}

}
