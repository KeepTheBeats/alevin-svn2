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
import vnreal.algorithms.utils.MiscelFunctions;
import vnreal.algorithms.utils.SubgraphBasicVN.Utils;
import vnreal.constraints.AbstractConstraint;
import vnreal.constraints.ILinkConstraint;
import vnreal.constraints.demands.AbstractDemand;
import vnreal.constraints.demands.BandwidthDemand;
import vnreal.constraints.demands.DemandVisitorAdapter;
import vnreal.mapping.Mapping;
import vnreal.network.Link;
import vnreal.network.NetworkEntity;

/**
 * A resource for bandwidth in Mbit/s.
 * 
 * N.b.: This resource is applicable for links only.
 * 
 * @author Michael Duelli
 * @since 2010-09-10
 */
public final class BandwidthResource extends AbstractResource implements
		ILinkConstraint {
	protected double bandwidth;
	public double occupiedBandwidth = 0;

	/*
	 * Method for the distributed algorithm
	 */
	public void setOccupiedBandwidth(Double occupiedBandwidth) {
		this.occupiedBandwidth = occupiedBandwidth;
	}

	public BandwidthResource(Link<? extends AbstractConstraint> owner) {
		super(owner);
	}
	
	public BandwidthResource(Link<? extends AbstractConstraint> owner, String name) {
		super(owner, name);
	}
	
	public BandwidthResource(double bandwidth, Link<? extends AbstractConstraint> owner) {
		super(owner);
		this.bandwidth = bandwidth;
	}
	
	public BandwidthResource(double bandwidth, Link<? extends AbstractConstraint> owner, String name) {
		super(owner, name);
		this.bandwidth = bandwidth;
	}


	@ExchangeParameter
	public void setBandwidth(Double bandwidth) {
		this.bandwidth = bandwidth;
	}

	@ExchangeParameter
	public Double getBandwidth() {
		return this.bandwidth;
	}
	
	public Double getOccupiedBandwidth() {
		return occupiedBandwidth;
	}

	public Double getAvailableBandwidth() {
		return MiscelFunctions
				.round(bandwidth - occupiedBandwidth, 3);
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
			public boolean visit(BandwidthDemand dem) {
				if (fulfills(dem)) {
					occupiedBandwidth += MiscelFunctions.round(dem.getDemandedBandwidth(), 3);
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
			public boolean visit(BandwidthDemand dem) {
				if (getMapping(dem) != null) {
					occupiedBandwidth -= MiscelFunctions.round(dem.getDemandedBandwidth(), 3);
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

		BandwidthResource clone = null;
			clone = new BandwidthResource((Link<? extends AbstractConstraint>) owner, this.getName());
		
		clone.bandwidth = bandwidth;
		
		if (setOccupied)
			clone.occupiedBandwidth = occupiedBandwidth;

		return clone;
	}

}
