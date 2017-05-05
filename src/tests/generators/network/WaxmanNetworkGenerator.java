package tests.generators.network;

import java.util.LinkedList;
import java.util.Random;

import mulavito.graph.generators.WaxmanGraphGenerator;
import tests.generators.constraints.ConstraintsGenerator;
import vnreal.algorithms.utils.SubgraphBasicVN.Utils;
import vnreal.constraints.AbstractConstraint;
import vnreal.constraints.demands.AbstractDemand;
import vnreal.constraints.resources.AbstractResource;
import vnreal.network.Link;
import vnreal.network.Network;
import vnreal.network.NetworkFactory;
import vnreal.network.Node;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNetworkFactory;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNetworkFactory;
import vnreal.network.virtual.VirtualNode;

public class WaxmanNetworkGenerator<T extends AbstractConstraint, V extends Node<T>, E extends Link<T>, N extends Network<T, V, E>> implements NetworkGenerator<T, V, E, N> {
	
	final NetworkFactory<T, V, E, N> factory;
	
	public WaxmanNetworkGenerator(NetworkFactory<T, V, E, N> factory) {
		this.factory = factory;
	}
	
	@Override
	public N generate(Random random,
			ConstraintsGenerator<N> constraintsGenerator,
			NetworkGeneratorParameter objParam) {
		
		WaxmanNetworkGeneratorParameter param = (WaxmanNetworkGeneratorParameter) objParam;
		
		WaxmanGraphGenerator<V, E> generator = new WaxmanGraphGenerator<V, E>(
				random,
				param.alpha, param.beta, false);
		
		N network = null;
		while (true) {
			network = factory.create();
			for (int i = 0; i < param.numNodes; ++i) {
				V sn = factory.createNode();
				sn.setName(sn.getId() + "");
				network.addVertex(sn);
			}
			generator.generate(network);
			
			if (!param.forceConnectivity || network.isConnected())
				break;
		}
		
		if (constraintsGenerator != null)
			constraintsGenerator.addConstraints(network, random);
		
//		sNetwork.generateDuplicateEdges();
		
		return network;
	}
	
	@Override
	public void setLayer(int layer) {
		this.factory.setLayer(layer);
	}


	public static class WaxmanNetworkGeneratorParameter implements NetworkGeneratorParameter {
		
		public final int numNodes;
		
		public final double alpha;
		public final double beta;
		public final boolean forceConnectivity;
		
		public WaxmanNetworkGeneratorParameter(
				int numNodes,
				double alpha, double beta,
				boolean forceConnectivity) {
			this.numNodes = numNodes;
			this.alpha = alpha;
			this.beta = beta;
			this.forceConnectivity = forceConnectivity;
		}
		
		@Override
		public String getSuffix(String prefix) {
			return Utils.toString(prefix, this);
		}
		
		@Override
		public String toString(String prefix) {
			return Utils.toString(prefix, this, "\n", " = ");
		}
	}
	
	public static class WaxmanNetworkGeneratorParameters extends NetworkGeneratorParameters {
		
		public final Integer[] numNodesArray;
		
		public final double alpha;
		public final double beta;
		public final boolean forceConnectivity;
		
		public WaxmanNetworkGeneratorParameters(Integer[] numNodesArray, double alpha, double beta, boolean forceConnectivity) {
			this.numNodesArray = numNodesArray;
			this.alpha = alpha;
			this.beta = beta;
			this.forceConnectivity = forceConnectivity;
		}
		
		@Override
		public LinkedList<NetworkGeneratorParameter> getParams() {
			LinkedList<NetworkGeneratorParameter> result =
					new LinkedList<NetworkGeneratorParameter>();
			
			for (int numNodes : numNodesArray)
				result.add(new WaxmanNetworkGeneratorParameter(
						numNodes, alpha, beta, forceConnectivity));
			
			return result;
		}
		
	}
	
	public static WaxmanNetworkGenerator<AbstractResource, SubstrateNode, SubstrateLink, SubstrateNetwork> createSubstrateNetworkInstance() {
		return new WaxmanNetworkGenerator<AbstractResource, SubstrateNode, SubstrateLink, SubstrateNetwork>(new SubstrateNetworkFactory(false));
	}
	
	public static WaxmanNetworkGenerator<AbstractDemand, VirtualNode, VirtualLink, VirtualNetwork> createVirtualNetworkInstance() {
		return new WaxmanNetworkGenerator<AbstractDemand, VirtualNode, VirtualLink, VirtualNetwork>(new VirtualNetworkFactory(false));
	}

}
