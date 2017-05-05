package vnreal.evaluations.metrics;

import vnreal.constraints.AbstractConstraint;
import vnreal.constraints.demands.CpuDemand;
import vnreal.network.NetworkStack;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

/**
 * Calculates the total CPU cycles demanded in all virtual networks.
 * Useful for generated random networks.
 * 
 * @author Andreas Fischer
 * @since 2014-10-20
 *
 */
public class TotalCPUDem implements EvaluationMetric<NetworkStack> {

	@Override
	public double calculate(NetworkStack stack) {
		double totalCPU = 0;
		for (VirtualNetwork vn : stack.getVirtuals()) {
			for (VirtualNode vnode : vn.getVertices() ) {
				AbstractConstraint dem = vnode.get(CpuDemand.class);
				if (dem != null) {
					totalCPU += ((CpuDemand) dem).getDemandedCycles();
				}
			}
		}
		return totalCPU;
	}

	@Override
	public String toString() {
		return "Total CPU demand";
	}

}
