package com.coin_notify.coin_notify.controller;

import com.coin_notify.coin_notify.data.dto.MarketResponseDto;
import com.coin_notify.coin_notify.response.ApiResponse;
import com.coin_notify.coin_notify.service.LikeMarketService;
import com.coin_notify.coin_notify.service.MarketService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
public class MarketController {
	private final MarketService marketService;
	private final LikeMarketService likeMarketService;

	@Autowired
	public MarketController(MarketService marketService, LikeMarketService likeMarketService) {
		this.marketService = marketService;
		this.likeMarketService = likeMarketService;
	}

	@GetMapping("/market-collect")
	public Mono<ResponseEntity<String>> process() {
		return marketService.marketCollectionProcess().thenReturn(ResponseEntity.ok("ok"));
	}

	@GetMapping("/market-list")
	public Mono<ResponseEntity<ApiResponse<List<MarketResponseDto>>>> marketList(HttpServletRequest request) {
		return marketService.marketList(request);
	}

	@PostMapping("/api/markets/like/{marketId}")
	public Mono<ResponseEntity<ApiResponse<String>>> addLikeMarket(@PathVariable(name = "marketId", required = true) Long marketId, HttpServletRequest request) {
		return likeMarketService.addLikeMarket(request, marketId);
	}
}
