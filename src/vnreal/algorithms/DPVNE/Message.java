package vnreal.algorithms.DPVNE;

import java.util.Collection;
import java.util.LinkedList;

public abstract class Message {
	
	final ClusterHead source;
	
	ClusterHead onThisClusterHead = null;
	private Collection<UpdateEntry> updates = null;
//	public Thread thread = null;
	
	public Message(ClusterHead source) {
		this.source = source;
	}
	
	public ClusterHead getSource() {
		return source;
	}

	public void addUpdates(Collection<UpdateEntry> newUpdates) {
		if (updates == null) {
			updates = new LinkedList<UpdateEntry>();
		}
		updates.addAll(newUpdates);
	}
	
	public Collection<UpdateEntry> getUpdates() {
		return updates;
	}
	
	/**
	 * This method executes the updates on the chosen Clusterhead and starts a thread
	 * @param onThisClusterHead The Chosen Clusterhead
	 */
	public void exec(ClusterHead onThisClusterHead) {
//		if (thread != null && thread.isAlive()) {
//			throw new Error();
//		}

		this.onThisClusterHead = onThisClusterHead;

		if (!onThisClusterHead.isMainNode) {
			onThisClusterHead.applyUpdates(updates);
			onThisClusterHead.updateLocalState(getUpdates());
		}

		this.run();
	}
	
	public abstract void run();
	
	public abstract String toString();
	
}
