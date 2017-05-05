package vnreal.network;

import org.apache.commons.collections15.Factory;

import vnreal.constraints.AbstractConstraint;


public interface NetworkFactory<T extends AbstractConstraint, V extends Node<T>, E extends Link<T>, N extends Network<T, V, E>>
extends Factory<N> {
	
	public N create(boolean directed);
	public V createNode();
	public E createEdge();
	public void setLayer(int layer);
	
}
