package vnreal.algorithms;

import java.util.List;

import mulavito.algorithms.AbstractAlgorithmStatus;

/**
 * This algorithm does nothing at all. In particular, no networks will be
 * mapped. Use it to check other parts of the evaluation process.  
 * 
 * @author Andreas Fischer
 * @since 2015-02-18
 *
 */
public class NullAlgorithm extends AbstractAlgorithm {
	
	public NullAlgorithm(AlgorithmParameter param) {
	}
	
	@Override
	public List<AbstractAlgorithmStatus> getStati() {
		return null;
	}

	@Override
	protected boolean preRun() {
		return true;
	}

	@Override
	protected void evaluate() {

	}

	@Override
	protected void postRun() {

	}

}
