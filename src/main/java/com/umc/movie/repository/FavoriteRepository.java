package com.umc.movie.repository;

import com.umc.movie.entity.Favorite;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteRepository
        extends JpaRepository<Favorite, Long> {

    // 내 즐겨찾기 목록 (최신순)
    List<Favorite> findByMemberMemberIdOrderByFavoriteIdDesc(
            Long memberId
    );

    // 해제할 즐겨찾기 찾기
    Optional<Favorite> findByMemberMemberIdAndMovieId(
            Long memberId,
            Long movieId
    );

    // 이미 즐겨찾기했는지
    boolean existsByMemberMemberIdAndMovieId(
            Long memberId,
            Long movieId
    );
}
