package com.back.standard.util;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// JSON 라이브러리 없이, 문자열·정수 값만 가진 평평한 객체를 만들고 읽는다.
public final class JsonUtil {
    private static final Pattern FIELD = Pattern.compile(
            "\"((?:\\\\.|[^\"\\\\])*)\"\\s*:\\s*(\"((?:\\\\.|[^\"\\\\])*)\"|-?\\d+)"
    );

    private JsonUtil() {
    }

    // keys 순서대로 필드를 쓴다.
    // {
    //   "id": 1,
    //   "content": "명언 1"
    // }
    public static String toJson(Map<String, Object> map, List<String> keys) {
        StringBuilder sb = new StringBuilder("{\n");

        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            Object value = map.get(key);

            sb.append("  \"").append(escape(key)).append("\": ");

            if (value instanceof Number) {
                sb.append(value);
            } else {
                sb.append('"').append(escape(String.valueOf(value))).append('"');
            }

            if (i < keys.size() - 1) sb.append(',');
            sb.append('\n');
        }

        return sb.append('}').toString();
    }

    // 숫자 값은 Integer, 문자열 값은 String 으로 돌려준다.
    public static Map<String, Object> parseObject(String json) {
        Map<String, Object> map = new LinkedHashMap<>();
        Matcher matcher = FIELD.matcher(json);

        while (matcher.find()) {
            String key = unescape(matcher.group(1));

            if (matcher.group(3) != null) {
                map.put(key, unescape(matcher.group(3)));
            } else {
                map.put(key, Integer.parseInt(matcher.group(2)));
            }
        }

        return map;
    }

    private static String escape(String s) {
        StringBuilder sb = new StringBuilder();

        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> sb.append(c);
            }
        }

        return sb.toString();
    }

    private static String unescape(String s) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (c != '\\' || i == s.length() - 1) {
                sb.append(c);
                continue;
            }

            char next = s.charAt(++i);
            switch (next) {
                case 'n' -> sb.append('\n');
                case 'r' -> sb.append('\r');
                case 't' -> sb.append('\t');
                default -> sb.append(next);
            }
        }

        return sb.toString();
    }
}
