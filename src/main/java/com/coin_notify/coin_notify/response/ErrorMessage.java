package com.coin_notify.coin_notify.response;

import com.coin_notify.coin_notify.exception.CustomException;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
public enum ErrorMessage {
	USER_UUID_NOT_FOUND(new CustomException(HttpStatus.BAD_REQUEST.value(), 990101, "세션에서 사용자 UUID를 찾을 수 없습니다.")),
	USER_NOT_FOUND(new CustomException(HttpStatus.BAD_REQUEST.value(), 990103, "회원을 찾을 수 없습니다."));

	private final CustomException exception;

	ErrorMessage(CustomException exception) {
		this.exception = exception;
	}

	public ResponseEntity<ApiResponse<String>> toResponseEntity() {
		ApiResponse<String> response = new ApiResponse<>(
				exception.serviceExceptionCode() + "",
				exception.httpStatusCode() == HttpStatus.OK.value() ? "Success" : "Bad Request",
				exception.message()
		);
		return ResponseEntity.status(HttpStatus.valueOf(exception.httpStatusCode())).body(response);
	}
}
