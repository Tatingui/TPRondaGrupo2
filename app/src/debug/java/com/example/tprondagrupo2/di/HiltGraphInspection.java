package com.example.tprondagrupo2.di;

import com.example.tprondagrupo2.data.repository.PublicationDetailSource;
import com.example.tprondagrupo2.data.repository.PublicationRepository;
import com.example.tprondagrupo2.db.AppDatabase;
import com.example.tprondagrupo2.db.dao.PublicacionDao;
import com.example.tprondagrupo2.network.SessionManager;
import com.example.tprondagrupo2.network.TokenManager;
import dagger.hilt.EntryPoint;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;

/** Solo debug: acceso al grafo real para comprobar identidad en tests instrumentados. */
@EntryPoint
@InstallIn(SingletonComponent.class)
public interface HiltGraphInspection {
    TokenManager tokenManager();
    SessionManager sessionManager();
    AppDatabase database();
    PublicacionDao publicacionDao();
    PublicationRepository publicationRepository();
    PublicationDetailSource publicationDetailSource();
    OkHttpClient httpClient();
}
