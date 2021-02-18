package com.bimromatic.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import com.android.build.gradle.AppExtension

 class MyPlugin implements Plugin<Project>{

    @Override
    void apply(Project project) {

        def android = project.extensions.findByType(AppExtension);
        android.registerTransform(new InsertTransform())
        project.gradle.addListener(new TaskListener())
    }
}