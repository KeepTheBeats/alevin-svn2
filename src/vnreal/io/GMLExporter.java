package vnreal.io;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;

import vnreal.constraints.AbstractConstraint;
import vnreal.network.Link;
import vnreal.network.Network;
import vnreal.network.Node;


public class GMLExporter {
	
	public static <T extends AbstractConstraint, V extends Node<T>, E extends Link<T>, N extends Network<T, V, E>> void export(String filename, N sNet) throws IOException {
		
		PrintWriter fw = null;
		try {
			fw = new PrintWriter(new FileWriter(new File(filename)), true);
			export(fw, sNet);
		} finally {
			if (fw != null)
				fw.close();
		}
		
	}
	
	public static <T extends AbstractConstraint, V extends Node<T>, E extends Link<T>, N extends Network<T, V, E>> String export(N sNet) throws IOException {
		
		ByteArrayOutputStream s = new ByteArrayOutputStream();
		
		PrintWriter fw = null;
		try {
			fw = new PrintWriter(s, true);
			export(fw, sNet);
		} finally {
			if (fw != null)
				fw.close();
		}
		
		return s.toString(Charset.defaultCharset().displayName());
	}
	
	public static <T extends AbstractConstraint, V extends Node<T>, E extends Link<T>, N extends Network<T, V, E>> void export(PrintWriter fw, N sNet) throws IOException {
		
		try {
			fw.println("graph [");
			fw.println("	directed " + (sNet.isDirected() ? "1" : "0"));
			fw.println("	id 0");
			
			for (V sn : sNet.getVertices()) {
				fw.println("	node [");
				fw.println("		id " + sn.getId());
				
				for (AbstractConstraint r : sn.get()) {
					fw.println("		" + r.getClass().getSimpleName() + " " + r.toString());
				}
					
				fw.println("	]");
			}
			
			for (E sn : sNet.getEdges()) {
				fw.println("	edge [");
				fw.println("		source " + sNet.getSource(sn).getId());
				fw.println("		target " + sNet.getDest(sn).getId());
				
				for (AbstractConstraint r : sn.get()) {
					fw.println("		" + r.getClass().getSimpleName() + " " + r.toString());
				}
				fw.println("	]");
			}
			
			
			fw.println("]");
			
		} finally {
			if (fw != null)
				fw.close();
		}
		
		
	}

}
