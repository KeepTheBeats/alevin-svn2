package edu.uci.ics.jung.algorithms.shortestpath;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.graph.Graph;

/**
 * This is an intentional subclass of the JUNG class DijkstraShortestPath.
 * Subclassing here allows us to limit the search depth of the Dijkstra
 * algorithm by a parameter epsilon, without effectively re-implementing
 * the entire algorithm.
 * 
 * Constructors are just handed through. The only method changed is getPath().
 * 
 * @author Michael Till Beck stop calculation if path gets too long getPath will
 *         now return null if no path could be found
 * @see DijkstraDistance
 */
public class MyDijkstraShortestPath<V, E> extends DijkstraShortestPath<V, E> {
	/**
	 * <p>
	 * Creates an instance of <code>DijkstraShortestPath</code> for the
	 * specified graph and the specified method of extracting weights from
	 * edges, which caches results locally if and only if <code>cached</code> is
	 * <code>true</code>.
	 * 
	 * @param g
	 *            the graph on which distances will be calculated
	 * @param nev
	 *            the class responsible for returning weights for edges
	 * @param cached
	 *            specifies whether the results are to be cached
	 */
	public MyDijkstraShortestPath(Graph<V, E> g,
			Transformer<E, ? extends Number> nev, boolean cached) {
		super(g, nev, cached);
	}

	/**
	 * <p>
	 * Creates an instance of <code>DijkstraShortestPath</code> for the
	 * specified graph and the specified method of extracting weights from
	 * edges, which caches results locally.
	 * 
	 * @param g
	 *            the graph on which distances will be calculated
	 * @param nev
	 *            the class responsible for returning weights for edges
	 */
	public MyDijkstraShortestPath(Graph<V, E> g,
			Transformer<E, ? extends Number> nev) {
		super(g, nev);
	}

	/**
	 * <p>
	 * Creates an instance of <code>DijkstraShortestPath</code> for the
	 * specified unweighted graph (that is, all weights 1) which caches results
	 * locally.
	 * 
	 * @param g
	 *            the graph on which distances will be calculated
	 */
	public MyDijkstraShortestPath(Graph<V, E> g) {
		super(g);
	}

	/**
	 * <p>
	 * Creates an instance of <code>DijkstraShortestPath</code> for the
	 * specified unweighted graph (that is, all weights 1) which caches results
	 * locally.
	 * 
	 * @param g
	 *            the graph on which distances will be calculated
	 * @param cached
	 *            specifies whether the results are to be cached
	 */
	public MyDijkstraShortestPath(Graph<V, E> g, boolean cached) {
		super(g, cached);
	}

	/**
	 * Returns a <code>List</code> of the edges on the shortest path from
	 * <code>source</code> to <code>target</code>, in order of their occurrence
	 * on this path. If either vertex is not in the graph for which this
	 * instance was created, throws <code>IllegalArgumentException</code>.
	 * 
	 * @param epsilon
	 *            max path length
	 */
	public LinkedList<E> getPath(V source, V target, int epsilon) {

		if (!g.containsVertex(source))
			throw new IllegalArgumentException("Specified source vertex "
					+ source + " is not part of graph " + g);

		if (!g.containsVertex(target))
			throw new IllegalArgumentException("Specified target vertex "
					+ target + " is not part of graph " + g);

		LinkedList<E> path = new LinkedList<E>();

		// collect path data; must use internal method rather than
		// calling getIncomingEdge() because getIncomingEdge() may
		// wipe out results if results are not cached
		Set<V> targets = new HashSet<V>();
		targets.add(target);
		singleSourceShortestPath(source, targets, g.getVertexCount());
		Map<V, E> incomingEdges = ((SourcePathData) sourceMap.get(source)).incomingEdges;

		if (incomingEdges.isEmpty() || incomingEdges.get(target) == null) {
			return null;
		}

		V current = target;
		while (!current.equals(source)) {
			E incoming = incomingEdges.get(current);
			path.addFirst(incoming);
			current = ((Graph<V, E>) g).getOpposite(current, incoming);
		}

		if (epsilon != -1 && path.size() > epsilon) {
			return null;
		}

		Double d = getDistance(source, target).doubleValue();
		if (d == Double.POSITIVE_INFINITY) {
			return null;
		}
		return path;
	}

}
