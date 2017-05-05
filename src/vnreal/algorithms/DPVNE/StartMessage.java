package vnreal.algorithms.DPVNE;

public class StartMessage extends Message {

	public StartMessage(ClusterHead source) {
		super(source);
	}

	@Override
	public void run() {
		onThisClusterHead.isActive = true;
	}

	@Override
	public String toString() {
		return onThisClusterHead + ":  Start (" + getSource() + "->" + onThisClusterHead + ")";
	}

}
