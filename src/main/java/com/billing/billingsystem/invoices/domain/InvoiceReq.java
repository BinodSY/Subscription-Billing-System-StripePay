package com.billing.billingsystem.invoices.domain;


import java.math.BigDecimal;
import java.util.UUID;

public record InvoiceReq(
    UUID subscriptionId,
    BigDecimal amount,
    String currency,
    String status
) {}

