package vnreal.generators.scenariochain;

import java.util.Map;
import java.util.Random;

import mulavito.graph.generators.WaxmanGraphGenerator;

import vnreal.core.Scenario;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;

@ElementParameter(parameters = { "alpha", "beta", "nodes", "bidirectional", "directed" })
public class WaxmanSubstrateElement implements IChainElement {
	
	private Double alpha = 0.0d;
	private Double beta = 0.0d;
	private Integer nodes = 0;
	private Boolean bidirectional = false;
	private Boolean directed = true;
	private Random rng = null;

	@Override
	public void init(Map<String, String> params, Random rng) {
		this.alpha = Double.valueOf(params.get("alpha"));
		this.beta = Double.valueOf(params.get("beta"));
		this.nodes = Integer.valueOf(params.get("nodes"));
		this.bidirectional = Boolean.valueOf(params.get("bidirectional"));
		this.directed = Boolean.valueOf(params.get("directed"));
		this.rng = rng;
	}

	@Override
	public Scenario process(Scenario scen) {
		
		WaxmanGraphGenerator<SubstrateNode, SubstrateLink> sGenerator = new WaxmanGraphGenerator<SubstrateNode, SubstrateLink>(
				rng, alpha, beta, bidirectional);
		
		SubstrateNetwork sNetwork = new SubstrateNetwork(false, this.directed);
		for (int i = 0; i < nodes; ++i) {
			SubstrateNode sn = new SubstrateNode();
			sn.setName(sn.getId() + "");
			sNetwork.addVertex(sn);
		}
		
		sGenerator.generate(sNetwork);
		
		scen.setSubstrate(sNetwork);
		return scen;
	}

}
