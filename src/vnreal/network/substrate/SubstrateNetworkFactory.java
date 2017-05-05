package vnreal.network.substrate;

import vnreal.constraints.resources.AbstractResource;
import vnreal.network.NetworkFactory;

public class SubstrateNetworkFactory implements NetworkFactory<AbstractResource, SubstrateNode, SubstrateLink, SubstrateNetwork> {

		long count = 0;
		boolean autoUnregisterConstraints;
		
		public SubstrateNetworkFactory(boolean autoUnregisterConstraints) {
			this.autoUnregisterConstraints = autoUnregisterConstraints;
		}
		
		@Override
		public SubstrateNetwork create(boolean directed) {
			return new SubstrateNetwork(autoUnregisterConstraints, directed);
		}
		
		@Override
		public SubstrateNetwork create() {
			return new SubstrateNetwork(autoUnregisterConstraints);
		}

		@Override
		public SubstrateNode createNode() {
			SubstrateNode sn = new SubstrateNode();
			sn.setName(count++ + "");
			return sn;
		}

		@Override
		public SubstrateLink createEdge() {
			SubstrateLink sl = new SubstrateLink();
			sl.setName(count++ + "");
			return sl;
		}

		@Override
		public void setLayer(int layer) {
			throw new AssertionError();
		}
		
}
