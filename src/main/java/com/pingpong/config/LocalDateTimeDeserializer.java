package com.pingpong.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        String rawData = p.getText();

        // 배열 형식인지 확인
        if (rawData.startsWith("[")) {
            // 배열을 int[]로 파싱
            int[] timestampArray = p.readValueAs(int[].class);

            if (timestampArray.length < 6) {
                throw new JsonProcessingException("Invalid timestamp array: " + p.getText()) {
                };
            }

            // 배열을 LocalDateTime으로 변환
            return LocalDateTime.of(
                    timestampArray[0], // year
                    timestampArray[1], // month
                    timestampArray[2], // day
                    timestampArray[3], // hour
                    timestampArray[4], // minute
                    timestampArray[5], // second
                    timestampArray.length > 6 ? timestampArray[6] : 0 // nanoseconds
            );
        } else {
            // 기존 로직: 문자열을 LocalDateTime으로 변환
            String time = rawData;

            // 'Z' 제거
            if (time.endsWith("Z")) {
                time = time.substring(0, time.length() - 1);
            }

            return LocalDateTime.parse(time, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
    }

}