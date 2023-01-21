package com.example.controller;

import io.netty.channel.nio.NioEventLoopGroup;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.Netty4ClientHttpRequestFactory;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class ExternalDependController {
    private final String URL1 = "http://localhost:8081/service1?req={req}";
    private final String URL2 = "http://localhost:8081/service2?req={req}";
    private final AtomicInteger count = new AtomicInteger(0);
    private final RestTemplate rt = new RestTemplate();
    private final AsyncRestTemplate art = new AsyncRestTemplate();
    private final AsyncRestTemplate artNio = new AsyncRestTemplate(
            new Netty4ClientHttpRequestFactory(new NioEventLoopGroup(1)));

    @GetMapping("/use/restTemplate")
    public String useRestTemplate() {
        // Blocking
        return rt.getForObject(URL1, String.class, count.addAndGet(1));
    }

    @GetMapping("/use/asyncRestTemplate")
    public ListenableFuture<ResponseEntity<String>> useAsyncRestTemplate() {
        // non Blocking - but using background thread per request
        return art.getForEntity(URL1, String.class, count.addAndGet(1));
    }

    @GetMapping("/use/asyncRestTemplate/nio")
    public DeferredResult<String> useAsyncRestTemplateWithNio() {
        DeferredResult<String> deferredResult = new DeferredResult<>();
        // non Blocking using 1 thread (nioEventLoopGroup)
        ListenableFuture<ResponseEntity<String>> future = artNio.getForEntity(URL1, String.class, count.addAndGet(1));
        future.addCallback(
                s -> deferredResult.setResult("hello " + s.getBody()),
                e -> deferredResult.setErrorResult(e.getMessage()));
        return deferredResult;
    }

    @GetMapping("/use/asyncRestTemplate/nio/cf")
    public DeferredResult<String> completableFuture() {
        DeferredResult<String> deferredResult = new DeferredResult<>();
        toCF(artNio.getForEntity(URL1, String.class, count.addAndGet(1)))
                .thenCompose(res -> toCF(artNio.getForEntity(URL2, String.class, res.getBody())))
                .thenAccept(res -> deferredResult.setResult(res.getBody()))
                .exceptionally(e -> {
                    deferredResult.setErrorResult(e.getMessage());
                    return null;
                });
        return deferredResult;
    }

    private <T> CompletableFuture<T> toCF(ListenableFuture<T> lf) {
        CompletableFuture<T> cf = new CompletableFuture<>();
        lf.addCallback(cf::complete, cf::completeExceptionally);
        return cf;
    }
}
