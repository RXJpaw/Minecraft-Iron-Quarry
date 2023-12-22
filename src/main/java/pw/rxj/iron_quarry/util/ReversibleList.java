package pw.rxj.iron_quarry.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ReversibleList<T, S> {
    private final List<T> list;
    private final List<T> listBackup;
    private final Supplier<S> returnSupplier;

    public ReversibleList(List<T> list, Supplier<S> returnSupplier) {
        this.list = list;
        this.listBackup = new ArrayList<>(list);
        this.returnSupplier = returnSupplier;
    }
    public static <T, S> ReversibleList<T, S> of(List<T> list, Supplier<S> returnSupplier) {
        return new ReversibleList<>(list, returnSupplier);
    }
    public static <T> ReversibleList<T, Void> of(List<T> list) {
        return of(list, () -> null);
    }

    public T takeLast() {
        if(list.isEmpty()) throw new IndexOutOfBoundsException();

        return this.take(list.size() - 1);
    }
    public T take(int index) {
        if(index >= list.size()) throw new IndexOutOfBoundsException();

        return list.remove(index);
    }

    public void revert() {
        this.list.clear();
        this.list.addAll(listBackup);
    }
    public S revertAndGet() {
        this.revert();

        return returnSupplier.get();
    }
}
