package org.clyze.jphantom.constraints;

import com.esotericsoftware.reflectasm.FieldAccess;
import org.clyze.jphantom.hier.graph.SettableEdge;
import org.jgrapht.graph.DefaultEdge;
import org.objectweb.asm.Type;

import java.util.function.Supplier;

public class SubtypeConstraint extends DefaultEdge implements Constraint, SettableEdge<SubtypeConstraint, Type>
{
    private static final FieldAccess parentAccessor;
    public Type subtype;
    public Type supertype;

    @Override
    public SubtypeConstraint set(Type subtype, Type supertype) {
        if (subtype == null)
            throw new IllegalArgumentException();
        if (supertype == null)
            throw new IllegalArgumentException();
        this.subtype = subtype;
        this.supertype = supertype;
        // Set "IntrusiveEdge" values, which JGraphT internally uses to optimize fetching vertices of an edge
        parentAccessor.set(this, "source", subtype);
        parentAccessor.set(this, "target", supertype);
        return this;
    }

    @Override
    public void accept(ConstraintVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return subtype.getClassName() + " <: " + supertype.getClassName();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this)
            return true;

        if (!(other instanceof SubtypeConstraint))
            return false;

        SubtypeConstraint o = (SubtypeConstraint) other;

        return subtype.equals(o.subtype) && supertype.equals(o.supertype);
    }

    @Override
    public int hashCode() {
        if (subtype != null && supertype != null) {
            int result = 17;
            result = 31 * result + subtype.hashCode();
            result = 31 * result + supertype.hashCode();
            return result;
        }
        return super.hashCode();
    }

    public static final Factory factory = new Factory();

    public static class Factory implements Supplier<SubtypeConstraint>
    {
        private Factory() {}

        @Override
        public SubtypeConstraint get() {
            return new SubtypeConstraint();
        }
    }


    static {
        try {
            parentAccessor = FieldAccess.get(Class.forName("org.jgrapht.graph.IntrusiveEdge"));
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException();
        }
    }
}
