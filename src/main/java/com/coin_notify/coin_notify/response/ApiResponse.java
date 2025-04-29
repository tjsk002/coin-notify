package com.coin_notify.coin_notify.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiResponse<T> {
	private String resultCode;
	private String resultMessage;
	private T resultData;

	public ApiResponse(String resultCode, String resultMessage, T resultData) {
		this.resultCode = resultCode;
		this.resultMessage = resultMessage;
		this.resultData = resultData;
	}
}
