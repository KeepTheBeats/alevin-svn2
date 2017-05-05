package vnreal.evaluations.metrics;

import vnreal.constraints.demands.MLSDemand;
import vnreal.constraints.resources.AbstractResource;
import vnreal.constraints.resources.MLSResource;
import vnreal.mapping.Mapping;
import vnreal.network.NetworkStack;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualNode;

/**
 * This metric measures the average difference between SNode-Demand and VNode(s)-Provide
 * 
 * @author Fabian Kokot
 *
 */
public class AvMLSSecDiffDemProv implements EvaluationMetric<NetworkStack> {
	
	@Override
	public double calculate(NetworkStack stack) {
		
		SubstrateNetwork sn = stack.getSubstrate();
		
		double diff = 0.0d;
		int counter = 0;
		
		//Work through all SNodes
		for (SubstrateNode n : sn.getVertices()) {
			//Get the MLSResource
			for(AbstractResource ar : n.get()) {
				if(ar instanceof MLSResource) {
					int dlevel = ((MLSResource)ar).getDemand();
					//Get the Mapping on this Resource
					for(Mapping m : ar.getMappings()) {
						//Check that the Mapping is from a Node (Links are handled in another Metric)
						if(m.getDemand().getOwner() instanceof VirtualNode) {
							//Calculate the difference from vNode Provide minus snode demand
							diff += ((MLSDemand)m.getDemand()).getProvide() - dlevel; 
							counter++;
						}
					}
				}
			}
		}
		
		if(counter == 0)
			return 0.0;
		
		return (diff / counter);
	}

	@Override
	public String toString() {
		return "AvMLSSecSpreadDemProv";
	}

}
