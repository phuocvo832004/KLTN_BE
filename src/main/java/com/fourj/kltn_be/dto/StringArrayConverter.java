package com.fourj.kltn_be.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class StringArrayConverter implements AttributeConverter<String[], String> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(String[] attribute) {
        try {
            return mapper.writeValueAsString(attribute);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String[] convertToEntityAttribute(String dbData) {
        try {
            return mapper.readValue(dbData, String[].class);
        } catch (Exception e) {
            return null;
        }
    }
}
