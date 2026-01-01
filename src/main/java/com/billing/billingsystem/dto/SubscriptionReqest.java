package com.billing.billingsystem.dto;

import java.util.UUID;

public record SubscriptionReqest(
    UUID userId,
    UUID planId
) {}
