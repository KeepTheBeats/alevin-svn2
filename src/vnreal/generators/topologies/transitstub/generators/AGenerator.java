package vnreal.generators.topologies.transitstub.generators;

import java.util.Random;

import vnreal.generators.topologies.transitstub.graph.Graph;

public abstract class AGenerator {

	private static final int MAX_TRIES = 100;

	public abstract Graph generate(int numVertices, Random random);

	public Graph generateConnected(int numVertices, Random random) {
		Graph result;
		int tries = 0;
		do {
			result = this.generate(numVertices, random);
			tries++;
		} while (!result.isConnected() && tries <= MAX_TRIES);

		if (tries > MAX_TRIES) {
			throw new RuntimeException("Failed to generate connected graph.");
		}

		return result;
	}

}
