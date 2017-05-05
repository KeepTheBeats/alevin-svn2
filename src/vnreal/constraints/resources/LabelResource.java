package vnreal.constraints.resources;

import java.util.LinkedList;

import vnreal.algorithms.utils.SubgraphBasicVN.Utils;
import vnreal.constraints.AbstractConstraint;
import vnreal.constraints.INodeConstraint;
import vnreal.constraints.demands.AbstractDemand;
import vnreal.constraints.demands.DemandVisitorAdapter;
import vnreal.constraints.demands.LabelDemand;
import vnreal.mapping.Mapping;
import vnreal.network.NetworkEntity;

public class LabelResource extends AbstractResource implements INodeConstraint {

	public final LinkedList<String> labels;

	public LabelResource(NetworkEntity<? extends AbstractConstraint> owner, LinkedList<String> labels) {
		super(owner);
		this.labels = labels;
	}
	
	@Override
	public boolean accepts(AbstractDemand dem) {
		return dem.getAcceptsVisitor().visit(this);
	}

	@Override
	public boolean fulfills(AbstractDemand dem) {
		return dem.getFulfillsVisitor().visit(this);
	}

	@Override
	protected DemandVisitorAdapter createOccupyVisitor() {
		return new DemandVisitorAdapter() {
			@Override
			public boolean visit(LabelDemand dem) {
				if (fulfills(dem)) {
					new Mapping(dem, getThis());
					return true;
				} else
					return false;
			}
		};
	}

	@Override
	protected DemandVisitorAdapter createFreeVisitor() {
		return new DemandVisitorAdapter() {
			@Override
			public boolean visit(LabelDemand dem) {
				if (getMapping(dem) != null) {
					return getMapping(dem).unregister();
				} else
					return false;
			}
		};
	}

	@Override
	public String toString() {
		return Utils.toString(this);
	}

	@Override
	public AbstractResource getCopy(
			NetworkEntity<? extends AbstractConstraint> owner, boolean setOccupied) {

		return new LabelResource(owner, this.labels);
	}

}
