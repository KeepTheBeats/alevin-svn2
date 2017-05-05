package vnreal.generators.scenariochain;

import java.util.Map;
import java.util.Random;

import vnreal.constraints.resources.CpuResource;
import vnreal.core.Scenario;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;

@ElementParameter(parameters = { "min", "max" })
public class CPUResourceElement implements IChainElement {

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
		
		for(SubstrateNode n : sNet.getVertices()) {
			CpuResource cpu = new CpuResource(n);
			int value = min.intValue() + rng.nextInt(max.intValue() - min.intValue());
			cpu.setCycles((double) value);
			n.add(cpu);
		}
		return scen;
	}

}
