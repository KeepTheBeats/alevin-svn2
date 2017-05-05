package tests.generators.network;

import java.util.LinkedList;

import vnreal.algorithms.utils.SubgraphBasicVN.Utils;

public abstract class NetworkGeneratorParameters {
	
	public abstract LinkedList<NetworkGeneratorParameter> getParams();
	
	public String toString(String prefix) {
		return Utils.toString(prefix, this, "\n", " = ", true);
	}

}
