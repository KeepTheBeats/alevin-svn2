package vnreal.algorithms.DPVNE;

import java.util.Collection;
import java.util.LinkedList;

import vnreal.network.substrate.SubstrateNetwork;

public class Partition {
	
	private SubstrateNetwork sNetwork;
	private ClusterHead clusterHead = null;
	private Collection<PartitionConnection> directedEdgesToNeighbors;

	public Partition() {
		this(new SubstrateNetwork(false));
	}
	
	public Partition(SubstrateNetwork sNetwork) {
		this.sNetwork = sNetwork;
		directedEdgesToNeighbors = new LinkedList<PartitionConnection>();
	}
	
	public SubstrateNetwork getSubstrateNetwork() {
		return sNetwork;
	}

	public void setClusterHead(ClusterHead clusterHead) {
		this.clusterHead = clusterHead;
	}


	public void setDirectedEdgesToNeighbors(
			Collection<PartitionConnection> directedEdgesToNeighbors) {
		this.directedEdgesToNeighbors = directedEdgesToNeighbors;
	}
	
	public ClusterHead getClusterHead() {
		return clusterHead;
	}

	
	public Collection<PartitionConnection> getDirectedEdgesToNeighbors() {
		return directedEdgesToNeighbors;
	}

}
