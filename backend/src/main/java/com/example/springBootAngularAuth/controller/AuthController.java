package com.example.springBootAngularAuth.controller;

import com.example.springBootAngularAuth.dto.AuthRequest;
import com.example.springBootAngularAuth.dto.AuthResponse;
import com.example.springBootAngularAuth.dto.AuthResult;
import com.example.springBootAngularAuth.dto.RegisterRequest;
import com.example.springBootAngularAuth.security.JwtCookieHelper;
import com.example.springBootAngularAuth.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	private final JwtCookieHelper jwtCookieHelper;

	@PostMapping("/register")
	public ResponseEntity<AuthResponse> register(
			@Valid @RequestBody RegisterRequest request,
			HttpServletResponse response) {
		AuthResult result = authService.register(request);
		jwtCookieHelper.addTokenCookie(response, result.token());
		return ResponseEntity.ok(new AuthResponse(result.email()));
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(
			@RequestBody AuthRequest request,
			HttpServletResponse response) {
		AuthResult result = authService.login(request);
		jwtCookieHelper.addTokenCookie(response, result.token());
		return ResponseEntity.ok(new AuthResponse(result.email()));
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(HttpServletResponse response) {
		jwtCookieHelper.clearTokenCookie(response);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/me")
	public ResponseEntity<AuthResponse> me(Authentication authentication) {
		return ResponseEntity.ok(new AuthResponse(authentication.getName()));
	}
}
