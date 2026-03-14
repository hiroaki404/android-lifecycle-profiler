package com.hiroaki404.lifecycle

import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.impl.IrStringConcatenationImpl
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.parentClassOrNull
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

class LifecycleIrTransformer(
    private val pluginContext: IrPluginContext,
) : IrElementTransformerVoidWithContext() {

    private val logAnnotationFqName = FqName("com.hiroaki404.lifecycle.LogLifecycle")

    private val lifecycleMethods = setOf(
        "onCreate", "onStart", "onResume",
        "onPause", "onStop", "onDestroy"
    )

    // android.util.Log.d(String, String) を解決
    @OptIn(UnsafeDuringIrConstructionAPI::class)
    private val logDSymbol by lazy {
        pluginContext.referenceFunctions(
            CallableId(
                classId = ClassId(FqName("android.util"), Name.identifier("Log")),
                callableName = Name.identifier("d"),
            )
        ).single { fn ->
            fn.owner.parameters.size == 2 &&
                fn.owner.parameters.all { it.type == pluginContext.irBuiltIns.stringType }
        }
    }

    // System.currentTimeMillis() を解決
    @OptIn(UnsafeDuringIrConstructionAPI::class)
    private val currentTimeMillisSymbol by lazy {
        pluginContext.referenceFunctions(
            CallableId(
                classId = ClassId(FqName("java.lang"), Name.identifier("System")),
                callableName = Name.identifier("currentTimeMillis"),
            )
        ).single()
    }

    @OptIn(UnsafeDuringIrConstructionAPI::class)
    override fun visitFunctionNew(declaration: IrFunction): IrStatement {
        val simpleFunction = declaration as? IrSimpleFunction
            ?: return super.visitFunctionNew(declaration)

        val parentClass = simpleFunction.parentClassOrNull
            ?: return super.visitFunctionNew(declaration)

        if (!parentClass.hasAnnotation(logAnnotationFqName)) {
            return super.visitFunctionNew(declaration)
        }

        val methodName = simpleFunction.name.asString()
        if (methodName !in lifecycleMethods) {
            return super.visitFunctionNew(declaration)
        }

        val body = simpleFunction.body as? IrBlockBody
            ?: return super.visitFunctionNew(declaration)

        val className = parentClass.kotlinFqName.shortName().asString()

        val builder = DeclarationIrBuilder(
            pluginContext,
            simpleFunction.symbol,
            simpleFunction.startOffset,
            simpleFunction.endOffset,
        )

        // Log.d("LC_VIZ", "ClassName|methodName|" + System.currentTimeMillis())
        val message = IrStringConcatenationImpl(
            startOffset = simpleFunction.startOffset,
            endOffset = simpleFunction.endOffset,
            type = pluginContext.irBuiltIns.stringType,
        ).apply {
            arguments.add(builder.irString("$className|$methodName|"))
            arguments.add(builder.irCall(currentTimeMillisSymbol))
        }

        val logCall = builder.irCall(logDSymbol).apply {
            arguments[0] = builder.irString("LC_VIZ")
            arguments[1] = message
        }

        body.statements.add(0, logCall)

        return super.visitFunctionNew(declaration)
    }
}
