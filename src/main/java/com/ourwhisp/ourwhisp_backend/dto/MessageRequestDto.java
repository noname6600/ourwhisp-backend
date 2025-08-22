package com.ourwhisp.ourwhisp_backend.dto;


import com.ourwhisp.ourwhisp_backend.model.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageRequestDto {
    private String content;

}
