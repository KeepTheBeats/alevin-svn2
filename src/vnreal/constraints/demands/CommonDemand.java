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


import java.util.LinkedList;
import java.util.List;

import vnreal.ExchangeParameter;
import vnreal.algorithms.utils.SubgraphBasicVN.Utils;
import vnreal.constraints.AbstractConstraint;
import vnreal.constraints.ILinkConstraint;
import vnreal.constraints.INodeConstraint;
import vnreal.constraints.resources.AbstractResource;
import vnreal.constraints.resources.CommonResource;
import vnreal.constraints.resources.ResourceVisitorAdapter;
import vnreal.network.NetworkEntity;


public class CommonDemand extends AbstractDemand implements
		ILinkConstraint, INodeConstraint {
	
	protected double demandedCapacity;
	public boolean isBackup = false;
	public int maxSharing = 1;
	public LinkedList<?> primaryResourceMappedTo = null;
//	public LinkedList<SubstrateLink> backupPath = null;
	
	public CommonDemand(NetworkEntity<? extends AbstractConstraint> owner) {
		super(owner);
	}

	public CommonDemand(NetworkEntity<? extends AbstractConstraint> ne, String name) {
		super(ne, name);
	}
	
	public CommonDemand(double demandedCapacity, NetworkEntity<? extends AbstractConstraint> owner) {
		super(owner);
		this.demandedCapacity = demandedCapacity;
	}

	public CommonDemand(double demandedCapacity, NetworkEntity<? extends AbstractConstraint> ne, String name) {
		super(ne, name);
		this.demandedCapacity = demandedCapacity;
	}
	
	public CommonDemand(double demandedCapacity, NetworkEntity<? extends AbstractConstraint> ne, String name, boolean isBackup, int maxSharing, LinkedList<?> primaryResourceMappedTo) {
		this(demandedCapacity, ne, name);
		this.isBackup = true;
		if (maxSharing < 1)
			throw new AssertionError();
		this.maxSharing = maxSharing;
		this.primaryResourceMappedTo = primaryResourceMappedTo;
	}
	
	public CommonDemand(double demandedCapacity, NetworkEntity<? extends AbstractConstraint> ne, String name, boolean isBackup, int maxSharing, Object primaryResourceMappedToObj) {
		this(demandedCapacity, ne, name);
		this.isBackup = true;
		if (maxSharing < 1)
			throw new AssertionError();
		this.maxSharing = maxSharing;
		LinkedList<Object> ls = new LinkedList<Object>();
		ls.add(primaryResourceMappedToObj);
		this.primaryResourceMappedTo = ls;
	}


	@ExchangeParameter
	public void setDemandedCapacity(Double demandedCapacity) {
		this.demandedCapacity = demandedCapacity;
	}

	@ExchangeParameter
	public Double getDemandedCapacity() {
		return demandedCapacity;
	}

	@Override
	protected ResourceVisitorAdapter createAcceptsVisitor() {
		return new ResourceVisitorAdapter() {
			@Override
			public boolean visit(CommonResource res) {
				return true;
			}
		};
	}

	@Override
	protected ResourceVisitorAdapter createFulfillsVisitor() {
		return new ResourceVisitorAdapter() {
			@Override
			public boolean visit(CommonResource res) {
				List<CommonDemand> demands = res.getMappedDemands();
				demands.add(CommonDemand.this);
				return res.getCapacity() >= CommonResource.getRequiredCapacity(demands);
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
		return "hash:" + this.hashCode() + "_" + Utils.toString(this);
	}

	@Override
	public AbstractDemand getCopy(NetworkEntity<? extends AbstractDemand> owner) {
		
		CommonDemand clone = new CommonDemand((NetworkEntity<? extends AbstractConstraint>) owner, this.getName());
		clone.demandedCapacity = this.demandedCapacity;

		return clone;
	}
}
