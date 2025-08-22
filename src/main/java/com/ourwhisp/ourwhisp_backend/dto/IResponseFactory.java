package com.ourwhisp.ourwhisp_backend.dto;



public interface IResponseFactory {

    <T> ApiResponse<T> success(T data);
    <T> ApiResponse<T> success(T data, String message);
    <T> ApiResponse<T> error(String message);
}
