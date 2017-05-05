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

import vnreal.ExchangeParameter;
import vnreal.algorithms.utils.MiscelFunctions;
import vnreal.algorithms.utils.SubgraphBasicVN.Utils;
import vnreal.constraints.AbstractConstraint;
import vnreal.constraints.ILinkConstraint;
import vnreal.constraints.INodeConstraint;
import vnreal.constraints.resources.AbstractResource;
import vnreal.constraints.resources.CpuResource;
import vnreal.constraints.resources.ResourceVisitorAdapter;
import vnreal.network.Link;
import vnreal.network.NetworkEntity;
import vnreal.network.Node;

/**
 * A demand for CPU cycles.
 * 
 * N.b.: This demand is applicable for nodes only.
 * 
 * @author Michael Duelli
 * @author Vlad Singeorzan
 * @since 2010-09-10
 */
public class CpuDemand extends AbstractDemand implements INodeConstraint,
		ILinkConstraint {
	protected double demandedCycles;
	
	public CpuDemand(Node<? extends AbstractConstraint> owner) {
		super(owner);
	}
	
	public CpuDemand(Link<? extends AbstractConstraint> owner) {
		super(owner);
	}
	
	public CpuDemand(NetworkEntity<? extends AbstractConstraint> owner, String name) {
		super(owner, name);
	}
	
	public CpuDemand(double demandedCycles, NetworkEntity<? extends AbstractConstraint> owner) {
		super(owner);
		this.demandedCycles = demandedCycles;
	}
	
	public CpuDemand(double demandedCycles, NetworkEntity<? extends AbstractConstraint> owner, String name) {
		super(owner, name);
		this.demandedCycles = demandedCycles;
	}


	@ExchangeParameter
	public void setDemandedCycles(Double cycles) {
		this.demandedCycles = cycles;
	}

	@ExchangeParameter
	public Double getDemandedCycles() {
		return demandedCycles;
	}

	@Override
	protected ResourceVisitorAdapter createAcceptsVisitor() {
		return new ResourceVisitorAdapter() {
			@Override
			public boolean visit(CpuResource res) {
				return true;
			}
		};
	}

	@Override
	protected ResourceVisitorAdapter createFulfillsVisitor() {
		return new ResourceVisitorAdapter() {
			@Override
			public boolean visit(CpuResource res) {
				return MiscelFunctions.round(res.getAvailableCycles(), 3)
						>= MiscelFunctions.round(getDemandedCycles(), 3);
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
		CpuDemand clone = null;
			clone = new CpuDemand((Node<? extends AbstractConstraint>) owner, this.getName());
		
		clone.demandedCycles = this.demandedCycles;
		
		return clone;
	}
}
