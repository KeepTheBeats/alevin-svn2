package tests.generators.network;

import java.util.Random;

import tests.generators.constraints.ConstraintsGenerator;

public interface NetworkGeneratorInterface<N> {
	
	public N generate(Random random,
			ConstraintsGenerator<N> constraintsGenerator,
			NetworkGeneratorParameter param);

	public void setLayer(int layer);

}
