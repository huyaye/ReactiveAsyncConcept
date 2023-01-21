package com.example;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
class LoadTest {
    @Test
    void sync() throws InterruptedException {
        dummyLoad("http://localhost:8080/sync");
    }

    @Test
    void async() throws InterruptedException {
        dummyLoad("http://localhost:8080/async");
    }

    static
    private void dummyLoad(String endpoint) throws InterruptedException {
        ExecutorService es = Executors.newFixedThreadPool(100);
        RestTemplate rt = new RestTemplate();

        StopWatch main = new StopWatch();
        main.start();

        AtomicInteger counter = new AtomicInteger(0);
        for (int i = 0; i < 100; i++) {
            es.execute(() -> {
                int idx = counter.addAndGet(1);
                log.info("Thread {}", idx);

                StopWatch sw = new StopWatch();
                sw.start();

                rt.getForObject(endpoint, String.class);

                sw.stop();
                log.info("Elapsed: " + idx + " ->  " + sw.getTotalTimeSeconds());
            });
        }

        es.shutdown();
        es.awaitTermination(100, TimeUnit.SECONDS);

        main.stop();
        log.info("Total: {}", main.getTotalTimeSeconds());
    }
}