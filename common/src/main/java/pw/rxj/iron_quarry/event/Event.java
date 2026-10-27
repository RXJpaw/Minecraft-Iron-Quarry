package pw.rxj.iron_quarry.event;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.function.Function;

public class Event<T> {
    private final Function<T[], T> invokerFactory;
    private T[] listeners;
    private T invoker;

    @SuppressWarnings("unchecked")
    private Event(Class<T> type, Function<T[], T> invokerFactory) {
        this.invokerFactory = invokerFactory;
        this.listeners = (T[]) Array.newInstance(type, 0);
        this.invoker = invokerFactory.apply(this.listeners);
    }
    public static <T> Event<T> createArrayBacked(Class<T> type, Function<T[], T> invokerFactory) {
        return new Event<>(type, invokerFactory);
    }

    public synchronized void register(T listener) {
        this.listeners = Arrays.copyOf(this.listeners, this.listeners.length + 1);
        this.listeners[this.listeners.length - 1] = listener;
        this.invoker = this.invokerFactory.apply(this.listeners);
    }

    public T invoker() {
        return this.invoker;
    }
}
