package com.example.springBootAngularAuth.service;

import com.example.springBootAngularAuth.dto.AuthRequest;
import com.example.springBootAngularAuth.dto.AuthResponse;
import com.example.springBootAngularAuth.dto.RegisterRequest;
import com.example.springBootAngularAuth.model.User;
import com.example.springBootAngularAuth.repository.UserRepository;
import com.example.springBootAngularAuth.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final AuthenticationManager authenticationManager;

	public AuthResponse register(RegisterRequest request) {
		if (userRepository.findByEmail(request.email()).isPresent()) {
			throw new RuntimeException("Email already registered");
		}

		User user = new User();
		user.setEmail(request.email());
		user.setPassword(passwordEncoder.encode(request.password()));

		userRepository.save(user);

		String token = jwtService.generateToken(user.getEmail());
		return new AuthResponse(token);
	}

	public AuthResponse login(AuthRequest request) {
		authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
						request.email(),
						request.password()));

		User user = userRepository.findByEmail(request.email())
				.orElseThrow(() -> new RuntimeException("User not found"));

		String token = jwtService.generateToken(user.getEmail());
		return new AuthResponse(token);
	}
}