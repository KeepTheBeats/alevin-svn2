package vnreal.generators.scenariochain;

import java.util.Map;
import java.util.Random;

import vnreal.core.Scenario;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;

@ElementParameter(parameters = { "p_edge", "nodes_min", "nodes_max", "bidirectional", "directed" })
public class RandomTopologySubstrateElement implements IChainElement {

	private double p_edge = 0;
	private int nodes_min = 0;
	private int nodes_max = 0;
	private boolean bidirectional = true;
	private boolean directed = true;
	private Random rng = null;
	
	@Override
	public void init(Map<String, String> params, Random rng) {
		this.p_edge = Double.valueOf(params.get("p_edge"));
		this.nodes_min = Integer.valueOf(params.get("nodes_min"));
		this.nodes_max = Integer.valueOf(params.get("nodes_max"));
		this.bidirectional = Boolean.valueOf(params.get("bidirectional"));
		this.directed = Boolean.valueOf(params.get("directed"));
		this.rng = rng;
	}

	@Override
	public Scenario process(Scenario scen) {
		
		
		SubstrateNetwork sNetwork = new SubstrateNetwork(false, this.directed);
		
		int nodes = this.nodes_min + rng.nextInt(this.nodes_max - this.nodes_min + 1);
		
		for (int j = 0; j < nodes; ++j) {
			SubstrateNode sn = new SubstrateNode();
			sn.setName(sn.getId() + "");
			sNetwork.addVertex(sn);
		}
		
		for (SubstrateNode u : sNetwork.getVertices()) {
			for (SubstrateNode v : sNetwork.getVertices()) {
				if (u == v) continue;
				if (sNetwork.findEdge(u, v) != null) continue;
				
				if (rng.nextDouble() <= p_edge) {
					sNetwork.addEdge(sNetwork.getEdgeFactory().create(), u, v);
					if (this.bidirectional && this.directed) {
						sNetwork.addEdge(sNetwork.getEdgeFactory().create(), v, u);
					}
				}
			}
		}
		
		scen.setSubstrate(sNetwork);
		
		return scen;
	}

}
