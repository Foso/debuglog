import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import de.jensklingenberg.cabret.Cabret
import de.jensklingenberg.cabret.LogHandler
import de.jensklingenberg.cabret.compiler.MyIrGenerationExtension
import junit.framework.Assert.assertEquals
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration

class TestCompo : ComponentRegistrar {
    override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {
        val messageCollector = configuration.get(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)
        Cabret.addListener(object : Cabret.Listener {
            override fun log(tag: String, name: String, LogLevel: Cabret.LogLevel) {
                println("N========================================" + name)
            }

        })
        IrGenerationExtension.registerExtension(project, MyIrGenerationExtension(messageCollector))

    }


}

class IrPluginTest {

    fun `IR plugin success`() {

        val result = compile(
            plugin = object : ComponentRegistrar {
                override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {
                    val messageCollector =
                        configuration.get(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)
                    Cabret.addListener(object : Cabret.Listener {
                        override fun log(tag: String, name: String, servity: Cabret.LogLevel) {
                            assertEquals(true, true)

                        }

                    })
                    IrGenerationExtension.registerExtension(project, MyIrGenerationExtension(messageCollector))

                }


            },
            sourceFile = SourceFile.kotlin(
                "main.kt", """
package de.jensklingenberg.cabret

annotation class DebugLog

fun main() {
  println(debug("Test"))
}
@DebugLog
fun debug(name:String) = "Hello, World!"
"""
            )
        )
        val kClazz = result.classLoader.loadClass("de.jensklingenberg.cabret.MainKt")
        val main = kClazz.declaredMethods.single { it.name == "main" && it.parameterCount == 0 }
        main.invoke(null)
        //assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }
}

fun compile(
    sourceFiles: List<SourceFile>,
    plugin: ComponentRegistrar = TestCompo(),
): KotlinCompilation.Result {
    return KotlinCompilation().apply {
        sources = sourceFiles
        useIR = true
        compilerPlugins = listOf(plugin)
        inheritClassPath = true
    }.compile()
}

fun compile(
    sourceFile: SourceFile,
    plugin: ComponentRegistrar = TestCompo(),
): KotlinCompilation.Result {
    return compile(listOf(sourceFile), plugin)
}