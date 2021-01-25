package org.clyze.jphantom.constraints.solvers;

import org.jgrapht.graph.AsUnmodifiableGraph;

public interface Solver<V,E,S> {

    AsUnmodifiableGraph<V,E> getConstraintGraph();

    S getSolution();

    Solver<V,E,S> solve() throws UnsatisfiableStateException;

    void addConstraintEdge(V source, V target);

    
    class UnsatisfiableStateException extends Exception
    {
        protected final static long serialVersionUID = 8345346567L;

        protected UnsatisfiableStateException(Throwable cause) {
            super(cause);
        }

        protected UnsatisfiableStateException() {
            super();
        }
    }
}

