package me.modernpage.data.local;

import android.util.Log;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import me.modernpage.data.local.entity.model.Link;
import me.modernpage.util.CustomStringUtil;

public class Converters {
    private static final String TAG = "Converters";

    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public String fromLinkToString(List<Link> info) {
        if (info == null) {
            return null;
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<Link>>() {
        }.getType();
        return gson.toJson(info, type);
    }

    @TypeConverter
    public List<Link> fromStringToLink(String object) {
        if (object == null) {
            return (null);
        }

        Gson gson = new Gson();
        Type type = new TypeToken<List<Link>>() {
        }.getType();
        return gson.fromJson(object, type);
    }

    @TypeConverter
    public List<Long> stringToLongList(String data) {
        if (data == null) {
            return Collections.emptyList();
        }
        return CustomStringUtil.splitToLongList(data);
    }

    @TypeConverter
    public String longListToString(List<Long> longs) {
        return CustomStringUtil.joinLongToString(longs);
    }

    @TypeConverter
    public Map<String, String> stringToMap(String data) {
        return new Gson().fromJson(data, new TypeToken<Map<String, String>>() {
        }.getType());
    }

    @TypeConverter
    public String mapToString(Map<String, String> data) {
        return data == null ? "" : new Gson().toJson(data);
    }
}
