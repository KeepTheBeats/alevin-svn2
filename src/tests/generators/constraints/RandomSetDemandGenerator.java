package tests.generators.constraints;

import java.util.Random;

import vnreal.algorithms.utils.SubgraphBasicVN.Utils;
import vnreal.constraints.demands.BandwidthDemand;
import vnreal.constraints.demands.CpuDemand;
import vnreal.constraints.demands.StaticMaxDelayDemand;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

public class RandomSetDemandGenerator implements ConstraintsGenerator<VirtualNetwork> {
	
	public final Integer[] CPUDemands; 
	public final Integer[] BandwidthDemands;
	public final Integer[] DelayDemands;
	public final int numDemandedLabels;
	public final String[] labels;
	
	public RandomSetDemandGenerator(Integer[] CPUDemands, Integer[] BandwidthDemands, Integer[] DelayDemands,
			int numDemandedLabels, String[] labels) {
		this.CPUDemands = CPUDemands;
		this.BandwidthDemands = BandwidthDemands;
		this.DelayDemands = DelayDemands;
		this.numDemandedLabels = numDemandedLabels;
		this.labels = labels;
	}
	
	@Override
	public void addConstraints(VirtualNetwork network, Random random) {
		if (CPUDemands != null && CPUDemands.length > 0)
			generateRandomCPUDemands(network, CPUDemands, random);
		
		if (BandwidthDemands != null && BandwidthDemands.length > 0)
			generateRandomBandwidthDemands(network, BandwidthDemands, random);
		
		if (DelayDemands != null && DelayDemands.length > 0)
			generateRandomDelayDemands(network, DelayDemands, random);
		
		if (labels != null)
			RandomDemandGenerator.generateRandomLabelDemands(network, numDemandedLabels, labels, random);
	}

	@Override
	public String getSuffix(String prefix) {
		return Utils.toString(prefix, this, true);
	}
	
	
	public static void generateRandomCPUDemands(VirtualNetwork vNetwork,
			Integer[] cpuDemands, Random random) {

		for (VirtualNode n : vNetwork.getVertices()) {
			CpuDemand cpu = new CpuDemand(n);
			int pos = Utils.rnd(0, cpuDemands.length - 1, random);
			int value = cpuDemands[pos];
			cpu.setDemandedCycles((double) value);
			n.add(cpu);
		}
	}
	
	public static void generateRandomBandwidthDemands(
			VirtualNetwork vNetwork,
			Integer[] bandwidthDemands, Random random) {

		for (VirtualLink l : vNetwork.getEdges()) {
			BandwidthDemand bw = new BandwidthDemand(l);
			int pos = Utils.rnd(0, bandwidthDemands.length - 1, random);
			int value = bandwidthDemands[pos];
			bw.setDemandedBandwidth((double) value);
			l.add(bw);
		}
	}
	
	public static void generateRandomDelayDemands(
			VirtualNetwork vNetwork,
			Integer[] delayDemands, Random random) {

		for (VirtualLink l : vNetwork.getEdges()) {
			int pos = Utils.rnd(0, delayDemands.length - 1, random);
			int value = delayDemands[pos];
			StaticMaxDelayDemand bw = new StaticMaxDelayDemand(l, value);
			l.add(bw);
		}
	}
	
}
