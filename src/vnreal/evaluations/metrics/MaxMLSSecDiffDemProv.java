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
 * This Metric describes the maximum difference between SNode-Demand and  mapped VNode(s)-Provide
 * 
 * A positive value says that the best VNode delivers more security than needed
 * A negative value says that the best VNode delivers fewer security than needed
 * 
 * @author Fabian Kokot
 *
 */
public class MaxMLSSecDiffDemProv implements EvaluationMetric<NetworkStack> {

	@Override
	public double calculate(NetworkStack stack) {
		SubstrateNetwork sn = stack.getSubstrate();
		
		double max = 0.0d;

		
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
							int diff = ((MLSDemand)m.getDemand()).getProvide() - dlevel; 
							if (diff > max)
								max = diff;
						}
					}
				}
			}
		}
		
		return (max);
	}

	@Override
	public String toString() {
		return "MaxMLSSecSpreadDemProv";
	}

}
