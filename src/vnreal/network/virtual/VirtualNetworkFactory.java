package vnreal.network.virtual;

import vnreal.constraints.demands.AbstractDemand;
import vnreal.network.NetworkFactory;

public class VirtualNetworkFactory implements NetworkFactory<AbstractDemand, VirtualNode, VirtualLink, VirtualNetwork> {

	protected int layer = 1;
	protected long count = 0;
	protected boolean autounregister;
	
	public VirtualNetworkFactory(boolean autounregister) {
		this.autounregister = autounregister;
	}
	
	public void setLayer(int layer) {
		this.layer = layer;
	}

	@Override
	public VirtualNode createNode() {
		VirtualNode sn = new VirtualNode(layer);
		sn.setName(count++ + "");
		return sn;
	}

	@Override
	public VirtualLink createEdge() {
		VirtualLink sl = new VirtualLink(layer);
		sl.setName(count++ + "");
		return sl;
	}

	@Override
	public VirtualNetwork create(boolean directed) {
		return new VirtualNetwork(layer, autounregister, directed);
	}
	
	@Override
	public VirtualNetwork create() {
		return new VirtualNetwork(layer, autounregister);
	}
	
}
