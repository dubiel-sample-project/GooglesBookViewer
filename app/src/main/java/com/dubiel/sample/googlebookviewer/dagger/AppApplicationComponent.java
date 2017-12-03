package com.dubiel.sample.googlebookviewer.dagger;


import dagger.Component;
import dagger.android.AndroidInjectionModule;
import dagger.android.AndroidInjector;

@Component(modules = { AndroidInjectionModule.class, AppApplicationModule.class})
public interface AppApplicationComponent extends AndroidInjector<AppApplication> {
}