package com.bimromatic.plugin

import groovy.xml.MarkupBuilder
import org.codehaus.groovy.ant.Groovy

//默认情况下，Groovy 在代码中包括以下库，因此您不需要显式导入它们。start
import java.lang.*
import java.util.*
import java.io.*
import java.net.*

import groovy.lang.*
import groovy.util.*

import java.math.BigInteger
import java.math.BigDecimal

// end


class StudyGroovy{
    //标识符被用来定义变量，函数或其他用户定义的变量。标识符以字母开头，美元或下划线。他们不能以数字开头。以下是有效标识符的一些例子
    def employeename
    def student1
    def student_name

    //其中，${def} 是在 Groovy 用来定义标识符的关键字。


    def xml = new MarkupBuilder()
    static void main(String[] args) {
        println("hello groovy")

        println getX()
        println getConetnt()


        //创建文件夹
        def file = new File('/Users/huwei/Workspace/android/maven/buildtasktime/txt')
        file.mkdir()

        //写入文件
        new File('/Users/huwei/Workspace/android/maven/buildtasktime/txt', 'example.txt').withWriter("UTF-8") {
            writer -> writer.writeLine 'Hello Groovy \n this is frist'
        }

        //读取文件的内容到字符串
        File redFile = new File("/Users/huwei/Workspace/android/maven/buildtasktime/txt/example.txt")
        println redFile.text

        //以下示例将输出Groovy中的文本文件的所有行。方法eachLine内置在Groovy中的File类中，目的是确保文本文件的每一行都被读取。
        new File("/Users/huwei/Workspace/android/maven/buildtasktime/txt/example.txt").eachLine {
            line -> println "line : $line";
        }

        //获取文件的大小
        File sizeFile = new File("/Users/huwei/Workspace/android/maven/buildtasktime/txt/example.txt")
        println "The file ${file.absolutePath} has ${file.length()} bytes"

        //测试文件是否是目录
        def pathFile = new File('/Users/huwei/Workspace/android/maven/buildtasktime/txt/example.txt')
        println "File? ${pathFile.isFile()}"
        println "Directory? ${pathFile.isDirectory()}"

        //创建目录
        def directoryFile = new File('/Users/huwei/Workspace/android/maven/buildtasktime/txt2')
        directoryFile.mkdir()

        //写入文件
        new File('/Users/huwei/Workspace/android/maven/buildtasktime/txt2', 'example.txt2').withWriter("UTF-8") {
            writer -> writer.writeLine 'Hello Groovy \nthis is frist two'
        }

        //删除文件
        //def deletFile = new File('/Users/huwei/Workspace/android/maven/buildtasktime/txt/example.txt')
        //deletFile.delete()

        //Groovy还提供将内容从一个文件复制到另一个文件的功能。以下示例显示如何完成此操作。
        def src = new File("/Users/huwei/Workspace/android/maven/buildtasktime/txt/example.txt")
        def dst = new File("/Users/huwei/Workspace/android/maven/buildtasktime/txt2/example.txt2")

        dst << src.text


        //Groovy还提供了列出驱动器中的驱动器和文件的功能。
        //以下示例显示如何使用File类的listRoots函数显示机器上的驱动器。
        def rootFiles = new File("test").listRoots()
        rootFiles.each {
            println file.absolutePath
        }
//        rootFiles.each {
//            file -> println file.absolutePath
//        }


        //以下示例显示如何使用File类的eachFile函数列出特定目录中的文件。
        new File("/Users/huwei/Workspace/android/maven/buildtasktime/txt").eachFile() {
           filess -> println  filess.getAbsolutePath()
        }

        new File("/Users/huwei/Workspace/android/maven/buildtasktime").eachFileRecurse() {
            files -> println files.getAbsolutePath()
        }

        def v = {
            v -> println v
        }

        testMethod v
    }

    static int X = 100
    static int getX(){
        return X
    }

    static def getConetnt(){
        return 123
    }




    static def testMethod(Closure closure){
        closure('闭包 test')
    }


}