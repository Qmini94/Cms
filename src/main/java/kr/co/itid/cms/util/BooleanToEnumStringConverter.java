package kr.co.itid.cms.util;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class BooleanToEnumStringConverter implements AttributeConverter<Boolean, String> {

    @Override
    public String convertToDatabaseColumn(Boolean attribute) {
        return (attribute != null && attribute) ? "true" : "false";
    }

    @Override
    public Boolean convertToEntityAttribute(String dbData) {
        return "true".equalsIgnoreCase(dbData);
    }
}

