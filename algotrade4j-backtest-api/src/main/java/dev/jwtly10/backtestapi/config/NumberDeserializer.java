package dev.jwtly10.backtestapi.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import dev.jwtly10.core.model.Number;

import java.io.IOException;

public class NumberDeserializer extends JsonDeserializer<Number> {
    @Override
    public Number deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return new Number(p.getValueAsString());
    }
}