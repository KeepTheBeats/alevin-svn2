package vnreal.generators.scenariochain;

import java.util.Map;
import java.util.Random;

import vnreal.core.Scenario;

public interface IChainElement {
	public void init(Map<String,String> params, Random rng);
	public Scenario process(Scenario scen);
}
