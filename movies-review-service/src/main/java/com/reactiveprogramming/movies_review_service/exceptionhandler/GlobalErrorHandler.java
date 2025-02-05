package com.reactiveprogramming.movies_review_service.exceptionhandler;

import com.reactiveprogramming.movies_review_service.exception.ReviewDataException;
import com.reactiveprogramming.movies_review_service.exception.ReviewNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
@Slf4j
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {

        log.error("Exception message: {}", ex.getMessage());

        DataBufferFactory dataBufferFactory = exchange.getResponse().bufferFactory();
        DataBuffer dataBuffer = dataBufferFactory.wrap(ex.getMessage().getBytes());

        if(ex instanceof ReviewDataException) {
            exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
            return exchange.getResponse().writeWith(Mono.just(dataBuffer));
        }

        if(ex instanceof ReviewNotFoundException) {
            exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
            return exchange.getResponse().writeWith(Mono.just(dataBuffer));
        }

        exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        return exchange.getResponse().writeWith(Mono.just(dataBuffer));
    }
}
