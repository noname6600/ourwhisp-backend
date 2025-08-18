package com.ourwhisp.ourwhisp_backend.dto;

import lombok.Data;

@Data
public class MessageSearchFilterDto {
    private String keyword;
    private String length;
    private Long minViews;
    private Integer page = 0;
    private Integer size = 10;
}