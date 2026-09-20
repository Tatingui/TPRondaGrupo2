package com.example.tprondagrupo2.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.tprondagrupo2.db.dao.PublicacionDao;
import com.example.tprondagrupo2.db.entity.PublicacionEntity;

@Database(entities = {PublicacionEntity.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    public abstract PublicacionDao publicacionDao();

}
