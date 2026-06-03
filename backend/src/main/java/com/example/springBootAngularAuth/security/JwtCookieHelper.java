package com.example.springBootAngularAuth.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class JwtCookieHelper {

	public static final String COOKIE_NAME = "access_token";

	@Value("${jwt.expiration}")
	private long expirationMs;

	public void addTokenCookie(HttpServletResponse response, String token) {
		ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, token)
				.httpOnly(true)
				.secure(false)
				.path("/")
				.maxAge(expirationMs / 1000)
				.sameSite("Lax")
				.build();
		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
	}

	public void clearTokenCookie(HttpServletResponse response) {
		ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, "")
				.httpOnly(true)
				.secure(false)
				.path("/")
				.maxAge(0)
				.sameSite("Lax")
				.build();
		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
	}
}
