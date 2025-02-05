package com.reactiveprogramming.movies_review_service.domain;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document
public class Review {

    @Id
    private String reviewId;

    @NotNull(message = "Review.movieInfoId must not be null")
    private Long movieInfoId;
    private String comment;

    @Min(value = 0L, message = "Please pass a non-negative value for Review.rating")
    private Double rating;
}
