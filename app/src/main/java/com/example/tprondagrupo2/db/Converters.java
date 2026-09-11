package com.example.tprondagrupo2.db;

import androidx.room.TypeConverter;

import com.example.tprondagrupo2.model.Vendedor;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class Converters {
    private static final Gson gson = new Gson();

    @TypeConverter
    public static String fromStringList(List<String> list) {
        return gson.toJson(list);
    }

    @TypeConverter
    public static List<String> toStringList(String data) {
        Type listType = new TypeToken<List<String>>() {}.getType();
        return gson.fromJson(data, listType);
    }

    @TypeConverter
    public static String fromVendedor(Vendedor vendedor) {
        return gson.toJson(vendedor);
    }

    @TypeConverter
    public static Vendedor toVendedor(String data) {
        return gson.fromJson(data, Vendedor.class);
    }
}
