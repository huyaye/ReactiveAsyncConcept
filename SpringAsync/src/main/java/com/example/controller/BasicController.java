package com.example.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
public class BasicController {
    @GetMapping("/sync")
    public String block() throws InterruptedException {
        log.info("In servlet thread");
        TimeUnit.MILLISECONDS.sleep(2000);
        return "Hello";
    }

    @GetMapping("/async/callable")
    public Callable<String> callable() {
        log.info("In servlet thread");
        return () -> {
            TimeUnit.MILLISECONDS.sleep(2000);
            log.info("In worker thread (non servlet thread)");
            return "Hello callable";
        };
    }

    /**
     * what’s the difference from Callable?
     * The difference is this time the thread is managed by us.
     * It is our responsibility to set the result of the DeferredResult in a different thread.
     * .. 라고 하는데 아래처럼 쓰는것은 Callable 대비 장점이라고 하기 어려울것 같다. 결국 쓰레드를 사용하는건 똑같으니..
     *
     * External event - Long polling
     * DeferredResult 를 저장하고 있다가 Callback등 외부 이벤트를 기반으로 사용하는 것이 의미있는것 같음.
     */
    @GetMapping("/async/deferred")
    public DeferredResult<String> deferredResult() {
        log.info("In servlet thread");
        DeferredResult<String> deferredResult = new DeferredResult<>();
        Executors.newSingleThreadExecutor().execute(() -> {
            log.info("In user thread");
            try {
                TimeUnit.MILLISECONDS.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            deferredResult.setResult("Hello DeferredResult");
        });
        return deferredResult;
    }

    @GetMapping("/emitter")
    public ResponseBodyEmitter emitter() {
        ResponseBodyEmitter emitter = new ResponseBodyEmitter();
        Executors.newSingleThreadExecutor().execute(() -> {
            for (int i = 1; i <= 50; i++) {
                try {
                    emitter.send("<p>Stream " + i + "</p>");
                    Thread.sleep(100);
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        return emitter;
    }
}
