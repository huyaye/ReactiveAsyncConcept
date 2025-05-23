package reactive.exercise;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static reactive.exercise.publisher.DataOperator.map;
import static reactive.exercise.publisher.DataOperator.take;

@Slf4j
@Data
public class ReactiveExercise {
    public static void main(String[] args) {
        Publisher<Integer> pub = iterPub(Stream.iterate(1, a -> a + 1).limit(10).collect(Collectors.toList()));
        pub = take(pub, 9);
//        pub = publishOn(pub);
        pub = map(pub, s -> s * 10);
//        pub = reduce(pub, 0, (a, b) -> a + b);
//        pub = subscribeOn(pub);
        pub.subscribe(logSub());

//        Publisher<String> pub2 = mapPub(pub, s -> "[" + s + "]");
//        Publisher<StringBuilder> pub3 = reducePub(pub2, new StringBuilder(), (a, b) -> a.append(b + ","));
//        pub3.subscribe(logSub());
    }

    private static Publisher<Integer> iterPub(Iterable<Integer> iter) {
        return new Publisher<>() {
            volatile boolean isCanceld = false;
            @Override
            public void subscribe(Subscriber<? super Integer> sub) {
                log.debug("[iterPub] subscribe()");
                sub.onSubscribe(new Subscription() {
                    @Override
                    public void request(long l) {
                        log.debug("call request()");
                        try {
                            Iterator<Integer> iterator = iter.iterator();
                            while (iterator.hasNext()) {
                                if (isCanceld) {
                                    break;
                                }
                                sub.onNext(iterator.next());
                            }
                            sub.onComplete();
                        } catch (Exception e) {
                            sub.onError(e);
                        }
                    }

                    @Override
                    public void cancel() {
                        log.debug("call cancel()");
                        isCanceld = true;
                    }
                });
            }
        };
    }

    private static <T> Subscriber<T> logSub() {
        return new Subscriber<>() {
            Subscription s;

            @Override
            public void onSubscribe(Subscription s) {
                log.debug("onSubscribe");
                this.s = s;
                s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(T data) {
                log.debug("onNext : {}", data);
            }

            @Override
            public void onError(Throwable t) {
                log.debug("onError : {}", t.getMessage());
            }

            @Override
            public void onComplete() {
                log.debug("onComplete");
            }
        };
    }
}
