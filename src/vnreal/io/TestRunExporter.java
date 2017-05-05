package vnreal.io;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;


import vnreal.core.Consts;
import vnreal.core.Run;

public class TestRunExporter implements IResultExporter {

	private XMLExporter xmlexp;

	@Override
	public void init(String resultsDir, Map<String, String> params) {
		String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
		String fileName = resultsDir + Consts.FILE_SEPARATOR + "TestRunExporter-" + timeStamp + ".xml";
		String exportNetsS = params.get("ExportNetworks");
		boolean exportNets = false;
		if (exportNetsS != null)
			exportNets = Boolean.parseBoolean(exportNetsS);
		xmlexp = new XMLExporter(fileName, exportNets);

	}

	@Override
	public void export(Run run) {
		xmlexp.exportTestRun(run.toOldFormat());
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		xmlexp.finishWriting();
	}

}
