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

import com.github.zerkseez.codegen.codewriter.CodeWriter;
import com.github.zerkseez.codegen.codewriter.JavaFileWriter;
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
	private final Class<?> clazz;
	private final Type<?> type;
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
		this.type = Type.of(clazz);
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

		// Class definition
		writer.write("public class %s", wrapperClassName);
		if (type.containsGenericParameters()) {
			writer.renderGenericTypeParameters(type.getGenericTypeParameters());
		}
		if (clazz.isInterface()) {
			writer.write(" implements ");
		} else {
			writer.write(" extends ");
		}
		writer.renderType(type).writeLine(" {");

		// Fields
		writer.indent().write("private final ").renderType(type).writeLine(" wrappedObject;").writeLine();

		// Constructor
		generateConstructor(writer.newCodeBlock());

		// Methods
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
				methodBodies.add(generateMethod(methodInfo, writer.newCodeBlock(false)));
			}
		}
		final List<String> methodNames = new ArrayList<String>(methods.keySet());
		methodNames.sort(Comparator.naturalOrder());
		for (String methodName : methodNames) {
			final List<String> methodBodies = methods.get(methodName);
			methodBodies.sort((a, b) -> Integer.compare(a.length(), b.length()));
			for (String methodBody : methodBodies) {
				writer.writeLine().write(methodBody);
			}
		}

		return writer.writeLine("}").toString();
	}

	protected String generateConstructor(final CodeWriter<?> w) {
		w.indent().write("public %s(final ", wrapperClassName).renderType(type).writeLine(" wrappedObject) {");
		w.indent(2).writeLine("this.wrappedObject = wrappedObject;");
		return w.indent().writeLine("}").toString();
	}

	protected String generateMethod(final MethodInfo method, final CodeWriter<?> w) {
		// Annotations
		w.indent().writeLine("@Override");
		if (method.isAnnotationPresent(Deprecated.class)) {
			w.indent().writeLine("@Deprecated");
		}
		
		// Modifiers
		w.indent().write("public ");
		
		// Type parameters
		if (method.isGenericMethod()) {
			w.renderGenericTypeParameters(method.getTypeArguments()).space();
		}
		
		// Return type
		final String returnType = w.renderTypeToString(method.getReturnType());
		w.write(returnType).space();
		
		// Method name
		w.write(method.getName()).write("(");
		
		// Parameters
		final ParameterList parameterList = method.getParameters();
		final List<String> parameters = new ArrayList<String>();
		final List<String> parameterNames = new ArrayList<String>();
		for (int i = 0; i < parameterList.size(); i++) {
			final ParameterInfo parameterInfo = parameterList.get(i);
			parameters.add(String.format("final %s %s",
					w.renderTypeToString(parameterInfo.getParameterType()), parameterInfo.getName()));
			parameterNames.add(parameterInfo.getName());
		}
		w.writeList(", ", parameters).write(") ");

		// Throws
		if (!method.getThrownTypes().isEmpty()) {
			w.write("throws ");
			for (int i = 0; i < method.getThrownTypes().size(); i++) {
				if (i != 0) {
					w.write(", ");
				}
				w.renderType(method.getThrownTypes().get(i));
			}
			w.space();
		}
		w.writeLine("{");
		
		// Body
		w.indent(2);
		if (!"void".equals(returnType)) {
			w.write("return ");
		}
		w.write("wrappedObject.%s(", method.getName());
		for (int i = 0; i < parameterList.size(); i++) {
			final ParameterInfo parameterInfo = parameterList.get(i);
			parameters.add(parameterInfo.getName());
		}
		w.writeList(", ", parameterNames).writeLine(");");

		return w.indent().writeLine("}").toString();
	}
}
