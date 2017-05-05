package vnreal.constraints.resources;

import java.util.ArrayList;

import vnreal.AdditionalConstructParameter;
import vnreal.ExchangeParameter;
import vnreal.algorithms.utils.SubgraphBasicVN.Utils;
import vnreal.constraints.AbstractConstraint;
import vnreal.constraints.INodeConstraint;
import vnreal.constraints.demands.AbstractDemand;
import vnreal.constraints.demands.DemandVisitorAdapter;
import vnreal.constraints.demands.MLSDemand;
import vnreal.mapping.Mapping;
import vnreal.network.NetworkEntity;
import vnreal.network.Node;

/**
 * This Ressource is for labeling Nodes with MLS Labels
 * Technically this is a resource and a demand in one
 * @author Fabian Kokot
 * @since 2012-05-01
 */
@AdditionalConstructParameter(
		parameterNames = {"demand", "provide", "categories"},
		parameterGetters = {"getDemand", "getProvide", "getCategories"}
		)
public class MLSResource extends AbstractResource implements INodeConstraint{

	//demanded and Provided Level
	protected int mDemand, mProvide;
	//The Categories of the Node
	protected ArrayList<String> mCategories;
	
	
	/**
	 * Creates a MLSDemand
	 * @param ne A Node
	 * @param demand Demanded level of Security
	 * @param provide Provided level of Security
	 * @param categories The categories
	 */
	public MLSResource(Node<? extends AbstractResource> ne, Integer demand, Integer provide, ArrayList<String> categories) {
		super(ne);
		this.mDemand = demand;
		this.mProvide = provide;
		this.mCategories = categories;
	}
	
	
	public MLSResource(Node<? extends AbstractResource> ne, Integer demand, Integer provide, ArrayList<String> categories, String name) {
		super(ne, name);
		this.mDemand = demand;
		this.mProvide = provide;
		this.mCategories = categories;
	}
	
	//Only some getter and setter 
	

	@ExchangeParameter
	public int getDemand() {
		return mDemand;
	}



	@ExchangeParameter
	public void setDemand(Integer demand) {
		this.mDemand = demand;
	}



	@ExchangeParameter
	public int getProvide() {
		return mProvide;
	}



	@ExchangeParameter
	public void setProvide(Integer provide) {
		this.mProvide = provide;
	}



	@SuppressWarnings("unchecked")
	@ExchangeParameter
	public ArrayList<String> getCategories() {
		return (ArrayList<String>) mCategories.clone();
	}



	@ExchangeParameter
	public void setCategories(ArrayList<String> categories) {
		this.mCategories = categories;
	}






	@Override
	protected DemandVisitorAdapter createOccupyVisitor() {
		return new DemandVisitorAdapter() {
			@Override
			public boolean visit(MLSDemand dem) {
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
			public boolean visit(MLSDemand dem) {
				if (getMapping(dem) != null) {
					//System.out.println("Called free");
					return getMapping(dem).unregister();
				} else
					return false;
			}
		};
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
	public AbstractResource getCopy(NetworkEntity<? extends AbstractConstraint> owner, boolean setOccupied) {
		@SuppressWarnings("unchecked")
		MLSResource res = new MLSResource((Node<? extends AbstractResource>)owner, mDemand, mProvide, mCategories, this.getName());
		return res;
	}

	@Override
	public String toString() {
		return Utils.toString(this);
	}
	
}
