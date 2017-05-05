package vnreal.algorithms.DPVNE;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.virtual.VirtualNetwork;

public class MetricsCounter {
	
	public int numberOfMessages = 0;
	public Map<ClusterHead, Integer> numberOfMessagesSentPerNode = null;
	public Map<ClusterHead, Integer> numberOfMessagesReceivedPerNode = null;
//	public HashMap<LinkedList<SubstrateLink>, Integer> pathsUsedForSendingMessages = null;
	public Map<ClusterHead, List<VirtualNetwork>> numberOfVNRsSuccessfullyEmbeddedPerPartition = null;
	public Map<ClusterHead, List<VirtualNetwork>> numberOfVNRsAttemptsEmbeddedPerPartition = null;
	
	public List<BandwidthRatio> successfullyBandwidthRatio = null;
	public List<BandwidthRatio> attemptsBandwidthRatio = null;
	
	/**
	 * This class counts the number of send messages

	 */
	public MetricsCounter() {
		numberOfMessages = 0;
		numberOfMessagesSentPerNode = new HashMap<ClusterHead, Integer>();
		numberOfMessagesReceivedPerNode = new HashMap<ClusterHead, Integer>();
//		pathsUsedForSendingMessages = new HashMap<LinkedList<SubstrateLink>, Integer>();
		numberOfVNRsSuccessfullyEmbeddedPerPartition = new HashMap<ClusterHead, List<VirtualNetwork>>();
		numberOfVNRsAttemptsEmbeddedPerPartition = new HashMap<ClusterHead, List<VirtualNetwork>>();
		
		successfullyBandwidthRatio = new LinkedList<BandwidthRatio>();
		attemptsBandwidthRatio = new LinkedList<BandwidthRatio>();
	}
	
	public void sent(ClusterHead from, ClusterHead to, Message msg) {
		if (from.isMainNode || to.isMainNode) {
			return;
		}
		
		numberOfMessages++;
		
//		LinkedList<SubstrateLink> path =
//			Utils.findShortestPath(
//					sNetwork,
//					nodemap.get(from.getClusterHeadNode().getName()),
//					nodemap.get(to.getClusterHeadNode().getName()),
//					new NoTransformer());
//		
//		LinkedList<SubstrateLink> entry = path;
//		for (LinkedList<SubstrateLink> l : pathsUsedForSendingMessages.keySet()) {
//			if (l.size() != path.size()) {
//				continue;
//			}
//			Iterator<SubstrateLink> i1 = l.iterator();
//			Iterator<SubstrateLink> i2 = path.iterator();
//			while (i1.hasNext() & i2.hasNext()) {
//				if (!i1.next().getName().equals(i2.next().getName())) {
//					continue;
//				}
//			}
//			
//			entry = l;
//		}
//		
//		Integer ix = pathsUsedForSendingMessages.get(entry);
//		if (ix == null) {
//			ix = new Integer(0);
//		}
//		ix++;
//		pathsUsedForSendingMessages.put(entry, ix);
		
		Integer i = numberOfMessagesSentPerNode.get(from);
		if (i == null) {
			i = new Integer(0);
		}
		i++;
		numberOfMessagesSentPerNode.put(from, i);
	}
	
	public void received(ClusterHead from, ClusterHead to, Message msg) {
		if (from.isMainNode || to.isMainNode) {
			return;
		}
		
		Integer i = numberOfMessagesReceivedPerNode.get(to);
		if (i == null) {
			i = new Integer(0);
		}
		i++;
		numberOfMessagesReceivedPerNode.put(to, i);
	}
	
	public void successfullyEmbedded(ClusterHead head, VirtualNetwork vn, double bandwidthratio) {
		List<VirtualNetwork> vns = numberOfVNRsSuccessfullyEmbeddedPerPartition.get(head);
		if (vns == null) {
			vns = new LinkedList<VirtualNetwork>();
			numberOfVNRsSuccessfullyEmbeddedPerPartition.put(head, vns);
		}
		vns.add(vn);
		
		successfullyBandwidthRatio.add(new BandwidthRatio(head.cluster, vn, bandwidthratio));
	}
	
	public void attemptEmbedded(ClusterHead head, VirtualNetwork vn, double bandwidthratio) {
		List<VirtualNetwork> vns = numberOfVNRsAttemptsEmbeddedPerPartition.get(head);
		if (vns == null) {
			vns = new LinkedList<VirtualNetwork>();
			numberOfVNRsAttemptsEmbeddedPerPartition.put(head, vns);
		}
		vns.add(vn);
		
		attemptsBandwidthRatio.add(new BandwidthRatio(head.cluster, vn, bandwidthratio));
	}
	
	public static class BandwidthRatio {
		public final SubstrateNetwork sn;
		public final VirtualNetwork vn;
		public final double ratio;
		
		public BandwidthRatio(SubstrateNetwork sn, VirtualNetwork vn, double ratio) {
			this.sn = sn;
			this.vn = vn;
			this.ratio = ratio;
		}
	}
	
}
