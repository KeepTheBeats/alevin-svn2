package vnreal.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Map;

import vnreal.core.Consts;
import vnreal.core.Run;
import vnreal.core.oldFramework.TestRun;
import vnreal.network.Network;
import vnreal.network.NetworkStack;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

/**
 * This class will create a simple .dot file from any layer in the
 * NetworkStack. It can be used to get a quick topological view of
 * a given substrate or virtual network. Files have to be further
 * processed with GraphViz.
 * 
 * @author Andreas Fischer
 * @since 2015-03-02
 *
 */
public class DOTExporter implements IResultExporter {
	
	private String directory = "";
	
	public void exportTestRun(TestRun tr) {
		try {
			String finaldir = directory + Consts.FILE_SEPARATOR + tr;
			new File(finaldir).mkdirs();
			NetworkStack ns = tr.getScenario().getNetworkStack();
			SubstrateNetwork snet = ns.getSubstrate();
			export(finaldir + Consts.FILE_SEPARATOR + "0.dot", snet);
			
			for (Network<?, ?, ?> net : ns.getVirtuals()) {
				export(finaldir + Consts.FILE_SEPARATOR + net.getLayer() + ".dot", 
						(VirtualNetwork) net);
			}
		} catch (IOException e) {
			System.err.println("Problem while exporting DOT files:");
			System.err.println(e);
		}
	}
	
	/**
	 * Export a SubstrateNetwork
	 * @param filename The file to write to
	 * @param snet The network to export
	 * @throws IOException
	 */
	public void export(String filename, SubstrateNetwork snet) throws IOException {
			
		FileOutputStream outStream = new FileOutputStream(filename);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outStream));
		
		String firstLine = snet.isDirected() ? "digraph" : "graph";
		String edge = snet.isDirected() ? " -> " : " -- ";
		firstLine += " \"" + snet.getLabel() + "\" {\n";
		bw.write(firstLine);
		
		for (SubstrateNode sn: snet.getVertices()) {
			bw.write("\t" + sn.getId() + ";\n");
		}
		
		for (SubstrateLink sl : snet.getEdges()) {
			SubstrateNode n1 = snet.getSource(sl);
			SubstrateNode n2 = snet.getDest(sl);
			String line = "\t" + n1.getId() + edge + n2.getId() + ";\n";
			bw.write(line);
		}
		
		bw.write("}");
		bw.flush();
		bw.close();
	}
	
	/**
	 * Export a VirtualNetwork
	 * @param filename The file to write to
	 * @param snet The network to export
	 * @throws IOException
	 */
	public void export(String filename, VirtualNetwork vnet) throws IOException {
		
		FileOutputStream outStream = new FileOutputStream(filename);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outStream));
		
		String firstLine = vnet.isDirected() ? "digraph" : "graph";
		String edge = vnet.isDirected() ? " -> " : " -- ";
		firstLine += " \"" + vnet.getLabel() + "\" {\n";
		bw.write(firstLine);

		for (VirtualNode vn: vnet.getVertices()) {
			bw.write("\t" + vn.getId() + ";\n");
		}
		
		for (VirtualLink vl : vnet.getEdges()) {
			VirtualNode n1 = vnet.getSource(vl);
			VirtualNode n2 = vnet.getDest(vl);
			String line = "\t" + n1.getId() + edge + n2.getId() + ";\n";
			bw.write(line);
		}
		
		bw.write("}");
		bw.flush();
		bw.close();
	}

	@Override
	public void init(String resultsDir, Map<String, String> params) {
		this.directory = resultsDir;
	}


	@Override
	public void export(Run run) {
		exportTestRun(run.toOldFormat());		
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}
}
