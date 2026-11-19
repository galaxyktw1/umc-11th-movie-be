package com.umc.movie.controller;

import com.umc.movie.dto.ApiResponse;
import com.umc.movie.dto.CreateRatingRequest;
import com.umc.movie.dto.RatingResponse;
import com.umc.movie.dto.UpdateRatingRequest;
import com.umc.movie.interceptor.AuthInterceptor;
import com.umc.movie.service.RatingService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    // 공개: 특정 회원의 평점 목록
    @GetMapping("/members/{memberId}/ratings")
    public ResponseEntity<ApiResponse<List<RatingResponse>>> getRatings(
            @PathVariable Long memberId
    ) {
        List<RatingResponse> response =
                ratingService.getRatingsByMember(memberId);
        return ResponseEntity.ok(ApiResponse.ok(
                "평점 목록 조회에 성공했습니다.",
                response
        ));
    }

    // 로그인 필요: 평점 등록
    @PostMapping("/ratings")
    public ResponseEntity<ApiResponse<RatingResponse>> create(
            @RequestAttribute(AuthInterceptor.MEMBER_ID)
            Long memberId,
            @Valid @RequestBody CreateRatingRequest request
    ) {
        RatingResponse response =
                ratingService.create(memberId, request);
        return ResponseEntity.status(201)
                .body(ApiResponse.created(
                        "평점 등록에 성공했습니다.",
                        response
                ));
    }

    // 로그인 필요: 평점 수정
    @PatchMapping("/ratings/{ratingId}")
    public ResponseEntity<ApiResponse<RatingResponse>> update(
            @RequestAttribute(AuthInterceptor.MEMBER_ID)
            Long memberId,
            @PathVariable Long ratingId,
            @Valid @RequestBody UpdateRatingRequest request
    ) {
        RatingResponse response = ratingService.update(
                memberId, ratingId, request
        );
        return ResponseEntity.ok(ApiResponse.ok(
                "평점 수정에 성공했습니다.",
                response
        ));
    }

    // 로그인 필요: 평점 삭제
    @DeleteMapping("/ratings/{ratingId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @RequestAttribute(AuthInterceptor.MEMBER_ID)
            Long memberId,
            @PathVariable Long ratingId
    ) {
        ratingService.delete(memberId, ratingId);
        return ResponseEntity.ok(ApiResponse.ok(
                "평점 삭제에 성공했습니다.",
                null
        ));
    }
}