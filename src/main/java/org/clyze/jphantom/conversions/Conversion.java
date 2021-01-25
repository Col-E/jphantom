package org.clyze.jphantom.conversions;

import org.objectweb.asm.Type;
import org.clyze.jphantom.constraints.*;

public abstract class Conversion
{
    public final Type from;
    public final Type to;

    Conversion(Type from, Type to) {
        this.from = from;
        this.to = to;
    }

    public Constraint asConstraint() {
        SubtypeConstraint constraint = SubtypeConstraint.factory.get();
        //constraint.set(from, to);
        constraint.set(from, to);
        return constraint;
    }

    public abstract void accept(ConversionVisitor visitor);
}
