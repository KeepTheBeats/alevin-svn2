package vnreal.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

import tests.generators.constraints.ConstraintsGenerator;
import tests.generators.network.NetworkGenerator;
import tests.generators.network.NetworkGeneratorParameter;
import tests.generators.network.NetworkGeneratorParameters;
import vnreal.constraints.resources.AbstractResource;
import vnreal.constraints.resources.IdResource;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;


public class AdjListNetworkGenerator {

	private AdjListNetworkGenerator() {
		throw new Error();
	}

	public static class AdjListSubstrateNetworkGenerator implements NetworkGenerator<AbstractResource, SubstrateNode, SubstrateLink, SubstrateNetwork> {

		public AdjListSubstrateNetworkGenerator() {
		}

		@Override
		public SubstrateNetwork generate(Random random,
				ConstraintsGenerator<SubstrateNetwork> constraintsGenerator,
				NetworkGeneratorParameter objParam) {

			AdjListNetworkGeneratorParameter param = (AdjListNetworkGeneratorParameter) objParam;

			try {
				SubstrateNetwork snet = importSNet(param.networkName);
				if (constraintsGenerator != null)
					constraintsGenerator.addConstraints(snet, random);
				return snet;
			} catch (FileNotFoundException e) {
				throw new Error(e.getMessage());
			} catch (IOException e) {
				throw new Error(e.getMessage());
			}
		}
		
		@Override
		public void setLayer(int layer) {
			throw new AssertionError();
		}

	}

	public static class AdjListNetworkGeneratorParameter implements NetworkGeneratorParameter {

		public final String networkName;

		public AdjListNetworkGeneratorParameter(String networkName) {
			this.networkName = networkName;
		}

		@Override
		public String getSuffix(String prefix) {
			return prefix + "networkName:" + new File(this.networkName).getName().replace("_", "-");
		}
		
		@Override
		public String toString(String prefix) {
			return prefix + "networkName:" + new File(this.networkName).getName().replace("_", "-");
		}

	}

	public static class AdjListNetworkGeneratorParameters extends NetworkGeneratorParameters {

		public final String[] networkNameArray;

		public AdjListNetworkGeneratorParameters(String[] networkNameArray) {
			this.networkNameArray = networkNameArray;
		}

		@Override
		public LinkedList<NetworkGeneratorParameter> getParams() {
			LinkedList<NetworkGeneratorParameter> result =
					new LinkedList<NetworkGeneratorParameter>();

			for (String networkName : networkNameArray)
				result.add(new AdjListNetworkGeneratorParameter(networkName));

			return result;
		}

	}


	static SubstrateNetwork importSNet(String file) throws IOException {
		SubstrateNetwork result = new SubstrateNetwork();
		result.setName(file);
		HashMap<String, SubstrateNode> nodes = new HashMap<String, SubstrateNode>();

		BufferedReader r = null;
		try {
			r = new BufferedReader(new FileReader(file));
			for (String line = null; (line = r.readLine()) != null; ) {
				if (line.trim().startsWith("#"))
					continue;

				String[] split = line.split(" ");

				SubstrateNode source = nodes.get(split[0]);
				if (source == null) {
					source = new SubstrateNode();
					source.setName(split[0] + "");
					source.add(new IdResource(split[0], source, result));
					nodes.put(split[0], source);
					result.addVertex(source);
				}

				for (int i = 1; i < split.length; ++i) {
					SubstrateNode node = nodes.get(split[i]);
					if (node == null) {
						node = new SubstrateNode();
						node.setName(split[i]);
						node.add(new IdResource(split[i], node, result));
						nodes.put(split[i], node);
						result.addVertex(node);
					}

					SubstrateLink link = new SubstrateLink();
					result.addEdge(link, source, node);
				}

			}
		} finally {
			if (r != null)
				r.close();
		}

		return result;
	}

}
