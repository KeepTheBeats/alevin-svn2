package vnreal.algorithms.DPVNE;

import java.util.LinkedList;
import java.util.Queue;

import vnreal.network.virtual.VirtualNetwork;


public class StopMessage extends DelegateRequestMessage {

	public StopMessage(
			ClusterHead source,
			VirtualNetwork vNetwork,
			Queue<ClusterHead> stoppedDelegationNodes) {
		
		super(source, vNetwork, stoppedDelegationNodes);
	}
	
	@Override
	public void run() {
		assert (onThisClusterHead.isDelegationNode());
		
		onThisClusterHead.isActive = false;
		LinkedList<UpdateEntry> embedResult = delegate();
		
		if (embedResult == null) {
			embedResult = embedItHere();
		}


		if (DistributedMappingAlgorithm.DEBUG) {
			System.out.println(" Stop " + this + ": success: " + (embedResult != null) + ". Notifying sender ...");
		}

		EmbeddingResultMessage result = new EmbeddingResultMessage(onThisClusterHead, this, embedResult);
		onThisClusterHead.send(result, getSource());
	}
	
	@Override
	public String toString() {
		return onThisClusterHead + "(vNet:" + vNetwork.getName() + "):  Stop (" + getSource() + "->" + onThisClusterHead + ")";
	}
	
}
