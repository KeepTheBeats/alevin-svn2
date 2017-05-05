package vnreal.constraints.demands;

import java.util.LinkedList;

import vnreal.algorithms.utils.SubgraphBasicVN.Utils;
import vnreal.constraints.AbstractConstraint;
import vnreal.constraints.INodeConstraint;
import vnreal.constraints.resources.AbstractResource;
import vnreal.constraints.resources.LabelResource;
import vnreal.constraints.resources.ResourceVisitorAdapter;
import vnreal.network.NetworkEntity;

public class LabelDemand extends AbstractDemand implements INodeConstraint {
	
	public final LinkedList<String> labels;
	
	public LabelDemand(NetworkEntity<? extends AbstractConstraint> owner, String label) {
		super(owner);
		this.labels = new LinkedList<String>();
		this.labels.add(label);
	}
	
	public LabelDemand(NetworkEntity<? extends AbstractConstraint> owner, LinkedList<String> labels) {
		super(owner);
		this.labels = labels;
	}
	
	@Override
	protected ResourceVisitorAdapter createAcceptsVisitor() {
		return new ResourceVisitorAdapter() {
			@Override
			public boolean visit(LabelResource res) {
				return true;
			}
		};
	}

	@Override
	protected ResourceVisitorAdapter createFulfillsVisitor() {
		return new ResourceVisitorAdapter() {
			@Override
			public boolean visit(LabelResource res) {
				return (LabelDemand.this.labels == null ||
						res.labels.containsAll(LabelDemand.this.labels));
			}
		};
	}

	@Override
	public boolean occupy(AbstractResource res) {
		return res.getOccupyVisitor().visit(this);
	}

	@Override
	public boolean free(AbstractResource res) {
		return res.getFreeVisitor().visit(this);
	}

	@Override
	public String toString() {
		return Utils.toString(this);
	}

	@Override
	public LabelDemand getCopy(NetworkEntity<? extends AbstractDemand> owner) {
		return new LabelDemand((NetworkEntity<? extends AbstractConstraint>) owner, this.labels);
	}
	
}
