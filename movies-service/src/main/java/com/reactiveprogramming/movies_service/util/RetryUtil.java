package com.reactiveprogramming.movies_service.util;

import com.reactiveprogramming.movies_service.exception.MoviesInfoServerException;
import com.reactiveprogramming.movies_service.exception.ReviewsServerException;
import reactor.core.Exceptions;
import reactor.util.retry.Retry;

import java.time.Duration;

public class RetryUtil {

    public static Retry getRetrySpec() {

        //This code piece propagates original exception to the client instead of RetryExhaustedException
        //Filter out 4xx errors (retrying specific exceptions)
        return Retry.fixedDelay(3, Duration.ofSeconds(1))
                .filter(ex -> ex instanceof MoviesInfoServerException || ex instanceof ReviewsServerException)
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> Exceptions.propagate(retrySignal.failure()));
    }
}
