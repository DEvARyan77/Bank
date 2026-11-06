package com.devaryan.minibank.dto;

import java.math.BigDecimal;

// What we send to the Python fraud service (field names match its JSON)
public record FraudFeatures(BigDecimal amount, int hourOfDay, double accountAgeDays,
                            long transfersLastDay, double avgTransferAmount, boolean newRecipient) {
}
