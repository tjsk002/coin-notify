package com.coin_notify.coin_notify.controller;

import com.coin_notify.coin_notify.service.MarketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class MarketController {
	private final MarketService marketService;

	@Autowired
	public MarketController(MarketService marketService) {
		this.marketService = marketService;
	}

	@GetMapping("/market-collect")
	public Mono<ResponseEntity<String>> process() {
		return marketService.marketCollectionProcess().thenReturn(ResponseEntity.ok("ok"));
	}
}
