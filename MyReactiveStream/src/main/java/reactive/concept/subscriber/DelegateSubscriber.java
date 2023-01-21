package reactive.concept.subscriber;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

@Slf4j
public class DelegateSubscriber<T, R> implements Subscriber<T> {
    private final Subscriber subscriber;
    private Subscription subscription;
    private final String name;

    public DelegateSubscriber(Subscriber<? super R> subscriber, String name) {
        this.subscriber = subscriber;
        this.name = name;
    }

    @Override
    public void onSubscribe(Subscription s) {
        log.debug("[{}] onSubscribe", name);
        this.subscription = s;
        subscriber.onSubscribe(s);
    }

    @Override
    public void onNext(T data) {
        log.debug("[{}] onNext:{}", name, data);
        subscriber.onNext(data);
    }

    @Override
    public void onError(Throwable throwable) {
        log.debug("[{}] onError:{}", name, throwable.getMessage());
        subscriber.onError(throwable);
    }

    @Override
    public void onComplete() {
        log.debug("[{}] onComplete", name);
        subscriber.onComplete();
    }

    public void cancel() {
        log.debug("[{}] cancle", name);
        subscription.cancel();
    }
}
