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

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import vnreal.algorithms.utils.SubgraphBasicVN.Utils;
import vnreal.constraints.AbstractConstraint;
import vnreal.constraints.ILinkConstraint;
import vnreal.constraints.INodeConstraint;
import vnreal.constraints.demands.AbstractDemand;
import vnreal.constraints.demands.BackupDemand;
import vnreal.constraints.demands.BandwidthDemand;
import vnreal.constraints.demands.CapacityDemand;
import vnreal.constraints.demands.DemandVisitorAdapter;
import vnreal.mapping.Mapping;
import vnreal.network.Link;
import vnreal.network.NetworkEntity;
import vnreal.network.Node;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualNode;


public final class BackupResource extends AbstractResource implements INodeConstraint, ILinkConstraint {
	
	public AbstractDemand reservedCapacity = null;
	public final int backupSharingFactor;
	
	public static void main(String[] args) {
		
		int backupSharingFactor = 2;
		
		SubstrateNode sn = new SubstrateNode();
		CapacityResource cr = new CapacityResource(1000, sn);
		sn.add(cr);
		BackupResource br = new BackupResource(sn, backupSharingFactor);
		sn.add(br);
		
		VirtualNode vn1 = new VirtualNode(1);
		CapacityDemand c1 = new CapacityDemand(50, vn1);
		vn1.add(c1);
		
		
		VirtualNode vn2 = new VirtualNode(1);
		CapacityDemand c2 = new CapacityDemand(50, vn2);
		vn2.add(c2);
		
		
		VirtualNode vn3 = new VirtualNode(1);
		CapacityDemand c3 = new CapacityDemand(50, vn3);
		vn3.add(c3);

		
		BackupDemand d1 = new BackupDemand(50, null, vn1);
		BackupDemand d2 = new BackupDemand(50, null, vn2);
		BackupDemand d3 = new BackupDemand(50, null, vn3);
		BackupDemand d4 = new BackupDemand(500, null, vn3);
		
		System.out.println(br);
		System.out.println(cr);
		System.out.println(br.getAvailableFreeBackupCapacity());
		System.out.println();
		
		System.out.println("+50");
		d1.occupy(br);
		System.out.println(br);
		System.out.println(cr);
		System.out.println(br.getAvailableFreeBackupCapacity());
		System.out.println();
		
		System.out.println(br.getMappings());
		
		System.out.println("+50");
		d2.occupy(br);
		System.out.println(br);
		System.out.println(cr);
		System.out.println(br.getAvailableFreeBackupCapacity());
		System.out.println();
		
		System.out.println(br.getMappings());
		
		System.out.println("+50");
		d3.occupy(br);
		System.out.println(br);
		System.out.println(cr);
		System.out.println(br.getAvailableFreeBackupCapacity());
		System.out.println();
		
		System.out.println("+500");
		d4.occupy(br);
		System.out.println(br);
		System.out.println(cr);
		System.out.println(br.getAvailableFreeBackupCapacity());
		System.out.println();
		
		System.out.println(br.getMappings());
		
		System.out.println("-50");
		d1.free(br);
		System.out.println(br);
		System.out.println(cr);
		System.out.println(br.getAvailableFreeBackupCapacity());
		System.out.println();
		
		System.out.println("-50");
		d2.free(br);
		System.out.println(br);
		System.out.println(cr);
		System.out.println(br.getAvailableFreeBackupCapacity());
		System.out.println();
		
		
		System.out.println("-500");
		d4.free(br);
		System.out.println(br);
		System.out.println(cr);
		System.out.println(br.getAvailableFreeBackupCapacity());
		System.out.println();

		String ms = "";
		for (AbstractResource r : sn.get())
			for (Mapping m : r.getMappings())
				ms += m.toString() + "\n";
		System.out.println(ms);
		
	}
	
	
		
	public BackupResource(NetworkEntity<? extends AbstractConstraint> owner, int backupSharingFactor) {
		super(owner);
		this.backupSharingFactor = backupSharingFactor;
	}
	
	public BackupResource(NetworkEntity<? extends AbstractConstraint> owner, String name, int backupSharingFactor) {
		super(owner, name);
		this.backupSharingFactor = backupSharingFactor;
	}
	
	private AbstractResource getCapacityResource() {
		for (AbstractConstraint c : this.getOwner().get()) {
			if (this.getOwner() instanceof Node && c instanceof CapacityResource)
				return ((CapacityResource) c);
			if (this.getOwner() instanceof Link && c instanceof BandwidthResource)
				return ((BandwidthResource) c);
		}
		
		throw new AssertionError();
	}
	
	public double getAvailCapacityFromResource() {
		AbstractResource res = getCapacityResource();
		if (this.getOwner() instanceof Node)
			return ((CapacityResource) res).getAvailableCapacity();
		if (this.getOwner() instanceof Link)
			return ((BandwidthResource) res).getAvailableBandwidth();
		
		throw new AssertionError(res);
	}
	
	
	public double getAvailableFreeBackupCapacity() {
		if (this.getMappings().size() >= this.backupSharingFactor)
			return 0d;
		
		double reservedCapacity = getReservedCapacity();
		return reservedCapacity;
		
		
//		double[] demandedCapacitySlots = new double[this.backupSharingFactor];
//		
//		for (BackupDemand d : backupDemands) {
//			for (int i = 0; i < demandedCapacitySlots.length; ++i) {
//				if (demandedCapacitySlots[i] + d.demandedBackupCapacity <= reservedCapacity) {
//					demandedCapacitySlots[i] += d.demandedBackupCapacity;
//					break;
//				}
//			}
//		}
//		
//		double free = 0.0d;
//		for (int i = 0; i < demandedCapacitySlots.length; ++i) {
//			if (reservedCapacity - demandedCapacitySlots[i] > free)
//				free = reservedCapacity - demandedCapacitySlots[i];
//		}
		
//		System.out.println(reservedCapacity + ":   " + Arrays.toString(demandedCapacitySlots) + "   " + free);
//		return free;
	}
	
	
	public double getReservedCapacity() {
		if (this.reservedCapacity != null) {
			if (this.getOwner() instanceof Node)
				return ((CapacityDemand) this.reservedCapacity).getDemandedCapacity();
			else
				return ((BandwidthDemand) this.reservedCapacity).getDemandedBandwidth();
		}
		
		return 0.0d;
	}



	public double getAvailableBackupCapacity() {
		if (this.getMappings().size() >= this.backupSharingFactor)
			return 0d;
		
		double availCapacity = getAvailCapacityFromResource();
		
		double freeSlotCapacity = this.getAvailableFreeBackupCapacity();
		double result = availCapacity + freeSlotCapacity;
		
		return result;
	}
	
	private void allocate(List<BackupDemand> backupCapacities) {
		
		if (this.reservedCapacity != null) {
			for (Mapping m : new LinkedList<Mapping>(this.reservedCapacity.getMappings())) {
				this.reservedCapacity.free(m.getResource());
			}
			
			this.reservedCapacity = null;
		}
		
		double reserved = getRequiredBackupCapacity(backupCapacities);
		
//		LinkedList<BackupDemand> mapped = new LinkedList<BackupDemand>();
//		for (BackupDemand d : backupCapacities) {
//			double free = getAvailableFreeSlotCapacity(mapped, reserved);
//			double needed = d.demandedBackupCapacity - free;
//			
//			if (needed > 0.0d) {
//				reserved += needed;
//			}
//			
//			mapped.add(d);
//		}

		
		if (reserved > 0.0d) {
			if (this.getOwner() instanceof Node) {
				this.reservedCapacity = new CapacityDemand(reserved, null);
			} else {
				this.reservedCapacity = new BandwidthDemand(reserved, null);
			}
			
			AbstractResource capacityResource = this.getCapacityResource();
			Utils.occupyResource(this.reservedCapacity, capacityResource);
		}
		
		
	}
	
	public double getRequiredBackupCapacity(List<BackupDemand> backupCapacities) {
		HashMap<NetworkEntity<?>, Double> demandsMap = new HashMap<NetworkEntity<?>, Double>();
		for (BackupDemand d : backupCapacities) {
			Double mapEntry = demandsMap.get(d.getOriginalOwner());
			if (mapEntry == null) {
				mapEntry = 0.0d;
			}
			mapEntry += d.demandedBackupCapacity;
			
			demandsMap.put(d.getOriginalOwner(), mapEntry);
		}
		
		LinkedList<Double> demands = new LinkedList<Double>(demandsMap.values());
		Collections.sort(demands, new Comparator<Double>() {

			@Override
			public int compare(Double arg0, Double arg1) {
				if (arg0 > arg1)
					return -1;
				if (arg0 < arg1)
					return +1;
				return 0;
			}
			
		});
		
		return demands.isEmpty() ? 0.0d : demands.getFirst();
	}



	public List<BackupDemand> getBackupCapacityDemands() {
		List<BackupDemand> demands = new LinkedList<BackupDemand>();
		for (Mapping m : this.getMappings()) {
			demands.add((BackupDemand) m.getDemand());
		}
		return demands;
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
			public boolean visit(BackupDemand dem) {
				if (fulfills(dem)) {
					new Mapping(dem, getThis());
					BackupResource.this.allocate(getBackupCapacityDemands());
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
			public boolean visit(BackupDemand dem) {
				if (getMapping(dem) != null) {
					if (getMapping(dem).unregister()) {
						BackupResource.this.allocate(getBackupCapacityDemands());
						return true;
					}
					return false;
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

		return new BackupResource((NetworkEntity<? extends AbstractConstraint>) owner, this.getName(), this.backupSharingFactor);
	}
}