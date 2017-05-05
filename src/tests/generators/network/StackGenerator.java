package tests.generators.network;

import java.util.LinkedList;
import java.util.Random;

import tests.generators.constraints.ConstraintsGenerator;
import vnreal.constraints.demands.AbstractDemand;
import vnreal.constraints.resources.AbstractResource;
import vnreal.network.NetworkStack;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

public class StackGenerator {

	final NetworkGenerator<AbstractResource, SubstrateNode, SubstrateLink, SubstrateNetwork> sNetGenerator;
	final ConstraintsGenerator<SubstrateNetwork> sNetConstraintGenerator;

	final NetworkGenerator<AbstractDemand, VirtualNode, VirtualLink, VirtualNetwork> vNetGenerator;
	final ConstraintsGenerator<VirtualNetwork> vNetConstraintGenerator;


	public StackGenerator(
			NetworkGenerator<AbstractResource, SubstrateNode, SubstrateLink, SubstrateNetwork> sNetGenerator,
			ConstraintsGenerator<SubstrateNetwork> sNetConstraintGenerator,
			NetworkGenerator<AbstractDemand, VirtualNode, VirtualLink, VirtualNetwork> vNetGenerator,
			ConstraintsGenerator<VirtualNetwork> vNetConstraintGenerator) {

		this.sNetGenerator = sNetGenerator;
		this.sNetConstraintGenerator = sNetConstraintGenerator;
		this.vNetGenerator = vNetGenerator;
		this.vNetConstraintGenerator = vNetConstraintGenerator;
	}


	public NetworkStack generate(int numVNets,
			NetworkGeneratorParameter sNetGeneratorParam,
			NetworkGeneratorParameter vNetGeneratorParam,
			Random random) {

		SubstrateNetwork sNetwork = sNetGenerator.generate(random, sNetConstraintGenerator, sNetGeneratorParam);

		LinkedList<VirtualNetwork> vNetworks = new LinkedList<VirtualNetwork>();
		for (int layer = 1; layer <= numVNets; ++layer) {
			boolean validated = false;
			while (!validated) {
				VirtualNetwork vNetwork = vNetGenerator.generate(random, vNetConstraintGenerator, vNetGeneratorParam);

				if (!vNetwork.isConnected()) {
					System.out.println("NOTE: vNet was not connected -> regenerate");
					continue;
				}

				validated = true;
				vNetworks.add(vNetwork);
			}
		}

		return new NetworkStack(sNetwork, vNetworks);
	}

}
