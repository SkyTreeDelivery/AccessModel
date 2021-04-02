package UncheckedWrapper;

import java.util.function.Function;

@FunctionalInterface
public interface FunctionWrapper<T, R, E extends Throwable> {
    static <T, R, E extends Throwable> Function<T, R> unchecked(FunctionWrapper<T, R, E> f) {
        return t -> {
            try {
                return f.apply(t);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };
    }

    R apply(T t) throws E;
}