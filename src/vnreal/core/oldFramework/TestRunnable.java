package vnreal.core.oldFramework;

import mulavito.algorithms.IAlgorithm;


/**
 * This a basic Runnable class to run tests
 * 
 * @author Fabian Kokot
 *
 */
public class TestRunnable implements Runnable {

	
	private IAlgorithm mAlgo;
	private final TestRun mRun;
	private final TestRunner mRunner;
	private final Long mStartSeed;

	/**
	 * 
	 * @param run The actual {@link TestRun}
	 * @param runner The TestRunner which is used
	 * @param startSeed The seed which is used 
	 */
	public TestRunnable(TestRun run, TestRunner runner, long startSeed) {
		mRun = run;
		mRunner = runner;
		mStartSeed = startSeed;
		
	}

	@Override
	public void run() {
		
		mRun.clearResults();
		
		mRunner.prepareTestStage1(mRun, mStartSeed);
		
		mAlgo = mRunner.prepareRunnerStage2(mRun);
		
		System.out.println("Test startet on Thread "+Thread.currentThread().getId());
		
		long startTime = System.currentTimeMillis();
		mAlgo.performEvaluation();
		long elapsedTime = System.currentTimeMillis() - startTime;
		
		
		mRun.addResult("Runtime", (double)elapsedTime);
		
		mRunner.export(mRun);
	}

}
