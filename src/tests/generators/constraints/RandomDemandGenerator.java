package tests.generators.constraints;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;

import vnreal.algorithms.utils.SubgraphBasicVN.Utils;
import vnreal.constraints.demands.BandwidthDemand;
import vnreal.constraints.demands.CommonDemand;
import vnreal.constraints.demands.CpuDemand;
import vnreal.constraints.demands.LabelDemand;
import vnreal.constraints.demands.StaticMaxDelayDemand;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

public class RandomDemandGenerator implements ConstraintsGenerator<VirtualNetwork> {
	
	public final RandomDemandGeneratorParameter param;
	public boolean useCommonConstraints = false;
	
	public RandomDemandGenerator(RandomDemandGeneratorParameter param) {
		this.param = param;
	}
	
	public void addConstraints(VirtualNetwork network, Random random) {
		if (param.minCPUDemand != -1)
			generateRandomCPUDemands(network, param.minCPUDemand, param.maxCPUDemand, random, useCommonConstraints);
		
		if (param.minBandwidthDemand != -1)
			generateRandomBandwidthDemands(network, param.minBandwidthDemand, param.maxBandwidthDemand, random, useCommonConstraints);
		
		if (param.minDelayDemand != -1)
			generateRandomMaxDelayDemands(network, param.minDelayDemand, param.maxDelayDemand, random);
		
		if (param.labels != null)
			generateRandomLabelDemands(network, param.numDemandedLabels, param.labels, random);
	}
	
	public static void generateRandomLabelDemands(VirtualNetwork network,
			int numDemandedLabels, String[] labels, Random rnd) {
		
		for (VirtualNode vn : network.getVertices()) {
			LinkedList<String> labelsShuffle = new LinkedList<String>();
			for (String s : labels)
				labelsShuffle.add(s);
			Collections.shuffle(labelsShuffle, rnd);
			
			LinkedList<String> demandedLabels = new LinkedList<String>();
			int i = 0;
			for (String label : labelsShuffle) {
				if (i >= numDemandedLabels)
					break;
				
				demandedLabels.add(label);
				i++;
			}
			vn.add(new LabelDemand(vn, demandedLabels));
		}
	}

	public String getSuffix(String prefix) {
		return Utils.toString(prefix, param, true);
	}
	
	
	public static class RandomDemandGeneratorParameter {
		
		public final int minCPUDemand;
		public final int maxCPUDemand;
		
		public final int minBandwidthDemand;
		public final int maxBandwidthDemand;
		
		public final int minDelayDemand;
		public final int maxDelayDemand;
		
		public final int numDemandedLabels;
		public final String[] labels;

		public RandomDemandGeneratorParameter(
				int minCPUDemand, int maxCPUDemand,
				int minBandwidthDemand, int maxBandwidthDemand,
				int minDelayDemand, int maxDelayDemand,
				int numDemandedLabels,
				String[] labels) {
			super();
			this.minCPUDemand = minCPUDemand;
			this.maxCPUDemand = maxCPUDemand;
			this.minBandwidthDemand = minBandwidthDemand;
			this.maxBandwidthDemand = maxBandwidthDemand;
			this.minDelayDemand = minDelayDemand;
			this.maxDelayDemand = maxDelayDemand;
			this.numDemandedLabels = numDemandedLabels;
			this.labels = labels;
		}
		
	}
	
	
	public static void generateRandomMaxDelayDemands(VirtualNetwork vNetwork, int minDelayDemand, int maxDelayDemand, Random random) {
		for (VirtualLink n : vNetwork.getEdges()) {
			int value = Utils.rnd(minDelayDemand, maxDelayDemand, random);
			n.add(new StaticMaxDelayDemand(n, value));
		}
	}
	
	public static void generateRandomCPUDemands(VirtualNetwork vNetwork,
			int minDemandCPU, int maxDemandCPU, Random random, boolean useCommonConstraints) {

		for (VirtualNode n : vNetwork.getVertices()) {
			int value = Utils.rnd(minDemandCPU, maxDemandCPU, random);
			if (useCommonConstraints) {
				CommonDemand d = new CommonDemand(value, n);
				n.add(d);
			} else {
				CpuDemand cpu = new CpuDemand(n);
				cpu.setDemandedCycles((double) value);
				n.add(cpu);
			}
		}
	}
	
	public static void generateRandomBandwidthDemands(
			VirtualNetwork vNetwork, int minDemandBandwidth,
			int maxDemandBandwidth, Random random,
			boolean useCommonConstraints) {
		
		for (VirtualLink l : vNetwork.getEdges()) {
			int value = Utils.rnd(minDemandBandwidth, maxDemandBandwidth, random);
			if (useCommonConstraints) {
				CommonDemand d = new CommonDemand(value, l);
				l.add(d);
			} else {
				BandwidthDemand bw = new BandwidthDemand(l);
				bw.setDemandedBandwidth((double) value);
				l.add(bw);
			}
		}
	}
	
}
