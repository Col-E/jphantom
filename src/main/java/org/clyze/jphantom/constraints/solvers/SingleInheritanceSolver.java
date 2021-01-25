package org.clyze.jphantom.constraints.solvers;

import org.clyze.jphantom.hier.graph.SettableEdge;
import org.clyze.jphantom.util.MapFactory;
import java.util.*;
import java.util.function.Supplier;

import org.jgrapht.*;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.*;

public class SingleInheritanceSolver<V,E extends SettableEdge<E,V>> extends AbstractSolver<V,E,Map<V,V>>
{
    protected final V root;

    ///////////////////// Constructors /////////////////////

    public SingleInheritanceSolver(Supplier<E> factory, V root) {
        super(factory, new MapFactory<V,V>());
        this.root = root;
        this._graph.addVertex(root);
    }

    public SingleInheritanceSolver(SimpleDirectedGraph<V,E> graph, V root) {
        super(graph, new MapFactory<V,V>());
        this.root = root;
        this._graph.addVertex(root);
        
        for (V v : this._graph.vertexSet())
            if (!v.equals(root))
                this._graph.addEdge(v, root, _graph.getEdgeSupplier().get().set(v, root));
    }

    ///////////////////// Methods /////////////////////

    @Override
    public void addConstraintEdge(V source, V target)
    {
        super.addConstraintEdge(source, target);
        _graph.addEdge(source, root, _graph.getEdgeSupplier().get().set(source, root));
        _graph.addEdge(target, root, _graph.getEdgeSupplier().get().set(target, root));
    }
    
    private SimpleDirectedGraph<V,E> getComponent(SimpleDirectedGraph<V,E> graph, V node)
    {
        AsUndirectedGraph<V,E> undirectedView = new AsUndirectedGraph<>(graph);

        Set<V> nodes = new ConnectivityInspector<>(undirectedView).connectedSetOf(node);
        SimpleDirectedGraph<V,E> subgraph = createSubgraph(graph, nodes);
        SimpleDirectedGraph<V,E> result = new SimpleDirectedGraph<>(null, graph.getEdgeSupplier(), false);
        Graphs.addGraph(result, subgraph);

        return result;
    }

    private SimpleDirectedGraph<V, E> createSubgraph(SimpleDirectedGraph<V, E> graph, Set<V> nodes) {
        // Code below is equivalent to:  new DirectedSubgraph<>(graph, nodes, null);
        //  - DirectedSubgraph stream filtering is quite slow
        //  - Manually generate a subgraph using SimpleDirectedGraph with passed vertices "nodes"
        SimpleDirectedGraph<V, E> subgraph = new SimpleDirectedGraph<>(null, graph.getEdgeSupplier(), false);
        for (V vertex : nodes) {
            subgraph.addVertex(vertex);
            for (E edge : graph.outgoingEdgesOf(vertex)) {
                V source = graph.getEdgeSource(edge);
                V target = graph.getEdgeTarget(edge);
                subgraph.addVertex(target);
                subgraph.addEdge(source, target, _graph.getEdgeSupplier().get().set(source, target));
            }
        }
        return subgraph;
    }

    private void placeUnder(V top, SimpleDirectedGraph<V,E> graph)
        throws GraphCycleException
    {
        // Remove vertex and remaining incoming edges
        graph.removeVertex(top);

        // Compute unconstrained nodes

        final Set<V> unconstrained = new HashSet<>();

        for (V vertex : graph.vertexSet())
            if (graph.outDegreeOf(vertex) == 0)
                unconstrained.add(vertex);

        // Determining the unconstrained node order
        Deque<V> ul = order(unconstrained, top);

        while (!ul.isEmpty())
        {
            // Remove an unconstrained node
            V next = ul.removeFirst();

            // Skip if next was visited in another component
            // of one of its neighbors
            if (!graph.containsVertex(next))
                continue;

            // Add subtype edge
            assert !solution.containsKey(next);
            solution.put(next, top);

            SimpleDirectedGraph<V,E> subgraph = getComponent(graph, next);

            // Remove subgraph from constraint graph

            graph.removeAllEdges(subgraph.edgeSet());

            for (V vertex : subgraph.vertexSet()) {
                assert graph.edgesOf(vertex).isEmpty();
                graph.removeVertex(vertex);
            }

            // Recursion
            placeUnder(next, subgraph);
        }

        // Sanity check
        if (!graph.edgeSet().isEmpty())
            throw new GraphCycleException();
    }

    @Override
    @SuppressWarnings("unchecked")
    public SingleInheritanceSolver<V,E> solve() throws UnsatisfiableStateException
    {
        return (SingleInheritanceSolver<V,E>) super.solve();
    }

    @Override
    protected void solve(SimpleDirectedGraph<V,E> graph) throws UnsatisfiableStateException
    {
        placeUnder(root, graph);
        assert graph.vertexSet().isEmpty();
    }

    protected Deque<V> order(Set<V> unconstrained, V prev) {
        return new LinkedList<>(unconstrained);
    }
}
