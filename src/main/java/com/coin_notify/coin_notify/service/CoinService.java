package com.coin_notify.coin_notify.service;

import com.coin_notify.coin_notify.data.entity.CoinEntity;
import com.coin_notify.coin_notify.data.repository.CoinRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class CoinService {
	private final ObjectMapper objectMapper;
	private final HttpClient client;
	private final CoinRepository coinRepository;

	public CoinService(ObjectMapper objectMapper, CoinRepository coinRepository) {
		this.objectMapper = objectMapper;
		this.coinRepository = coinRepository;
		this.client = HttpClient.newHttpClient();
	}

	public Mono<Void> coinCollectionProcess() {
		return fetchCoinData().flatMap(this::saveCoinData);
	}

	private Mono<JsonNode> fetchCoinData() {
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

	private Mono<Void> saveCoinData(JsonNode marketData) {
		return Flux.fromIterable(marketData)
				.publishOn(Schedulers.boundedElastic())
				.flatMap(data -> {
					String symbol = data.get("market").asText();
					String koreanName = data.get("korean_name").asText();
					String englishName = data.get("english_name").asText();
					String coinSymbol = symbol.split("-")[1];

					return coinRepository.existsBySymbol(coinSymbol)
							.flatMap(exists -> {
								if (Boolean.TRUE.equals(exists)) {
									System.out.println("Duplicate found, skipping: " + coinSymbol + " | " + koreanName + " | " + englishName);
									return Mono.empty();
								} else {
									CoinEntity coinEntity = new CoinEntity();
									coinEntity.setSymbol(coinSymbol);
									coinEntity.setKoreanName(koreanName);
									coinEntity.setEnglishName(englishName);

									return coinRepository.save(coinEntity)
											.doOnSuccess(saved -> System.out.println("Saved: " + saved.getSymbol()));
								}
							})
							.onErrorResume(error -> {
								System.err.println("Error occurred: " + error.getMessage());
								return Mono.empty();
							});
				})
				.doOnError(error -> System.err.println("Error during saving: " + error.getMessage()))
				.then();
	}
}
