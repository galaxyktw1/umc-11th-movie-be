package com.umc.movie.service;

import com.umc.movie.dto.CreateFavoriteRequest;
import com.umc.movie.dto.FavoriteResponse;
import com.umc.movie.dto.FavoriteStatusResponse;
import com.umc.movie.entity.Favorite;
import com.umc.movie.entity.Member;
import com.umc.movie.exception.ApiException;
import com.umc.movie.repository.FavoriteRepository;
import com.umc.movie.repository.MemberRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final MemberRepository memberRepository;

    // 즐겨찾기 등록
    @Transactional
    public FavoriteResponse create(
            Long memberId,
            CreateFavoriteRequest request
    ) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "MEMBER404_1",
                        "해당 회원을 찾을 수 없습니다."
                ));

        // 친절한 409를 위한 사전 검사
        // (동시 요청은 DB UNIQUE 제약이 최종으로 막음)
        boolean exists = favoriteRepository
                .existsByMemberMemberIdAndMovieId(
                        memberId, request.movieId()
                );
        if (exists) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "FAVORITE409_1",
                    "이미 이 영화를 즐겨찾기하셨습니다."
            );
        }

        Favorite favorite = Favorite.builder()
                .member(member)
                .movieId(request.movieId())
                .build();

        Favorite saved = favoriteRepository.save(favorite);
        return FavoriteResponse.from(saved);
    }

    // 내 즐겨찾기 목록
    @Transactional(readOnly = true)
    public List<FavoriteResponse> getMyFavorites(Long memberId) {
        return favoriteRepository
                .findByMemberMemberIdOrderByFavoriteIdDesc(memberId)
                .stream()
                .map(FavoriteResponse::from)
                .toList();
    }

    // 특정 영화 즐겨찾기 여부 (안 했어도 정상 → false)
    @Transactional(readOnly = true)
    public FavoriteStatusResponse getStatus(
            Long memberId,
            Long movieId
    ) {
        boolean isFavorite = favoriteRepository
                .existsByMemberMemberIdAndMovieId(
                        memberId, movieId
                );
        return new FavoriteStatusResponse(isFavorite);
    }

    // 즐겨찾기 해제
    @Transactional
    public void delete(Long memberId, Long movieId) {
        Favorite favorite = favoriteRepository
                .findByMemberMemberIdAndMovieId(memberId, movieId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "FAVORITE404_1",
                        "즐겨찾기한 영화를 찾을 수 없습니다."
                ));

        favoriteRepository.delete(favorite);
    }
}
