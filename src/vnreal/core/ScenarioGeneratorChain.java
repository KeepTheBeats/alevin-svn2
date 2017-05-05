package vnreal.core;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

import vnreal.generators.scenariochain.ElementParameter;
import vnreal.generators.scenariochain.IChainElement;

public class ScenarioGeneratorChain {
	private LinkedList<IChainElement> chain = null;
	private Random rng = null;
	
	public ScenarioGeneratorChain() {
		chain = new LinkedList<IChainElement>();
		rng = new Random();
	}
	
	/**
	 * This method initializes the entire generation chain.
	 * @param params A LinkedHashMap defining the scenario generation chain.
	 * Keys are the class names of IScenarioGeneratorChainElements. Values are a Map
	 * of parameters for the respective element.
	 * @throws ClassNotFoundException 
	 */
	public void init(LinkedHashMap<String, Map<String, String>> params) throws ClassNotFoundException {
		Registry reg = Registry.getInstance();
		
		for (Map.Entry<String, Map<String, String>> entry : params.entrySet()) {
			String key = entry.getKey();
			Map<String, String> value = entry.getValue();
			
			if (key.equals("RNG")) {
				Long seed = Long.decode(value.get("Seed"));
				rng.setSeed(seed);
			} else {
			
				IChainElement elem = reg.getScenarioGeneratorChainElement(key);
				
				validate(elem, value);
				elem.init(value, rng);
				chain.add(elem);
			}
		}
	}
	
	public Scenario generate() {
		Scenario scen = new Scenario();
		for (IChainElement elem : chain) {
			scen = elem.process(scen);
		}
		return scen;
	}
	
	public void reset() {
		chain.clear();
	}
	
	private void validate(IChainElement elem, Map<String, String> params) {
		for (String param : elem.getClass().getAnnotation(ElementParameter.class).parameters()) {
			String value = params.get(param);
			if (value == null) 
				throw new IllegalArgumentException("Missing required chain element parameter: " + param);
		}
	}

}
