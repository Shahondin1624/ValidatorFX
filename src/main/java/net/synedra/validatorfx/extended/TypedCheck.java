package net.synedra.validatorfx.extended;

import net.synedra.validatorfx.Check;

public class TypedCheck<Type extends Typed<?>> extends Check {
    private final Type type;

    public TypedCheck(Type type) {
        super();
        this.type = type;
    }

    public Type getType() {
        return type;
    }
}
