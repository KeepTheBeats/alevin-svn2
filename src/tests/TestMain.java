package tests;

import vnreal.core.oldFramework.TestOrchestrator;


public class TestMain {
	
	/**
	 * @param args
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) {
		System.out.println("Use of this class is deprecated. This class will be removed in the future. " +
							"Please use vnreal.Main with commandline parameter -test $TESTFILE instead.");
		if (args.length != 1) {
			System.out.println("Expected argument missing: Parameter specification (*.test)");
			System.exit(1);
		} else {
			TestOrchestrator.execute(args[0]);
		}
	}
	
}
