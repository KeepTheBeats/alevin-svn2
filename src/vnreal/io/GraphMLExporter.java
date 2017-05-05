package vnreal.io;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;

import vnreal.constraints.AbstractConstraint;
import vnreal.constraints.resources.AbstractResource;
import vnreal.mapping.Mapping;
import vnreal.network.Link;
import vnreal.network.Network;
import vnreal.network.NetworkEntity;
import vnreal.network.Node;

public class GraphMLExporter {
	
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
			fw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
					+  "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\"\n"
					+  "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
					+ "xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns\n" 
                    + "http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">");
			
			fw.println("<key id=\"d0\" for=\"node\" attr.name=\"mapping\" attr.type=\"string\"/>");
			fw.println("<key id=\"d1\" for=\"edge\" attr.name=\"mapping\" attr.type=\"string\"/>");
			fw.println("<key id=\"d2\" for=\"node\" attr.name=\"name\" attr.type=\"string\"/>");
			fw.println("<key id=\"d3\" for=\"edge\" attr.name=\"name\" attr.type=\"string\"/>");

			int keyId = 4;
			
			HashMap<Class<?>, Integer> noderes = new HashMap<Class<?>, Integer>();
			for (V sn : sNet.getVertices()) {
				for (AbstractConstraint r : sn.get()) {
					if (noderes.get(r.getClass()) == null) {
						noderes.put(r.getClass(), keyId);
						
						fw.println("<key id=\"d" + keyId + "\" for=\"node\" attr.name=\"" + r.getClass().getSimpleName() + "\" attr.type=\"string\"/>");
						keyId++;
					}
				}
			}
			
			HashMap<Class<?>, Integer> linkres = new HashMap<Class<?>, Integer>();
			for (E sn : sNet.getEdges()) {
				for (AbstractConstraint r : sn.get()) {
					if (linkres.get(r.getClass()) == null) {
						linkres.put(r.getClass(), keyId);
						
						fw.println("<key id=\"d" + keyId + "\" for=\"edge\" attr.name=\"" + r.getClass().getSimpleName() + "\" attr.type=\"string\"/>");
						keyId++;
					}
				}
			}
			
			
			fw.println("<graph id=\"G\" edgedefault=\"" + (sNet.isDirected() ? "directed" : "undirected") + "\">");
			
			for (V sn : sNet.getVertices()) {
				fw.println("<node id=\"n" + sn.getId() + "\">");
				
				LinkedList<String> mappings = new LinkedList<String>();
				for (AbstractConstraint r : sn.get()) {
					fw.println("  <data key=\"d" + noderes.get(r.getClass()) + "\">" + r.toString() + "</data>");
					
					for (Mapping m : r.getMappings()) {
						NetworkEntity<?> owner = ((r instanceof AbstractResource) ? m.getDemand() : m.getResource()).getOwner();
						if (owner != null) {
							String name = "layer:" + owner.getLayer() + "_name:" + owner.getName() + "_id:" + owner.getId();
							if (!mappings.contains(name)) {
								mappings.add(name);
							}
						}
					}
					
				}
				fw.println("  <data key=\"d0\">" + mappings.toString() + "</data>");
				if (sn.getName() != null)
					fw.println("  <data key=\"d2\">" + sn.getName() + "</data>");
				
				fw.println("</node>");
			}
			
			for (E sn : sNet.getEdges()) {
				fw.println("<edge id=\"n" + sn.getId() + "\" source=\"n" + sNet.getSource(sn).getId() + "\" target=\"n" + sNet.getDest(sn).getId() + "\">");
				
				LinkedList<String> mappings = new LinkedList<String>();
				for (AbstractConstraint r : sn.get()) {
					fw.println("  <data key=\"d" + linkres.get(r.getClass()) + "\">" + r.toString() + "</data>");
					
					for (Mapping m : r.getMappings()) {
						NetworkEntity<?> owner = ((r instanceof AbstractResource) ? m.getDemand() : m.getResource()).getOwner();
						if (owner != null) {
							String name = "layer:" + owner.getLayer() + "_name:" + owner.getName() + "_id:" + owner.getId();
							if (!mappings.contains(name)) {
								mappings.add(name);
							}
						}
					}
				}
				fw.println("  <data key=\"d1\">" + mappings.toString() + "</data>");
				if (sn.getName() != null)
					fw.println("  <data key=\"d3\">" + sn.getName() + "</data>");
				
				fw.println("</edge>");
			}
			
			
			fw.println("</graph>\n</graphml>");
			
		} finally {
			if (fw != null)
				fw.close();
		}
		
		
	}

}
