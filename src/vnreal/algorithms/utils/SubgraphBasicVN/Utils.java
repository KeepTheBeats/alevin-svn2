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
package vnreal.algorithms.utils.SubgraphBasicVN;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import vnreal.constraints.AbstractConstraint;
import vnreal.constraints.demands.AbstractDemand;
import vnreal.constraints.demands.BandwidthDemand;
import vnreal.constraints.demands.CpuDemand;
import vnreal.constraints.demands.IdDemand;
import vnreal.constraints.demands.PowerDemand;
import vnreal.constraints.resources.AbstractResource;
import vnreal.constraints.resources.BandwidthResource;
import vnreal.constraints.resources.CpuResource;
import vnreal.constraints.resources.IdResource;
import vnreal.constraints.resources.PowerResource;
import vnreal.constraints.resources.StaticEnergyResource;
import vnreal.core.Scenario;
import vnreal.hiddenhopmapping.HiddenHopEnergyDemand;
import vnreal.mapping.Mapping;
import vnreal.network.NetworkEntity;
import vnreal.network.NetworkStack;
import vnreal.network.Node;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;
import cern.colt.Arrays;
import edu.uci.ics.jung.graph.util.Pair;

/**
 * TODO: move these methods somewhere else ... I collect them here until I don't
 * have to merge so much code every week ...
 */
public class Utils {

	private Utils() {
	}
	
	public static int rnd(int min, int max, Random random) {
		int value = (int) (min + (max - min + 1.0d) * random.nextDouble());
		return value;
	}
	
	public static double rnd(double min, double max, Random random) {
		double value = min + (max - min) * random.nextDouble();
		return value;
	}
	
	//	public static double F(double lambda, double x) {
	//	return (1.0d - Math.pow(Math.E, -lambda * x));
	//}

	public static double exponentialDistribution(Random r, double mu_L) {
		return (-Math.log(1.0d - r.nextDouble()) / mu_L);
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends AbstractResource> T getResource(NetworkEntity<AbstractResource> s, Class<T> type) {
		for (AbstractResource r : s.get()) {
			if (r.getClass().isAssignableFrom(type)) {
				return (T) r;
			}
		}
		return null;
	}
	
	public static void clearVnrMappings(SubstrateNetwork snet, int layer) {
		for (SubstrateNode n : snet.getVertices()) {
			for (AbstractResource r : n.get()) {
				for (Mapping m : new LinkedList<Mapping>(r.getMappings())) {
					if (m.getDemand().getOwner() != null && m.getDemand().getOwner().getLayer() == layer) {
						m.getDemand().free(m.getResource());
					}
				}
			}
		}

		for (SubstrateLink l : snet.getEdges()) {
			for (AbstractResource r : l.get()) {
				for (Mapping m : new LinkedList<Mapping>(r.getMappings())) {
					if (m.getDemand().getOwner() != null && m.getDemand().getOwner().getLayer() == layer) {
						m.getDemand().free(m.getResource());
					}
				}
			}
		}
	}
	
	public static double[] ci(double[] all) {
        // compute sample mean
        double sumx = 0.0;
        for (int i = 0; i < all.length; ++i) {
            sumx  += all[i];
        }
        double mean = sumx / all.length;

        // compute sample variance
        double xxbar = 0.0;
        for (int i = 0; i < all.length; i++) {
            xxbar += (all[i] - mean) * (all[i] - mean);
        }
        double variance = xxbar / (all.length - 1);
        double stddev = Math.sqrt(variance);
        
        double lo = mean - 1.96 * stddev;
        double hi = mean + 1.96 * stddev;
        
        return new double[] {mean, lo, hi};
	}

	public static String toString(Object c) {
		return "classname:" + c.getClass().getSimpleName() + "_" + toString("", c, "_", ":", false);
	}
	
	public static String toString(String prefix, Object c) {
		return toString(prefix, c, "_", ":", false);
	}
	
	public static String toString(String prefix, Object c, boolean rec) {
		return toString(prefix, c, "_", ":", rec);
	}
	
	/**
	 * returns String representation of the Object's fields
	 */
	public static String toString(Object c, String lineSeparator, String keyValueSeparator) {
		return toString("", c, lineSeparator, keyValueSeparator, false);
	}
	
	public static String toString(String prefix, Object c, String lineSeparator, String keyValueSeparator) {
		return toString(prefix, c, lineSeparator, keyValueSeparator, false);
	}
	
	public static String toString(String prefix, Object c, String lineSeparator, String keyValueSeparator, boolean rec) {
		String result = "";
		Class<?> thisClass = c.getClass();
		
		do {
			if (result.length() != 0)
				result += lineSeparator;
			
			Field[] fields = thisClass.getDeclaredFields();
			boolean first = true;

			try {
				for (int i = 0; i < fields.length; ++i) {
					if (Modifier.isPrivate(fields[i].getModifiers()))
						continue;
					fields[i].setAccessible(true);

					if (first) {
						first = false;
					} else {
						result += lineSeparator;
					}

					Field f = fields[i];

					if (f.getType().isArray()) {
						result += prefix + f.getName().replace(lineSeparator, "-").replace(keyValueSeparator, "-") + (keyValueSeparator
								+ (f.get(c) == null ? "null" : Arrays.toString((Object[]) f.get(c)).replace(lineSeparator, "-").replace(keyValueSeparator, "-")));
					} else if (Map.class.isAssignableFrom(f.getType())) {

						result += prefix + f.getName().replace(lineSeparator, "-").replace(keyValueSeparator, "-") + keyValueSeparator;
						if (f.get(c) == null) {
							result += "null";
						} else {
							result += "[";
							boolean firstentry = true;
							for (Entry<?, ?> e : ((Map<?,?>) f.get(c)).entrySet()) {
								if (firstentry)
									firstentry = false;
								else
									result += ",";
								result += (e.getValue() == null ? "null" : e.getValue().toString().replace(lineSeparator, "-").replace(keyValueSeparator, "-")) + "->" + (e.getKey() == null ? "null" : e.getKey().toString().replace(lineSeparator, "-").replace(keyValueSeparator, "-"));
							}
							result += "]";
						}

					} else if (!Collection.class.isAssignableFrom(f.getType()) && !String.class.isAssignableFrom(f.getType()) && !f.getType().isPrimitive() && !f.getType().isEnum()
							&& !Number.class.isAssignableFrom(f.getType())) {
						if (f.get(c) == null)
							result += prefix + f.getName().replace(lineSeparator, "-").replace(keyValueSeparator, "-") + keyValueSeparator + "null";
						else {
							String newPrefix = prefix + f.getName() + ".";
							String childRes = toString(newPrefix, f.get(c), lineSeparator, keyValueSeparator, rec);
							result += prefix + f.getName().replace('_', '-') + keyValueSeparator +
									(f.get(c).getClass().getSimpleName().replace(lineSeparator, "-").replace(keyValueSeparator, "-") +
											(childRes.length() == 0 ? "" : (lineSeparator + childRes)));
						}
					} else {
						result += prefix + f.getName().replace(lineSeparator, "-").replace(keyValueSeparator, "-") + keyValueSeparator + (f.get(c) == null ? "null" : f.get(c).toString().replace(lineSeparator, "-").replace(keyValueSeparator, "-"));
					}
				}

			} catch (IllegalArgumentException e) {
				throw new AssertionError();
			} catch (IllegalAccessException e) {
				throw new AssertionError();
			}
			
			thisClass = thisClass.getSuperclass();
			prefix += "super.";
		} while(rec && thisClass != Object.class);

		return result;
	}
	
	public static boolean fulfills(
			AbstractDemand dem,
			Collection<AbstractResource> ress) {

		if (dem != null) {
			if (dem.getMappings().isEmpty()) {
				boolean found = false;
				for (AbstractResource res : ress) {
					if (res.accepts(dem) && res.fulfills(dem)) {
						found = true;
						break;
					}
				}
				if (!found) {
					return false;
				}
			}
		}

		return true;
	}
	
	public static boolean fulfills(
			Collection<AbstractDemand> demands,
			Collection<AbstractResource> ress) {

		if (demands != null) {
			for (AbstractDemand dem : demands) {
				boolean found = false;
				for (AbstractResource res : ress) {
					if (res.accepts(dem) && res.fulfills(dem)) {
						found = true;
						break;
					}
				}
				if (!found) {
					return false;
				}
			}
		}

		return true;
	}

	public static boolean fulfills(
			NetworkEntity<AbstractResource> sn,
			NetworkEntity<AbstractDemand> vn) {
		return fulfills(vn.get(), sn.get());
	}

	public static boolean fulfills(List<SubstrateLink> path, VirtualLink vl) {
		return fulfills(path, vl.get());
	}
	
	//Fulfills taking into account hidden hops
	public static boolean fulfills(List<SubstrateLink> path, VirtualLink vl, SubstrateNode sourceSnode, SubstrateNetwork sNet) {
		return fulfills(path, vl.get(), sourceSnode, vl.getHiddenHopDemands(), sNet);
	}

	public static boolean fulfills(List<SubstrateLink> path,
			Collection<AbstractDemand> dem) {

		for (SubstrateLink sl : path) {
			if (!fulfills(dem, sl.get())) {
				return false;
			}
		}

		return true;
	}
	
	public static boolean fulfills(List<SubstrateLink> path,
			Collection<AbstractDemand> dem, SubstrateNode sourceSnode, List<AbstractDemand> HhDem, SubstrateNetwork sNet) {

		for (SubstrateLink sl : path) {
			if (!fulfills(dem, sl.get())) {
				return false;
			}
			if(!(sNet.getSource(sl).equals(sourceSnode)) && !fulfills(HhDem,sNet.getSource(sl).get())){
				return false;
			}
		}

		return true;
	}
	
	
	public static ResourceDemandEntry occupyResource(
			AbstractDemand dem,
			AbstractResource res) {
		
		if (res.accepts(dem) && res.fulfills(dem) && dem.occupy(res)) {
			return new ResourceDemandEntry(res, dem);
		}
		
		String ms = "";
		for (AbstractConstraint c : res.getOwner().get()) {
			for (Mapping m : c.getMappings())
				ms += "MAPPING: " + m.getResource().getClass().getSimpleName() + ":" + m.getResource() + " .. " + m.getDemand().getClass().getSimpleName() + ":" + m.getDemand().toString() + "\n";
		}
		
		throw new AssertionError(dem.getClass().getSimpleName() + ":" + dem + "\n\n" + res.getClass().getSimpleName() + ":" + res + "\n\n" + ms);
	}
	

	public static Collection<ResourceDemandEntry> occupyResources(
			Collection<AbstractDemand> n1,
			Collection<AbstractResource> n2) {

		Collection<ResourceDemandEntry> result = new LinkedList<ResourceDemandEntry>();

		for (AbstractDemand dem : n1) {
			boolean found = false;
			for (AbstractResource res : n2) {
				if (res.accepts(dem) && res.fulfills(dem) && dem.occupy(res)) {
					result.add(new ResourceDemandEntry(res, dem));
					found = true;
					break;
				}
			}
			if (!found)
				throw new AssertionError(dem);
		}
		
		return result;
	}
	
	//Hidden hops demands occupation
	public static Collection<ResourceDemandEntry> occupyResources(
			VirtualLink n1,
			SubstrateNode n2) {
		
		Collection<ResourceDemandEntry> resources =
				new LinkedList<ResourceDemandEntry>();

		for (AbstractDemand dem : n1.getHiddenHopDemands()) {

			boolean found = false;
			for (AbstractResource res : n2) {
			
				if (res.accepts(dem) && res.fulfills(dem) && dem.occupy(res)) {
					found = true;
					resources.add(new ResourceDemandEntry(res, dem));
					break;
				}
			}
			assert (found);
		}
		
		return resources;
	}

	public static LinkedList<ResourceDemandEntry> occupyPathResources(
			List<AbstractDemand> demands, List<AbstractDemand> hiddenhopDemands,
			List<SubstrateLink> path, SubstrateNetwork sNetwork) {
		
		LinkedList<ResourceDemandEntry> resources =
				new LinkedList<ResourceDemandEntry>();

			int i = 1;
			for (SubstrateLink e : path) {
				resources.addAll(occupyResources(demands, e.get()));

				if (i != path.size()) {
					Pair<SubstrateNode> endpoints = sNetwork.getEndpoints(e);
					Collection<ResourceDemandEntry> entry =
						occupyHiddenHop(endpoints.getSecond(), hiddenhopDemands);
					resources.addAll(entry);
				}
				++i;
			}
			
			return resources;
	}

	
	public static LinkedList<ResourceDemandEntry> occupyPathResources(
			VirtualLink vl,
			List<SubstrateLink> path, SubstrateNetwork sNetwork) {

		return occupyPathResources(vl.get(), vl.getHiddenHopDemands(), path, sNetwork);
	}
	
//	public static Collection<ResourceDemandEntry<? extends AbstractResource, ? extends AbstractDemand>> occupyBackupPathResources(
//			VirtualLink vl,
//			List<SubstrateLink> path, SubstrateNetwork sNetwork, boolean useEnergyDemand) {
//
//		Collection<ResourceDemandEntry<? extends AbstractResource, ? extends AbstractDemand>> resources =
//			new LinkedList<ResourceDemandEntry<? extends AbstractResource, ? extends AbstractDemand>>();
//
//		Collection<AbstractDemand> rDems = new LinkedList<AbstractDemand>(vl.get());
//		rDems.add(new ResilienceDemand(vl));
//		
//		int i = 1;
//		for (SubstrateLink e : path) {
//			resources.addAll(occupyResources(rDems, e.get()));
//
//			if (i != path.size()) {
//				Pair<SubstrateNode> endpoints = sNetwork.getEndpoints(e);
//				Collection<ResourceDemandEntry<AbstractResource, AbstractDemand>> entry =
//					occupyHiddenHop(endpoints.getSecond(), vl.getHiddenHopDemands(), useEnergyDemand);
//				resources.addAll(entry);
//			}
//			++i;
//		}
//		
//		return resources;
//	}
	
	//Including hidden hops cpu demand
	public static Collection<ResourceDemandEntry> occupyPathResources(
			VirtualLink vl, List<SubstrateLink> path, SubstrateNetwork sNetwork, SubstrateNode sourceSnode) {

		Collection<ResourceDemandEntry> resources =
			new LinkedList<ResourceDemandEntry>();

		int i = 1;
		for (SubstrateLink e : path) {
			resources.addAll(occupyResources(vl.get(), e.get()));
			
			if(!(sNetwork.getSource(e).equals(sourceSnode))) {
				Collection<ResourceDemandEntry> resources2 =
						occupyResources(vl, sNetwork.getSource(e));
				if (resources2 == null) {
					throw new AssertionError();
				}
				resources.addAll(resources2);
			}

			if (i != path.size()) {
				Pair<SubstrateNode> endpoints = sNetwork.getEndpoints(e);
				Collection<ResourceDemandEntry> entry =
					occupyHiddenHop(endpoints.getSecond(), vl.getHiddenHopDemands());
				resources.addAll(entry);
			}
			++i;
		}
		
		return resources;
	}

	public static void freeResource(ResourceDemandEntry m) {
		m.dem.free(m.res);
	}

	public static void freeResources(
			Collection<ResourceDemandEntry> mappings) {

		for (ResourceDemandEntry m : mappings) {
			freeResource(m);
		}
	}

	private static Collection<ResourceDemandEntry> occupyHiddenHop(
			SubstrateNode s, List<AbstractDemand> hiddenHopCPUDemands) {
		
		LinkedList<ResourceDemandEntry> result =
			new LinkedList<ResourceDemandEntry>();
		
		PowerResource er = Utils.getEnergyResource(s);
		if (er != null) {
			HiddenHopEnergyDemand demand = new HiddenHopEnergyDemand(s);
			demand.occupy(er);
			
			result.add(new ResourceDemandEntry(er, demand));
		}
		
		if (hiddenHopCPUDemands != null)
			for (AbstractDemand hd : hiddenHopCPUDemands) {
				for (AbstractResource r : s.get()) {
					if (r.accepts(hd) && r.fulfills(hd) && hd.occupy(r)) {
						result.add(new ResourceDemandEntry(r, hd));
						continue;
					}
				}

				throw new AssertionError();
			}
		
		return result;
	}

	public static PowerResource getEnergyResource(SubstrateNode sn) {
		for (AbstractResource r : sn.get()) {
			if (r instanceof PowerResource) {
				return (PowerResource) r;
			}
		}

		return null;
	}

	public static boolean isSolelyForwardingHop(SubstrateNode sn) {
		boolean isUsed = false;
		
		for (AbstractResource r : sn.get()) {
			if (r instanceof PowerResource) {
				for (vnreal.mapping.Mapping m : r.getMappings()) {
					if (!(m.getDemand() instanceof HiddenHopEnergyDemand)) {
						return false;
					}
				}
				if (((PowerResource) r).isUsed()) {
					isUsed = true;
				}
			} else {
				if (!r.getMappings().isEmpty()) {
					return false;
				}
			}
		}

		return isUsed;
	}

	/**
	 * map nodes which have an ID demand; add those nodes to v_a
	 * 
	 * @param vNetwork
	 * @param sNetwork
	 * @param result
	 * @param v_a
	 */
	public static List<SubstrateNode> mapIdDemands(VirtualNetwork vNetwork,
			SubstrateNetwork sNetwork, vnreal.algorithms.utils.SubgraphBasicVN.NodeLinkMapping m) {
		List<SubstrateNode> result = new LinkedList<SubstrateNode>();

		vnodes: for (VirtualNode vn : vNetwork.getVertices()) {
			Collection<ResourceDemandEntry> demandMappings = new LinkedList<ResourceDemandEntry>();
			for (AbstractDemand d : vn.get()) {
				if (d instanceof IdDemand) {

					for (SubstrateNode sn : sNetwork.getVertices()) {
						for (AbstractResource r : sn.get()) {
							if (r instanceof IdResource) {
								IdDemand idDemand = (IdDemand) d;
								IdResource idResource = (IdResource) r;
								if (idDemand.getDemandedId() == idResource
										.getId()) {
									if (Utils.fulfills(sn, vn)) {
										m.add(vn, sn);
										idDemand.occupy(idResource);
										demandMappings
												.add(new ResourceDemandEntry(idResource, idDemand));
										result.add(sn);
										continue vnodes;
									}
								}
							}
						}
					}

					for (ResourceDemandEntry dm : demandMappings) {
						dm.dem.free(dm.res);
					}

					// no valid id mapping possible
					result.clear();
					return null;
				}
			}
		}

		return result;
	}

	public static int getStressLevel(NetworkEntity<? extends AbstractResource> e) {
		List<NetworkEntity<?>> vEntities = new LinkedList<NetworkEntity<?>>();
		
		for (AbstractResource r : e.get()) {
			for (vnreal.mapping.Mapping m : r.getMappings()) {
				AbstractDemand d = m.getDemand();

				if (!(d instanceof HiddenHopEnergyDemand)) {

					NetworkEntity<?> owner = d.getOwner();

					if (!vEntities.contains(owner)) {
						vEntities.add(owner);
					}

				}
			}
		}
		
		return vEntities.size();
	}
	
	public static double getCpuAvailable(
			NetworkEntity<? extends AbstractResource> n1) {
		for (AbstractResource res : n1.get()) {
			if (res instanceof CpuResource) {
				return ((CpuResource) res).getAvailableCycles();
			}
		}
		return 0.0;
	}
	
	public static double getBandwidthAvailable(
			NetworkEntity<? extends AbstractResource> n1) {
		for (AbstractResource res : n1.get()) {
			if (res instanceof BandwidthResource) {
				return ((BandwidthResource) res).getAvailableBandwidth();
			}
		}
		return 0.0;
	}
	
	
	public static double getBandwidthDemand(NetworkEntity<? extends AbstractDemand> n) {
		for (AbstractDemand dem : n.get()) {
			if (dem instanceof BandwidthDemand) {
				return ((BandwidthDemand) dem).getDemandedBandwidth();
			}
		}
		return 0.0;
	}

	public static double getCpuRemaining(
			NetworkEntity<? extends AbstractResource> n1,
			NetworkEntity<? extends AbstractDemand> n2) {
		for (AbstractResource res : n1.get()) {
			if (res instanceof CpuResource) {
				for (AbstractDemand dem : n2.get()) {
					if (dem instanceof CpuDemand) {
						return ((CpuResource) res).getAvailableCycles()
								- (((CpuDemand) dem)).getDemandedCycles();
					}
				}
			}
		}
		return 0.0;
	}

	public static CpuResource getCpuResource(
			NetworkEntity<? extends AbstractResource> n) {
		for (AbstractResource res : n.get()) {
			if (res instanceof CpuResource) {
				return (CpuResource) res;
			}
		}
		return null;
	}

	public static CpuDemand getCpuDemand(NetworkEntity<? extends AbstractDemand> n) {
		for (AbstractDemand dem : n.get()) {
			if (dem instanceof CpuDemand) {
				return (CpuDemand) dem;
			}
		}
		return null;
	}

	public static <T extends NetworkEntity<?>> boolean contains(T n,
			Collection<T> ns) {
		for (NetworkEntity<?> v : ns) {
			if (v.getId() == n.getId()) {
				return true;
			}
		}
		return false;
	}

	public static <T extends NetworkEntity<?>> boolean equals(Collection<T> ts,
			Collection<T> us) {
		if (ts.size() != us.size()) {
			return false;
		}
		for (T t : ts) {
			if (!contains(t, us)) {
				return false;
			}
		}
		return true;
	}

	public static <T extends Node<?>> Collection<T> minus(Collection<T> c1,
			Collection<T> c2) {
		Collection<T> result = new LinkedList<T>();
		for (T n : c1) {
			if (!Utils.contains(n, c2)) {
				result.add(n);
			}
		}
		return result;
	}

	public static Collection<List<SubstrateLink>> findAllPaths(
			SubstrateNetwork sNetwork, SubstrateNode n1, SubstrateNode n2,
			Collection<AbstractDemand> vldemands) {
		Collection<List<SubstrateLink>> result = new LinkedList<List<SubstrateLink>>();
		findAllPaths(sNetwork, n1, n2, vldemands,
				new LinkedList<SubstrateNode>(),
				new LinkedList<SubstrateLink>(), result);

		return result;
	}
	
	public static void findAllPaths(SubstrateNetwork sNetwork,
			SubstrateNode n1, SubstrateNode n2,
			Collection<AbstractDemand> vldemands,
			Collection<SubstrateNode> visited, List<SubstrateLink> currentPath,
			Collection<List<SubstrateLink>> result) {
		if (n1 == n2) {
			if (fulfills(vldemands, n1.get())) {
				result.add(currentPath);
			}
			return;
		}
		if (visited.contains(n1)) {
			return;
		}
		visited.add(n1);

		for (SubstrateLink l : sNetwork.getOutEdges(n1)) {
			if (fulfills(vldemands, l.get())) {
				List<SubstrateLink> newPath = new LinkedList<SubstrateLink>(
						currentPath);
				newPath.add(l);
				findAllPaths(sNetwork, sNetwork.getOpposite(n1, l), n2,
						vldemands, visited, newPath, result);
			}
		}
	}

	public static boolean hasResourceMappings(SubstrateNetwork sNetwork) {
		for (SubstrateNode n : sNetwork.getVertices()) {
			for (AbstractResource r : n.get()) {
				if (!r.getMappings().isEmpty()) {
					return true;
				}
			}
		}
		return false;
	}

	public static void printResourceMappings(SubstrateNetwork sNetwork) {
		for (SubstrateNode n : sNetwork.getVertices()) {
			for (AbstractResource r : n.get()) {
				if (!r.getMappings().isEmpty()) {
					System.out.println(r.getMappings().size()
							+ " mappings for node " + n);
					for (vnreal.mapping.Mapping as : r.getMappings())
						System.out.println("  " + as.getDemand() + "  "
								+ as.getResource());
				}
			}
		}
	}

	public static void generateEnergyDemands(VirtualNetwork vNetwork) {
		for (VirtualNode vn : vNetwork.getVertices()) {
			vn.add(new PowerDemand(vn));
		}
	}
	
	public static void generateHetEnergyConstraints(NetworkStack stack, int min, int max, Scenario scen) {
		
		SubstrateNetwork sNetwork = stack.getSubstrate();
		for (SubstrateNode sn : sNetwork.getVertices()) {
			int consumption = (int) (Math.random() * (max - min) + 1) + min;
			sn.add(new StaticEnergyResource(sn, consumption));
		}
		
		for (int i = 1; scen.getNetworkStack().getLayer(i) != null; i++) {
			VirtualNetwork vNetwork = (VirtualNetwork) scen
					.getNetworkStack().getLayer(i);
			
			for (VirtualNode vn : vNetwork.getVertices()) {
				vn.add(new PowerDemand(vn));
			}
		}
	}
	
}
