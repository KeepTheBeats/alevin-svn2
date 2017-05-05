package vnreal.evaluations.metrics;

import java.util.LinkedList;

import vnreal.constraints.resources.AbstractResource;
import vnreal.mapping.Mapping;
import vnreal.network.NetworkStack;
import vnreal.network.substrate.SubstrateNode;


public class NumberOfEmbeddedVNets implements EvaluationMetric<NetworkStack> {
	
	public double calculate(NetworkStack stack) {
		
		LinkedList<Integer> vnetslayers = new LinkedList<Integer>();
		for (SubstrateNode sn : stack.getSubstrate().getVertices()) {
			for (AbstractResource r : sn.get()) {
				for (Mapping m : r.getMappings()) {
					if (m.getDemand().getOwner() != null) {
						if (!vnetslayers.contains(m.getDemand().getOwner().getLayer())) {
							vnetslayers.add(m.getDemand().getOwner().getLayer());
						}
					}
				}
			}
		}
		
		return vnetslayers.size();
	}
	
	public String toString() {
		return this.getClass().getSimpleName();
	}
	
}
