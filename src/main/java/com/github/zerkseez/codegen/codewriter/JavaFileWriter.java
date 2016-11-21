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
package com.github.zerkseez.codegen.codewriter;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Generated;

import com.google.common.base.Strings;

/**
 * Writer class for writing a Java source code file
 * 
 * @author xerxes
 *
 */
public class JavaFileWriter extends CodeWriter<JavaFileWriter> {
    private static final String DEFAULT_PACKAGE_NAME = "java.lang.";
    
    private String packageName = null;
    private Class<?> generatorClass = null;
    private final Map<String, String> imports;

    /**
     * Constructs a JavaFileWriter
     */
    public JavaFileWriter() {
        this(null);
    }

	/**
	 * Constructs a JavaFileWriter and sets the package name
	 * 
	 * @param packageName
	 *            The package name of this java file
	 */
    public JavaFileWriter(final String packageName) {
        this.packageName = packageName;
        this.imports = new HashMap<String, String>();
    }

    /**
     * Gets the package name for this Java file
     * 
     * @return The package name
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Sets the package name for this Java file
     * 
     * @param packageName
     *            The package name
     * @return This JavaFileWriter object for chaining purpose
     */
    public JavaFileWriter setPackageName(final String packageName) {
        this.packageName = packageName;
        return this;
    }

    /**
     * Gets the code generator class
     * 
     * @return The code generator class
     */
    public Class<?> getGeneratorClass() {
        return generatorClass;
    }

    /**
     * Sets the code generator class
     * 
     * @param generatorClass
     *            The code generator class
     * @return This JavaFileWriter object for chaining purpose
     */
    public JavaFileWriter setGeneratorClass(final Class<?> generatorClass) {
        this.generatorClass = generatorClass;
        return this;
    }

    /**
     * Gets the Java file content
     */
    @Override
    public String toString() {
        final StringBuilder s = new StringBuilder();
        final String annotations = getAnnotations();

        if (!Strings.isNullOrEmpty(getPackageName())) {
            s.append(String.format("package %s;\n", getPackageName()));
        }

        String lastRootPackageName = "";
        for (String className : imports.values().stream().sorted().collect(Collectors.toList())) {
            final String rootPackageName = className.substring(0, className.indexOf('.'));
            if (!lastRootPackageName.equals(rootPackageName)) {
                lastRootPackageName = rootPackageName;
                s.append(NEW_LINE);
            }
            s.append(String.format("import %s;\n", className));
        }

        s.append(NEW_LINE).append(annotations).append(super.toString());
        return s.toString();
    }

    protected String getAnnotations() {
        final StringBuilder s = new StringBuilder();
        if (getGeneratorClass() != null) {
            s.append("@").append(getStringOfType(Generated.class));
            s.append("(\"").append(getGeneratorClass().getName()).append("\")").append(NEW_LINE);
        }
        return s.toString();
    }

    @Override
    public boolean isImported(final String className) {
        if (
                className.startsWith(DEFAULT_PACKAGE_NAME)
                && !className.substring(DEFAULT_PACKAGE_NAME.length()).contains(".")
        ) {
            return true;
        }
        
        final int indexOfLastDot = className.lastIndexOf('.');
        if (indexOfLastDot != -1) {
            final String simpleName = className.substring(indexOfLastDot + 1);
            if (imports.containsKey(simpleName)) {
                return imports.get(simpleName).equals(className);
            }
            imports.put(simpleName, className);
            return true;
        }

        return true;
    }
}
