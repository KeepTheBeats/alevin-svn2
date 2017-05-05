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
package vnreal.constraints.demands;

import vnreal.algorithms.utils.SubgraphBasicVN.Utils;
import vnreal.constraints.AbstractConstraint;
import vnreal.constraints.ILinkConstraint;
import vnreal.constraints.INodeConstraint;
import vnreal.constraints.resources.AbstractResource;
import vnreal.constraints.resources.CapacityResource;
import vnreal.constraints.resources.ResourceVisitorAdapter;
import vnreal.network.NetworkEntity;

public class CapacityDemand extends AbstractDemand implements INodeConstraint,
		ILinkConstraint {

	protected double demand;
	
	public CapacityDemand(double demand, NetworkEntity<? extends AbstractConstraint> owner) {
		super(owner);
		this.demand = demand;
	}
	
	public CapacityDemand(double demand, NetworkEntity<? extends AbstractConstraint> owner, String name) {
		super(owner, name);
		this.demand = demand;
	}
	
	public double getDemandedCapacity() {
		return this.demand;
	}
	
	public void setDemandedCapacity(double demand) {
		this.demand = demand;
	}

	@Override
	protected ResourceVisitorAdapter createAcceptsVisitor() {
		return new ResourceVisitorAdapter() {
			@Override
			public boolean visit(CapacityResource res) {
				return true;
			}
		};
	}

	@Override
	protected ResourceVisitorAdapter createFulfillsVisitor() {
		return new ResourceVisitorAdapter() {
			@Override
			public boolean visit(CapacityResource res) {
				return res.getAvailableCapacity()
						>= getDemandedCapacity();
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
		CapacityDemand clone = new CapacityDemand(this.demand, owner, this.getName());
		return clone;
	}
	
	
	public static interface DemandSource {
		public double getDemand();
	}
}
