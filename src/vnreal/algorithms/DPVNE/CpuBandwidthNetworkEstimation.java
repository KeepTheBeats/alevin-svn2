package vnreal.algorithms.DPVNE;

import java.util.Collection;

import vnreal.constraints.demands.AbstractDemand;
import vnreal.constraints.demands.CpuDemand;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;
import vnreal.constraints.resources.AbstractResource;
import vnreal.constraints.resources.CpuResource;

public class CpuBandwidthNetworkEstimation implements NetworkEstimation {

	//private double vcpuWeight
	private final double vbandwidthWeight;
	
	public CpuBandwidthNetworkEstimation(
			//double vcpuWeight,
			double vbandwidthWeight) {
		//this.vcpuWeight = vcpuWeight;
		this.vbandwidthWeight = vbandwidthWeight;
	}

	@Override
	public double estimate(SubstrateNetwork sNetwork, VirtualNetwork vNetwork) {
		
		if (sNetwork.getVertexCount() < vNetwork.getVertexCount())
			return 0;
		
		double cpu = estimateCPU(sNetwork.getVertices(), vNetwork.getVertices());
		double bandwidth = estimateBandwith(sNetwork.getEdges(), vNetwork.getEdges());
		
		if (cpu < 1 || bandwidth < 1)
			return 0;
		
		double result = (cpu * bandwidth);

		return result;
	}
	
	
	
	
//	@Override
//	public double estimate(NetworkInformation sInfo, VirtualNetworkInformation vInfo) {
//		
//		if (sInfo.getNodeCount() < vInfo.getNodeCount())
//			return 0;
//		
//		double cpu = (vInfo.getSumCPU() == 0.0d) ? (100.0d) : (sInfo.getSumCPU() / vInfo.getSumCPU());
//		double bandwidth = (vInfo.getSumBandwidth() == 0.0d) ? (100.0d) : (sInfo.getSumBandwidth() / (vbandwidthWeight * vInfo.getSumBandwidth()));
//		
//		double result = (cpu * bandwidth);
//
//		return result;
//	}
	
	

	public double estimateBandwith(Collection<SubstrateLink> sLinks,
			Collection<VirtualLink> vLinks) {
		
		double bandwidthres = 0.0d;
		for (SubstrateLink sl : sLinks) {
			for (AbstractResource r : sl) {
				if (r instanceof CpuResource) {
					double c = ((CpuResource) r).getAvailableCycles();
					bandwidthres += c;
					break;
				}
			}
		}
		
		double bandwidthdem = 0.0d;
		for (VirtualLink vl : vLinks) {
			for (AbstractDemand d : vl) {
				if (d instanceof CpuDemand) {
					double c = ((CpuDemand) d).getDemandedCycles();
					bandwidthdem += c;
					break;
				}
			}
		}
		
		double bandwidth = (bandwidthdem == 0.0d) ? (100.0d) : (bandwidthres / (vbandwidthWeight * bandwidthdem));
		
		return bandwidth;
	}
	
	
	public double estimateCPU(Collection<SubstrateNode> sNodes,
			Collection<VirtualNode> vNodes) {
		
		double cpures = 0.0d;
		for (SubstrateNode vn : sNodes) {
			for (AbstractResource r : vn) {
				if (r instanceof CpuResource) {
					double c = ((CpuResource) r).getAvailableCycles();
					cpures += c;
					break;
				}
			}
		}
		double cpudem = 0.0d;
		for (VirtualNode vn : vNodes) {
			for (AbstractDemand d : vn) {
				if (d instanceof CpuDemand) {
					double c = ((CpuDemand) d).getDemandedCycles();
					cpudem += c;
					break;
				}
			}
		}
		
		double cpu = (cpudem == 0.0d) ? (100.0d) : (cpures / cpudem);
		
		return cpu;
	}
	
	
	public String toString() {
		return "CpuBandwidthNetworkEstimation"
				//+ "_cpuWeight" + vcpuWeight
				+ "_bandwidthWeight" + vbandwidthWeight;
	}
	
}
