package com.dubiel.sample.googlebookviewer.dagger;


import com.dubiel.sample.googlebookviewer.MainActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class AppApplicationModule {
    @ContributesAndroidInjector
    abstract MainActivity contributeActivityInjector();
}
