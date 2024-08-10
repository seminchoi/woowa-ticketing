package com.thirdparty.ticketing.domain.member.service;

import com.thirdparty.ticketing.domain.member.Member;
import com.thirdparty.ticketing.domain.member.repository.MemberRepository;
import com.thirdparty.ticketing.domain.member.service.response.LoginResponse;
import jakarta.transaction.Transactional;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public LoginResponse login(String email, String rawPassword) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("이메일/비밀번호가 일치하지 않습니다."));
        passwordEncoder.checkMatches(member, rawPassword);
        String accessToken =  jwtProvider.createAccessToken(member);
        return LoginResponse.of(member, accessToken);
    }
}
