package com.umc.movie.service;

import com.umc.movie.dto.CreateRatingRequest;
import com.umc.movie.dto.RatingResponse;
import com.umc.movie.dto.UpdateRatingRequest;
import com.umc.movie.entity.Member;
import com.umc.movie.entity.Rating;
import com.umc.movie.exception.ApiException;
import com.umc.movie.repository.MemberRepository;
import com.umc.movie.repository.RatingRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final MemberRepository memberRepository;

    // 특정 회원의 평점 목록 (공개 API)
    @Transactional(readOnly = true)
    public List<RatingResponse> getRatingsByMember(
            Long memberId
    ) {
        findMember(memberId);
        return ratingRepository
                .findByMemberMemberIdOrderByRatingIdDesc(
                        memberId
                )
                .stream()
                .map(RatingResponse::from)
                .toList();
    }

    // 평점 등록 (memberId는 토큰에서)
    @Transactional
    public RatingResponse create(
            Long memberId,
            CreateRatingRequest request
    ) {
        Member member = findMember(memberId);

        boolean exists = ratingRepository
                .existsByMemberMemberIdAndMovieId(
                        memberId, request.movieId()
                );
        if (exists) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "RATING409_1",
                    "이미 평점을 남긴 영화입니다."
            );
        }

        Rating rating = Rating.builder()
                .member(member)
                .movieId(request.movieId())
                .score(request.score())
                .comment(request.comment())
                .build();

        Rating saved = ratingRepository.save(rating);
        return RatingResponse.from(saved);
    }

    // 평점 수정 (내 평점만)
    @Transactional
    public RatingResponse update(
            Long memberId,
            Long ratingId,
            UpdateRatingRequest request
    ) {
        Rating rating = findOwnedRating(memberId, ratingId);
        rating.update(request.score(), request.comment());
        return RatingResponse.from(rating);
    }

    // 평점 삭제 (내 평점만)
    @Transactional
    public void delete(Long memberId, Long ratingId) {
        Rating rating = findOwnedRating(memberId, ratingId);
        ratingRepository.delete(rating);
    }

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "MEMBER404_1",
                        "존재하지 않는 회원입니다."
                ));
    }

    // 평점이 없으면 404, 내 것이 아니면 403
    private Rating findOwnedRating(
            Long memberId,
            Long ratingId
    ) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "RATING404_1",
                        "존재하지 않는 평점입니다."
                ));

        Long ownerId = rating.getMember().getMemberId();
        if (!ownerId.equals(memberId)) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "RATING403_1",
                    "본인의 평점만 수정/삭제할 수 있습니다."
            );
        }
        return rating;
    }
}