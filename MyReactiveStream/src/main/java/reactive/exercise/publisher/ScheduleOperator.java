package reactive.exercise.publisher;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class ScheduleOperator {

    // Slow producer - Fast consumer
    public static <T> Publisher<T> subscribeOn(Publisher<T> pub) {
        return new Publisher<>() {
            // Reactive 규약 - 순서보장을 위한 단일 스레드 사용
            final ExecutorService es = Executors.newSingleThreadExecutor();

            @Override
            public void subscribe(Subscriber<? super T> sub) {
                log.debug("[subscribeOn] subscribe()");
                es.execute(() -> pub.subscribe(sub));
                es.shutdown();
            }
        };
    }

    // Fast producer - Slow consumer
    public static <T> Publisher<T> publishOn(Publisher<T> pub) {
        return new Publisher<>() {
            // Reactive 규약 - 순서보장을 위한 단일 스레드 사용
            final ExecutorService es = Executors.newSingleThreadExecutor();

            @Override
            public void subscribe(Subscriber<? super T> sub) {
                log.debug("[publishOn] subscribe()");
                pub.subscribe(new Subscriber<>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        log.debug("[publishOn] onSubscribe()");
                        sub.onSubscribe(s);
                    }

                    @Override
                    public void onNext(T t) {
                        es.execute(() -> sub.onNext(t));
                    }

                    @Override
                    public void onError(Throwable t) {
                        es.execute(() -> sub.onError(t));
                        es.shutdown();
                    }

                    @Override
                    public void onComplete() {
                        es.execute(() -> sub.onComplete());
                        es.shutdown();
                    }
                });
            }
        };
    }
}
