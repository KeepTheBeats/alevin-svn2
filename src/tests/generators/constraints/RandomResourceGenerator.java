package tests.generators.constraints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

import vnreal.algorithms.utils.SubgraphBasicVN.Utils;
import vnreal.algorithms.utils.mls.MLSLattice;
import vnreal.constraints.resources.BackupResource;
import vnreal.constraints.resources.BandwidthResource;
import vnreal.constraints.resources.CapacityResource;
import vnreal.constraints.resources.CommonResource;
import vnreal.constraints.resources.CpuResource;
import vnreal.constraints.resources.FreeSlotsResource;
import vnreal.constraints.resources.IdResource;
import vnreal.constraints.resources.LabelResource;
import vnreal.constraints.resources.MLSResource;
import vnreal.constraints.resources.StaticDelayResource;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;

public class RandomResourceGenerator implements ConstraintsGenerator<SubstrateNetwork> {
	
	public final RandomResourceGeneratorParameter param;
	public boolean useCommonConstraints = false;
	
	public RandomResourceGenerator(RandomResourceGeneratorParameter param) {
		this.param = param;
	}
	
	public void addConstraints(SubstrateNetwork network, Random random) {
		
		if (param.generateIDResources)
			generateIdResources(network);
		
		if (param.minFreeSlotsResources != -1)
			generateFreeSlotsResources(network, param.labelsAndVNFTypes, param.minFreeSlotsResources, param.maxFreeSlotsResources, random);
		
		if (param.minCPUResource != -1)
			generateRandomCPUResources(network, param.minCPUResource, param.maxCPUResource, random, this.useCommonConstraints);
		
		if (param.minCapacity != -1)
			generateRandomCapacityResources(network, param.minCapacity, param.maxCapacity, random, this.useCommonConstraints);
		
		if (param.minBandwidthResource != -1)
			generateRandomBandwidthResources(network, param.minBandwidthResource, param.maxBandwidthResource, random, this.useCommonConstraints);
		
		if (param.minDelayResource != -1)
			generateRandomDelayResources(network, param.minDelayResource, param.maxDelayResource, random);
		
	}
	
	public static void generateIdResources(SubstrateNetwork network) {
		for (SubstrateNode n : network.getVertices()) {
			IdResource idResource = new IdResource(n, network);
			idResource.setId(n.getName());
			n.add(idResource);
		}
	}
	
	public static void generateBackupResources(SubstrateNetwork G, int backupResourcesSharingFactor) {
		for (SubstrateNode sn : G.getVertices())
			sn.add(new BackupResource(sn, backupResourcesSharingFactor));

		for (SubstrateLink sn : G.getEdges())
			sn.add(new BackupResource(sn, backupResourcesSharingFactor));
	}
	
	public static void generateFreeSlotsResources(SubstrateNetwork network, HashMap<String, LinkedList<String>> labelsAndVNFTypes, int minFreeSlotsResources, int maxFreeSlotsResources, Random random) {
		for (SubstrateNode n : network.getVertices()) {
			int slots = Utils.rnd(minFreeSlotsResources, maxFreeSlotsResources, random);
			
			LinkedList<String> keys = new LinkedList<String>(labelsAndVNFTypes.keySet());
			String label = keys.get(Utils.rnd(0, keys.size() - 1, random));
			n.add(new FreeSlotsResource(label, slots, n));
		}
	}

	public String getSuffix(String prefix) {
		return Utils.toString(prefix, param, true);
	}
	
	
	public static class RandomResourceGeneratorParameter {
		
		public final boolean generateIDResources;
		
		public final HashMap<String, LinkedList<String>> labelsAndVNFTypes;
		public final int minFreeSlotsResources;
		public final int maxFreeSlotsResources;
		
		public final int minCPUResource;
		public final int maxCPUResource;
		
		public final int minCapacity;
		public final int maxCapacity;
		
		public final int minBandwidthResource;
		public final int maxBandwidthResource;
		
		public final int minDelayResource;
		public final int maxDelayResource;

		public RandomResourceGeneratorParameter(
				boolean generateIDResources,
				HashMap<String, LinkedList<String>> labelsAndVNFTypes,
				int minFreeSlotsResources, int maxFreeSlotsResources,
				int minCPUResource, int maxCPUResource,
				int minCapacity, int maxCapacity,
				int minBandwidthResource, int maxBandwidthResource,
				int minDelayResource, int maxDelayResource,
				String[] labels) {
			super();
			this.generateIDResources = generateIDResources;
			this.labelsAndVNFTypes = labelsAndVNFTypes;
			this.minFreeSlotsResources = minFreeSlotsResources;
			this.maxFreeSlotsResources = maxFreeSlotsResources;
			this.minCPUResource = minCPUResource;
			this.maxCPUResource = maxCPUResource;
			this.minCapacity = minCapacity;
			this.maxCapacity = maxCapacity;
			this.minBandwidthResource = minBandwidthResource;
			this.maxBandwidthResource = maxBandwidthResource;
			this.minDelayResource = minDelayResource;
			this.maxDelayResource = maxDelayResource;
		}
		
	}
	
	
	public static void generateRandomLabelResources(SubstrateNetwork network,
			int numDemandedLabels, String[] labels, Random random) {
		
		for (SubstrateNode sn : network.getVertices()) {
			LinkedList<String> labelsShuffle = new LinkedList<String>();
			for (String s : labels)
				labelsShuffle.add(s);
			Collections.shuffle(labelsShuffle, random);
			
			LinkedList<String> providedLabels = new LinkedList<String>();
			int i = 0;
			for (String label : labelsShuffle) {
				if (i >= numDemandedLabels)
					break;
				
				providedLabels.add(label);
				i++;
			}
			sn.add(new LabelResource(sn, providedLabels));
		}
	}
	
	
	public static void generateRandomDelayResources(SubstrateNetwork sNetwork,
			int minDelayResource, int maxDelayResource, Random random) {

		for (SubstrateLink n : sNetwork.getEdges()) {
			int value = (int) (minDelayResource + (maxDelayResource
					- minDelayResource + 1)
					* random.nextDouble());
			StaticDelayResource res = new StaticDelayResource(n, value);
			n.add(res);
		}
	}
	
	public static void generateRandomCPUResources(SubstrateNetwork sNetwork,
			int minResourceCPU, int maxResourceCPU, Random random, boolean useCommonConstraints) {

		for (SubstrateNode n : sNetwork.getVertices()) {
			int value = Utils.rnd(minResourceCPU, maxResourceCPU, random);
			
			if (useCommonConstraints) {
				CommonResource r = new CommonResource(value, n);
				n.add(r);
			} else {
				CpuResource cpu = new CpuResource(n);
				cpu.setCycles((double) value);
				n.add(cpu);
			}
		}
	}
	
	public static void generateRandomCapacityResources(SubstrateNetwork sNetwork,
			int minCapacity, int maxCapacity, Random random,
			boolean useCommonRessource) {

		int value = Utils.rnd(minCapacity, maxCapacity, random);
		for (SubstrateNode n : sNetwork.getVertices()) {
			if (useCommonRessource) {
				n.add(new CommonResource(value, n));
			} else {
				n.add(new CapacityResource(value, n));
			}
		}
	}

	public static void generateRandomBandwidthResources(
			SubstrateNetwork sNetwork, int minResourceBandwidth,
			int maxResourceBandwidth, Random random,
			boolean useCommonConstraints) {

		for (SubstrateLink l : sNetwork.getEdges()) {
			int value = Utils.rnd(minResourceBandwidth, maxResourceBandwidth, random);
			
			if (useCommonConstraints) {
				CommonResource r = new CommonResource(value, l);
				l.add(r);
			} else {
				BandwidthResource bw = new BandwidthResource(l);
				bw.setBandwidth((double) value);
				l.add(bw);
			}
		}
	}

	/**
	 * This method generates MLS Labels for every node 
	 * @param sNetwork Substrate Network
	 * @param lattice A MLS Label lattice
	 */
	public static void generateMLSResources(SubstrateNetwork sNetwork, MLSLattice lattice, Long seed) {
		Random random = new Random();
		if(seed != null) 
			random.setSeed(seed);


		int maxLevel = lattice.getNumberOfLevels() - 1;
		ArrayList<String> cats = lattice.getCategories();

		//Label all Substratenodes (actual Random)
		for (SubstrateNode n :  sNetwork.getVertices()) {
			//Create Random resource
			int resDem = (int)((maxLevel + 1) * random.nextDouble());
			int resProv = (int)((maxLevel + 1) * random.nextDouble());

			//we need at least one Category
			int countCat;
			do {
				countCat = (int)((cats.size()+1) * random.nextDouble());
			} while (countCat < 0);
			ArrayList<String> chosenCats = new ArrayList<String>();
			for (int c = 0; c < countCat; c++) {
				String cat;
				do {
					cat = cats.get((int)(cats.size() * random.nextDouble()));
				} while (chosenCats.contains(cat));
				chosenCats.add(cat);
			}

			MLSResource res = new MLSResource(n, resDem, resProv, chosenCats);
			n.add(res);
		}
	}
	
	/**
	 * This method generates MLS Labels for every node so that all demands are fulfillable
	 * @param sNetwork Substrate Network
	 * @param lattice A MLS Label lattice
	 * @param seed Seed for random, null if fully random
	 */
	public static ArrayList<MLSResource> generateMLSResourcesReasonable(SubstrateNetwork sNetwork, MLSLattice lattice, Long seed) {

		ArrayList<MLSResource> resList = new ArrayList<MLSResource>();

		Random random = new Random();
		if(seed != null) 
			random.setSeed(seed);


		int maxLevel = lattice.getNumberOfLevels() - 1;
		ArrayList<String> cats = lattice.getCategories();

		//Label all Substratenodes (actual Random)
		for (SubstrateNode n : sNetwork.getVertices()) {
			//Create Random resource
			int resDem = (int)((maxLevel + 1) * random.nextDouble());
			int resProv = (int)((maxLevel + 1) * random.nextDouble());

			//we need at least one Category
			int countCat;
			do {
				countCat = (int)((cats.size()+1) * random.nextDouble());
			} while (countCat < 0);
			ArrayList<String> chosenCats = new ArrayList<String>();
			for (int c = 0; c < countCat; c++) {
				String cat;
				do {
					cat = cats.get((int)(cats.size() * random.nextDouble()));
				} while (chosenCats.contains(cat));
				chosenCats.add(cat);
			}

			MLSResource res = new MLSResource(n, resDem, resProv, chosenCats);
			resList.add(res);
			n.add(res);
		}

		return resList;

	}

}
