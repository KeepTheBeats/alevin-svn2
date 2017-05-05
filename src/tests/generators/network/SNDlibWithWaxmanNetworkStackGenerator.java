package tests.generators.network;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import mulavito.graph.generators.WaxmanGraphGenerator;
import tests.generators.GeneratorParameter;
import vnreal.core.Scenario;
import vnreal.core.oldFramework.ConversionHelper;
import vnreal.io.SNDlibImporter;
import vnreal.network.NetworkStack;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

/**
 * This generator is used to import Networks from SNDlib
 * 
 * @author Fabian Kokot
 *
 */
@GeneratorParameter(
		parameters = { "TR:SNDlibFile", "TR:Waxman_alpha", "TR:Waxman_beta" , "TR:NumVNodesPerNet", "TR:NumVNets", "Seed:Seed"}
) 
public class SNDlibWithWaxmanNetworkStackGenerator extends AbstractNetworkStackGenerator {

	@Override
	public NetworkStack generate(ArrayList<Object> parameters) {
		String filename = ConversionHelper.paramObjectToString(parameters.get(0));
		Double alpha = (Double) parameters.get(1);
		Double beta = (Double) parameters.get(2);
		Integer numVNodesPerNet = ConversionHelper.paramObjectToInteger(parameters.get(3));
		Integer numVNets = ConversionHelper.paramObjectToInteger(parameters.get(4));
		Long seed = ConversionHelper.paramObjectToLong(parameters.get(5));
		
		Random random = seed == null ? new Random() : new Random(seed);
		
		try {
			SNDlibImporter importer = new SNDlibImporter(filename, true);
			
			Scenario scen = new Scenario();
			importer.setNetworkStack(scen);
			
			List<VirtualNetwork> vList = createVirtualNetworks(alpha, beta, numVNets, numVNodesPerNet, random);
			
			return new NetworkStack(scen.getNetworkStack().getSubstrate(), vList);
			
		} catch (FileNotFoundException e) {
			throw new Error("SNDLib import failed with "+e.getLocalizedMessage());
		}
	}

	/**
	 * This method creates the virtual networks
	 * 
	 * @param alpha
	 * @param beta
	 * @param numVNets
	 * @param numVNodesPerNet
	 * @return
	 */
	protected List<VirtualNetwork> createVirtualNetworks(Double alpha,
			Double beta, Integer numVNets, Integer numVNodesPerNet, Random random) {
		WaxmanGraphGenerator<VirtualNode, VirtualLink> vGenerator = new WaxmanGraphGenerator<VirtualNode, VirtualLink>(
				random,
				alpha, beta, false);
		
		
		List<VirtualNetwork> vNetworks = new LinkedList<VirtualNetwork>();
		for (int i = 1; i <= numVNets; ++i) {
			VirtualNetwork vNetwork = new VirtualNetwork(i);
			vNetwork.setName(i + "");
			for (int n = 0; n < numVNodesPerNet; ++n) {
				VirtualNode vn = new VirtualNode(i);

				vNetwork.addVertex(vn);
			}
			vGenerator.generate(vNetwork);
			
			vNetworks.add(vNetwork);
		}
		
		return vNetworks;
	}
	
	
	@Override
	public void reset() {

	}

}
