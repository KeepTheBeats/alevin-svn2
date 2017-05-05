package tests.generators.constraints;

import java.util.Random;

public interface ConstraintsGenerator<N> {
	
	public abstract void addConstraints(N network, Random random);
	public String getSuffix(String prefix);
	
}
