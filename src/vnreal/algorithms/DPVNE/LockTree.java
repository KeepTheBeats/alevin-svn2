package vnreal.algorithms.DPVNE;

import java.util.Collection;
import java.util.LinkedList;

import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualNetwork;

public class LockTree {

	private LockTree parent;
	private ClusterHead clusterHead;
	private Collection<LockTree> children;

	public TreeState state;
	public int locks;
	public Collection<LockTree> childsMapping;
	
	private Integer depthOfTree = null;
	
	Collection<UpdateEntry> updates = new LinkedList<UpdateEntry>();
	
	final String name;

	/**
	 * Creates a new LockTree
	 * @param name Name 
	 * @param parent Parent Locktree
	 * @param clusterHead The ClusterHead
	 */
	public LockTree(String name, LockTree parent, ClusterHead clusterHead) {

		this.name = name;
		
		this.parent = parent;
		this.clusterHead = clusterHead;
		this.state = TreeState.UNLOCKED;

		this.locks = 0;
		this.childsMapping = new LinkedList<LockTree>();
	}
	
	public int getDepth() {
		if (depthOfTree == null) {
			throw new AssertionError();
		}
		return depthOfTree;
	}
	
	public void setDepth(int depth) {
		this.depthOfTree = depth;
	}
	
	public String getName() {
		return name;
	}
	
	public LockTree getSubLockTreeFor(ClusterHead find) {
		LockTree childResult = getSubLockTreeFor_children(find);
		if (childResult != null) {
			return childResult;
		}
		
		for (LockTree p = parent; p != null; p = p.parent) {
			if (p.clusterHead == find) {
				return p;
			}
		}
		
		throw new AssertionError();
	}
	
	private LockTree getSubLockTreeFor_children(ClusterHead find) {
		if (this.clusterHead == find) {
			return this;
		}
		
		if (children != null) {
			for (LockTree c : children) {
				LockTree r = c.getSubLockTreeFor_children(find);
				if (r != null) {
					return r;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Looks up whether the given {@link ClusterHead} is in the Subtree
	 * 
	 * @param c {@link ClusterHead}
	 * @return true if is child, false otherwise
	 */
	public boolean isChild(ClusterHead c) {
		if(this.clusterHead == c)
			//we the node, true 
			return true;
		else if (children == null){
			//we have no children, false
			return false;
		}
		else {
			//Search in Children
			for(LockTree t : children) {
				if(t.isChild(c))
					return true;
			}
		}
		
		//if we got here, no match found
		return false;

	}
	
	public void propagateUpdates(Collection<UpdateEntry> vNets) {
		for (LockTree p = parent; p != null; p = p.parent) {
			for (UpdateEntry v : vNets) {
				if (!p.updates.contains(v)) {
					p.updates.add(v);
				}
			}
		}
		
		addUpdatesToChildren(vNets, true);
	}
	
	public void setCounterRecursively(MetricsCounter counter) {
		getClusterHead().setCounter(counter);
		if (getChildren() != null) {
			for (LockTree c : getChildren()) {
				c.setCounterRecursively(counter);
			}
		}
	}
	
	private void addUpdatesToChildren(Collection<UpdateEntry> vNet,
			boolean first) {

		if (!first) {
			for (UpdateEntry v : vNet)
				if (!this.updates.contains(v))
					this.updates.add(v);
		}
		
		if (children != null) {
			for (LockTree c : children) {
				c.addUpdatesToChildren(vNet, false);
			}
		}
	}
	
	public Collection<UpdateEntry> getUpdates() {
		return updates;
	}
	
	public void clearUpdates() {
		updates.clear();
	}

	public TreeState getState() {
		return state;
	}

	public void setState(TreeState state) {
		this.state = state;
	}

	public boolean isUnLocked() {
		return (state.equals(TreeState.UNLOCKED));
	}

	public boolean isPartiallyLocked() {
		return (state.equals(TreeState.PARTIALLY_LOCKED));
	}
	
	public boolean isFullyLocked() {
		return (state.equals(TreeState.FULLY_LOCKED));
	}

	/*
	 * public void setLock(boolean incr) { for (LockTree p = parent; p != null;
	 * p = p.parent) { assert (p.locks >= 0); if (incr) { p.locks++; } else {
	 * p.locks--; } }
	 * 
	 * LinkedList<LockTree> queue = new LinkedList<LockTree>(); queue.add(this);
	 * while (!queue.isEmpty()) { LockTree c = queue.pop(); assert (c.locks >=
	 * 0); if (incr) { c.locks++; // increments this.locks, too! } else {
	 * c.locks--; }
	 * 
	 * Collection<LockTree> children = c.getChildren(); if (children != null) {
	 * for (LockTree child : children) { queue.add(child); } } } }
	 */

	public void setFullLock(boolean incr) {
		if (!state.equals(TreeState.PARTIALLY_LOCKED)) {
			for (LockTree p = parent; p != null; p = p.parent) {
				assert (!p.state.equals(TreeState.FULLY_LOCKED));
				assert(p.locks >= 0);
				if (incr) {
					p.setState(TreeState.PARTIALLY_LOCKED);
					p.locks++;
				} else {
					p.locks--;
					if (p.locks == 0)
						p.setState(TreeState.UNLOCKED);
				}
			}
			LinkedList<LockTree> queue = new LinkedList<LockTree>();
			queue.add(this);
			while (!queue.isEmpty()) {
				LockTree c = queue.pop();
				assert (!c.state.equals(TreeState.PARTIALLY_LOCKED) && c.locks == 0);
				if (incr) {
					c.setState(TreeState.FULLY_LOCKED);
				} else {
					c.setState(TreeState.UNLOCKED);
				}
				Collection<LockTree> children = c.getChildren();
				if (children != null) {
					for (LockTree child : children) {
						queue.add(child);
					}
				}
			}
		}

	}

	public boolean hasMappingChildren() {
		return !childsMapping.isEmpty();
	}

	public void addMappingChild(LockTree child) {
		childsMapping.add(child);
	}

	public void removeMappingChild(LockTree child) {
		childsMapping.remove(child);
	}

	public void setParent(LockTree parent) {
		this.parent = parent;
	}

	public LockTree getParent() {
		return parent;
	}

	public ClusterHead getClusterHead() {
		return clusterHead;
	}

	public Collection<LockTree> getChildren() {
		return children;
	}

	public void setChildren(Collection<LockTree> children) {
		this.children = children;
	}

	public LockTree cloneUnLockedSubTree() {

		LinkedList<LockTree> parents = new LinkedList<LockTree>();
		for (LockTree currentParent = parent; currentParent != null; currentParent = currentParent.parent) {
			parents.addFirst(currentParent);
		}
		LockTree oldPClone = null;
		for (LockTree p : parents) {

			LockTree pClone = new LockTree(name, oldPClone, p.clusterHead);

			if (oldPClone != null) {
				LinkedList<LockTree> pChild = new LinkedList<LockTree>();
				pChild.add(pClone);
				oldPClone.setChildren(pChild);
			}

			oldPClone = pClone;
		}

		LockTree clone = new LockTree(name, oldPClone, clusterHead);
		cloneChildren(this, clone);

		return clone;
	}

	private void cloneChildren(LockTree current, LockTree currentClone) {
		if (current.children != null) {
			LinkedList<LockTree> childrenClones = new LinkedList<LockTree>();
			for (LockTree child : current.children) {
				LockTree cClone = new LockTree(child.getName(), currentClone, child.clusterHead);
				cloneChildren(child, cClone);
				childrenClones.add(cClone);
			}
			currentClone.setChildren(childrenClones);
		}
	}

	public String toString() {
		return toString("");
	}
	
	private String toString(String prefix) {
		String thisString = name + "(";
		if (clusterHead.cluster != null) {
		    for (SubstrateNode sn : clusterHead.cluster.getVertices()) {
		        thisString += sn.getName() + " ";
		    }
		    thisString += "\n[links: ";
		    for (SubstrateLink sn : clusterHead.cluster.getEdges()) {
		        thisString += sn.getName() + " ";
		    }
		    thisString += "]\n";
		} else {
		    thisString += "partknownode";
		}
		thisString += "):" + clusterHead.isDelegationNode();

		String childrenString = "";
		if (children != null) {
			for (LockTree child : children) {
				childrenString += prefix + "\n   " + child.toString(prefix + "  ");
			}
		}
		return prefix + thisString + " [" + childrenString + "]";
	}

	public void propagateUnmapping(VirtualNetwork vNet) {
		propagateUnmapping(vNet, this.clusterHead.cluster);
	}

	private void propagateUnmapping(VirtualNetwork vNet,
			SubstrateNetwork cluster) {
		
		Collection<LockTree> children = this.getChildren();
		if (children != null) {
			for (LockTree c : children) {
				c.propagateUnmapping(vNet, c.clusterHead.cluster);
			}
		}
		vnreal.algorithms.utils.SubgraphBasicVN.Utils.clearVnrMappings(cluster, vNet.getLayer());
	}
	
}
