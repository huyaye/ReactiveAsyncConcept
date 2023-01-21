import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;

@Slf4j
public class AsyncEx {
    @Test
    public void future() throws ExecutionException, InterruptedException {
        ExecutorService es = Executors.newCachedThreadPool();
        Future<String> f = es.submit(() -> {
//            int a = 5 / 0;
            TimeUnit.SECONDS.sleep(2);
            log.debug("Running in other thread");
            return "Hello world";
        });
        log.debug("Future is done : " + f.isDone());
        log.debug("Future result is : " + f.get());
    }

    @Test
    public void futureTask() throws InterruptedException {
        ExecutorService es = Executors.newCachedThreadPool();
        FutureTask<String> f = new FutureTask<>(() -> {
            TimeUnit.SECONDS.sleep(2);
            log.debug("Running in other thread");
            return "Hello world";
        }) {
            @Override
            protected void done() {
                try {
                    log.debug("Future result is : " + get());   // Same thread
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        es.execute(f);
        TimeUnit.SECONDS.sleep(3);  // JUnit 은 non-daemon thread 가 있는 경우에도 프로세스 강제종료하므로 필요.
    }
}