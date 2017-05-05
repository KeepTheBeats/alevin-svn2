package vnreal.constraints.demands;

import vnreal.algorithms.utils.SubgraphBasicVN.Utils;
import vnreal.constraints.AbstractConstraint;
import vnreal.constraints.ILinkConstraint;
import vnreal.constraints.resources.AbstractResource;
import vnreal.constraints.resources.ResourceVisitorAdapter;
import vnreal.constraints.resources.StaticDelayResource;
import vnreal.network.Link;
import vnreal.network.NetworkEntity;

public class StaticMaxDelayDemand extends AbstractDemand implements ILinkConstraint {
	
	public final double maxDelayMS;
	
	public StaticMaxDelayDemand(NetworkEntity<? extends AbstractConstraint> owner, double maxDelayMS) {
		super(owner);
		this.maxDelayMS = maxDelayMS;
	}
	
	@Override
	protected ResourceVisitorAdapter createAcceptsVisitor() {
		return new ResourceVisitorAdapter() {
			@Override
			public boolean visit(StaticDelayResource res) {
				return true;
			}
		};
	}

	@Override
	protected ResourceVisitorAdapter createFulfillsVisitor() {
		return new ResourceVisitorAdapter() {
			@Override
			public boolean visit(StaticDelayResource res) {
				return (maxDelayMS < 0 || res.delayMS <= maxDelayMS);
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
	public AbstractDemand getCopy(NetworkEntity<? extends AbstractDemand> owner) {
		StaticMaxDelayDemand clone = null;
			clone = new StaticMaxDelayDemand((Link<? extends AbstractConstraint>) owner, maxDelayMS);
		
		return clone;
	}
	
}
