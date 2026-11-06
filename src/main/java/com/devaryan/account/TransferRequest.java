package com.devaryan.account;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record TransferRequest(
    @NotBlank String fromAccount,
    @NotBlank String toAccount,
    @NotNull @Digits(integer = 17, fraction = 2) @Positive BigDecimal amount
) {
    
}
