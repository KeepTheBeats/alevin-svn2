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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import vnreal.constraints.AbstractConstraint;

/**
 * The building blocks of a network: either node or link.
 * 
 * @author Michael Duelli
 * 
 * @param <T>
 *            The constraint parameter, either resource or demand.
 */
public abstract class NetworkEntity<T extends AbstractConstraint> implements
		Iterable<T> {
	private final HashMap<Class<? extends AbstractConstraint>, T> constraints = new HashMap<Class<? extends AbstractConstraint>, T>();

	private static Object syncObject = new Object();
	
	private String name = null, type = null;

	private final long id;
	
	private int layer;

	/**
	 * Used in GUI methods.
	 */
	protected NetworkEntity(int layer) {
		this.layer = layer;
		
		synchronized (syncObject) {
			this.id = IDSource.getID();
		}
		
		this.name = this.id + "";
	}
	
	
	public void setLayer(int layer) {
		this.layer = layer;
	}
	
	public int getLayer() {
		return layer;
	}

	/**
	 * Add a constraint.
	 * 
	 * @param t
	 *            The considered constraint.
	 * @return true on success, false otherwise.
	 */
	public final boolean add(T t) {
		if (preAddCheck(t)) {
			if (t.getOwner() == this) {
				constraints.put(t.getClass(), t);
				return true;
			} else {
				System.err.println("Cannot add constraint " + t.getClass().getSimpleName() + " to entity "
						+ this + " because owner != " + this);
				return false;
			}
		}
		return false;
	}

	protected abstract boolean preAddCheck(T t);
	
	/**
	 * Get a particular constraint from the list of constraints. The constraint is identified by its class object.
	 * @param constraint The class object
	 * @return Null, if no matching constraint is found; the respective AbstractConstraint otherwise
	 * (still has to be cast by the caller).
	 */
	public final T get(Class<? extends AbstractConstraint> constraint) {
		return constraints.get(constraint);
	}

	/**
	 * Remove a constraint.
	 * 
	 * @param t
	 *            The considered constraint.
	 * @return true on success, false otherwise.
	 */
	public final boolean remove(T t) {
		if (constraints.containsKey(t.getClass())) {
			constraints.remove(t.getClass());
			return t.unregisterAll();
		}
		System.err.println("Cannot remove constraint " + t + " from entity "
				+ this + ": Constraint not found in list.");
		return false;
	}

	public final void removeAll() {
		for (AbstractConstraint c : constraints.values()) {
			c.unregisterAll();
		}
		constraints.clear();
	}

	public final List<T> get() {
		return Collections.unmodifiableList(new ArrayList<T>(constraints.values()));
	}

	@Override
	public final Iterator<T> iterator() {
		return constraints.values().iterator();
	}

	public final long getId() {
		return id;
	}

	public abstract String toStringShort();

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public abstract NetworkEntity<T> getCopy(boolean deepCopy);
	
}
