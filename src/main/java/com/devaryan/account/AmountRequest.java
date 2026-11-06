package com.devaryan.account;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record AmountRequest(
        @NotNull @Positive @Digits(integer = 17, fraction = 2) BigDecimal amount) { }