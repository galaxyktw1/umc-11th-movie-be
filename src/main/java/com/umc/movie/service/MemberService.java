package com.umc.movie.service;

import com.umc.movie.dto.EmailCheckResponse;
import com.umc.movie.dto.MemberInfoResponse;
import com.umc.movie.dto.MemberResponse;
import com.umc.movie.dto.MemberUpdateRequest;
import com.umc.movie.dto.NicknameCheckResponse;
import com.umc.movie.dto.SignUpRequest;
import com.umc.movie.entity.Member;
import com.umc.movie.exception.ApiException;
import com.umc.movie.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public NicknameCheckResponse checkNickname(String nickname) {
        boolean exists =
                memberRepository.existsByNickname(nickname);
        return new NicknameCheckResponse(!exists);
    }

    @Transactional(readOnly = true)
    public EmailCheckResponse checkEmail(String email) {
        boolean exists =
                memberRepository.existsByEmail(email);
        return new EmailCheckResponse(!exists);
    }

    @Transactional
    public MemberResponse signUp(SignUpRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException(
                    "이미 사용 중인 이메일입니다."
            );
        }
        if (memberRepository.existsByNickname(
                request.nickname())) {
            throw new IllegalArgumentException(
                    "이미 사용 중인 닉네임입니다."
            );
        }

        String encodedPassword =
                passwordEncoder.encode(request.password());

        Member member = Member.builder()
                .email(request.email())
                .password(encodedPassword)
                .nickname(request.nickname())
                .build();

        Member savedMember = memberRepository.save(member);
        return new MemberResponse(
                savedMember.getMemberId(),
                savedMember.getEmail(),
                savedMember.getNickname()
        );
    }

    // 토큰에서 꺼낸 memberId로 내 정보 조회
    @Transactional(readOnly = true)
    public MemberInfoResponse getMyInfo(Long memberId) {
        Member member = findMember(memberId);
        return MemberInfoResponse.from(member);
    }

    // 내 닉네임 수정
    @Transactional
    public MemberInfoResponse updateMyInfo(
            Long memberId,
            MemberUpdateRequest request
    ) {
        Member member = findMember(memberId);
        String nickname = request.nickname();

        // 나를 제외한 다른 회원이 쓰는 닉네임이면 409
        // (지금 내 닉네임 그대로 보내는 건 허용)
        boolean taken = memberRepository
                .existsByNicknameAndMemberIdNot(
                        nickname, memberId
                );
        if (taken) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "MEMBER409_1",
                    "이미 사용 중인 닉네임입니다."
            );
        }

        // 변경 감지로 트랜잭션 끝날 때 UPDATE
        member.updateNickname(nickname);
        return MemberInfoResponse.from(member);
    }

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "MEMBER404_1",
                        "존재하지 않는 회원입니다."
                ));
    }
}