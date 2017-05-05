/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2010-2011, The VNREAL Project Team.
 * 
 * This work has been funded by the European FP7
 * Network of Excellence "Euro-NF" (grant agreement no. 216366)
 * through the Specific Joint Developments and Experiments Project
 * "Virtual Network Resource Embedding Algorithms" (VNREAL). 
 *
 * The VNREAL Project Team consists of members from:
 * - University of Wuerzburg, Germany
 * - Universitat Politecnica de Catalunya, Spain
 * - University of Passau, Germany
 * See the file AUTHORS for details and contact information.
 * 
 * This file is part of ALEVIN (ALgorithms for Embedding VIrtual Networks).
 *
 * ALEVIN is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License Version 3 or later
 * (the "GPL"), or the GNU Lesser General Public License Version 3 or later
 * (the "LGPL") as published by the Free Software Foundation.
 *
 * ALEVIN is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * or the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License and
 * GNU Lesser General Public License along with ALEVIN; see the file
 * COPYING. If not, see <http://www.gnu.org/licenses/>.
 *
 * ***** END LICENSE BLOCK ***** */
package vnreal.network;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import mulavito.graph.ILayer;
import vnreal.constraints.AbstractConstraint;
import edu.uci.ics.jung.graph.DirectedOrderedSparseMultigraph;
import edu.uci.ics.jung.graph.ObservableGraph;
import edu.uci.ics.jung.graph.UndirectedOrderedSparseMultigraph;

@SuppressWarnings("serial")
public abstract class Network<T extends AbstractConstraint, V extends Node<T>, E extends Link<T>>
		extends ObservableGraph<V, E> implements ILayer<V, E> {

	private boolean autoUnregisterConstraints = true;
	private boolean directed = true;
	private String name = null;
	protected int layer = 0;

	protected Network(boolean autoUnregisterConstraints) {
		this(autoUnregisterConstraints, true);
	}
		
	protected Network(boolean autoUnregisterConstraints, boolean directed) {
		super(directed ? new DirectedOrderedSparseMultigraph<V, E>() : new UndirectedOrderedSparseMultigraph<V,E>());
		this.autoUnregisterConstraints = autoUnregisterConstraints;
		this.directed = directed;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setLayer(int layer) {
		this.layer = layer;
		for (V v : this.getVertices()) {
			v.setLayer(layer);
		}
		for (E e : this.getEdges()) {
			e.setLayer(layer);
		}
	}
	
	@Override
	public boolean removeVertex(V v) {
		if (autoUnregisterConstraints) {
			// unregister all mappings first
			boolean unregistered = true;
			List<T> constraints = v.get();
			for (T cons : constraints)
				unregistered = unregistered && cons.unregisterAll();
			return (unregistered && super.removeVertex(v));
		} else {
			return super.removeVertex(v);
		}
	}
	
	public abstract void generateDuplicateEdges();

	@Override
	public boolean removeEdge(E e) {
		if (autoUnregisterConstraints) {
			// unregister all mappings first
			boolean unregistered = true;
			List<T> constraints = e.get();
			for (T cons : constraints)
				unregistered = unregistered && cons.unregisterAll();
			return (unregistered && super.removeEdge(e));
		} else {
			return super.removeEdge(e);
		}
	}

	public abstract Network<T, V, E> getInstance(boolean autoUnregister);

	public abstract Network<T, V, E> getCopy(boolean autoUnregister, boolean deep);
	
	/**
	 * Tries to find a Entity by name
	 * 
	 * @param name Name of the Entity
	 * @return The Entity or null if not found
	 */
	public NetworkEntity<T> getEntitieByName(String name) {
		for(Node<T> n : getVertices()) {
			if(n.getName().equals(name))
				return n;
		}
		
		for(Link<T> l : getEdges()) {
			if(l.getName().equals(name))
				return l;
		}
		
		return null;
	}
	
	public boolean isConnected() {
		LinkedList<V> nodes = new LinkedList<V>(getVertices());
		V first = nodes.getFirst();
		for (V n : getVertices()) {
			if (n == first)
				continue;

			if (findUndirectedPath(first, n).isEmpty()) {
				return false;
			}
		}
		return true;
	}
	
	public boolean isDirected() {
		return this.directed;
	}
	
	Collection<List<E>> findUndirectedPath(
			V n1, V n2) {
		Collection<List<E>> result = new LinkedList<List<E>>();
		findUndirectedPath(n1, n2,
				new LinkedList<V>(),
				new LinkedList<E>(),
				result);
	
		return result;
	}
	
	
	void findUndirectedPath(V n1, V n2,
			Collection<V> visited,
			List<E> currentPath,
			Collection<List<E>> result) {
		if (n1 == n2) {
			result.add(currentPath);
			return;
		}
		if (visited.contains(n1)) {
			return;
		}
		visited.add(n1);
	
		LinkedList<E> ls = new LinkedList<E>();
		ls.addAll(getOutEdges(n1));
		ls.addAll(getInEdges(n1));
		for (E l : ls) {
			List<E> newPath = new LinkedList<E>(
					currentPath);
			newPath.add(l);
			findUndirectedPath(getOpposite(n1, l), n2,
					visited, newPath, result);
		}
	}
	
	public abstract LinkedList<String> removeDuplicateEdges();
	
	
}
