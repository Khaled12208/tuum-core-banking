package com.tuum.fsaccountsservice.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.tuum.fsaccountsservice.config.CurrencyDeserializer;

@JsonDeserialize(using = CurrencyDeserializer.class)
public enum Currency {
    EUR, SEK, GBP, USD
} 