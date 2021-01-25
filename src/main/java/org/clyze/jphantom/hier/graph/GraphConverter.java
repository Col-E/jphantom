package org.clyze.jphantom.hier.graph;

import org.clyze.jphantom.Types;
import org.clyze.jphantom.hier.*;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.graph.*;
import org.objectweb.asm.Type;

public class GraphConverter implements Types
{
    private final ClassHierarchy hierarchy;

    public GraphConverter(ClassHierarchy hierarchy) {
        this.hierarchy = hierarchy;
    }

    public SimpleDirectedGraph<Node,DefaultEdge> convert()
    {
        SimpleDirectedGraph<Node,DefaultEdge> graph =
                new SimpleDirectedGraph<>(DefaultEdge.class);

        // Add vertices

        for (Type t : hierarchy)
            graph.addVertex(Node.get(t));

        // Add edges

        for (Type t : hierarchy)
        {
            for (Type i : hierarchy.getInterfaces(t)) {
                Node target = Node.get(i);
                Node source = Node.get(t);
                graph.addVertex(target);
                graph.addEdge(source, target);
            }

            Type sc = hierarchy.getSuperclass(t);

            if (sc == null) {
                assert t.equals(OBJECT) : t;
                continue;
            }

            graph.addVertex(Node.get(sc));
            graph.addEdge(Node.get(t), Node.get(sc));
        }

        // Check for cycles 

        if (new CycleDetector<>(graph).detectCycles())
            throw new CyclicHierarchyException(graph.toString());

        return graph;
    }
}
