package vnreal.generators.scenariochain;

import java.util.Map;
import java.util.Random;

import mulavito.graph.generators.WaxmanGraphGenerator;

import vnreal.core.Scenario;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

@ElementParameter(parameters = { "num", "alpha", "beta", "nodes", "bidirectional", "directed" })
public class WaxmanVirtualElement implements IChainElement {
	
	private Integer num = 0;
	private Double alpha = 0.0d;
	private Double beta = 0.0d;
	private Integer nodes = 0;
	private Boolean bidirectional = false;
	private Boolean directed = true;
	private Random rng = null;

	@Override
	public void init(Map<String, String> params, Random rng) {
		this.num = Integer.valueOf(params.get("num"));
		this.alpha = Double.valueOf(params.get("alpha"));
		this.beta = Double.valueOf(params.get("beta"));
		this.nodes = Integer.valueOf(params.get("nodes"));
		this.bidirectional = Boolean.valueOf(params.get("bidirectional"));
		this.directed = Boolean.valueOf(params.get("directed"));
		this.rng = rng;
	}

	@Override
	public Scenario process(Scenario scen) {
		
		int layers = scen.getVirtuals().size();
				
		for (int i = 0; i < num; i++) {
		
			WaxmanGraphGenerator<VirtualNode, VirtualLink> vGenerator = new WaxmanGraphGenerator<VirtualNode, VirtualLink>(
					rng, alpha, beta, bidirectional);
			
			VirtualNetwork vNetwork = new VirtualNetwork(layers + i, false, this.directed);
			for (int j = 0; j < nodes; ++j) {
				VirtualNode vn = new VirtualNode(layers + i);
				vn.setName(vn.getId() + "");
				vNetwork.addVertex(vn);
			}
			
			vGenerator.generate(vNetwork);	
			scen.addVirtual(vNetwork);
		}
		
		return scen;
	}

}
