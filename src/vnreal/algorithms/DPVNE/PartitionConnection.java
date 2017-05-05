package vnreal.algorithms.DPVNE;

import vnreal.network.substrate.SubstrateLink;

/**
 * This class describes a Connection (Link) between to partitions of the substrate network
 * 
 * @author Michel Beck
 * @author Fabian Kokot
 *
 */
public class PartitionConnection {
	
	public final String id;
	private final String startPoint;
	private final Partition startPartition;
	private final String endPoint;
	private final Partition endPartition;
	private final SubstrateLink connection;

	
	/**
	 * 
	 * @param startPoint getName() of the StartNode
	 * @param startPartition Part of the StartNode
	 * @param endPoint getName() of the EndNode
	 * @param endPartition Part of the EndNode
	 * @param connection The connection
	 */
	public PartitionConnection(String startPoint, Partition startPartition,
			String endPoint, Partition endPartition, SubstrateLink connection) {
		super();
		this.id = startPoint+"_"+endPoint+"_"+connection.getName();
		this.startPoint = startPoint;
		this.startPartition = startPartition;
		this.endPoint = endPoint;
		this.endPartition = endPartition;
		this.connection = connection;
	}

	/**
	 * @param id ID
	 * @param startPoint getName() of the StartNode
	 * @param startPartition Part of the StartNode
	 * @param endPoint getName() of the EndNode
	 * @param endPartition Part of the EndNode
	 * @param connection The connection
	 */
	public PartitionConnection(String id, String startPoint, Partition startPartition,
			String endPoint, Partition endPartition, SubstrateLink connection) {
		super();
		this.id = id;
		this.startPoint = startPoint;
		this.startPartition = startPartition;
		this.endPoint = endPoint;
		this.endPartition = endPartition;
		this.connection = connection;
	}

	public String getStartPoint() {
		return startPoint;
	}


	public Partition getStartPartition() {
		return startPartition;
	}


	public String getEndPoint() {
		return endPoint;
	}


	public Partition getEndPartition() {
		return endPartition;
	}


	public SubstrateLink getConnection() {
		return connection;
	}

	
	public PartitionConnection getCopy() {
		PartitionConnection clone = new PartitionConnection(this.id, startPoint, startPartition, 
				endPoint, endPartition, getConnection().getCopy(true));
		return clone;
	}

	
}
