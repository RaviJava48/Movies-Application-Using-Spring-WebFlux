package com.reactiveprogramming.movies_review_service.repository;

import com.reactiveprogramming.movies_review_service.domain.Review;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface ReviewReactiveRepository extends ReactiveMongoRepository<Review, String> {

    Flux<Review> findByMovieInfoId(Long movieInfoId);
}
