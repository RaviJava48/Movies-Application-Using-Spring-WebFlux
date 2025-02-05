package com.reactiveprogramming.movies_info_service.repository;

import com.reactiveprogramming.movies_info_service.domain.MoviesInfo;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MoviesInfoRepository extends ReactiveMongoRepository<MoviesInfo, String> {

    Flux<MoviesInfo> findByYear(Integer year);

    Mono<MoviesInfo> findByName(String name);
}
