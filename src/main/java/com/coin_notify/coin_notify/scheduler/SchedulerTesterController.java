package com.coin_notify.coin_notify.scheduler;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/scheduler-test")
public class SchedulerTesterController {
	private final PriceScheduler priceScheduler;

	public SchedulerTesterController(
			PriceScheduler priceScheduler
	) {
		this.priceScheduler = priceScheduler;
	}

	@GetMapping()
	public String runSchedulerManually() {
		priceScheduler.fetchRealTimePrice();
		return "-- 스케줄러 수동 실행 완료!";
	}
}
