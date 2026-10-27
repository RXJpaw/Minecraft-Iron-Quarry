package pw.rxj.iron_quarry.util;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class SupplicableAlt<T> {
    boolean when;
    @NotNull Supplier<T> then;
    @NotNull Supplier<T> or;


    private SupplicableAlt(boolean when) {
        this.when = when;
        this.then = () -> null;
        this.or = () -> null;
    }

    public static <T> SupplicableAlt<T> when(Supplier<Boolean> when) {
        return new SupplicableAlt<>(when.get());
    }
    public static <T> SupplicableAlt<T> when(boolean when) {
        return new SupplicableAlt<>(when);
    }
    public SupplicableAlt<T> then(Supplier<T> then) {
        this.then = then;

        return this;
    }
    public SupplicableAlt<T> or(Supplier<T> or) {
        this.or = or;

        return this;
    }

    public <N> SupplicableAlt<N> copy() {
        return new SupplicableAlt<>(when);
    }

    public T get() {
        return when ? then.get() : or.get();
    }

    /** Despite what the method's name suggests, {@link SupplicableAlt#when(boolean)} is not called again. Only a cached value is returned.*/
    public boolean test() {
        return when;
    }
}
