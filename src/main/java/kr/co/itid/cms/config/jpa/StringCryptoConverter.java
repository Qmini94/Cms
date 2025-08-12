package kr.co.itid.cms.config.jpa;


import kr.co.itid.cms.util.CryptoUtil;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/** String <-> VARBINARY(BLOB) 변환 컨버터 (AES-GCM) */
@Converter(autoApply = false)
public class StringCryptoConverter implements AttributeConverter<String, byte[]> {
    @Override public byte[] convertToDatabaseColumn(String attribute) { return CryptoUtil.encryptToBytes(attribute); }
    @Override public String convertToEntityAttribute(byte[] dbData)   { return CryptoUtil.decryptToString(dbData); }
}