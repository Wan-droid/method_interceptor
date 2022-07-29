package com.method.intercept.plugin

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import org.objectweb.asm.ClassVisitor
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.FileInputStream

abstract class InterceptorClassVisitorFactory : AsmClassVisitorFactory<MethodInterceptParam> {

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor {
        val param = parameters.get()
        val yaml = Yaml()
        val data = yaml.loadAs(FileInputStream(param.configFile.get()), Intercept::class.java)

        val outputDir = param.outputLogDir.get()
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }
        val outputLogFile = File(outputDir, RECORD_FILE_NAME)
        if (!outputLogFile.exists()) {
            outputLogFile.createNewFile()
        }
        return createClassVisitor(
            data.intercept,
            outputLogFile,
            param.packagePrefix.get(),
            nextClassVisitor
        )
    }

    override fun isInstrumentable(classData: ClassData): Boolean {
        val param = parameters.get()
        val prefix = param.packagePrefix.get()
        if (classData.className.startsWith(prefix)) {
            return false
        }
        val blackList = param.blackList.get()
        blackList.forEach {
            if (it.endsWith(".*")) {
                val substring = it.substring(0, it.length - 2)
                if (classData.className.startsWith(substring)) {
                    return false
                }
            }
            val lastIndexOf = classData.className.lastIndexOf(".")
            val packageName = classData.className.substring(0, lastIndexOf)
            println(packageName)
            if (packageName == it) {
                return false
            }
        }
        return true
    }
}