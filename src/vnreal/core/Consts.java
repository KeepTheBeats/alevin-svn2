package vnreal.core;

import java.io.File;
import java.nio.file.Paths;

/**
Collected constants of general utility.

<P>All members of this class are immutable.

 Class extracted from:
 
 http://www.javapractices.com/topic/TopicAction.do?Id=2
 
 Modified by:

@author Juan Felipe Botero
@author Andreas Fischer
@since 27-04-2011 
*/
public final class Consts  {

	/** 
	 * Useful for String operations, which return an index of <tt>-1</tt> when 
	 * an item is not found. 
	 */
	public static final int NOT_FOUND = -1;

	/** System property - <tt>line.separator</tt>*/
	public static final String NEW_LINE = System.getProperty("line.separator");
	/** System property - <tt>file.separator</tt>*/
	public static final String FILE_SEPARATOR = System.getProperty("file.separator");

	public static final String ALEVIN_DIR = Paths.get("").toAbsolutePath().toString(); // Assume pwd is basedir
	public static final String RESULTS_DIR = ALEVIN_DIR + File.separator  + "results";
	public static final String SYSINFOTOOL = ALEVIN_DIR + File.separator  + "tools" + File.separator + "sysinfo.sh";
	public static final String EXPERIMENT_XML = ALEVIN_DIR + File.separator + "src" + File.separator + "XML" + File.separator + "Experiment.xsd";

	// LP solver files folder
	public static String LP_SOLVER_FOLDER = ALEVIN_DIR + File.separator + "ILP-LP-Models" + File.separator;
	public static String LP_SOLVER_DATAFILE = "datafile";
	public static String LP_LINKMAPPING_MODEL = "VNE-Model.mod";
	public static String LP_LINKMAPPING_MODEL_HIDDENHOPS = "HHVNE-Model.mod";
	public static String LP_NODEMAPPING_MODEL = "VNE-Model-NodeMapping.mod";
	public static String ILP_EXACTMAPPING_MODEL = "VNE-ExactMIP.mod";
	public static String ILP_EXACTMAPPING_MODEL_HIDDEN_HOP = "VNE-ExactMIP-WithHiddenHops.mod";
	public static String ILP_EXACTMAPPING_ENERGY_MODEL = "VNE-ExactMIP-EnergyEfficiency.mod";
	public static String ILP_EXACTMAPPING_ENERGY_MODEL_HIDDEN_HOP = "VNE-ExactMIP-EnergyEfficiencyHiddenHop.mod";
	public static String ILP_EXACTMAPPING_ENERGY_MODEL_HIDDEN_HOP_WITH_COST = "VNE-ExactMIP-EnergyEfficiencyHiddenHop-WithCost.mod";
	public static String ILP_NODEMAPPING_RELAXED_TEST_ENERGY_MODEL = "VNE-Model-NodeMapping-EnergyTest.mod";
	public static String LP_LINKMAPPING_RELAXED_ENERGY_MODEL = "HHVNE-Model-LinkMappingRelaxedEnergyTest-NumberI.mod";
	public static String LP_LINKMAPPING_RELAXED_ENERGY_MODEL_TEST = "HHVNE-Model-LinkMappingRelaxedEnergyTest.mod";
	public static String LP_LINKMAPPING_MIP_ENERGY_MODEL_TEST = "HHVNE-Model-LinkMappingMIPEnergyTest.mod";
	public static String LP_LINKMAPPING_MIP_ENERGY_MODEL_TEST_FORNODES = "HHVNE-Model-LinkMappingMIPEnergyNode.mod";

	// PRIVATE //

	/**
	 * The caller references the constants using <tt>Consts.EMPTY_STRING</tt>, 
	 * and so on. Thus, the caller should be prevented from constructing objects of 
	 * this class, by declaring this private constructor. 
	 */
	private Consts(){
		//this prevents even the native class from 
		//calling this actor as well :
		throw new AssertionError();
	}
}