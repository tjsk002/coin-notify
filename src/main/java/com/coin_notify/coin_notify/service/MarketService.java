package com.coin_notify.coin_notify.service;

import com.coin_notify.coin_notify.data.entity.MarketEntity;
import com.coin_notify.coin_notify.data.repository.MarketRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class MarketService {
	private final ObjectMapper objectMapper;
	private final HttpClient client;
	private final MarketRepository marketRepository;

	public MarketService(
			ObjectMapper objectMapper,
			MarketRepository marketRepository
	) {
		this.objectMapper = objectMapper;
		this.marketRepository = marketRepository;
		this.client = HttpClient.newHttpClient();
	}

	public Mono<Void> marketCollectionProcess() {
		return fetchMarketData().flatMap(this::saveMarketData);
	}

	private Mono<JsonNode> fetchMarketData() {
		String url = "https://api.upbit.com/v1/market/all?is_details=true";

		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("accept", "application/json").build();

		return Mono.fromCallable(() -> {
			try {
				HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
				return objectMapper.readTree(response.body());
			} catch (Exception e) {
				throw new RuntimeException("Failed to fetch market data", e);
			}
		});
	}

	private Mono<Void> saveMarketData(JsonNode marketData) {
		return Flux.fromIterable(marketData).map(data -> {
			MarketEntity marketEntity = new MarketEntity();
			marketEntity.setMarketCode(data.get("market").asText());
			marketEntity.setKoreanName(data.get("korean_name").asText());
			marketEntity.setEnglishName(data.get("english_name").asText());
			return marketEntity;
		}).flatMap(marketRepository::save).doOnNext(saved -> System.out.println("Saved: " + saved.getMarketCode())).doOnError(error -> System.err.println("Error saving: " + error.getMessage())).then();
	}
}
