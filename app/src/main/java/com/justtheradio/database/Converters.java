package com.justtheradio.database;

import androidx.room.TypeConverter;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.justtheradio.util.source.ServiceLocator;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Converters {
    private static final Gson gson = ServiceLocator.getInstance().getGson();

    @TypeConverter
    public static String fromUUID(UUID uuid) {
        return uuid == null ? null : uuid.toString();
    }

    @TypeConverter
    public static UUID toUUID(String stringUUID) {
        return stringUUID == null ? null : UUID.fromString(stringUUID);
    }

    @TypeConverter
    public static Long fromDate(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static Date toDate(Long epochMilliseconds) {
        return epochMilliseconds == null ? null : new Date(epochMilliseconds);
    }

    @TypeConverter
    public static String fromList(List<String> list) {
        return list == null ? null : gson.toJson(list);
    }

    @TypeConverter
    public static List<String> toList(String jsonList) {
        Type type = new TypeToken<List<String>>() {}.getType();
        return jsonList == null ? null : gson.fromJson(jsonList, type);
    }


}
