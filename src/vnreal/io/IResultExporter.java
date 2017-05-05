package vnreal.io;

import java.util.Map;

import vnreal.core.Run;


public interface IResultExporter {
	public void init(String resultsDir, Map<String, String> params);
	public void export(Run run);
	public void close();
}
