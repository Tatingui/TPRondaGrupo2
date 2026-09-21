package com.example.tprondagrupo2.di;

import com.example.tprondagrupo2.data.repository.PublicationDetailSource;
import com.example.tprondagrupo2.data.repository.PublicationRepository;
import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public abstract class RepositoryModule {
    @Binds
    public abstract PublicationDetailSource bindPublicationDetailSource(PublicationRepository repository);
}
