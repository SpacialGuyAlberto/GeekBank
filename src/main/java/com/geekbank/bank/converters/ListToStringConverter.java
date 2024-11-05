package com.geekbank.bank.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Converter
public class ListToStringConverter implements AttributeConverter<List<String>, String> {

    private static final String SPLIT_CHAR = ",";

    @Override
    public String convertToDatabaseColumn(List<String> list) {
        return list != null && !list.isEmpty()
                ? list.stream()
                .map(String::trim)
                .collect(Collectors.joining(SPLIT_CHAR))
                : "";
    }

    @Override
    public List<String> convertToEntityAttribute(String string) {
        return string != null && !string.isEmpty()
                ? Arrays.asList(string.split(SPLIT_CHAR))
                : List.of();
    }
}
