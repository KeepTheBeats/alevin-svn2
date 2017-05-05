package tests.io;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import vnreal.algorithms.AlgorithmParameter;

/**
 * This class will import parameters for a test run from a predefined file.
 * Expected file format is:
 * 
 * "key1_as_string": Double1,Double2,...
 * "key2_as_string": Double3,Double4,...
 * ...
 * 
 * @author Andreas Fischer
 *
 */
public class TestParamImporter {
		private ArrayList<SimpleEntry<String, Object[]>> params = new ArrayList<SimpleEntry<String, Object[]>>();
		private String algo;
		private String netGen;
		private AlgorithmParameter algoParams = new AlgorithmParameter();
		private List<String> metrics;
		private List<String> resGens;
		private List<String> demGens;
		private int runs;
		private String saveNetworks = ""; // Recognized: "", "XML", "DOT"
		
		public TestParamImporter(String filename) throws IOException {
				Path fn = Paths.get(filename);
				List<String> lines = Files.readAllLines(fn, StandardCharsets.UTF_8);
				for (String line : lines) {
					parseLine(line);
				}
		}

		public ArrayList<SimpleEntry<String, Object[]>> getParams() {
			return params;
		}
		
		public String getAlgo() {
			return algo;
		}
		
		public String getNetGen() {
			return netGen;
		}

		public List<String> getMetrics() {
			return metrics;
		}
		
		public List<String> getResGens() {
			return resGens;
		}
		
		public List<String> getDemGens() {
			return demGens;
		}

		public int getRuns() {
			return runs;
		}

		public String getSaveNetworks() {
			return saveNetworks;
		}
		
		public AlgorithmParameter getAlgoParams() {
			return algoParams;
		}

		
		private void parseLine(String line) {
			String[] key_value = line.split(": ");
			String key = key_value[0].replaceAll("^\"|\"$", "");
			String[] values = key_value[1].split(",");
			
			if (key.equals("Algorithm")) {
				algo = values[0];
			} else if (key.equals("NetworkGenerator")) {
				netGen = values[0];
			} else if (key.equals("Metrics")) {
				metrics = Arrays.asList(values);
			} else if (key.equals("ResourceGenerators")) {
				resGens = Arrays.asList(values);
			} else if (key.equals("DemandGenerators")) {
				demGens = Arrays.asList(values);
			} else if (key.equals("Runs")) {
				runs = Integer.parseInt(values[0]);
			} else if (key.equals("SaveNetworks")) {
				if (values[0].equals("") 
						|| values[0].equals("XML") 
						|| values[0].equals("DOT")) {
					saveNetworks = values[0];
				}
			} else if (key.equals("AlgorithmParameter")) {
				parseAlgoParamLine(values);
			} else {
				SimpleEntry<String, Object[]> entry = parseParamLine(key, values);
				params.add(entry);
			}
		}
		
		private SimpleEntry<String, Object[]> parseParamLine(String key, String[] values) {
				List<Double> vallist = new ArrayList<Double>();

				for (String val: values) {
						vallist.add(Double.parseDouble(val));
				}
				
				return new SimpleEntry<String, Object[]>(key, vallist.toArray());
		}
		
		private void parseAlgoParamLine(String[] values) {
			for (String val: values) {
				String[] key_value = val.split("=");
				algoParams.put(key_value[0], key_value[1]);
			}
		}


}
