package UncheckedWrapper;

import java.util.function.Consumer;

@FunctionalInterface
public interface ConsumerWrapper <T, E extends Throwable>  {
    static <T, E extends Throwable> Consumer<T> uncheck(ConsumerWrapper<T, E> consumerWrapper){
        return t ->{
            try {
                consumerWrapper.accept(t);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };
    }

    void accept(T t) throws E;
}
