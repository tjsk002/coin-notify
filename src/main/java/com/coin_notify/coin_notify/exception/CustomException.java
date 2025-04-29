package com.coin_notify.coin_notify.exception;

public record CustomException(int httpStatusCode, int serviceExceptionCode, String message) {

}