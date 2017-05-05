package vnreal.algorithms.DPVNE;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Random;

import vnreal.io.MetisIO;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;

public class MetisPartitioningAlgorithm implements PartitioningAlgorithm {
	
	
//	private static HashMap<SubstrateNetwork, Collection<Partition>> cache = new HashMap<SubstrateNetwork, Collection<Partition>>();
    
    public static final String METIS_COMMAND = "/usr/bin/gpmetis"; // "/opt/metis/bin/gpmetis";
	
	public final String exportFileName;
	public final String importPartitionsFileName;
	final int parts;
	
	public MetisPartitioningAlgorithm(int parts) {
		this.parts = parts;
		
		//extension for Disributed Tests and parallel tests
		Random rand = new Random();
		int randInt = rand.nextInt();
		
		this.exportFileName = "/tmp/metisexport"+Thread.currentThread().getId()+randInt+".tmp";
		this.importPartitionsFileName = "/tmp/metisexport"+Thread.currentThread().getId()+randInt+".tmp.part." + parts;
	}

	
	
	@Override
	public Collection<Partition> getPartitions(SubstrateNetwork cluster) {
		if (parts <= 1) {
			return null;
		}
		if (cluster.getEdgeCount() == 0) {
			return null;
		}

		LinkedList<Partition> result = null; //cache.get(cluster);
//		if (result != null) {
//			return result;
//		}
		
		SubstrateNode[] metisIDs = MetisIO.exportSubstrate(cluster, exportFileName);
		
		try {
			Process p = Runtime.getRuntime().exec(new String[] {
					METIS_COMMAND,
					"-contig",
					exportFileName,
					Integer.toString(parts)});

			while (true) {
				try {
					int v = p.waitFor();
					
					if(v != 0) {
						byte[] b = new byte[10];
						while(p.getErrorStream().read(b) != -1)
							for ( byte c : b ) 
								  System.out.print( (char)(c & 0xff) );
						
						System.out.println();
						throw new Error();
					}
					break;
				} catch (InterruptedException e) {
				}
			}
		} catch (IOException e) {
			throw new Error(e);
		}
		
		result = MetisIO.importPartitions(metisIDs, cluster, importPartitionsFileName);
		
		new File(exportFileName).delete();
		new File(importPartitionsFileName).delete();
		
//		cache.put(cluster, result);
//		System.out.println("cache size: "+cache.size());
		return result;
	}

}
