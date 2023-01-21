package reactive.concept.publisher;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import reactive.concept.subscriber.DelegateSubscriber;

import java.util.function.BiFunction;
import java.util.function.Function;

@Slf4j
public class DataOperator {

    public static <T, R> Publisher<R> map(Publisher<T> pub, Function<T, R> f) {
        return new Publisher<>() {
            @Override
            public void subscribe(Subscriber<? super R> sub) {
                log.debug("[map] subscribe()");
                pub.subscribe(new DelegateSubscriber<T, R>(sub, "map") {
                    @Override
                    public void onNext(T data) {
                        log.debug("[map] onNext:{}", data);
                        sub.onNext(f.apply(data));
                    }
                });
            }
        };
    }

    public static <T, R> Publisher<R> reduce(Publisher<T> pub, R init, BiFunction<R, T, R> f) {
        return new Publisher<>() {
            @Override
            public void subscribe(Subscriber<? super R> sub) {
                log.debug("[reduce] subscribe()");
                pub.subscribe(new DelegateSubscriber<T, R>(sub, "reduce") {
                    R result = init;

                    @Override
                    public void onNext(T data) {
                        log.debug("[reduce] onNext:{}", data);
                        result = f.apply(result, data);
                    }

                    @Override
                    public void onComplete() {
                        log.debug("[reduce] onComplete");
                        sub.onNext(result);
                        sub.onComplete();
                    }
                });
            }
        };
    }

    public static <T> Publisher<T> take(Publisher<T> pub, int limit) {
        return new Publisher<>() {
            @Override
            public void subscribe(Subscriber<? super T> sub) {
                pub.subscribe(new DelegateSubscriber<T, T>(sub, "take") {
                    int count = 0;

                    @Override
                    public void onNext(T data) {
                        if (++count <= limit) {
                            sub.onNext(data);
                        } else {
                            this.cancel();
                        }
                    }
                });
            }
        };
    }
}
