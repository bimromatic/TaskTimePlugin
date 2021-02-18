package com.bimromatic.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.dependency.CustomClassVisitor
import com.android.build.gradle.internal.pipeline.TransformManager
import groovyjarjarasm.asm.ClassReader
import groovyjarjarasm.asm.ClassWriter
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils

//Transform
//简单来讲，Transform 是Gradle在编译项目时的一个task,在.class 文件转换成.dex的流程中会执行这些task, 很明显，.class文件转换为.dex之前就是我们操作.class文件的最佳也是唯一一个机会了

class InsertTransform extends Transform{


    //设置我们自定义的Transform对应的Task名称
    //设置自定义的Transform 对应的task名称， Gradle 在编译的时候，会将这个名称显示
    @Override
    String getName() {
        return "MyPlugin"
    }



    //指定输入的类型，通过这里设定，可以指定我们要处理的文件类型
    //这样确保其他类型的文件不会传入
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    //指定Transfrom的作用范围
    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        //return TransformManager.SCOPE_FULL_PROJECT
        return TransformManager.PROJECT_ONLY
    }

    @Override
    boolean isIncremental() {
        return false
    }


    //注意input的类型，分为“文件夹”和“jar文件”，”文件夹”里面的就是我们写的类对应的class文件，jar文件一般为第三方库。此时，能成功运行，但是这里我们没有注入任何代码。
    @Override
    void transform(Context context, Collection<TransformInput> inputs,
                   Collection<TransformInput> referencedInputs,
                   TransformOutputProvider outputProvider,
                   boolean isIncremental) throws IOException, TransformException, InterruptedException {
        // Transform的inputs有两种类型，一种是目录，一种是jar包，要分开遍历
        inputs.each { TransformInput input ->
            //对类型为“文件夹”的input进行遍历
            input.directoryInputs.each { DirectoryInput directoryInput ->

                //System.out.println("find class: " + file.name)
                //文件夹里面包含的是我们手写的类以及R.class、BuildConfig.class以及R$XXX.class等
                // 获取output目录
                def dest = outputProvider.getContentLocation(directoryInput.name,
                        directoryInput.contentTypes, directoryInput.scopes,
                        Format.DIRECTORY)
                //这里执行字节码的注入，不操作字节码的话也要将输入路径拷贝到输出路径
                FileUtils.copyDirectory(directoryInput.file, dest)

//                File dir = directoryInput.file
//                if (dir) {
//                    dir.traverse(type: FileType.FILES, nameFilter: ~/.*\.class/) { File file ->
//                        System.out.println("find class: " + file.name)
//                        // 对Class 文件进行读取与解析
//                        ClassReader classReader = new ClassReader(file.bytes)
//                        // class 文件写入
//                        ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
//                        // 访问class 文件相应的内容、解析某一个结构就会通知到ClassVisitor的相应方法
//                        CustomClassVisitor classVisitor = new CustomClassVisitor(classWriter)
//                        // 依次调用ClassVisitor 接口的各个方法
//                        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
//                        // 将最终修改的字节码以byte数组形式返回
//                        byte[] bytes = classWriter.toByteArray()
//                        // 通过文件流写入方式覆盖原先的内容，实现class文件的改写
//                        FileOutputStream fileOutputStream = new FileOutputStream(file.path)
//                        fileOutputStream.write(bytes)
//                        fileOutputStream.close()
//                    }
//                    // 处理完输入之后吧输出传给下一个文件
//                    def dest = outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
//                    FileUtils.copyDirectory(directoryInput.file, dest)
//                }
            }

            //对类型为jar文件的input进行遍历
            input.jarInputs.each { JarInput jarInput ->
                //jar文件一般是第三方依赖库jar文件
                // 重命名输出文件（同目录copyFile会冲突）
                def jarName = jarInput.name
                def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length() - 4)
                }
                //生成输出路径 + md5Name
                def dest = outputProvider.getContentLocation(jarName + md5Name,
                        jarInput.contentTypes, jarInput.scopes, Format.JAR)

                //这里执行字节码的注入，不操作字节码的话也要将输入路径拷贝到输出路径
                FileUtils.copyFile(jarInput.file, dest)
            }
        }
    }


//    /**
//     * 自定义时最重要的方法，在这个方法中可以获取两个数据的流向
//     * inputs: inputs 中传过来的输入流，其中有两种格式，jat包格式，directory（目录格式）
//     * outputProvider：outputProvider 获取到输出目录，最后将修改的文件复制到输出目录，这一步必须做，否则编译会报错。
//     * @param transformInvocation
//     * @throws TransformException* @throws InterruptedException* @throws IOException
//     */
//    @Override
//    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
//        // 拿到所有的class文件
//        Collection<TransformInput> transformInputs = transformInvocation.inputs
//        TransformOutputProvider outputProvider = transformInvocation.outputProvider
//        if (outputProvider != null) {
//            outputProvider.deleteAll()
//        }
//        transformInputs.each { TransformInput transformInput ->
//            transformInput.jarInputs.each { JarInput jarInput ->
//                File file = jarInput.file
//                System.out.println("find jar input: " + file.name)
//                def dest = outputProvider.getContentLocation(jarInput.name,
//                        jarInput.contentTypes,
//                        jarInput.scopes, Format.JAR)
//                FileUtils.copyFile(file, dest)
//            }
//            // // 遍历directoryInputs(文件夹中的class文件) directoryInputs代表着以源码方式参与项目编译的所有目录结构及其目录下的源码文件
//            //            // 比如我们手写的类以及R.class、BuildConfig.class以及MainActivity.class等
//            transformInput.directoryInputs.each { DirectoryInput directoryInput ->
//                File dir = directoryInput.file
//                if (dir) {
//                    dir.traverse(type: FileType.FILES, nameFilter: ~/.*\.class/) { File file ->
//                        System.out.println("find class: " + file.name)
//                        // 对Class 文件进行读取与解析
//                        ClassReader classReader = new ClassReader(file.bytes)
//                        // class 文件写入
//                        ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
//                        // 访问class 文件相应的内容、解析某一个结构就会通知到ClassVisitor的相应方法
//                        CustomClassVisitor classVisitor = new CustomClassVisitor(classWriter)
//                        // 依次调用ClassVisitor 接口的各个方法
//                        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
//                        // 将最终修改的字节码以byte数组形式返回
//                        byte[] bytes = classWriter.toByteArray()
//                        // 通过文件流写入方式覆盖原先的内容，实现class文件的改写
//                        FileOutputStream fileOutputStream = new FileOutputStream(file.path)
//                        fileOutputStream.write(bytes)
//                        fileOutputStream.close()
//                    }
//                    // 处理完输入之后吧输出传给下一个文件
//                    def dest = outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
//                    FileUtils.copyDirectory(directoryInput.file, dest)
//                }
//            }
//        }
//
//    }
}