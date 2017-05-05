package vnreal.algorithms.DPVNE;

import vnreal.constraints.demands.AbstractDemand;

public class UpdateEntry {

	public final boolean isNode;
	public final int layer;
	public final String sEntityName;
	public final AbstractDemand demand;
	
	public UpdateEntry(boolean isNode, int layer, String sEntityName, AbstractDemand demand) {
		this.isNode = isNode;
		this.layer = layer;
		this.sEntityName = sEntityName;
		this.demand = demand;
	}
	
}
