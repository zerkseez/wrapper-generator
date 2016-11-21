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
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.zerkseez.codegen.codewriter.CodeWriter;
import com.github.zerkseez.codegen.codewriter.JavaFileWriter;
import com.github.zerkseez.reflection.MethodInfo;
import com.github.zerkseez.reflection.ParameterInfo;
import com.github.zerkseez.reflection.Reflection;
import com.github.zerkseez.reflection.TypeInfo;

/**
 * Generates wrapper class for the specified class or interface
 * 
 * @author xerxes
 *
 */
public class WrapperGenerator {
    private final Class<?> clazz;
    private final TypeInfo<?> type;
    private final String wrapperPackageName;
    private final String wrapperClassName;

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
        this.type = Reflection.getTypeInfo(clazz);
        this.wrapperPackageName = wrapperPackageName;
        this.wrapperClassName = wrapperClassName;
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
     *             If an I/O error occurs.
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
     *             If an I/O error occurs.
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
     *             If an I/O error occurs.
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
        final JavaFileWriter writer = new JavaFileWriter(wrapperPackageName);
        writer.setGeneratorClass(getClass());

        // Class definition
        writer.append("public class %s", wrapperClassName);
        if (type.hasTypeVariables()) {
            writer.appendTypeVariables(type.getTypeVariables());
        }
        if (clazz.isInterface()) {
            writer.append(" implements ");
        } else {
            writer.append(" extends ");
        }
        writer.append(type).appendLine(" {");

        // Fields
        writer.indent().append("private final ").append(type).appendLine(" wrappedObject;").appendLine();

        // Constructor
        generateConstructor(writer.newCodeBlock());

        // Methods
        final Map<String, List<String>> methods = new HashMap<String, List<String>>();
        final List<MethodInfo> methodList = type.getPublicMethods();
        for (int i = 0; i < methodList.size(); i++) {
            final MethodInfo methodInfo = methodList.get(i);
            if (!Modifier.isFinal(methodInfo.getModifiers()) && !Modifier.isStatic(methodInfo.getModifiers())) {
                List<String> methodBodies = methods.get(methodInfo.getName());
                if (methodBodies == null) {
                    methodBodies = new ArrayList<String>();
                    methods.put(methodInfo.getName(), methodBodies);
                }
                methodBodies.add(generateMethod(methodInfo, writer.newCodeBlock(false)));
            }
        }
        final List<String> methodNames = new ArrayList<String>(methods.keySet());
        methodNames.sort(Comparator.naturalOrder());
        for (String methodName : methodNames) {
            final List<String> methodBodies = methods.get(methodName);
            methodBodies.sort((a, b) -> Integer.compare(a.length(), b.length()));
            for (String methodBody : methodBodies) {
                writer.appendLine().append(methodBody);
            }
        }

        return writer.appendLine("}").toString();
    }

    protected String generateConstructor(final CodeWriter<?> w) {
        w.indent().append("public %s(final ", wrapperClassName).append(type).appendLine(" wrappedObject) {");
        w.indent(2).appendLine("this.wrappedObject = wrappedObject;");
        return w.indent().appendLine("}").toString();
    }

    protected String generateMethod(final MethodInfo method, final CodeWriter<?> w) {
        // Annotations
        w.indent().appendLine("@Override");
        if (method.isAnnotationPresent(Deprecated.class)) {
            w.indent().appendLine("@Deprecated");
        }
        
        // Modifiers
        w.indent().append("public ");
        
        // Type parameters
        if (method.hasDeclaredTypeVariables()) {
            w.appendTypeVariables(method.getDeclaredTypeVariables()).space();
        }
        
        // Return type
        final String returnType = w.getStringOfType(method.getReturnType());
        w.append(returnType).space();
        
        // Method name
        w.append(method.getName()).append("(");
        
        // Parameters
        final List<ParameterInfo> parameterList = method.getParameters();
        final List<String> parameters = new ArrayList<String>();
        final List<String> parameterNames = new ArrayList<String>();
        for (int i = 0; i < parameterList.size(); i++) {
            final ParameterInfo parameterInfo = parameterList.get(i);
            parameters.add(String.format("final %s %s",
                    w.getStringOfType(parameterInfo.getType()), parameterInfo.getName()));
            parameterNames.add(parameterInfo.getName());
        }
        w.appendList(", ", parameters).append(") ");

        // Throws
        if (!method.getExceptionTypes().isEmpty()) {
            w.append("throws ");
            for (int i = 0; i < method.getExceptionTypes().size(); i++) {
                if (i != 0) {
                    w.append(", ");
                }
                w.append(method.getExceptionTypes().get(i));
            }
            w.space();
        }
        w.appendLine("{");
        
        // Body
        w.indent(2);
        if (!"void".equals(returnType)) {
            w.append("return ");
        }
        w.append("wrappedObject.%s(", method.getName());
        for (int i = 0; i < parameterList.size(); i++) {
            final ParameterInfo parameterInfo = parameterList.get(i);
            parameters.add(parameterInfo.getName());
        }
        w.appendList(", ", parameterNames).appendLine(");");

        return w.indent().appendLine("}").toString();
    }
}
