package vnreal.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;

import vnreal.algorithms.DPVNE.Partition;
import vnreal.algorithms.DPVNE.PartitionConnection;
import vnreal.constraints.resources.AbstractResource;
import vnreal.constraints.resources.BandwidthResource;
import vnreal.constraints.resources.CpuResource;
import vnreal.constraints.resources.IdResource;
import vnreal.constraints.resources.SelectionResource;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;

public class MetisIO {
	
	private MetisIO() {
	}
	
	/**
	 * Export a SubstrateNetwork to a METIS GRAPH File
	 * 
	 * @param 	substNetwork
	 * @throws IOException
	 * @return {(metisID, substrate node ID)}
	 */
	public static SubstrateNode[] exportSubstrate(SubstrateNetwork substNetwork, String fileName){
		SubstrateNode[] nodes = substNetwork.getVertices().toArray(new SubstrateNode[0]);
		
		HashMap<SubstrateNode, Integer> map = new HashMap<SubstrateNode, Integer>();
		
		//assumption: the graph has both edge and vertex weights
		int FMT = 11;

		double nodeWeight;
		double linkWeight;
		SubstrateNode neighborNode;
		SubstrateLink substLink;

		//nodeID's are not consecutive
		for(int i=0; i<nodes.length; i++){
			map.put(nodes[i], i+1);
		}

		//the first line lists the number of nodes and the number of edges
		try {
			BufferedWriter metisFile = new BufferedWriter(new FileWriter(fileName));

			metisFile.write(substNetwork.getVertices().size() + " ");
			metisFile.write(substNetwork.getEdgeCount()/2 + " ");
			metisFile.write(FMT + "");


			//iterate over all the nodes
			for (int metisID = 0; metisID < nodes.length; ++metisID) {
				nodeWeight = getNodeWeight(nodes[metisID]);

				//write in a file that node has nodeWeight
				metisFile.newLine();
				metisFile.write((int)nodeWeight +"");

				Iterator<SubstrateLink> itrLink = substNetwork.getOutEdges(nodes[metisID]).iterator();			
				// iterate over all outgoing edges from a node
				while(itrLink.hasNext()){

					substLink = itrLink.next();
					neighborNode = substNetwork.getOpposite(nodes[metisID], substLink);

					// only if backward-edge exist
					if(substNetwork.isSuccessor(neighborNode, nodes[metisID])){					
						linkWeight = getLinkWeight(substLink);

						//is connected to node on an edge with weight
						metisFile.write(" " + (map.get(neighborNode)) + " " + (int)linkWeight);
					}else{
						System.err.println("Error: Backward edge does not exist!");
					}
				}
			}

			metisFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return nodes;
	}
	
	
	
	/**
	 * @param 	substNode
	 * 
	 * @return 	NodeWeight, if this Node has CpuResource,
	 * 			0.0 otherwise.
	 */
	private static double getNodeWeight(SubstrateNode substNode){
		Iterator<AbstractResource> itr = substNode.get().listIterator();
		AbstractResource abstRes;
		
		while(itr.hasNext()){
			abstRes = itr.next();
			
			if(abstRes instanceof CpuResource)
				return ((CpuResource)abstRes).getCycles();
		}
		return 0.0;
	}
	
	
	
	/**
	 * @param	substLink
	 * 
	 * @return 	bandwidth, if this link has bandwidthResource,
	 * 			0.0 otherwise.
	 */
	private static double getLinkWeight(SubstrateLink substLink){
		ListIterator<AbstractResource> itr = substLink.get().listIterator();
		AbstractResource abstRes;
		
		while (itr.hasNext()){
			abstRes = itr.next();
			
			if (abstRes instanceof BandwidthResource)
				return ((BandwidthResource)abstRes).getBandwidth();
		}
		return 0.0;
	}
	
	
	
	/**
	 * Import a METIS GRAPH File into a SubstrateNetwork
	 * 
	 * @param 	substNetwork
	 * 
	 */
	public static SubstrateNetwork importSubstrate(String fileName){
		SubstrateNetwork substNetwork = new SubstrateNetwork(false);
		int rowNumber = 0;
		int nodeNumber = 0;
	    String s;
	    String[] line;
	    int[][] networkMatrix = null;
	    HashMap<Integer, SubstrateNode> map = new HashMap<Integer, SubstrateNode>();
	    
		//read METIS GRAPH File
		try {
		      FileReader fr = new FileReader(fileName);
		      BufferedReader br = new BufferedReader(fr);

		      
		      while((s = br.readLine()) != null){
		    	  line = s.split(" ");

		    	  //create networkMatrix
		    	  if(rowNumber == 0){
		    		  nodeNumber = Integer.parseInt(line[0]);
		    		  networkMatrix = new int[nodeNumber+1][];
		    	  }
		    	  
		    	  //create networkMatrix rows
	    		  networkMatrix[rowNumber] = new int[line.length];
	    		  
		    	  //write all nodes and edges into the networkMatrix
		    	  for(int i=0; i<line.length; i++){
		    		  networkMatrix[rowNumber][i] = Integer.parseInt(line[i]);
		    	  }
		    	  
		    	  //create nodes
		    	  if(rowNumber>0){
	    			  createSubstNodes(substNetwork, networkMatrix[rowNumber][0], rowNumber, map, substNetwork);
	    		  }
		    	  rowNumber=rowNumber+1;
		      }
		    	  fr.close();
		    }
		    catch(Exception e) {
		    	e.printStackTrace();
		    }
		
		//create edges
		for(int m=1; m<networkMatrix.length; m++){
			for(int n=1; n<networkMatrix[m].length; n=n+2){
				createSubstLinks(substNetwork, map.get(m), map.get(networkMatrix[m][n]), networkMatrix[m][n+1]);
			}
		}
		return substNetwork;
	}
	
	
	
	/**
	 * Create and add new node
	 * 
	 * @param	substNetwork
	 * @param	nodeNumber
	 *          
	 * @return 	substrateNetwork with nodes
	 */
	private static void createSubstNodes(SubstrateNetwork substNetwork, double cpuResource, int nodeID, HashMap<Integer, SubstrateNode> result, SubstrateNetwork sNetwork){
		IdResource idRes;
		CpuResource cpuRes;
		SelectionResource selRes;
		
		SubstrateNode subsNode = new SubstrateNode();
		idRes = new IdResource(subsNode, sNetwork);
		idRes.setId(Integer.toString(1));
		subsNode.add(idRes);
		cpuRes = new CpuResource(subsNode);
		cpuRes.setCycles(cpuResource);
		subsNode.add(cpuRes);
		selRes = new SelectionResource(subsNode);
		subsNode.add(selRes);
		substNetwork.addVertex(subsNode);
		
		
		
		result.put(nodeID, subsNode);		
	}
	
	
	
	/**
	 * Add new edge between sourceNode and destNode
	 * 
	 * @param	substNetwork
	 * @param	sourceNode
	 * @param	destNode
	 * @param	bwResource
	 *          
	 * @return 	substrateNetwork with nodes end new edge
	 */
	private static void createSubstLinks(SubstrateNetwork substNetwork, SubstrateNode sourceNode, SubstrateNode destNode, double bwResource){
		BandwidthResource bwRes;
		SubstrateLink subsLink = new SubstrateLink();
		bwRes = new BandwidthResource(subsLink);
		bwRes.setBandwidth(bwResource);
		subsLink.add(bwRes);
		substNetwork.addEdge(subsLink, sourceNode, destNode);
	}
	
	
	public static LinkedList<Partition> importPartitions(
			SubstrateNode[] metisIDs,
			SubstrateNetwork origNetwork,
			String fileName) {
		
		Map<Integer, Partition> result = new HashMap<Integer, Partition>();
	    HashMap<SubstrateNode, Integer> origMap = new HashMap<SubstrateNode, Integer>();
	    HashMap<Integer, SubstrateNode> map = new HashMap<Integer, SubstrateNode>();
		Iterator<SubstrateNode> nodeItr = origNetwork.getVertices().iterator();

		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(fileName));

			String line;
			int pos = 0;
			
			//create Partitions
			while((line = br.readLine()) != null) {
				Integer partNo = Integer.parseInt(line);
				//get current Partition
				Partition part = result.get(partNo);
				if (part == null) {
					part = new Partition();
					result.put(partNo, part);
				}
				SubstrateNetwork sNetwork = part.getSubstrateNetwork();
				
				//add cloneNode to Partition and map originalNode and cloneNode
				SubstrateNode node = nodeItr.next();
				origMap.put(node, pos);
				SubstrateNode cloneNode = node.getCopy(true);
				map.put(pos, cloneNode);
				sNetwork.addVertex(cloneNode);
				
				for (SubstrateLink l : origNetwork.getInEdges(node)) {
					if (origMap.containsKey(origNetwork.getOpposite(node, l))){
						SubstrateLink newLink = l.getCopy(true);
						int mapNo = -1;
						SubstrateNode oppositeNode = origNetwork.getOpposite(node, l);
						mapNo = origMap.get(oppositeNode);
						//add inEdges inside a Partition
						if (sNetwork.containsVertex(map.get(mapNo))) {
							sNetwork.addEdge(newLink, map.get(mapNo), cloneNode);
						}else{
							//add PartitionConnections
							Iterator<Partition> partItr = result.values().iterator();
							while(partItr.hasNext()){
								Partition neighborPart = partItr.next();
								
								if(!neighborPart.equals(part)){ //current Partition unequal neighborPartition
									if(neighborPart.getSubstrateNetwork().containsVertex(map.get(mapNo))){ //oppositeNode is in neighborPartition
										//add inPartitionConnection
										part.getDirectedEdgesToNeighbors().add(new PartitionConnection(cloneNode.getName(), part, oppositeNode.getName(), neighborPart, newLink));
										//create outPartitionConnection
										SubstrateLink backwardLink = origNetwork.findEdge(node, origNetwork.getOpposite(node, l));
										//add outPartitionConnection
										neighborPart.getDirectedEdgesToNeighbors().add(new PartitionConnection(oppositeNode.getName(), neighborPart, cloneNode.getName(), part, backwardLink.getCopy(true)));
									}
								}
							}
						}
					}
					
				}
				for (SubstrateLink l : origNetwork.getOutEdges(node)) {
					if (origMap.containsKey(origNetwork.getOpposite(node, l))){
						int mapNo = -1;
						mapNo = origMap.get(origNetwork.getOpposite(node, l));
						//add inEdges inside a Partition
						if (sNetwork.containsVertex(map.get(mapNo))) {
							SubstrateLink newLink = l.getCopy(true);
							sNetwork.addEdge(newLink, cloneNode, map.get(mapNo));
						}
					}
				}
				
				++pos;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return new LinkedList<Partition>(result.values());
	}

}
