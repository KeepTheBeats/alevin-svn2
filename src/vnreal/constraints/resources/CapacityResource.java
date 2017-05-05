/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2010-2011, The VNREAL Project Team.
 * 
 * This work has been funded by the European FP7
 * Network of Excellence "Euro-NF" (grant agreement no. 216366)
 * through the Specific Joint Developments and Experiments Project
 * "Virtual Network Resource Embedding Algorithms" (VNREAL). 
 *
 * The VNREAL Project Team consists of members from:
 * - University of Wuerzburg, Germany
 * - Universitat Politecnica de Catalunya, Spain
 * - University of Passau, Germany
 * See the file AUTHORS for details and contact information.
 * 
 * This file is part of ALEVIN (ALgorithms for Embedding VIrtual Networks).
 *
 * ALEVIN is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License Version 3 or later
 * (the "GPL"), or the GNU Lesser General Public License Version 3 or later
 * (the "LGPL") as published by the Free Software Foundation.
 *
 * ALEVIN is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * or the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License and
 * GNU Lesser General Public License along with ALEVIN; see the file
 * COPYING. If not, see <http://www.gnu.org/licenses/>.
 *
 * ***** END LICENSE BLOCK ***** */
package vnreal.constraints.resources;

import vnreal.ExchangeParameter;
import vnreal.algorithms.utils.SubgraphBasicVN.Utils;
import vnreal.constraints.AbstractConstraint;
import vnreal.constraints.INodeConstraint;
import vnreal.constraints.demands.AbstractDemand;
import vnreal.constraints.demands.CapacityDemand;
import vnreal.constraints.demands.DemandVisitorAdapter;
import vnreal.mapping.Mapping;
import vnreal.network.NetworkEntity;
import vnreal.network.Node;


public final class CapacityResource extends AbstractResource implements INodeConstraint {
	public double capacity;
	public double occupiedCapacity = 0.0d;

	public CapacityResource(double capacity, Node<? extends AbstractConstraint> owner) {
		super(owner);
		this.capacity = capacity;
	}
	
	public CapacityResource(double capacity, Node<? extends AbstractConstraint> owner, String name) {
		super(owner, name);
		this.capacity = capacity;
	}

	@ExchangeParameter
	public void setCycles(Double capacity) {
		this.capacity = capacity;
	}

	@ExchangeParameter
	public Double getCapacity() {
		return this.capacity;
	}

	/*
	 * Method for the distributed algorithm
	 */
	public void setOccupiedCapacity(Double occupiedCapacity) {
		this.occupiedCapacity = occupiedCapacity;
	}

	public Double getAvailableCapacity() {
		return capacity - occupiedCapacity;
	}
	
	public double getOccupiedCapacity() {
		return occupiedCapacity;
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
			public boolean visit(CapacityDemand dem) {
				if (fulfills(dem)) {
					occupiedCapacity += dem.getDemandedCapacity();
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
			public boolean visit(CapacityDemand dem) {
				if (getMapping(dem) != null) {
					occupiedCapacity -= dem.getDemandedCapacity();
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

		CapacityResource clone = new CapacityResource(capacity, (Node<? extends AbstractConstraint>) owner, this.getName());
		
		if (setOccupied)
			clone.occupiedCapacity = occupiedCapacity;

		return clone;
	}
}
