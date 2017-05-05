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

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import vnreal.ExchangeParameter;
import vnreal.algorithms.utils.MiscelFunctions;
import vnreal.algorithms.utils.SubgraphBasicVN.Utils;
import vnreal.constraints.AbstractConstraint;
import vnreal.constraints.ILinkConstraint;
import vnreal.constraints.INodeConstraint;
import vnreal.constraints.demands.AbstractDemand;
import vnreal.constraints.demands.CommonDemand;
import vnreal.constraints.demands.DemandVisitorAdapter;
import vnreal.mapping.Mapping;
import vnreal.network.NetworkEntity;

/**
 * A resource for bandwidth in Mbit/s.
 * 
 * N.b.: This resource is applicable for links only.
 * 
 * @author Michael Duelli
 * @since 2010-09-10
 */
public final class CommonResource extends AbstractResource implements
		ILinkConstraint, INodeConstraint {
	protected double capacity;
	public double occupiedCapacity = 0;
	
	
	public List<CommonDemand> getMappedDemands() {
		List<CommonDemand> demands = new LinkedList<CommonDemand>();
		for (Mapping m : this.getMappings()) {
			demands.add((CommonDemand) m.getDemand());
		}
		return demands;
	}
	
	public static double getRequiredCapacity(Collection<CommonDemand> demands) {
		HashMap<Object, Double> demandsMap = new HashMap<Object, Double>();
		double fixedCost = 0.0d;
		
		int maxSharing = 1;
		
		for (CommonDemand d : demands) {
			
			if (!d.isBackup) {
				fixedCost += d.getDemandedCapacity();
				continue;
			} else {
				maxSharing = d.maxSharing;
			}
			
			if (d.primaryResourceMappedTo == null) {
				demandsMap.put(d, d.getDemandedCapacity());
			} else {
				for (Object ooowner : d.primaryResourceMappedTo) {

					Double mapEntry = demandsMap.get(ooowner);
					if (mapEntry == null) {
						mapEntry = 0.0d;
					}
					mapEntry += d.getDemandedCapacity();

					demandsMap.put(ooowner, mapEntry);
				}
//			} else {
//				throw new AssertionError();
			}
		}
		
		LinkedList<Double> demandslist = new LinkedList<Double>(demandsMap.values());
		Collections.sort(demandslist, new Comparator<Double>() {

			@Override
			public int compare(Double arg0, Double arg1) {
				if (arg0 > arg1)
					return -1;
				if (arg0 < arg1)
					return +1;
				return 0;
			}
			
		});
		
		int pos = 0;
		double backupcost = 0.0d;
		for (Double d : demandslist) {
			if (pos % maxSharing == 0) {
				backupcost += d;
			}
			
			pos++;
		}
		
		return (fixedCost + backupcost);
	}
	
	/*
	 * Method for the distributed algorithm
	 */
	public void setOccupied(Double occupiedCapacity) {
		this.occupiedCapacity = occupiedCapacity;
	}

	public CommonResource(NetworkEntity<? extends AbstractConstraint> owner) {
		super(owner);
	}
	
	public CommonResource(NetworkEntity<? extends AbstractConstraint> owner, String name) {
		super(owner, name);
	}
	
	public CommonResource(double capacity, NetworkEntity<? extends AbstractConstraint> owner) {
		super(owner);
		this.capacity = capacity;
	}
	
	public CommonResource(double capacity, NetworkEntity<? extends AbstractConstraint> owner, String name) {
		super(owner, name);
		this.capacity = capacity;
	}


	@ExchangeParameter
	public void setBandwidth(Double capacity) {
		this.capacity = capacity;
	}

	@ExchangeParameter
	public Double getCapacity() {
		return this.capacity;
	}
	
	public Double getOccupiedCapacity() {
		return occupiedCapacity;
	}

	public Double getAvailableCapacity() {
		return MiscelFunctions.round(capacity - occupiedCapacity, 3);
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
			public boolean visit(CommonDemand dem) {
				if (fulfills(dem)) {
					new Mapping(dem, getThis());
					CommonResource.this.occupiedCapacity = CommonResource.getRequiredCapacity(CommonResource.this.getMappedDemands());
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
			public boolean visit(CommonDemand dem) {
				Mapping m = getMapping(dem);
				if (m != null) {
					if (m.unregister()) {
						CommonResource.this.occupiedCapacity = CommonResource.getRequiredCapacity(CommonResource.this.getMappedDemands());
						return true;
					}
				}
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

		CommonResource clone = new CommonResource((NetworkEntity<? extends AbstractConstraint>) owner, this.getName());
		clone.capacity = capacity;
		
		if (setOccupied)
			clone.occupiedCapacity = occupiedCapacity;

		return clone;
	}

}
