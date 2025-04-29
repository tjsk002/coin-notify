package com.coin_notify.coin_notify.controller;

import com.coin_notify.coin_notify.response.ApiResponse;
import com.coin_notify.coin_notify.service.LikeCoinService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/coins")
public class LikeCoinController {
	private final LikeCoinService likeCoinService;

	@Autowired
	public LikeCoinController(LikeCoinService likeCoinService) {
		this.likeCoinService = likeCoinService;
	}

	@PostMapping("/like/{coinId}")
	public Mono<ResponseEntity<ApiResponse<String>>> addLikeCoin(@PathVariable(name = "coinId", required = true) Long coinId, HttpServletRequest request) {
		return likeCoinService.addLikeCoin(request, coinId);
	}
}
