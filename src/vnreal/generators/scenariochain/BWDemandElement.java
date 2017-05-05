package vnreal.generators.scenariochain;

import java.util.Map;
import java.util.Random;

import vnreal.constraints.demands.BandwidthDemand;
import vnreal.core.Scenario;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;

@ElementParameter(parameters = { "min", "max" })
public class BWDemandElement implements IChainElement {

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
		for (VirtualNetwork vnet : scen.getVirtuals()) {
			for (VirtualLink l : vnet.getEdges()) {
				BandwidthDemand bw = new BandwidthDemand(l);
				int value = min.intValue() + rng.nextInt(max.intValue() - min.intValue());
				bw.setDemandedBandwidth((double) value);
				l.add(bw);
			}
		}
		return scen;
	}

}
