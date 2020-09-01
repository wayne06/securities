package com.wnzhong.counter.util;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.util.List;

public class JsonUtil {

    private static Gson gson = new Gson();

    private static JsonParser jsonParser = new JsonParser();

    public static String toJson(Object o) {
        return gson.toJson(o);
    }

    public static <T> T fromJson(String json, Class<T> classOfT) throws JsonSyntaxException {
        return gson.fromJson(json, classOfT);
    }

    public static <T> List<T> fromJsonArr(String json, Class<T> classOfListT) {
        List<T> beans = Lists.newArrayList();
        for (JsonElement jsonElement : jsonParser.parse(json).getAsJsonArray()) {
            T bean = gson.fromJson(jsonElement, classOfListT);
            beans.add(bean);
        }
        return beans;
    }

}
