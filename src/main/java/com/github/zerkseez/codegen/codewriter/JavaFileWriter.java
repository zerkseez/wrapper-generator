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

import com.google.common.base.Strings;

/**
 * Writer class for writing a Java source code file
 * 
 * @author xerxes
 *
 */
public class JavaFileWriter extends CodeWriter<JavaFileWriter> {
    private String packageName = null;
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
     */
    public JavaFileWriter setPackageName(final String packageName) {
        this.packageName = packageName;
        return this;
    }

    /**
     * Gets the Java file content
     */
    @Override
    public String toString() {
        final StringBuilder s = new StringBuilder();
        if (!Strings.isNullOrEmpty(getPackageName())) {
            s.append(String.format("package %s;\n\n", getPackageName()));
        }
        for (String className : imports.values().stream().sorted().collect(Collectors.toList())) {
            s.append(String.format("import %s;\n", className));
        }
        s.append("\n");
        s.append(super.toString());
        return s.toString();
    }

    @Override
    protected String tryImportType(final String typeFullName) {
        final int indexOfLastDot = typeFullName.lastIndexOf('.');
        if (indexOfLastDot != -1) {
            final String simpleName = typeFullName.substring(indexOfLastDot + 1);
            if (imports.containsKey(simpleName)) {
                return imports.get(simpleName).equals(typeFullName) ? simpleName : typeFullName;
            }
            imports.put(simpleName, typeFullName);
            return simpleName;
        }
        return typeFullName;
    }
}
