import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class ReactorEx {
    @Test
    public void simpleFlux() {
        Flux.<Integer>create(e -> {
                    e.next(1);
                    e.next(2);
                    e.next(3);
                    e.complete();
                })
                .log()
                .map(s -> s * 10)
                .log()
                .reduce(0, (a, b) -> a + b)
                .log()
                .subscribe(System.out::println);
    }

    @Test
    public void schedule() {
        Flux.range(1, 10)
                .subscribeOn(Schedulers.newSingle("sub"))
                .log()
                .map(s -> s * 10)
                .log()
                .publishOn(Schedulers.newSingle("pub"))
                .log()
                .subscribe(System.out::println);
        System.out.println("Exit");
    }

    @Test
    public void interval() throws InterruptedException {
        Flux.interval(Duration.ofMillis(200))
                .log()
                .take(10)
                .log()
                .subscribe(System.out::println);
        TimeUnit.SECONDS.sleep(10);  // Because Flux.interval use daemon thread.
    }
}
