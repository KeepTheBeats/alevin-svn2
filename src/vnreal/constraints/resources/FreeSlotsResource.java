package vnreal.constraints.resources;
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


import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import vnreal.algorithms.utils.SubgraphBasicVN.Utils;
import vnreal.constraints.AbstractConstraint;
import vnreal.constraints.INodeConstraint;
import vnreal.constraints.demands.AbstractDemand;
import vnreal.constraints.demands.DemandVisitorAdapter;
import vnreal.constraints.demands.FreeSlotsDemand;
import vnreal.mapping.Mapping;
import vnreal.network.NetworkEntity;
import vnreal.network.Node;


public final class FreeSlotsResource extends AbstractResource implements
		INodeConstraint {
	
	public final String label;
	protected final int slots;
	
	
	public static void main(String[] args) {
		FreeSlotsResource r = new FreeSlotsResource("net", 2, null);
		
		FreeSlotsDemand d1 = new FreeSlotsDemand("net", "FW1", null, new LinkedList<NetworkEntity<AbstractResource>>());
		FreeSlotsDemand d2 = new FreeSlotsDemand("net", "FW2", null, new LinkedList<NetworkEntity<AbstractResource>>());
		FreeSlotsDemand d3 = new FreeSlotsDemand("net", "FW3", null, new LinkedList<NetworkEntity<AbstractResource>>());
		FreeSlotsDemand d4 = new FreeSlotsDemand("net", "FW4", null, new LinkedList<NetworkEntity<AbstractResource>>());
		
		System.out.println(d1.occupy(r));
		System.out.println(r);
		System.out.println(d2.occupy(r));
		System.out.println(r);
		System.out.println(d3.occupy(r));
		System.out.println(r);
		System.out.println(d4.occupy(r));
		System.out.println(r);
	}
	
	
	public FreeSlotsResource(String label, int slots, Node<? extends AbstractConstraint> owner, String name) {
		super(owner, name);
		this.label = label;
		this.slots = slots;
	}
	
	public FreeSlotsResource(String label, int slots, Node<? extends AbstractConstraint> owner) {
		super(owner);
		this.label = label;
		this.slots = slots;
	}
	
	public int getSlots() {
		return this.slots;
	}

	public int getAvailableSlots() {
		int occupiedSlots = getOccupiedSlots();
		return (slots - occupiedSlots);
	}
	
	public int getOccupiedSlots() {
		LinkedList<FreeSlotsDemand> demands = new LinkedList<FreeSlotsDemand>();
		List<Mapping> ms = this.getMappings();
		for (Mapping m : ms) {
			if (m.getDemand() instanceof FreeSlotsDemand) {
				demands.add((FreeSlotsDemand) m.getDemand());
			}
		}
		return getOccupiedSlots(demands);
	}
	
	public int getOccupiedSlots(LinkedList<FreeSlotsDemand> demands) {
		/*
		 demandedLabel:label0_VNFType:label0-type1_isBackup:false
		 demandedLabel:label0_VNFType:label0-type0_isBackup:false
		 demandedLabel:label0_VNFType:label0-type0_isBackup:false 
		 demandedLabel:label0_VNFType:label0-type2_isBackup:true
		 demandedLabel:label0_VNFType:label0-type2_isBackup:true
		 */
		
		Collections.sort(demands, new Comparator<FreeSlotsDemand>() {

			@Override
			public int compare(FreeSlotsDemand arg0, FreeSlotsDemand arg1) {
				if (!arg0.isBackup && arg1.isBackup)
					return -1;
				if (arg0.isBackup && !arg1.isBackup)
					return +1;
				
//				long arg0id = arg0.getOriginalOwner() == null ? 0 : arg0.getOriginalOwner().getId();
//				long arg1id = arg1.getOriginalOwner() == null ? 0 : arg1.getOriginalOwner().getId();
//				if (arg0id < arg1id)
//					return -1;
//				if (arg0id > arg1id)
//					return -1;
				
				long arg0id2 = arg0.hashCode();
				long arg1id2 = arg1.hashCode();
				if (arg0id2 < arg1id2)
					return -1;
				if (arg0id2 > arg1id2)
					return +1;
				
				return 0;
				
//				return arg0.toString().compareTo(arg1.toString());
				
			}
			
		});

		int result = 0;
		LinkedList<FreeSlotsDemand> mapped = new LinkedList<FreeSlotsDemand>();
		outer: for (FreeSlotsDemand d : demands) {
			for (FreeSlotsDemand m : mapped) {
				if (m.VNFType.equals(d.VNFType)) {  // VM of same type already mapped here!
					mapped.add(d);
					continue outer;
				}
			}

			if (d.isBackup) {
				LinkedList<NetworkEntity<AbstractResource>> thisOriginalMappedTo = d.getOriginalNodeMappedTo();
				if (thisOriginalMappedTo.isEmpty())
					throw new AssertionError();

				for (FreeSlotsDemand m : mapped) {
					LinkedList<NetworkEntity<AbstractResource>> oodemands = m.getOriginalNodeMappedTo();
					
					for (NetworkEntity<?> t : thisOriginalMappedTo) {
						if (oodemands.contains(t)) {
							result++;
							mapped.add(d);
							continue outer;
						}
					}
				}

				for (FreeSlotsDemand m : mapped) {
					if (m.isBackup) {
						mapped.add(d);
						continue outer;
					}
				}

			}
			
			mapped.add(d);
			result++;
		}

		return result;
	}
	
	public boolean isFree(FreeSlotsDemand demand) {
		if (demand == null)
			return false;
		
		LinkedList<FreeSlotsDemand> demands = new LinkedList<FreeSlotsDemand>();
		for (Mapping m : this.getMappings()) {
			if (m.getDemand() instanceof FreeSlotsDemand) {
				demands.add((FreeSlotsDemand) m.getDemand());
			}
		}
		int prev = this.getOccupiedSlots(demands);
		demands.add(demand);
		int now = this.getOccupiedSlots(demands);
		
		return (prev == now);
	}
	
//	public int getRequiredSlots(List<FreeSlotsDemand> freeSlotsDemands) {
//		HashMap<NetworkEntity<?>, Integer> demandsMap = new HashMap<NetworkEntity<?>, Integer>();
//		for (FreeSlotsDemand d : freeSlotsDemands) {
//			Integer mapEntry = demandsMap.get(d.getOriginalOwner());
//			if (mapEntry == null) {
//				mapEntry = 0;
//			}
//			mapEntry += 1;
//			
//			demandsMap.put(d.getOriginalOwner(), mapEntry);
//		}
//		
//		LinkedList<Integer> demands = new LinkedList<Integer>(demandsMap.values());
//		Collections.sort(demands, new Comparator<Integer>() {
//
//			@Override
//			public int compare(Integer arg0, Integer arg1) {
//				if (arg0 > arg1)
//					return -1;
//				if (arg0 < arg1)
//					return +1;
//				return 0;
//			}
//			
//		});
//		
//		return demands.isEmpty() ? 0 : demands.getFirst();
//	}

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
			public boolean visit(FreeSlotsDemand dem) {
				if (fulfills(dem)) {
					
					new Mapping(dem, getThis());
					
//					String ms ="!" + getOccupiedSlots() + " #" + FreeSlotsResource.this.getName() + " " + FreeSlotsResource.this + "\n";
//					ms += "M:" + dem + "\n";
//					for (Mapping m : FreeSlotsResource.this.getMappings())
//						ms += m.getDemand() + "\n";
//					System.out.println(ms);
					
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
			public boolean visit(FreeSlotsDemand dem) {
				Mapping mapping = getMapping(dem);
				if (mapping != null) {
					return mapping.unregister();

//						String ms ="!" + "->" + getOccupiedSlots() + " #" + FreeSlotsResource.this.getName() + " " + FreeSlotsResource.this + "\n";
//						ms += "U:" + dem + "\n";
//						for (Mapping m : FreeSlotsResource.this.getMappings())
//							ms += m.getDemand() + "\n";
//						System.out.println(ms);
				} else
					return false;
			}
		};
	}
	
	public String getLabel() {
		return this.label;
	}

	@Override
	public String toString() {
		return Utils.toString(this) + "_occupiedSlots:" + getOccupiedSlots();
	}

	@Override
	public AbstractResource getCopy(
			NetworkEntity<? extends AbstractConstraint> owner, boolean setOccupied) {

		FreeSlotsResource clone = new FreeSlotsResource(this.label, this.slots, (Node<? extends AbstractConstraint>) owner, this.getName());

		return clone;
	}
}