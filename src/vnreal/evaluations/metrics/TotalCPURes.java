package vnreal.evaluations.metrics;

import vnreal.constraints.resources.AbstractResource;
import vnreal.constraints.resources.CpuResource;
import vnreal.network.NetworkStack;
import vnreal.network.substrate.SubstrateNode;

/**
 * Calculates the total CPU cycles available in the substrate network.
 * Useful for generated random networks.
 * 
 * @author Andreas Fischer
 * @since 2014-10-20
 *
 */
public class TotalCPURes implements EvaluationMetric<NetworkStack> {

	@Override
	public double calculate(NetworkStack stack) {
		double totalCPU = 0;
		for (SubstrateNode node : stack.getSubstrate().getVertices()) {
			AbstractResource res = node.get(CpuResource.class);
			if (res != null) {
				totalCPU += ((CpuResource) res).getCycles();
			}
		}
		return totalCPU;
	}

	@Override
	public String toString() {
		return "Total CPU resources";
	}

}
