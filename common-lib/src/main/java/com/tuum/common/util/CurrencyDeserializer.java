package com.tuum.common.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.tuum.common.types.Currency;

import java.io.IOException;


public class CurrencyDeserializer extends JsonDeserializer<Currency> {
    
    @Override
    public Currency deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getValueAsString();
        
        if (value == null) {
            return null;
        }
        
        try {
            return Currency.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            String validCurrencies = String.join(", ", 
                Currency.EUR.name(), 
                Currency.SEK.name(), 
                Currency.GBP.name(), 
                Currency.USD.name()
            );
            
            throw new JsonMappingException(p, 
                String.format("Invalid currency '%s'. Valid currencies are: %s", value, validCurrencies));
        }
    }
} 