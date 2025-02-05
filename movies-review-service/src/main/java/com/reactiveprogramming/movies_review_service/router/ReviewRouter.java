package com.reactiveprogramming.movies_review_service.router;

import com.reactiveprogramming.movies_review_service.handler.ReviewHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class ReviewRouter {

    @Bean
    public RouterFunction<ServerResponse> reviewsRouter(ReviewHandler reviewHandler) {

        return route()
                .nest(path("/api/v1/review"), builder -> {
                    builder.POST("/addReview", reviewHandler::addReview)
                            .GET("/getAllReviews", reviewHandler::getAllReviews)
                            .PUT("/updateReview/{id}", reviewHandler::updateReview)
                            .DELETE("/deleteReview/{id}", reviewHandler::deleteReview)
                            .GET("/getReviews/stream", reviewHandler::getReviewsStream);
                })
                .GET("/api/v1/helloWorld", request -> ServerResponse.ok().bodyValue("Hello World"))
//                .POST("/api/v1/review/addReview", reviewHandler::addReview)
//                .GET("/api/v1/review/getAllReviews", reviewHandler::getAllReviews)
                .build();
    }
}
