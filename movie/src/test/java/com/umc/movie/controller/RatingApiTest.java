package com.umc.movie.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.movie.dto.request.CreateRatingRequest;
import com.umc.movie.entity.Member;
import com.umc.movie.repository.MemberRepository;
import com.umc.movie.repository.RatingRepository;
import com.umc.movie.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest        // 진짜 서버를 띄우듯 모든 Bean을 올립니다
@AutoConfigureMockMvc  // HTTP 요청을 흉내 내는 MockMvc를 준비합니다
@Transactional         // 테스트가 끝나면 저장한 데이터를 되돌립니다
class RatingApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; // 객체를 JSON 문자열로 바꿔줍니다

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private Member member;
    private String authorizationHeader;

    @BeforeEach
    void setUp() {
        // 로그인한 회원이 있어야 하므로 회원을 한 명 저장하고 토큰을 직접 발급합니다.
        member = memberRepository.save(Member.builder()
                .email("challenger@umc.com")
                .password("$2a$10$abcdefghijklmnopqrstuv") // 로그인 API를 거치지 않으므로 아무 값이나 괜찮습니다
                .nickname("영화조아")
                .build());

        authorizationHeader = "Bearer " + jwtUtil.createAccessToken(member.getMemberId(), member.getEmail());
    }

    @Test
    @DisplayName("토큰 없이 평점을 등록하면 401과 AUTH401_2를 응답한다")
    void createRatingWithoutToken() throws Exception {
        mockMvc.perform(post("/ratings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateRatingRequest(550L, 5, "인생 영화"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("AUTH401_2"));
    }

    @Test
    @DisplayName("토큰을 담아 평점을 등록하면 201과 저장된 영화 ID를 응답하고 DB에 저장된다")
    void createRating() throws Exception {
        mockMvc.perform(post("/ratings")
                        .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateRatingRequest(550L, 5, "인생 영화"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("COMMON201"))
                .andExpect(jsonPath("$.result.movieId").value(550));

        // 응답만 믿지 않고 실제로 DB에 남았는지도 확인합니다.
        assertThat(ratingRepository.existsByMember_MemberIdAndMovieId(member.getMemberId(), 550L)).isTrue();
    }
}