package com.coin_notify.coin_notify.controller;

import com.coin_notify.coin_notify.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
public class UserController {
	private final UserService userService;

	@Autowired
	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/")
	public Mono<ResponseEntity<UUID>> setupUserSession(HttpServletRequest request) {
		return userService.setupUserSession(request)
				.map(ResponseEntity::ok);
	}
}
