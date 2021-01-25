package org.clyze.jphantom.hier.graph;

public interface SettableEdge<T, V> {
	T set(V subtype, V supertype);
}
