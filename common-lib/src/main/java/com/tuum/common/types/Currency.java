package com.tuum.common.types;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.tuum.common.util.CurrencyDeserializer;


@JsonDeserialize(using = CurrencyDeserializer.class)
public enum Currency {
    EUR, SEK, GBP, USD
} 