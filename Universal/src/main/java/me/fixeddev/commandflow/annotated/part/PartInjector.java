package me.fixeddev.commandflow.annotated.part;

import me.fixeddev.commandflow.annotated.modifier.CommandModifierFactory;
import me.fixeddev.commandflow.command.modifiers.CommandModifier;
import me.fixeddev.commandflow.part.CommandPart;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface PartInjector {

    /**
     * Gets a {@link PartFactory} for a given {@link Type}.
     *
     * @param type The type of {@link me.fixeddev.commandflow.part.CommandPart} to generate.
     * @return An {@link PartFactory} for a specific {@link Type} of CommandPart.
     */
    @Nullable
    default PartFactory getFactory(Type type) {
        return getFactory(new Key(type));
    }

    /**
     * Gets a {@link PartFactory} for a given {@link Key}.
     *
     * @param key The type of {@link me.fixeddev.commandflow.part.CommandPart} to generate.
     * @return An {@link PartFactory} for a specific {@link Type} of CommandPart.
     */
    @Nullable PartFactory getFactory(Key key);

    /**
     * Gets a {@link PartFactory} for a given {@link Key}.
     *
     * @param annotation The type of {@link me.fixeddev.commandflow.part.CommandPart} to generate.
     * @return An {@link PartFactory} for a specific {@link Type} of CommandPart.
     */
    @Nullable PartModifier getModifier(Class<? extends Annotation> annotation);

    /**
     * Gets a {@link DelegatePartModifier} for a given list of {@link Annotation}.
     *
     * @param annotations The list of {@link Annotation} instances to delegate to.
     * @return An {@link PartFactory} for a specific {@link Type} of CommandPart.
     */
    @NotNull
    default PartModifier getModifiers(List<Class<? extends Annotation>> annotations) {
        List<PartModifier> modifiers = new ArrayList<>();

        for (Class<? extends Annotation> annotation : annotations) {
            PartModifier modifier = getModifier(annotation);

            if (modifier != null) {
                modifiers.add(modifier);
            }
        }

        return new DelegatePartModifier(modifiers);
    }

    /**
     * Gets a {@link DelegatePartModifier} for a given list of {@link Annotation}.
     *
     * @param annotations The list of {@link Annotation} instances to delegate to.
     * @return An {@link PartFactory} for a specific {@link Type} of CommandPart.
     */
    @NotNull
    default PartModifier getModifiers(Class<? extends Annotation>... annotations) {
        return getModifiers(Arrays.asList(annotations));
    }

    /**
     * Binds a {@link PartModifier} to a specific annotation type.
     *
     * @param annotation   The annotation type to bind to.
     * @param partModifier The {@link PartModifier} that's going to be bound.
     */
    void bindModifier(Class<? extends Annotation> annotation, PartModifier partModifier);

    /**
     * Binds a {@link PartFactory} to a specific Type.
     *
     * @param type        The type of value returned by the {@link CommandPart} created by the bound factory.
     * @param partFactory The {@link PartFactory} that's going to be bound.
     */
    default void bindFactory(Type type, PartFactory partFactory) {
        bindFactory(new Key(type), partFactory);
    }

    /**
     * Binds a {@link PartFactory} to a specific {@link Key}.
     *
     * @param key     The {@link Key} type of value returned by the {@link CommandPart} created by the bound factory.
     * @param factory The {@link PartFactory} that's going to be bound.
     */
    void bindFactory(Key key, PartFactory factory);

    /**
     * Creates a constant binding from a Type to a {@link CommandPart}.
     *
     * @param type The {@link Key} type of value returned by the given {@link CommandPart}.
     * @param part The {@link CommandPart} that's going to be constantly bound to a type.
     */
    default void bindPart(Type type, CommandPart part) {
        bindPart(new Key(type), part);
    }

    /**
     * Creates a constant binding from a Type to a {@link CommandPart}.
     *
     * @param key  The {@link Key} type of value returned by the given {@link CommandPart}.
     * @param part The {@link CommandPart} that's going to be constantly bound to a type.
     */
    default void bindPart(Key key, CommandPart part) {
        bindFactory(key, (name, modifiers) -> part);
    }

    /**
     * Gets a {@link CommandModifierFactory} for a given {@link Annotation}.
     *
     * @param annotation The annotation type to search modifier for.
     * @return An {@link CommandModifierFactory} for a specific {@link Type} of CommandModifier.
     */
    CommandModifierFactory getCommandModifierFactory(Class<? extends Annotation> annotation);

    /**
     * Binds a {@link CommandModifierFactory} to a specific {@link Class} anotation type.
     *
     * @param type  The {@link Class} annotation type used to identify the {@link CommandModifier}.
     * @param factory The {@link CommandModifierFactory} that's going to be bound.
     */
    void bindCommandModifierFactory(Class<? extends Annotation> type, CommandModifierFactory factory);

    /**
     * Creates a constant binding from a Type to a {@link CommandModifierFactory}.
     *
     * @param type  The {@link Class} annotation type used to identify the {@link CommandModifier}.
     * @param modifier The {@link CommandModifier} that's going to be constantly bound to an annotation.
     */
    default void bindCommandModifier(Class<? extends Annotation> type, CommandModifier modifier) {
        bindCommandModifierFactory(type, (command, annotations) -> modifier);
    }

    void install(Module module);

    static PartInjector create() {
        return new SimplePartInjector();
    }

}
