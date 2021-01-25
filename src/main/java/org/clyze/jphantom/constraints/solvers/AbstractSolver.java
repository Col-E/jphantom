package org.clyze.jphantom.constraints.solvers;

import org.clyze.jphantom.hier.graph.SettableEdge;
import org.clyze.jphantom.util.Factory;
import org.jgrapht.*;
import org.jgrapht.graph.*;

import java.util.function.Supplier;

public abstract class AbstractSolver<V,E extends SettableEdge<E,V>,S> implements Solver<V,E,S>
{
    protected boolean solved = false;
    protected S solution;
    private final Factory<S> solutionFactory;
    protected final Supplier<E> factory;
    protected final SimpleDirectedGraph<V,E> _graph;
    private final AsUnmodifiableGraph<V,E> unmodifiableGraph;

    ////////////// Constructors //////////////

    public AbstractSolver(Supplier<E> factory, Factory<S> solutionFactory) {
        this(new SimpleDirectedGraph<>(null, factory, false), solutionFactory);
    }

    public AbstractSolver(SimpleDirectedGraph<V,E> graph, Factory<S> solutionFactory) {
        this.solutionFactory = solutionFactory;
        this.factory = graph.getEdgeSupplier();
        this._graph = graph;
        this.unmodifiableGraph = new AsUnmodifiableGraph<>(graph);
    }

    //////////////// Methods ////////////////

    @Override
    public AsUnmodifiableGraph<V,E> getConstraintGraph() {
        return unmodifiableGraph;
    }

    @Override
    public S getSolution() {
        if (!solved)
            throw new IllegalStateException();
        return solution;
    }

    protected abstract void solve(SimpleDirectedGraph<V,E> graph)
        throws UnsatisfiableStateException;

    @Override
    public AbstractSolver<V,E,S> solve() throws UnsatisfiableStateException
    {
        if (solved) { return this; }

        SimpleDirectedGraph<V, E> backup = new SimpleDirectedGraph<>(null, factory, false);
        Graphs.addGraph(backup, _graph);
        solution = solutionFactory.create();
        solve(backup);
        solved = true;
        return this;
    }

    @Override
    public void addConstraintEdge(V source, V target)
    {
        if (!_graph.containsEdge(source, target))
            solved = false;

        if (!_graph.containsVertex(source))
            _graph.addVertex(source);

        if (!_graph.containsVertex(target))
            _graph.addVertex(target);

        _graph.addEdge(source, target, _graph.getEdgeSupplier().get().set(source, target));
    }


    ///////////////// Exceptions /////////////////

    protected static class GraphCycleException 
        extends UnsatisfiableStateException
    {
        protected final static long serialVersionUID = 2368453345L;

        protected GraphCycleException() {
            super();
        }
    }
}
