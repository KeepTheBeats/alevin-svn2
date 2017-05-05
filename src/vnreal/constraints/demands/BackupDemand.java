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

import java.util.List;

import vnreal.algorithms.utils.SubgraphBasicVN.Utils;
import vnreal.constraints.AbstractConstraint;
import vnreal.constraints.ILinkConstraint;
import vnreal.constraints.INodeConstraint;
import vnreal.constraints.resources.AbstractResource;
import vnreal.constraints.resources.BackupResource;
import vnreal.constraints.resources.ResourceVisitorAdapter;
import vnreal.network.NetworkEntity;

public class BackupDemand extends AbstractDemand implements INodeConstraint, ILinkConstraint {

	public final double demandedBackupCapacity;
	private final NetworkEntity<? extends AbstractConstraint> originalOwner;
	
	public BackupDemand(double demandedBackupCapacity, NetworkEntity<? extends AbstractConstraint> owner, NetworkEntity<? extends AbstractConstraint> originalOwner) {
		super(owner);
		this.demandedBackupCapacity = demandedBackupCapacity;
		this.originalOwner = originalOwner;
	}
	
	public BackupDemand(double demandedBackupCapacity, NetworkEntity<? extends AbstractConstraint> owner, NetworkEntity<? extends AbstractConstraint> originalOwner, String name) {
		super(owner, name);
		this.demandedBackupCapacity = demandedBackupCapacity;
		this.originalOwner = originalOwner;
	}
	
//	public double getDemandedBackupCapacity(BackupResource onThatResource) {
//		
//		List<BackupDemand> backupCapacities = onThatResource.getBackupCapacityDemands();
//		double prev = onThatResource.getRequiredBackupCapacity(backupCapacities);
//		backupCapacities.add(this);
//		double now = onThatResource.getRequiredBackupCapacity(backupCapacities);
//		return (now - prev);
//		
//		double totalDemand = this.relativeDemandedBackupCapacity;
//		
//		List<? extends AbstractConstraint> resources = this.originalOwner.get();
//		if (!resources.isEmpty()) {
//			List<Mapping> mappings = resources.get(0).getMappings();
//
//			if (!mappings.isEmpty()) {
//				NetworkEntity<?> thisOriginalMappedTo = mappings.get(0).getResource().getOwner();
//
//				if (thisOriginalMappedTo != null) {
//					for (Mapping m : onThatResource.getMappings()) {
//						BackupDemand d = (BackupDemand) m.getDemand();
//
//						AbstractConstraint origDem = d.originalOwner.get().get(0);
//						for (Mapping mm : origDem.getMappings()) {
//
//							if (mm.getResource().getOwner() == thisOriginalMappedTo) {
//								totalDemand += d.relativeDemandedBackupCapacity;
//								break;
//							}
//						}
//					}
//
//				}
//			}
//		}
//		
//		return totalDemand;
//	}

	@Override
	protected ResourceVisitorAdapter createAcceptsVisitor() {
		return new ResourceVisitorAdapter() {
			@Override
			public boolean visit(BackupResource res) {
				return true;
			}
		};
	}

	@Override
	protected ResourceVisitorAdapter createFulfillsVisitor() {
		return new ResourceVisitorAdapter() {
			@Override
			public boolean visit(BackupResource res) {
				if (res.getMappings().size() >= res.backupSharingFactor)
					return false;
				
				List<BackupDemand> demands = res.getBackupCapacityDemands();
				demands.add(BackupDemand.this);
				return res.getAvailCapacityFromResource() + res.getReservedCapacity() >= res.getRequiredBackupCapacity(demands);
			}
		};
	}
	
	public NetworkEntity<?> getOriginalOwner() {
		return this.originalOwner;
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
		BackupDemand clone = new BackupDemand(this.demandedBackupCapacity, owner, originalOwner, this.getName());
		return clone;
	}
	
}