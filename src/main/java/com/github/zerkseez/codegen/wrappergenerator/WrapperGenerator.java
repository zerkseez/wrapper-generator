/*******************************************************************************
 * Copyright 2016 Xerxes Tsang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.github.zerkseez.codegen.wrappergenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.strobel.reflection.MethodInfo;
import com.strobel.reflection.MethodList;
import com.strobel.reflection.ParameterInfo;
import com.strobel.reflection.ParameterList;
import com.strobel.reflection.Type;

/**
 * Generates wrapper class for the specified class or interface
 * 
 * @author xerxes
 *
 */
public class WrapperGenerator {
    public static final String INDENTATION = "    ";

    private final Class<?> clazz;
    private final Type<?> type;
    private final String wrapperPackageName;
    private final String wrapperClassName;
    private final TypeRenderer typeRenderer;

    /**
     * Constructs a WrapperGenerator with default wrapper class name
     * 
     * @param clazz
     *            The class to wrap
     * @param wrapperPackageName
     *            The package name for the wrapper
     */
    public WrapperGenerator(final Class<?> clazz, final String wrapperPackageName) {
        this(clazz, wrapperPackageName, String.format("Wrapped%s", clazz.getSimpleName()));
    }

    /**
     * Constructs a WrapperGenerator
     * 
     * @param clazz
     *            The class to wrap
     * @param wrapperPackageName
     *            The package name for the wrapper
     * @param wrapperClassName
     *            The class name for the wrapper, excluding the package name
     */
    public WrapperGenerator(final Class<?> clazz, final String wrapperPackageName, final String wrapperClassName) {
        this.clazz = clazz;
        this.type = Type.of(clazz);
        this.wrapperPackageName = wrapperPackageName;
        this.wrapperClassName = wrapperClassName;
        this.typeRenderer = new TypeRenderer();
    }

    /**
     * Generates the wrapper and write it to the specified directory
     * 
     * @param outputDirectory
     *            The output directory excluding the package path
     * @param createPackageDirectories
     *            Indicates if the parent directories should be created if
     *            missing
     * @throws IOException
     */
    public void writeTo(final String outputDirectory, final boolean createPackageDirectories) throws IOException {
        final File packageDirectory = new File(outputDirectory, wrapperPackageName.replaceAll("\\.", File.separator));
        final File javaFile = new File(packageDirectory, String.format("%s.java", wrapperClassName));
        if (createPackageDirectories) {
            packageDirectory.mkdirs();
        }
        writeTo(javaFile);
    }

    /**
     * Generates the wrapper and write it to the specified file
     * 
     * @param javaFile
     *            The output file
     * @throws IOException
     */
    public void writeTo(final File javaFile) throws IOException {
        try (OutputStream outputStream = new FileOutputStream(javaFile)) {
            writeTo(outputStream);
        }
    }

    /**
     * Generates the wrapper and write it to the specified output stream
     * 
     * @param outputStream
     *            The output stream to write to
     * @throws IOException
     */
    public void writeTo(final OutputStream outputStream) throws IOException {
        outputStream.write(generateWrapper().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates the wrapper code
     * 
     * @return The wrapper code in Java
     */
    public String generateWrapper() {
        final WrapperGeneratorContext context = new WrapperGeneratorContext(type);
        final StringBuilder sb = new StringBuilder();
        if (!Strings.isNullOrEmpty(wrapperPackageName)) {
            writeLine(sb, 0, "package %s;", wrapperPackageName);
            writeLine(sb);
        }
        writeLine(sb, 0, "import javax.annotation.Generated;");
        writeLine(sb);
        writeLine(sb, 0, "@Generated(\"%s\")", this.getClass().getName());
        write(sb, generateClassBody(context));
        return sb.toString();
    }

    protected String generateClassBody(final WrapperGeneratorContext context) {
        final StringBuilder sb = new StringBuilder();
        write(sb, "public class %s", wrapperClassName);
        if (type.containsGenericParameters()) {
            write(sb, typeRenderer.renderGenericTypeParameters(type.getGenericTypeParameters(), context));
        }
        if (clazz.isInterface()) {
            writeLine(sb, 0, " implements %s {", typeRenderer.renderType(type, context));
        }
        else {
            writeLine(sb, 0, " extends %s {", typeRenderer.renderType(type, context));
        }
        writeLine(sb, 1, "private final %s wrappedObject;", typeRenderer.renderType(type, context));
        writeLine(sb);
        write(sb, generateConstructor(context));
        final Map<String, List<String>> methods = new HashMap<String, List<String>>();
        final MethodList methodList = type.getMethods();
        for (int i = 0; i < methodList.size(); i++) {
            final MethodInfo methodInfo = methodList.get(i);
            if (!methodInfo.isFinal()) {
                List<String> methodBodies = methods.get(methodInfo.getName());
                if (methodBodies == null) {
                    methodBodies = new ArrayList<String>();
                    methods.put(methodInfo.getName(), methodBodies);
                }
                methodBodies.add(generateMethod(methodInfo, context.newFrame()));
            }
        }
        final List<String> methodNames = new ArrayList<String>(methods.keySet());
        methodNames.sort(Comparator.naturalOrder());
        for (String methodName : methodNames) {
            final List<String> methodBodies = methods.get(methodName);
            methodBodies.sort((a, b) -> Integer.compare(a.length(), b.length()));
            for (String methodBody : methodBodies) {
                writeLine(sb);
                write(sb, methodBody);
            }
        }
        writeLine(sb, 0, "}");
        return sb.toString();
    }

    protected String generateConstructor(final WrapperGeneratorContext context) {
        final StringBuilder sb = new StringBuilder();
        writeLine(sb, 1, "public %s(final %s wrappedObject) {", wrapperClassName,
                typeRenderer.renderType(type, context));
        writeLine(sb, 2, "this.wrappedObject = wrappedObject;");
        writeLine(sb, 1, "}");
        return sb.toString();
    }

    protected String generateMethod(final MethodInfo method, final WrapperGeneratorContext context) {
        final StringBuilder sb = new StringBuilder();
        writeLine(sb, 1, "@Override");
        if (method.isAnnotationPresent(Deprecated.class)) {
            writeLine(sb, 1, "@Deprecated");
        }
        write(sb, INDENTATION);
        write(sb, "public ");
        if (method.isGenericMethod()) {
            write(sb, typeRenderer.renderGenericTypeParameters(method.getTypeArguments(), context));
            write(sb, " ");
        }
        final String returnType = typeRenderer.renderType(method.getReturnType(), context);
        write(sb, "%s %s(", returnType, method.getName());
        final ParameterList parameterList = method.getParameters();
        final List<String> parameters = new ArrayList<String>();
        final List<String> parameterNames = new ArrayList<String>();
        for (int i = 0; i < parameterList.size(); i++) {
            final ParameterInfo parameterInfo = parameterList.get(i);
            parameters.add(String.format("final %s %s",
                    typeRenderer.renderType(parameterInfo.getParameterType(), context), parameterInfo.getName()));
            parameterNames.add(parameterInfo.getName());
        }
        write(sb, Joiner.on(", ").join(parameters));
        write(sb, ") ");
        if (!method.getThrownTypes().isEmpty()) {
            write(sb, "throws ");
            for (int i = 0; i < method.getThrownTypes().size(); i++) {
                if (i != 0) {
                    write(sb, ", ");
                }
                write(sb, typeRenderer.renderType(method.getThrownTypes().get(i), context));
            }
            write(sb, " ");
        }
        writeLine(sb, 0, "{");
        write(sb, INDENTATION);
        write(sb, INDENTATION);
        if (!"void".equals(returnType)) {
            write(sb, "return ");
        }
        write(sb, "wrappedObject.%s(", method.getName());
        for (int i = 0; i < parameterList.size(); i++) {
            final ParameterInfo parameterInfo = parameterList.get(i);
            parameters.add(parameterInfo.getName());
        }
        write(sb, Joiner.on(", ").join(parameterNames));
        writeLine(sb, 0, ");");
        writeLine(sb, 1, "}");
        return sb.toString();
    }

    protected void write(final StringBuilder sb, final String content, final Object... args) {
        sb.append(String.format(content, args));
    }

    protected void writeLine(final StringBuilder sb) {
        sb.append('\n');
    }

    protected void writeLine(final StringBuilder sb,
                             final int indentation,
                             final String content,
                             final Object... args) {
        for (int i = 0; i < indentation; i++) {
            sb.append(INDENTATION);
        }
        write(sb, content, args);
        writeLine(sb);
    }
}
