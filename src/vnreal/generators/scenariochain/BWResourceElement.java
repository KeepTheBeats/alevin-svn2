package vnreal.generators.scenariochain;

import java.util.Map;
import java.util.Random;

import vnreal.constraints.resources.BandwidthResource;
import vnreal.core.Scenario;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;

@ElementParameter(parameters = { "min", "max" })
public class BWResourceElement implements IChainElement {

	private Double min = 0.0d;
	private Double max = 0.0d;
	private Random rng = null;
	
	@Override
	public void init(Map<String, String> params, Random rng) {
		this.min = Double.valueOf(params.get("min"));
		this.max = Double.valueOf(params.get("max"));
		this.rng = rng;
	}

	@Override
	public Scenario process(Scenario scen) {
		
		SubstrateNetwork sNet = scen.getSubstrate();
		
		for(SubstrateLink l : sNet.getEdges()) {
			BandwidthResource bw = new BandwidthResource(l);
			int value = min.intValue() + rng.nextInt(max.intValue() - min.intValue());
			bw.setBandwidth((double) value);
			l.add(bw);
		}
		return scen;
	}

}
