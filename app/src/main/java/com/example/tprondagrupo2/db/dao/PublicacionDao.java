package com.example.tprondagrupo2.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.tprondagrupo2.db.entity.PublicacionEntity;

import java.util.List;

@Dao
public interface PublicacionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(PublicacionEntity publicacion);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<PublicacionEntity> publicaciones);

    @Query("SELECT * FROM publicaciones ORDER BY lastSeenTimestamp DESC")
    List<PublicacionEntity> getAll();

    @Query("SELECT * FROM publicaciones WHERE id = :id LIMIT 1")
    PublicacionEntity getById(String id);

    @Query("DELETE FROM publicaciones")
    void deleteAll();
}
