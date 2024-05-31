package com.rdrg.back.controller;

import java.util.ArrayList;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rdrg.back.dto.request.payment.PayRequestDto;
import com.rdrg.back.dto.request.payment.PostPaymentRequestDto;
import com.rdrg.back.dto.response.ResponseDto;
import com.rdrg.back.dto.response.payment.GetPaymentDetailListResponseDto;
import com.rdrg.back.dto.response.payment.GetPaymentListResponseDto;
import com.rdrg.back.dto.response.payment.GetPaymentResponseDto;
import com.rdrg.back.dto.response.payment.PostPaymentResponseDto;
import com.rdrg.back.service.PaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rdrg/payment")
@RequiredArgsConstructor
public class PaymentController {
    
    private final PaymentService paymentService;


    @PostMapping("/save")
    ResponseEntity<? super PostPaymentResponseDto> postPayment(
        @RequestBody @Valid PostPaymentRequestDto requestBody,
        @AuthenticationPrincipal String userId, ArrayList<String> rentSerialNumber
    ) {
        ResponseEntity<? super PostPaymentResponseDto> response = paymentService.postPayment(requestBody, userId);
        return response;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<? super GetPaymentResponseDto> getPayment(
        @PathVariable("userId") String userId
    ) {
        ResponseEntity<? super GetPaymentResponseDto> response = paymentService.getPayment(userId);
        return response;
    }

    @GetMapping("/myrentpage")
    public ResponseEntity<? super GetPaymentListResponseDto> getPaymentList(
        @AuthenticationPrincipal String userId
    ) {
        ResponseEntity<? super GetPaymentListResponseDto>  response = paymentService.getPaymentList(userId);
        return response;
    }

    @GetMapping("/myrentpage/{rentNumber}")
    public ResponseEntity<? super GetPaymentDetailListResponseDto> getPaymentDetailList(
        @AuthenticationPrincipal String userId,
        @PathVariable("rentNumber") int rentNumber
    ) {
        ResponseEntity<? super GetPaymentDetailListResponseDto> response = paymentService.getPaymentDetailList(userId, rentNumber);
        return response;
    }
}
