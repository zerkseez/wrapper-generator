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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Joiner;
import com.strobel.reflection.Type;
import com.strobel.reflection.TypeList;
import com.strobel.reflection.Types;

/**
 * Abstract writer class for writing Java source code
 * 
 * @author xerxes
 *
 * @param <T>
 *            Any concrete sub-class of this class
 */
public abstract class CodeWriter<T extends CodeWriter<T>> {
    public static final String INDENTATION = "    ";
    public static final String NEW_LINE = "\n";

    private final CodeWriter<?> parent;
    private final List<Object> segments;
    private final List<String> definedGenericParameters;
    private final List<CodeWriterListener> listeners;

    /**
     * Constructs a CodeWriter object with no parent
     */
    public CodeWriter() {
        this(null);
    }

    /**
     * Constructs a CodeWriter object with the specified parent
     */
    public CodeWriter(final CodeWriter<?> parent) {
        this.parent = parent;
        this.segments = new ArrayList<Object>();
        this.definedGenericParameters = new ArrayList<String>();
        this.listeners = new ArrayList<CodeWriterListener>();
    }

    /**
     * Gets the parent CodeWriter object
     * 
     * @return The parent CodeWriter object
     */
    public CodeWriter<?> getParent() {
        return parent;
    }

    /**
     * Writes the specified text
     * 
     * @param text
     *            The text to be written
     * @return This object for chaining purpose
     */
    public T write(final String text) {
        segments.add(text);
        return self();
    }

    /**
     * Writes the specified formatted text
     * 
     * @param pattern
     *            The pattern to be used in String.format()
     * @param args
     *            The arguments to be used in String.format()
     * @return This object for chaining purpose
     */
    public T write(final String pattern, final Object... args) {
        return write(String.format(pattern, args));
    }

    /**
     * Writes a new line
     *
     * @return This object for chaining purpose
     */
    public T writeLine() {
        return write(NEW_LINE);
    }

    /**
     * Writes the specified text followed by a new line
     * 
     * @param text
     *            The text to be written
     * @return This object for chaining purpose
     */
    public T writeLine(final String text) {
        return write(text).write(NEW_LINE);
    }

    /**
     * Writes the specified formatted text followed by a new line
     * 
     * @param pattern
     *            The pattern to be used in String.format()
     * @param args
     *            The arguments to be used in String.format()
     * @return This object for chaining purpose
     */
    public T writeLine(final String pattern, final Object... args) {
        return write(pattern, args).write(NEW_LINE);
    }

    /**
     * Joins the specified list with the specified separator and write the join
     * result
     * 
     * @param separator
     *            The separator
     * @param parts
     *            The parts to be joined
     * @return This object for chaining purpose
     */
    public T writeList(final String separator, final Iterable<?> parts) {
        return write(Joiner.on(separator).join(parts));
    }

    /**
     * Joins the specified list with the specified separator and write the join
     * result
     * 
     * @param separator
     *            The separator
     * @param parts
     *            The parts to be joined
     * @return This object for chaining purpose
     */
    public T writeList(final String separator, final Iterator<?> parts) {
        return write(Joiner.on(separator).join(parts));
    }

    /**
     * Joins the specified list with the specified separator and write the join
     * result
     * 
     * @param separator
     *            The separator
     * @param parts
     *            The parts to be joined
     * @return This object for chaining purpose
     */
    public T writeList(final String separator, final Object[] parts) {
        return write(Joiner.on(separator).join(parts));
    }

    /**
     * Writes an indentation
     * 
     * @return This object for chaining purpose
     */
    public T indent() {
        return write(INDENTATION);
    }

    /**
     * Writes the specified number of indentations
     * 
     * @return This object for chaining purpose
     */
    public T indent(final int times) {
        for (int i = 0; i < times; i++) {
            indent();
        }
        return self();
    }

    /**
     * Writes a space
     * 
     * @return This object for chaining purpose
     */
    public T space() {
        return write(" ");
    }

    /**
     * Writes the specified number of spaces
     * 
     * @return This object for chaining purpose
     */
    public T space(final int times) {
        for (int i = 0; i < times; i++) {
            space();
        }
        return self();
    }

    /**
     * Writes the name of the specified type including its generic type
     * parameters. The type will be imported if possible.
     * 
     * @param type
     *            The type to be rendered
     * @return This object for chaining purpose
     */
    public T renderType(final Class<?> type) {
        return renderType(type, true);
    }

    /**
     * Writes the name of the specified type including its generic type
     * parameters and optionally import the type if possible
     * 
     * @param type
     *            The type to be rendered
     * @param importIfPossible
     *            Indicates whether the type should be imported (if possible)
     * @return This object for chaining purpose
     */
    public T renderType(final Class<?> type, final boolean importIfPossible) {
        return renderType(Type.of(type), importIfPossible);
    }

    /**
     * Writes the name of the specified type including its generic type
     * parameters. The type will be imported if possible.
     * 
     * @param type
     *            The type to be rendered
     * @return This object for chaining purpose
     */
    public T renderType(final Type<?> type) {
        return renderType(type, true);
    }

    /**
     * Writes the name of the specified type including its generic type
     * parameters and optionally import the type if possible
     * 
     * @param type
     *            The type to be rendered
     * @param importIfPossible
     *            Indicates whether the type should be imported (if possible)
     * @return This object for chaining purpose
     */
    public T renderType(final Type<?> type, final boolean importIfPossible) {
        return write(renderTypeToString(type, importIfPossible));
    }

    /**
     * Renders the name of the specified type including its generic type
     * parameters to a string
     * 
     * @param type
     *            The type to be rendered
     * @return The render result
     */
    public String renderTypeToString(final Type<?> type) {
        return renderTypeToString(type, true);
    }

    /**
     * Renders the name of the specified type including its generic type
     * parameters to a string and optionally import the type if possible
     * 
     * @param type
     *            The type to be rendered
     * @param importIfPossible
     *            Indicates whether the type should be imported (if possible)
     * @return The render result
     */
    public String renderTypeToString(final Type<?> type, final boolean importIfPossible) {
        return renderTypeToString(type, importIfPossible, this);
    }

    /**
     * Renders the specified TypeList as generic type parameters
     * 
     * @param typeList
     *            The TypeList to be rendered
     * @return This object for chaining purpose
     */
    public T renderGenericTypeParameters(final TypeList typeList) {
        return write(renderGenericTypeParametersToString(typeList));
    }

    /**
     * Renders the specified TypeList as generic type parameters to a string
     * 
     * @param typeList
     *            The TypeList to be rendered
     * @return The render result
     */
    public String renderGenericTypeParametersToString(final TypeList typeList) {
        final StringBuilder sb = new StringBuilder();
        sb.append('<');
        for (int i = 0; i < typeList.size(); ++i) {
            if (i != 0) {
                sb.append(", ");
            }
            sb.append(renderTypeToString(typeList.get(i), true));
        }
        sb.append('>');
        return sb.toString();
    }

    /**
     * Starts a new code block. Generic type parameters defined in this code
     * block is not visible to its parent. The created code block is appended to
     * the output.
     * 
     * @return The CodeWriter object for the new code block
     */
    public CodeWriter<?> newCodeBlock() {
        return newCodeBlock(true);
    }

    /**
     * Starts a new code block. Generic type parameters defined in this code
     * block is not visible to its parent.
     * 
     * @param append
     *            Indicates if the created code block should be appended to the
     *            output
     * @return The CodeWriter object for the new code block
     */
    public CodeWriter<?> newCodeBlock(final boolean append) {
        final CodeBlockWriter methodWriter = new CodeBlockWriter(this);
        if (append) {
            segments.add(methodWriter);
        }
        return methodWriter;
    }

    /**
     * Gets all listeners, including listeners from parents
     * 
     * @return All listeners
     */
    public List<CodeWriterListener> getListeners() {
        final List<CodeWriterListener> result = new ArrayList<CodeWriterListener>(listeners);
        if (parent != null) {
            result.addAll(parent.getListeners());
        }
        return result;
    }

    /**
     * Adds a listener
     * 
     * @param listener
     *            The listener to be added
     */
    public void addListener(final CodeWriterListener listener) {
        listeners.add(listener);
    }

    /**
     * Gets the content written so far
     */
    @Override
    public String toString() {
        return Joiner.on("").join(segments);
    }

    protected void addGenericParameter(final String name) {
        definedGenericParameters.add(name);
    }

    protected boolean isGenericParameterDefined(final String name) {
        if (definedGenericParameters.contains(name)) {
            return true;
        }
        if (parent != null) {
            return parent.isGenericParameterDefined(name);
        }
        return false;
    }

    protected abstract String tryImportType(final String typeFullName);

    protected String renderTypeToString(final Type<?> type,
                                        final boolean importIfPossible,
                                        final CodeWriter<?> context) {
        final StringBuilder sb = new StringBuilder();
        final String typeFullName = normalizeTypeFullName(type.getFullName());

        if (type.isArray()) {
            sb.append(renderTypeToString(type.getElementType(), true));
            sb.append("[]");
        }
        else if (type.isGenericType()) {
            onVisitType(type);
            sb.append(importIfPossible ? tryImportType(typeFullName) : typeFullName);
            sb.append(renderGenericTypeParametersToString(type.getTypeArguments()));
        }
        else if (type.isGenericParameter()) {
            sb.append(typeFullName);
            if (!context.isGenericParameterDefined(typeFullName)) {
                context.addGenericParameter(typeFullName);
                sb.append(renderSuperAndExtendsBounds(type));
            }
        }
        else if (type.isCompoundType()) {
            final Type<?> baseType = type.getBaseType();
            final TypeList interfaces = type.getInterfaces();
            if (baseType != Types.Object) {
                sb.append(renderTypeToString(baseType, true, newCodeBlock()));
                if (!interfaces.isEmpty()) {
                    sb.append(" & ");
                }
            }
            for (int i = 0, n = interfaces.size(); i < n; i++) {
                if (i != 0) {
                    sb.append(" & ");
                }
                sb.append(renderTypeToString(interfaces.get(i), true, newCodeBlock()));
            }
        }
        else if (type.isWildcardType()) {
            sb.append('?');
            sb.append(renderSuperAndExtendsBounds(type));
        }
        else {
            onVisitType(type);
            sb.append(importIfPossible ? tryImportType(typeFullName) : typeFullName);
        }

        return sb.toString();
    }

    protected String renderSuperAndExtendsBounds(final Type<?> type) {
        final StringBuilder sb = new StringBuilder();
        final Type<?> superBound = type.getSuperBound();
        if (superBound != null && superBound != Types.Object) {
            final String superTypeName = renderTypeToString(superBound, true, newCodeBlock());
            if (!"<any>".equals(superTypeName)) {
                sb.append(" super ");
                sb.append(superTypeName);
            }
        }
        final Type<?> upperBound = type.getExtendsBound();
        if (upperBound != null && upperBound != Types.Object) {
            sb.append(" extends ");
            sb.append(renderTypeToString(upperBound, true, newCodeBlock()));
        }
        return sb.toString();
    }

    protected String convertBinaryNameToJavaName(final String binaryName) {
        return binaryName.replace('$', '.');
    }

    protected String removeDefaultPackageName(final String typeName) {
        return typeName.replace("java.lang.", "");
    }

    protected String normalizeTypeFullName(final String typeName) {
        return convertBinaryNameToJavaName(removeDefaultPackageName(typeName));
    }

    protected void onVisitType(final Type<?> type) {
        for (CodeWriterListener listener : getListeners()) {
            listener.onVisitType(type);
        }
    }

    @SuppressWarnings("unchecked")
    protected T self() {
        return (T) this;
    }

    protected static class CodeBlockWriter extends CodeWriter<CodeBlockWriter> {
        protected CodeBlockWriter(final CodeWriter<?> parent) {
            super(parent);
        }

        @Override
        protected String tryImportType(final String typeFullName) {
            return getParent().tryImportType(typeFullName);
        }
    }
}
