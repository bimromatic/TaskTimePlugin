package com.bimromatic.plugin

import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.tasks.TaskState

import java.text.SimpleDateFormat

/**
 *
 */
class BuildTimeCostPlugin implements Plugin<Project>{

    private final String TAG = "[TASKTIME] ";
    //List<TaskInfo> taskInfoList = []
    //用来记录 task 的执行时长等信息
    List<TaskInfo> taskInfoList = new ArrayList<>()

    long startMillis
    long beginOfSetting

    def beginOfConfig
    def configHasBegin = false
    def beginOfProjectConfig = new HashMap()
    def beginOfProjectExcute

    @Override
    void apply(Project project) {
        startMillis = System.currentTimeMillis()
        beginOfSetting = System.currentTimeMillis()

        //监听每个task的执行
        /**
         * TaskExecutionListener 来监听整个构建过程中 task 的执行
         */
        project.getGradle().addListener(new TaskExecutionListener() {
            @Override
            void beforeExecute(Task task) {
                //task开始执行之前搜集task的信息
                //记录开始时间
                task.ext.startTime = System.currentTimeMillis()
            }

            @Override
            void afterExecute(Task task, TaskState taskState) {
                //task执行完之后，记录结束时的时间
                //计算该 task 的执行时长
                def exeDuration = System.currentTimeMillis() - task.ext.startTime
                if (exeDuration >= 500) {
                    taskInfoList.add(new TaskInfo(task: task, exeDuration: exeDuration))
                }
            }
        })

        //编译结束之后：
        /**
         * BuildListener 来监听整个构建是否完成
         */
        project.getGradle().addBuildListener(new BuildListener() {
            @Override
            void buildStarted(Gradle gradle) {

            }

            @Override
            void settingsEvaluated(Settings settings) {

            }

            @Override
            void beforeSettings(Settings settings) {
                super.beforeSettings(settings)
            }

            @Override
            void projectsLoaded(Gradle gradle) {
                //println '初始化阶段，耗时：' + (System.currentTimeMillis() - beginOfSetting) + 'ms'
            }

            @Override
            void projectsEvaluated(Gradle gradle) {

            }

            @Override
            void buildFinished(BuildResult buildResult) {
                println "---------------------------------------"
                println "build finished, now println all task execution time:"
                println "---------------------------------------"
                //按 task 执行顺序打印出执行时长信息
                if (!taskInfoList.isEmpty()&&taskInfoList!=null) {
                    //Comparator接口的compare方法来完成自定义排序
                    Collections.sort(taskInfoList, new Comparator<TaskInfo>() {
                        @Override
                        int compare(TaskInfo t, TaskInfo t1) {
                            //返回值为int类型，大于0表示正序，小于0表示逆序
                            return t1.exeDuration - t.exeDuration
                        }
                    })
                    StringBuilder sb = new StringBuilder()
                    int buildSec = (System.currentTimeMillis() - startMillis) / 1000;
                    int m = buildSec / 60;
                    int s = buildSec % 60;
                    def timeInfo = (m == 0 ? "${s}s" : "${m}m ${s}s (${buildSec}s)")
                    sb.append("BUILD FINISHED in $timeInfo\n")

                    taskInfoList.each {
                        sb.append(TAG+String.format("%7sms %s\n", it.exeDuration, it.task.path))
                    }
                    def content = sb.toString()
                    println(content)
                    File file = new File(project.getGradle().rootProject.buildDir.getAbsolutePath(),
                            "build_task_time_records_" + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date()) + ".txt")
                    file.getParentFile().mkdirs()
                    file.write(content)
                }
                println "---------------------------------------"
                println "----------------END------------------"
                println "---------------------------------------"
            }
        })
    }

    //关于 task 的执行信息
    class TaskInfo {
        Task task
        long exeDuration
    }
}