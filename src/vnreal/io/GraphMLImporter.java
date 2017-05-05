package vnreal.io;

import java.
io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import vnreal.algorithms.utils.SubgraphBasicVN.Utils;
import vnreal.constraints.AbstractConstraint;
import vnreal.constraints.demands.BandwidthDemand;
import vnreal.constraints.demands.CapacityDemand;
import vnreal.constraints.demands.FreeSlotsDemand;
import vnreal.constraints.resources.AbstractResource;
import vnreal.constraints.resources.BackupResource;
import vnreal.constraints.resources.BandwidthResource;
import vnreal.constraints.resources.CapacityResource;
import vnreal.constraints.resources.FreeSlotsResource;
import vnreal.network.Link;
import vnreal.network.Network;
import vnreal.network.NetworkFactory;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNetworkFactory;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNetworkFactory;
import vnreal.network.virtual.VirtualNode;

public class GraphMLImporter {
	
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
		SubstrateNetworkFactory snetfactory = new SubstrateNetworkFactory(false);
		VirtualNetworkFactory vnetfactory = new VirtualNetworkFactory(false);
		
		boolean occupyWithoutMapping = false;
		
		Collection<VirtualNetwork> vNets = new LinkedList<VirtualNetwork>();
		vNets.add(doImport(vnetfactory, null, "/home/beck/v1.graphml", occupyWithoutMapping));
		
		SubstrateNetwork sNet = doImport(snetfactory, vNets, "/home/beck/sNet.graphml", occupyWithoutMapping);
		System.out.println(sNet);
	}
	
	public static <T extends AbstractConstraint, V extends vnreal.network.Node<T>, E extends Link<T>, N extends Network<T, V, E>> N doImport(NetworkFactory<T, V, E, N> netfactory, Collection<VirtualNetwork> vNets, String filename, boolean occupyWithoutMapping) throws ParserConfigurationException, SAXException, IOException {
		HashMap<String, VirtualNode> vNodes = new HashMap<String, VirtualNode>();
		HashMap<String, VirtualLink> vLinks = new HashMap<String, VirtualLink>();
		if (vNets != null) {
			for (VirtualNetwork vNet : vNets) {
				for (VirtualNode n : vNet.getVertices()) {
					vNodes.put(n.getName(), n);
				}
				for (VirtualLink l : vNet.getEdges())
					vLinks.put(l.getName(), l);
			}
		}
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(new File(filename));
		Element root = doc.getDocumentElement();
		
		HashMap<String, String> nodeConstraints = new HashMap<String, String>();
		HashMap<String, String> linkConstraints = new HashMap<String, String>();
		
		for (Node node : getChildrenElements(root, "key")) {
			NamedNodeMap attribs = node.getAttributes();
			String id = attribs.getNamedItem("id").getNodeValue();
			String type = attribs.getNamedItem("for").getNodeValue();
			String value = attribs.getNamedItem("attr.name").getNodeValue();
			
			if (type.equals("node")) {
				if (nodeConstraints.get(id) != null)
					throw new AssertionError();
				nodeConstraints.put(id, value);
			} else if (type.equals("edge")) {
				if (linkConstraints.get(id) != null)
					throw new AssertionError();
				linkConstraints.put(id, value);
			} else {
				throw new AssertionError();
			}
		}
		
		LinkedList<Node> graph = getChildrenElements(root, "graph");
		if (graph.size() != 1)
			throw new AssertionError();
		Node G = graph.getFirst();
		NamedNodeMap Gattr = G.getAttributes();
		String Gid = Gattr.getNamedItem("id").getNodeValue();
		String Gtype = Gattr.getNamedItem("edgedefault").getNodeValue();
		boolean directed = false;
		if (Gtype.equals("directed"))
			directed = true;
		else if (Gtype.equals("undirected"))
			directed = false;
		else
			throw new AssertionError();
		
		N net = netfactory.create(directed);
		net.setName(Gid);
		
		HashMap<String, V> nodes = new HashMap<String, V>();
		for (Node n : getChildrenElements(G, "node")) {
			String id = n.getAttributes().getNamedItem("id").getNodeValue();
			
			V node = netfactory.createNode();
			node.setName(id);
			if (nodes.get(id) != null)
				throw new AssertionError();
			nodes.put(id, node);
			
			String[] mappings = null;
			for (Node d : getChildrenElements(n, "data")) {
				String datakey = d.getAttributes().getNamedItem("key").getNodeValue();
				String datavalue = d.getTextContent();
				
				String constraintType = nodeConstraints.get(datakey);
				if (constraintType.equals("mapping")) {
					String ms = datavalue.substring(1, datavalue.length() - 1);
					if (ms.length() > 0)
						mappings = ms.split(", ");
				} else
					node.add((T) createConstraint(constraintType, datavalue, node, occupyWithoutMapping));
			}
			
			if (!occupyWithoutMapping && mappings != null && vNets != null) {
				for (String m : mappings) {
					String mappingId = "n" + getValue(m, "id");
					VirtualNode vNode = vNodes.get(mappingId);
					if (vNode != null)
						Utils.occupyResources(vNode.get(), (Collection<AbstractResource>) node.get());
				}
			}
			
			net.addVertex(node);
		}
		
		
		HashMap<String, E> edges = new HashMap<String, E>();
		for (Node n : getChildrenElements(G, "edge")) {
			NamedNodeMap edgeattr = n.getAttributes();
			String id = edgeattr.getNamedItem("id").getNodeValue();
			V source = nodes.get(edgeattr.getNamedItem("source").getNodeValue());
			V target = nodes.get(edgeattr.getNamedItem("target").getNodeValue());
			if (source == null || target == null)
				throw new AssertionError();
			
			
			E link = netfactory.createEdge();
			link.setName(id);
			if (edges.get(id) != null)
				throw new AssertionError();
			edges.put(id, link);
			
			String[] mappings = null;
			for (Node d : getChildrenElements(n, "data")) {
				String datakey = d.getAttributes().getNamedItem("key").getNodeValue();
				String datavalue = d.getTextContent();
				
				String constraintType = linkConstraints.get(datakey);
				if (constraintType.equals("mapping")) {
					String ms = datavalue.substring(1, datavalue.length() - 1);
					if (ms.length() > 0)
						mappings = datavalue.substring(1, datavalue.length() - 1).split(", ");
				} else
					link.add((T) createConstraint(constraintType, datavalue, link, occupyWithoutMapping));
			}
			if (!occupyWithoutMapping && mappings != null && vNets != null) {
				for (String m : mappings) {
					String mappingId = "n" + getValue(m, "id");
					VirtualLink vLink = vLinks.get(mappingId);
					if (vLink != null)
						Utils.occupyResources(vLink.get(), (Collection<AbstractResource>) link.get());
				}
			}
			
			net.addEdge(link, source, target);
		}
		
		return net;
	}

	private static AbstractConstraint createConstraint(String constraintType, String datavalue,
			vnreal.network.NetworkEntity<? extends AbstractConstraint> owner, boolean occupyWithoutMapping) {
		
		AbstractConstraint result = null;
		
		switch (constraintType) {
		
		
		case "FreeSlotsResource":
			//classname:FreeSlotsResource_label:label1_slots:4_occupiedSlots:1
			String label = getValue(datavalue, "label");
			int slots = Integer.parseInt(getValue(datavalue, "slots"));
//			int occupiedSlots = Integer.parseInt(getValue(datavalue, "occupiedSlots"));
			FreeSlotsResource slotsRes = new FreeSlotsResource(label, slots, (vnreal.network.Node<?>) owner);
			result = slotsRes;
		break;
		
		case "CapacityResource":
			//classname:CapacityResource_capacity:76.0_occupiedCapacity:50.0
			double capacity = Double.parseDouble(getValue(datavalue, "capacity"));
			double occupied = Double.parseDouble(getValue(datavalue, "occupiedCapacity"));
			CapacityResource capRes = new CapacityResource(capacity, (vnreal.network.Node<?>) owner);
			if (occupyWithoutMapping)
				capRes.occupiedCapacity = occupied;
			result = capRes;
			break;
			
		case "BandwidthResource":
			//classname:BandwidthResource_bandwidth:88.0_occupiedBandwidth:50.0
			double bw = Double.parseDouble(getValue(datavalue, "bandwidth"));
			double occbw = Double.parseDouble(getValue(datavalue, "occupiedBandwidth"));
			BandwidthResource bwres = new BandwidthResource(bw, (Link<?>) owner);
			if (occupyWithoutMapping)
				bwres.occupiedBandwidth = occbw;
			result = bwres;
			break;
			
		case "BackupResource":
			//classname:BackupResource_reservedCapacity:CapacityDemand_reservedCapacity.demand:20.0_backupSharingFactor:1
			int backupSharingFactor = Integer.parseInt(getValue(datavalue, "backupSharingFactor"));
			BackupResource backres = new BackupResource(owner, backupSharingFactor);
			String rescap = getValue(datavalue, "reservedCapacity");
			if (occupyWithoutMapping && !rescap.equals("null")) {
				if (rescap.equals("CapacityDemand")) {
					Double reserved = Double.parseDouble(getValue(datavalue, "reservedCapacity.demand"));
					backres.reservedCapacity = new CapacityDemand(reserved, null);
				} else if (rescap.equals("BandwidthDemand")) {
					Double reserved = Double.parseDouble(getValue(datavalue, "reservedCapacity.demandedBandwidth"));
					backres.reservedCapacity = new BandwidthDemand(reserved, null);
				}
					
			}
			result = backres;
			break;
			
		
		case "FreeSlotsDemand":
			//classname:FreeSlotsDemand_demandedLabel:label0_VNFType:label0-type0_isBackup:false
			String demandedLabel = getValue(datavalue, "demandedLabel");
			String VNFType = getValue(datavalue, "VNFType");
			result = new FreeSlotsDemand(demandedLabel, VNFType, owner, null);
			break;
			
		case "CapacityDemand":
			//classname:CapacityDemand_demand:50.0
			double capdemand = Double.parseDouble(getValue(datavalue, "demand"));
			result = new CapacityDemand(capdemand, owner);
			break;
			
		case "BandwidthDemand":
			//classname:BandwidthDemand_demandedBandwidth:24.0
			double bwdemand = Double.parseDouble(getValue(datavalue, "demandedBandwidth"));
			result = new BandwidthDemand(bwdemand, (Link<?>) owner);
			break;

			
		default:
			throw new AssertionError();
		}
		
		return result;
	}
	
	static String getValue(String input, String key) {
		for (String s : input.split("_")) {
			String[] kv = s.split(":");
			if (kv[0].equals(key))
				return kv[1];
		}
		return null;
	}

	static LinkedList<Node> getChildrenElements(Node current, String type) {
		LinkedList<Node> result = new LinkedList<Node>();
		NodeList children = current.getChildNodes();
		for (int i = 0; i < children.getLength(); ++i) {
			Node c = children.item(i);
			if (c.getNodeType() == Node.ELEMENT_NODE && c.getNodeName().equals(type))
				result.add(c);
		}
		return result;
	}
}

