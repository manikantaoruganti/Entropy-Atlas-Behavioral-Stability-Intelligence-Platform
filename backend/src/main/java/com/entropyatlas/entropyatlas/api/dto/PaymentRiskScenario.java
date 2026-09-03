package com.entropyatlas.entropyatlas.api.dto;

public enum PaymentRiskScenario {
    NORMAL_PAYMENT_TRAFFIC,
    VELOCITY_SPIKE,
    GEO_DRIFT,
    DEVICE_DRIFT,
    AMOUNT_ANOMALY,
    PAYMENT_METHOD_SHIFT,
    FAILURE_CLUSTER,
    COORDINATED_PAYMENT_ABUSE,
    AI_SERVICE_FAILURE,
    POLICY_BLOCK
}
