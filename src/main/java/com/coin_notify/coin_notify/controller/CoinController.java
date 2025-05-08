package com.coin_notify.coin_notify.controller;

import com.coin_notify.coin_notify.data.dto.CoinResponseDto;
import com.coin_notify.coin_notify.response.ApiResponse;
import com.coin_notify.coin_notify.service.CoinService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
public class CoinController {
	private final CoinService coinService;

	@Autowired
	public CoinController(CoinService coinService) {
		this.coinService = coinService;
	}

	@GetMapping("/coin-collect")
	public Mono<ResponseEntity<String>> process() {
		return coinService.coinCollectionProcess().thenReturn(ResponseEntity.ok("ok"));
	}

	@GetMapping("/coin-list")
	public Mono<ResponseEntity<ApiResponse<List<CoinResponseDto>>>> coinList(HttpServletRequest request) {
		return coinService.coinList(request);
	}
}
