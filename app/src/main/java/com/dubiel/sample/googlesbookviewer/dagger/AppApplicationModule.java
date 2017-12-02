package com.dubiel.sample.googlesbookviewer.dagger;


import android.content.Context;

import com.dubiel.sample.googlesbookviewer.MainActivity;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class AppApplicationModule {
    @ContributesAndroidInjector
    abstract MainActivity contributeActivityInjector();
}
