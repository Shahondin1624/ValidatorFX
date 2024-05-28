package net.synedra.validatorfx.extended;

import net.synedra.validatorfx.Validator;

import java.util.Optional;

public class TypedValidator<Type extends Typed<?>> extends Validator {
    public TypedCheck<Typed<?>> createTypedCheck(Type type) {
        TypedCheck<Typed<?>> check = new TypedCheck<>(type);
        add(check);
        return check;
    }

    @SuppressWarnings("unchecked")
    public Optional<TypedCheck<Type>> getFor(Type type) {
        return super.checks.keySet().stream().filter(TypedCheck.class::isInstance).map(check -> (TypedCheck<Type>) check).filter(typedCheck -> typedCheck.getType().equals(type)).findFirst();
    }

}
