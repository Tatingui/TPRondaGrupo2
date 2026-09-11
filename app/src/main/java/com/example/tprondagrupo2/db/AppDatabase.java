package com.example.tprondagrupo2.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.tprondagrupo2.db.dao.PublicacionDao;
import com.example.tprondagrupo2.db.entity.PublicacionEntity;

@Database(entities = {PublicacionEntity.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract PublicacionDao publicacionDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "ronda_db")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries() // Simplificamos para este TPO, en prod seria async
                    .build();
        }
        return instance;
    }
}
