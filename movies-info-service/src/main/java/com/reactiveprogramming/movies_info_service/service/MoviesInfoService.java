package com.reactiveprogramming.movies_info_service.service;

import com.reactiveprogramming.movies_info_service.domain.MoviesInfo;
import com.reactiveprogramming.movies_info_service.repository.MoviesInfoRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class MoviesInfoService {

    private final MoviesInfoRepository moviesInfoRepository;

    public MoviesInfoService(MoviesInfoRepository moviesInfoRepository) {
        this.moviesInfoRepository = moviesInfoRepository;
    }

    public Mono<MoviesInfo> addMoviesInfo(MoviesInfo moviesInfo) {

        return moviesInfoRepository.save(moviesInfo);
    }

    public Flux<MoviesInfo> getAllMoviesInfo() {

        return moviesInfoRepository.findAll();
    }

    public Mono<MoviesInfo> getMoviesInfoById(String id) {

        return moviesInfoRepository.findById(id);
    }

    public Mono<MoviesInfo> updateMoviesInfo(MoviesInfo updatedMoviesInfo, String id) {

        return moviesInfoRepository.findById(id)
                .flatMap(moviesInfo -> {
                    moviesInfo.setName(updatedMoviesInfo.getName());
                    moviesInfo.setYear(updatedMoviesInfo.getYear());
                    moviesInfo.setCast(updatedMoviesInfo.getCast());
                    moviesInfo.setReleaseDate(updatedMoviesInfo.getReleaseDate());

                    return moviesInfoRepository.save(moviesInfo);
                });
    }

    public Mono<Void> deleteMoviesInfo(String id) {

        return moviesInfoRepository.deleteById(id);
    }

    public Flux<MoviesInfo> getMoviesInfoByYear(Integer year) {

        return moviesInfoRepository.findByYear(year);
    }
}
