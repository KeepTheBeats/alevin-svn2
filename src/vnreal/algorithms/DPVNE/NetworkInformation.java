package vnreal.algorithms.DPVNE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import vnreal.constraints.resources.AbstractResource;
import vnreal.constraints.resources.BandwidthResource;
import vnreal.constraints.resources.CpuResource;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;

/**
 * This class holds statistical data for a virtual Network
 * @author Fabian Kokot
 *
 */
public class NetworkInformation {
	private double sumCPU, sumBandwidth;
	private int nodeCount, linkCount;
	private Collection<PartitionConnection> conns;
    //This list contains the entitienames
	private List<String> entitieNames;
	public double getSumCPU() {
		return sumCPU;
	}
	public void setSumCPU(double sumCPU) {
		this.sumCPU = sumCPU;
	}
	public double getSumBandwidth() {
		return sumBandwidth;
	}
	public void setSumBandwidth(double sumBandwidth) {
		this.sumBandwidth = sumBandwidth;
	}
	public int getNodeCount() {
		return nodeCount;
	}
	public void setNodeCount(int nodeCount) {
		this.nodeCount = nodeCount;
	}
	public int getLinkCount() {
		return linkCount;
	}
	public void setLinkCount(int linkCount) {
		this.linkCount = linkCount;
	}
	public Collection<PartitionConnection> getConns() {
		return conns;
	}
	public void setConns(Collection<PartitionConnection> conns) {
		this.conns = conns;
	}
	public List<String> getEntitieNames() {
		return entitieNames;
	}
	public void setEntitieNames(List<String> entitieNames) {
		this.entitieNames = entitieNames;
	}
	
	
	/**
	 * 
	 * @param s Substrate Network
	 * @param conns Connection to other partitions
	 * @return Information-Object
	 */
	public NetworkInformation(SubstrateNetwork s, Collection<PartitionConnection> conns) {
		//Create the list of entitynames
		for(SubstrateNode sn : s.getVertices()) {
			getEntitieNames().add(sn.getName());
		}
		for(SubstrateLink sl : s.getEdges()) {
			getEntitieNames().add(sl.getName());
		}
		
		//Initial create statistic data
		setLinkCount(s.getEdgeCount());
		setNodeCount(s.getVertexCount());
		setConns(conns);
		
		//CPU and BW 
		double cpures = 0.0d;
		for (SubstrateNode vn : s.getVertices()) {
			for (AbstractResource r : vn) {
				if (r instanceof CpuResource) {
					double c = ((CpuResource) r).getAvailableCycles();
					cpures += c;
					break;
				}
			}
		}
		setSumCPU(cpures);
		
		double bandwidthres = 0.0d;
		for (SubstrateLink sl : s.getEdges()) {
			for (AbstractResource r : sl) {
				if (r instanceof BandwidthResource) {
					double c = ((BandwidthResource) r).getAvailableBandwidth();
					bandwidthres += c;
					break;
				}
			}
		}
		setSumBandwidth(bandwidthres);
		
	}
	
	private NetworkInformation() {};
	
	
	/**
	 * Checks wether the given entityname is conatined in the local cluster
	 * 
	 * @param name Name of the entity 
	 * @return true if it contained
	 */
	public boolean containsEntity(String name) {
		if(entitieNames.contains(name))
			return true;
		
		return false; 
	}
	
	
	/**
	 * 
	 * 
	 * @param name Name of the PC
	 * @return {@link PartitionConnection} or <code>null</code>
	 */
	public PartitionConnection getPartitionConnectionByName(String name) {
		for(PartitionConnection pc : conns) {
			if(pc.id.equals(name))
				return pc;
		}
		return null;
	}
	
	/**
	 * 
	 * @return A deep copy
	 */
	public NetworkInformation getCopy() {
		NetworkInformation clone = new NetworkInformation();
		
		clone.linkCount = linkCount;
		clone.nodeCount = nodeCount;
		clone.sumBandwidth = sumBandwidth;
		clone.sumCPU = sumCPU;
		
		ArrayList<String> ename_clone = new ArrayList<String>();
		for(String s : entitieNames) {
			ename_clone.add(s);
		}
		
		clone.entitieNames = ename_clone;
		
		
		ArrayList<PartitionConnection> conn_clone = new ArrayList<PartitionConnection>();
		for(PartitionConnection pc : conns) {
			conn_clone.add(pc.getCopy());
		}
		
		clone.conns = conn_clone;
		
		return clone;
	}
	
}
