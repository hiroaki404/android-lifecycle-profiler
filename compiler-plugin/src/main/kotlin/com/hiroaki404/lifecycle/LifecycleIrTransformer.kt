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
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.isNullableAny
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
        "onPause", "onStop", "onDestroy",
        "onCleared"
    )

    // android.util.Log.d(String, String) を referenceClass 経由で解決
    @OptIn(UnsafeDuringIrConstructionAPI::class)
    private val logDSymbol: IrSimpleFunctionSymbol? by lazy {
        val logClass = pluginContext.referenceClass(
            ClassId(FqName("android.util"), Name.identifier("Log"))
        ) ?: return@lazy printlnFallback()

        logClass.owner.declarations
            .filterIsInstance<IrSimpleFunction>()
            .filter { it.name == Name.identifier("d") }
            .firstOrNull { fn -> fn.parameters.size == 2 }
            ?.symbol
            ?: printlnFallback()
    }

    // System.currentTimeMillis() を referenceClass 経由で解決
    @OptIn(UnsafeDuringIrConstructionAPI::class)
    private val currentTimeMillisSymbol: IrSimpleFunctionSymbol? by lazy {
        val systemClass = pluginContext.referenceClass(
            ClassId(FqName("java.lang"), Name.identifier("System"))
        ) ?: return@lazy null

        systemClass.owner.declarations
            .filterIsInstance<IrSimpleFunction>()
            .firstOrNull { it.name == Name.identifier("currentTimeMillis") }
            ?.symbol
    }

    @OptIn(UnsafeDuringIrConstructionAPI::class)
    private fun printlnFallback(): IrSimpleFunctionSymbol? =
        pluginContext.referenceFunctions(
            CallableId(packageName = FqName("kotlin.io"), callableName = Name.identifier("println"))
        ).singleOrNull { fn ->
            fn.owner.parameters.size == 1 && fn.owner.parameters[0].type.isNullableAny()
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

        val logFn = logDSymbol ?: return super.visitFunctionNew(declaration)
        val className = parentClass.kotlinFqName.shortName().asString()

        val builder = DeclarationIrBuilder(
            pluginContext,
            simpleFunction.symbol,
            simpleFunction.startOffset,
            simpleFunction.endOffset,
        )

        val timeCall = currentTimeMillisSymbol?.let { builder.irCall(it) }

        val message = IrStringConcatenationImpl(
            startOffset = simpleFunction.startOffset,
            endOffset = simpleFunction.endOffset,
            type = pluginContext.irBuiltIns.stringType,
        ).apply {
            arguments.add(builder.irString("$className|$methodName|"))
            if (timeCall != null) arguments.add(timeCall)
        }

        val logCall = builder.irCall(logFn).apply {
            if (logFn.owner.parameters.size == 2) {
                // Log.d(tag, message)
                arguments[0] = builder.irString("LC_VIZ")
                arguments[1] = message
            } else {
                // println fallback
                arguments[0] = message
            }
        }

        body.statements.add(0, logCall)

        return super.visitFunctionNew(declaration)
    }
}
