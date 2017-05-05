package tests.generators.network;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

import tests.generators.constraints.ConstraintsGenerator;
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

public class BarabasiAlbertNetworkGenerator<T extends AbstractConstraint, V extends Node<T>, E extends Link<T>, N extends Network<T, V, E>> implements NetworkGenerator<T, V, E, N> {
	
	final NetworkFactory<T, V, E, N> factory;
	
	public BarabasiAlbertNetworkGenerator(NetworkFactory<T, V, E, N> factory) {
		 this.factory = factory;
	}
	
	@Override
	public N generate(Random random,
			ConstraintsGenerator<N> constraintsGenerator,
			NetworkGeneratorParameter objParam) {
		
		BarabasiAlbertNetworkGeneratorParameter param = (BarabasiAlbertNetworkGeneratorParameter) objParam;
		
		N network = generateBarabasiNetwork(random, true, param.edgesToAttach, param.numTimeSteps, false);
		
		if (constraintsGenerator != null)
			constraintsGenerator.addConstraints(network, random);
		
//		sNetwork.generateDuplicateEdges();
		
		return network;
	}
	
	
	public N generateBarabasiNetwork(
			Random random, boolean directed, int numEdgesToAttach, int numTimeSteps, boolean autoUnregisterConstraints) {
		int init_vertices = 1;
		HashSet<V> seedVertices = new HashSet<V>();
		BarabasiAlbertEvolvingGraphGenerator<T, V, E, N> barabasiGen = null;
		if (random == null) {
			barabasiGen = new BarabasiAlbertEvolvingGraphGenerator<T, V, E, N>(
				factory, init_vertices,
				numEdgesToAttach, seedVertices,
				autoUnregisterConstraints,
				directed);
		} else {
			barabasiGen = new BarabasiAlbertEvolvingGraphGenerator<T, V, E, N>(
					factory, init_vertices,
					numEdgesToAttach, random, seedVertices,
					autoUnregisterConstraints,
					directed);			
		}

		barabasiGen.evolveGraph(numTimeSteps);
		
		return barabasiGen.create();
	}
	
	@Override
	public void setLayer(int layer) {
		this.factory.setLayer(layer);
	}
	
	
	public static class BarabasiAlbertNetworkGeneratorParameter implements NetworkGeneratorParameter {
		
		public final int edgesToAttach;
		public final int numTimeSteps;
		
		public BarabasiAlbertNetworkGeneratorParameter(
				int edgesToAttach,
				int numTimeSteps) {
			
			this.edgesToAttach = edgesToAttach;
			this.numTimeSteps = numTimeSteps;
		}
		
		@Override
		public String getSuffix(String prefix) {
			return prefix + "numTimeSteps:" + numTimeSteps +
					"_" + prefix + "edgesToAttach:" + edgesToAttach;
		}
		
		@Override
		public String toString(String prefix) {
			return prefix + "numTimeSteps:" + numTimeSteps +
					"\n" + prefix + "edgesToAttach:" + edgesToAttach;
		}
		
	}
	
	public static class BarabasiAlbertNetworkGeneratorParameters extends NetworkGeneratorParameters {
		
		public final int edgesToAttach;
		public final Integer[] numTimeStepsArray;
		
		public BarabasiAlbertNetworkGeneratorParameters(int edgesToAttach, Integer[] numTimeStepsArray) {
			this.edgesToAttach = edgesToAttach;
			this.numTimeStepsArray = numTimeStepsArray;
		}
		
		
		@Override
		public LinkedList<NetworkGeneratorParameter> getParams() {
			LinkedList<NetworkGeneratorParameter> result =
					new LinkedList<NetworkGeneratorParameter>();
			
			for (int numTimeSteps : numTimeStepsArray)
				result.add(new BarabasiAlbertNetworkGeneratorParameter(
						edgesToAttach, numTimeSteps));
			
			return result;
		}
		
	}

	public static BarabasiAlbertNetworkGenerator<AbstractResource, SubstrateNode, SubstrateLink, SubstrateNetwork> createSubstrateNetworkInstance() {
		return new BarabasiAlbertNetworkGenerator<AbstractResource, SubstrateNode, SubstrateLink, SubstrateNetwork>(new SubstrateNetworkFactory(false));
	}
	
	public static BarabasiAlbertNetworkGenerator<AbstractDemand, VirtualNode, VirtualLink, VirtualNetwork> createVirtualNetworkInstance() {
		return new BarabasiAlbertNetworkGenerator<AbstractDemand, VirtualNode, VirtualLink, VirtualNetwork>(new VirtualNetworkFactory(false));
	}


}
