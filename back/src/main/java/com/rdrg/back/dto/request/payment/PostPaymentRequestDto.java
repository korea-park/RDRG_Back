package com.rdrg.back.dto.request.payment;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PostPaymentRequestDto {
    @NotBlank
    private String rentUserId;
    @NotNull
    @Size(min=1)
    private List<String> rentSerialNumber;
    @NotBlank
    private String rentPlace;
    @NotBlank
    private String rentReturnPlace;
    @NotBlank
    private String rentDatetime;
    @NotBlank
    private String rentReturnDatetime;
    @NotNull
    private Integer rentTotalPrice;
    @NotNull
    private String rentStatus;
}
