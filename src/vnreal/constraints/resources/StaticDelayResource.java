package vnreal.constraints.resources;

import vnreal.algorithms.utils.SubgraphBasicVN.Utils;
import vnreal.constraints.AbstractConstraint;
import vnreal.constraints.ILinkConstraint;
import vnreal.constraints.demands.AbstractDemand;
import vnreal.constraints.demands.CpuDemand;
import vnreal.constraints.demands.DemandVisitorAdapter;
import vnreal.mapping.Mapping;
import vnreal.network.NetworkEntity;

public class StaticDelayResource extends AbstractResource implements ILinkConstraint {

	public final double delayMS;

	public StaticDelayResource(NetworkEntity<? extends AbstractConstraint> owner, double delayMS) {
		super(owner);
		this.delayMS = delayMS;
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
			public boolean visit(CpuDemand dem) {
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
			public boolean visit(CpuDemand dem) {
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

		StaticDelayResource clone = null;
			clone = new StaticDelayResource(owner, delayMS);
		
		return clone;
	}

}
