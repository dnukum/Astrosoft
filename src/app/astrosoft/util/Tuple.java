package app.astrosoft.util;

/** Immutable ordered pair of values of the same type. */
public final class Tuple<T> {
    private final T first;
    private final T second;
    public Tuple(T first, T second) { this.first = first; this.second = second; }
    public T getFirst() { return first; }
    public T getSecond() { return second; }
}
