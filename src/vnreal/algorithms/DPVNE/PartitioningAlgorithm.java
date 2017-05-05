package vnreal.algorithms.DPVNE;

import java.util.Collection;

import vnreal.network.substrate.SubstrateNetwork;

public interface PartitioningAlgorithm {
	
	public Collection<Partition> getPartitions(SubstrateNetwork cluster);
	
}
