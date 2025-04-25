package com.coin_notify.coin_notify;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CoinNotifyApplication {
	public static void main(String[] args) {
		SpringApplication.run(CoinNotifyApplication.class, args);
	}
}
