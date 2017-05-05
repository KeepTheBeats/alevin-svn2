package tests.generators.constraints;

import java.util.ArrayList;
import java.util.Random;

import vnreal.algorithms.utils.mls.MLSLattice;
import vnreal.constraints.demands.MLSDemand;
import vnreal.constraints.demands.NullDemand;
import vnreal.constraints.resources.MLSResource;
import vnreal.constraints.resources.MultiCoreEnergyResource;
import vnreal.constraints.resources.NullResource;
import vnreal.constraints.resources.PowerResource;
import vnreal.constraints.resources.StaticEnergyResource;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

public class ConstraintsUtils {
	
	private ConstraintsUtils() {
		throw new Error();
	}



	public static void generatePowerResources(
			SubstrateNetwork sNetwork) {
		for (SubstrateNode n : sNetwork.getVertices()) {
			PowerResource r = new PowerResource(n);

			n.add(r);
		}
	}

	public static void generateFixedStaticEnergyConsumptionResources(
			SubstrateNetwork sNetwork, int consumption) {
		for (SubstrateNode n : sNetwork.getVertices()) {
			StaticEnergyResource r = new StaticEnergyResource(n, consumption);

			n.add(r);
		}
	}

	public static void generateRandomStaticEnergyConsumptionResources(
			SubstrateNetwork sNetwork,
			int myminConsumption, int mymaxConsumption, Long seed) {
		Random random = new Random();
		if(seed != null) 
			random.setSeed(seed);


		for (SubstrateNode n : sNetwork.getVertices()) {
			int value = (int) (myminConsumption + (mymaxConsumption
					- myminConsumption + 1)
					* random.nextDouble());
			StaticEnergyResource r = new StaticEnergyResource(n, value);

			n.add(r);
		}
	}

	public static void generateMultiCoreEnergyConsumptionResources(
			SubstrateNetwork sNetwork, int idleConsumption,
			int additionalConsumptionPerCore, int numberOfCores) {

		for (SubstrateNode n : sNetwork.getVertices()) {
			MultiCoreEnergyResource r = new MultiCoreEnergyResource(n,
					idleConsumption, additionalConsumptionPerCore,
					numberOfCores);

			n.add(r);
		}
	}

	/**
	 * This method generates MLS Labels for every node 
	 * @param vNetwork Virtual Network
	 * @param lattice A MLS Label lattice
	 */
	public static void generateMLSDemands(VirtualNetwork vNetwork, MLSLattice lattice, Long seed) {
		Random random = new Random();
		if(seed != null) 
			random.setSeed(seed);


		int maxLevel = lattice.getNumberOfLevels() - 1;
		ArrayList<String> cats = lattice.getCategories();

		for (VirtualNode n : vNetwork.getVertices()) {
			//Create Random resource
			int resDem = (int)((maxLevel + 1) * random.nextDouble());
			int resProv = (int)((maxLevel + 1) * random.nextDouble());

			//we need at least one Category
			int countCat;
			do {
				countCat = (int)((cats.size()+1) * random.nextDouble());
			} while (countCat < 1);
			ArrayList<String> chosenCats = new ArrayList<String>();
			for (int c = 0; c < countCat; c++) {
				String cat;
				do {
					cat = cats.get((int)(cats.size() * random.nextDouble()));
				} while (chosenCats.contains(cat));
				chosenCats.add(cat);
			}

			MLSDemand res = new MLSDemand(n, resDem, resProv, chosenCats);
			n.add(res);
		}
	}





	/**
	 * This method generates MLS Labels for every node so that all demands are fulfillable
	 * @param vNetwork Virtual Network
	 * @param lattice A MLS Label lattice
	 * @param resList ArrayList of MLSRessources
	 * @param seed Seed for random, null if fully random
	 */
	public static void generateMLSDemandsReasonable(VirtualNetwork vNetwork, MLSLattice lattice, ArrayList<MLSResource> resList, Long seed) {

		Random random = new Random();
		if(seed != null) 
			random.setSeed(seed);

		int maxLevel = lattice.getNumberOfLevels() - 1;
		ArrayList<String> cats = lattice.getCategories();

		for (VirtualNode n : vNetwork.getVertices()) {

			MLSDemand dem;
			do {
				//Create Random resource
				int resDem = (int)((maxLevel + 1) * random.nextDouble());
				int resProv = (int)((maxLevel + 1) * random.nextDouble());

				//we need at least one Category
				int countCat;
				do {
					countCat = (int)((cats.size()+1) * random.nextDouble());
				} while (countCat < 1);
				ArrayList<String> chosenCats = new ArrayList<String>();
				for (int c = 0; c < countCat; c++) {
					String cat;
					do {
						cat = cats.get((int)(cats.size() * random.nextDouble()));
					} while (chosenCats.contains(cat));
					chosenCats.add(cat);
				}

				dem = new MLSDemand(n, resDem, resProv, chosenCats);
			}while(!checkMLSWorking(resList, dem));


			n.add(dem);
		}
	}

	/**
	 * Checks if the given demand is mappable on at least one Resource
	 * @param resList List of {@link MLSResource}
	 * @param dem {@link MLSDemand}
	 * @return true if mappable on at least one resource, false otherwise
	 */
	private static boolean checkMLSWorking(ArrayList<MLSResource> resList, MLSDemand dem) {
		for(MLSResource res : resList) {
			if(dem.getAcceptsVisitor().visit(res) && dem.getFulfillsVisitor().visit(res))
				return true;
		}
		return false;
	}


	/**
	 * This method generates Null Labels for every SubstrateNode 
	 * Only needed when you don't have other Demands or Resources, and metrics need them
	 */
	public static void generateNullResources(SubstrateNetwork sNetwork) {
		//Label all Substratenodes
		for (SubstrateNode n : sNetwork.getVertices()) {
			NullResource res = new NullResource(n);
			n.add(res);
		}

		for(SubstrateLink l : sNetwork.getEdges()) {
			NullResource res = new NullResource(l);
			l.add(res);
		}

	}

	/**
	 * This method generates Null Labels for every VirtualNode of the given network
	 * Only needed when you don't have other Demands or Resources, and metrics need them
	 */
	public static void generateNullDemands(VirtualNetwork vNetwork) {
		for (VirtualNode n : vNetwork.getVertices()) {				
			NullDemand res = new NullDemand(n);
			n.add(res);
		}

		for (VirtualLink l : vNetwork.getEdges()) {
			NullDemand res = new NullDemand(l);
			l.add(res);
		}
	}


}
