package com.umc.movie.repository;

import com.umc.movie.entity.Rating;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RatingRepository
        extends JpaRepository<Rating, Long> {

    // 특정 회원의 평점 목록 (최신순)
    List<Rating> findByMemberMemberIdOrderByRatingIdDesc(
            Long memberId
    );

    // 같은 회원이 같은 영화에 이미 평점 남겼는지
    boolean existsByMemberMemberIdAndMovieId(
            Long memberId,
            Long movieId
    );
}