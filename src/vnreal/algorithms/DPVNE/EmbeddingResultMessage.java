package vnreal.algorithms.DPVNE;

import java.util.LinkedList;

/**
 * Message that notifies about a positive/negative result.
 * Might also include (as all other message types as well)
 * some additional resource updates. 
 */
public class EmbeddingResultMessage extends Message {
	
	public DelegateRequestMessage thisIsAnAnswerTo;
	private final LinkedList<UpdateEntry> embeddingResult;

	public EmbeddingResultMessage(
			ClusterHead source,
			DelegateRequestMessage thisIsAnAnswerTo,
			LinkedList<UpdateEntry> embeddingResult) {
		
		super(source);
		this.thisIsAnAnswerTo = thisIsAnAnswerTo;
		this.embeddingResult = embeddingResult;
	}
	
	public LinkedList<UpdateEntry> getEmbeddingResult() {
		return embeddingResult;
	}

	public boolean succeded() {
		return (embeddingResult != null);
	}

	@Override
	public void run() {
		thisIsAnAnswerTo.setReceivedAnswer(this);
	}
	
	@Override
	public String toString() {
		return onThisClusterHead + ":  EmbeddingResult (" + getSource() + "->" + onThisClusterHead + ")";
	}
	
}
