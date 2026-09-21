package com.example.tprondagrupo2.di;

import android.content.Context;
import androidx.room.Room;
import com.example.tprondagrupo2.db.AppDatabase;
import com.example.tprondagrupo2.db.dao.PublicacionDao;
import javax.inject.Singleton;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public final class StorageModule {
    @Provides
    @Singleton
    public static AppDatabase provideDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(context, AppDatabase.class, "ronda_db")
                .fallbackToDestructiveMigration()
                // Se conserva hasta mover Home y detalle a persistencia asincrónica (incremento 6).
                .allowMainThreadQueries()
                .build();
    }

    @Provides
    @Singleton
    public static PublicacionDao providePublicacionDao(AppDatabase database) {
        return database.publicacionDao();
    }
}
