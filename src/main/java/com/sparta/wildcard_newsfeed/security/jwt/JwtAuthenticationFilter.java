package com.sparta.wildcard_newsfeed.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.wildcard_newsfeed.domain.common.CommonResponseDto;
import com.sparta.wildcard_newsfeed.domain.common.error.ErrorResponseDto;
import com.sparta.wildcard_newsfeed.domain.user.entity.User;
import com.sparta.wildcard_newsfeed.domain.user.repository.UserRepository;
import com.sparta.wildcard_newsfeed.exception.customexception.UserNotFoundException;
import com.sparta.wildcard_newsfeed.security.jwt.dto.AuthRequestDto;
import com.sparta.wildcard_newsfeed.security.jwt.dto.TokenDto;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

import static com.sparta.wildcard_newsfeed.security.jwt.JwtConstants.ACCESS_TOKEN_HEADER;
import static com.sparta.wildcard_newsfeed.security.jwt.JwtConstants.REFRESH_TOKEN_HEADER;

//@Slf4j(topic = "로그인 및 JWT 생성")
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final ObjectMapper objectMapper;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;


    public JwtAuthenticationFilter(ObjectMapper objectMapper, JwtUtil jwtUtil, UserRepository userRepository
    ) {
        this.objectMapper = objectMapper;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        setFilterProcessesUrl("/api/v1/user/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        log.info(request.getRequestURI());
        try {
            AuthRequestDto requestDto = new ObjectMapper().readValue(request.getInputStream(), AuthRequestDto.class);

            return getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(
                            requestDto.getUsercode(),
                            requestDto.getPassword(),
                            null
                    )
            );
        } catch (IOException e) {
            //TODO 로그인의 요청파라미터가 없는 경우
            log.error("attemptAuthentication 예외 발생 {} ", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException {
        log.info("로그인 성공 및 JWT 토큰 발행");
        User user = userRepository.findByUsercode(authResult.getName())
                .orElseThrow(UserNotFoundException::new);

        TokenDto tokenDto = jwtUtil.generateAccessTokenAndRefreshToken(user.getUsercode());
        String refreshTokenValue = tokenDto.getRefreshToken().substring(7);
        user.setRefreshToken(refreshTokenValue);
        userRepository.save(user);

        loginSuccessResponse(response, tokenDto);
    }


    private void loginSuccessResponse(HttpServletResponse response, TokenDto tokenDto) throws IOException {
        CommonResponseDto responseDto = CommonResponseDto.builder()
                .message("로그인 성공")
                .statusCode(HttpStatus.OK.value())
                .build();
        String body = objectMapper.writeValueAsString(responseDto);

        response.setStatus(HttpStatus.OK.value());
        response.setContentType("application/json;charset=UTF-8");
        response.addHeader(ACCESS_TOKEN_HEADER, tokenDto.getAccessToken());
        response.addHeader(REFRESH_TOKEN_HEADER, tokenDto.getRefreshToken());
        response.getWriter().write(body);
    }


    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException {
        log.error("unsuccessfulAuthentication | 로그인 실패");
        ErrorResponseDto errorResponseDto = ErrorResponseDto.builder()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .message("로그인 실패하였습니다.")
                .build();

        String body = objectMapper.writeValueAsString(errorResponseDto);

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(body);
    }
}