package com.billing.billingsystem.subscriptions.domain;

import java.util.UUID;

public record SubscriptionReqest(
    UUID userId,
    UUID planId,
    boolean autoRenew
) {}
