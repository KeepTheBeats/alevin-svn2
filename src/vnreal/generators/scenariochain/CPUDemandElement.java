package vnreal.generators.scenariochain;

import java.util.Map;
import java.util.Random;

import vnreal.constraints.demands.CpuDemand;
import vnreal.core.Scenario;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

@ElementParameter(parameters = { "min", "max" })
public class CPUDemandElement implements IChainElement {

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
			for (VirtualNode n : vnet.getVertices()) {
				CpuDemand cpu = new CpuDemand(n);
				int value = min.intValue() + rng.nextInt(max.intValue() - min.intValue());
				cpu.setDemandedCycles((double) value);
				n.add(cpu);
			}
		}
		
		return scen;
	}

}
