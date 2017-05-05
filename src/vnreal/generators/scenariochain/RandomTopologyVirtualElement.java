package vnreal.generators.scenariochain;

import java.util.Map;
import java.util.Random;

import vnreal.core.Scenario;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

@ElementParameter(parameters = { "num", "p_edge", "nodes_min", "nodes_max", "bidirectional", "directed" })
public class RandomTopologyVirtualElement implements IChainElement {

	private int num = 0;
	private double p_edge = 0;
	private int nodes_min = 0;
	private int nodes_max = 0;
	private boolean bidirectional = true;
	private boolean directed = true;
	private Random rng = null;
	
	@Override
	public void init(Map<String, String> params, Random rng) {
		this.num = Integer.valueOf(params.get("num"));
		this.p_edge = Double.valueOf(params.get("p_edge"));
		this.nodes_min = Integer.valueOf(params.get("nodes_min"));
		this.nodes_max = Integer.valueOf(params.get("nodes_max"));
		this.bidirectional = Boolean.valueOf(params.get("bidirectional"));
		this.directed = Boolean.valueOf(params.get("directed"));
		this.rng = rng;
	}

	@Override
	public Scenario process(Scenario scen) {
		int layers = scen.getVirtuals().size();
		
		for (int i = 0; i < num; i++) {
		
			VirtualNetwork vNetwork = new VirtualNetwork(layers + i, false, this.directed);
			
			int nodes = this.nodes_min + rng.nextInt(this.nodes_max - this.nodes_min + 1);
			
			for (int j = 0; j < nodes; ++j) {
				VirtualNode vn = new VirtualNode(layers + i);
				vn.setName(vn.getId() + "");
				vNetwork.addVertex(vn);
			}
			
			for (VirtualNode u : vNetwork.getVertices()) {
				for (VirtualNode v : vNetwork.getVertices()) {
					if (u == v) continue;
					if (vNetwork.findEdge(u, v) != null) continue;
					
					if (rng.nextDouble() <= p_edge) {
						vNetwork.addEdge(vNetwork.getEdgeFactory().create(), u, v);
						if (this.bidirectional && this.directed) {
							vNetwork.addEdge(vNetwork.getEdgeFactory().create(), v, u);
						}
					}
				}
			}
			
			scen.addVirtual(vNetwork);
		}
		
		return scen;
	}

}
