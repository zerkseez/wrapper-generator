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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.github.zerkseez.reflection.Reflection;
import com.github.zerkseez.reflection.TypeInfo;
import com.github.zerkseez.reflection.TypeVariableInfo;
import com.google.common.base.Joiner;

/**
 * Abstract writer class for writing Java source code
 * 
 * @author xerxes
 *
 * @param <T>
 *            Any concrete sub-class of this class
 */
public abstract class CodeWriter<T extends CodeWriter<T>> implements TypeInfo.ToStringContext {
    public static final String INDENTATION = "    ";
    public static final String NEW_LINE = "\n";

    private final CodeWriter<?> parent;
    private final List<Object> segments;
    private final Set<String> definedTypeVariables;

    /**
     * Constructs a CodeWriter object with no parent
     */
    public CodeWriter() {
        this(null);
    }

	/**
	 * Constructs a CodeWriter object with the specified parent
	 * 
	 * @param parent
	 *            The parent CodeWriter object
	 */
    public CodeWriter(final CodeWriter<?> parent) {
        this.parent = parent;
        this.segments = new ArrayList<Object>();
        this.definedTypeVariables = new HashSet<String>();
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
     * Appends the specified text
     * 
     * @param text
     *            The text to be appended
     * @return This object for chaining purpose
     */
    public T append(final String text) {
        segments.add(text);
        return self();
    }

    /**
     * Appends the specified formatted text
     * 
     * @param pattern
     *            The pattern to be used in String.format()
     * @param args
     *            The arguments to be used in String.format()
     * @return This object for chaining purpose
     */
    public T append(final String pattern, final Object... args) {
        return append(String.format(pattern, args));
    }

    /**
     * Appends a new line
     *
     * @return This object for chaining purpose
     */
    public T appendLine() {
        return append(NEW_LINE);
    }

    /**
     * Appends the specified text followed by a new line
     * 
     * @param text
     *            The text to be appended
     * @return This object for chaining purpose
     */
    public T appendLine(final String text) {
        return append(text).append(NEW_LINE);
    }

    /**
     * Appends the specified formatted text followed by a new line
     * 
     * @param pattern
     *            The pattern to be used in String.format()
     * @param args
     *            The arguments to be used in String.format()
     * @return This object for chaining purpose
     */
    public T appendLine(final String pattern, final Object... args) {
        return append(pattern, args).append(NEW_LINE);
    }
    
	/**
	 * Appends the specified string as string literal
	 * 
	 * @param text
	 *            The string to be appended
	 * @return This object for chaining purpose
	 */
    public T appendStringLiteral(final String text) {
        return append("\"").append(text.replaceAll("\\\\", "\\\\").replaceAll("\"", "\\\"")).append("\"");
    }

    /**
     * Joins the specified list with the specified separator and appends the
     * join result
     * 
     * @param separator
     *            The separator
     * @param parts
     *            The parts to be joined
     * @return This object for chaining purpose
     */
    public T appendList(final String separator, final Iterable<?> parts) {
        return append(Joiner.on(separator).join(parts));
    }

    /**
     * Joins the specified list with the specified separator and appends the
     * join result
     * 
     * @param separator
     *            The separator
     * @param parts
     *            The parts to be joined
     * @return This object for chaining purpose
     */
    public T appendList(final String separator, final Iterator<?> parts) {
        return append(Joiner.on(separator).join(parts));
    }

    /**
     * Joins the specified list with the specified separator and appends the
     * join result
     * 
     * @param separator
     *            The separator
     * @param parts
     *            The parts to be joined
     * @return This object for chaining purpose
     */
    public T appendList(final String separator, final Object[] parts) {
        return append(Joiner.on(separator).join(parts));
    }

    /**
     * Appends an indentation
     * 
     * @return This object for chaining purpose
     */
    public T indent() {
        return append(INDENTATION);
    }

	/**
	 * Appends the specified number of indentations
	 * 
	 * @param times
	 *            Number of indentations
	 * @return This object for chaining purpose
	 */
    public T indent(final int times) {
        for (int i = 0; i < times; i++) {
            indent();
        }
        return self();
    }

    /**
     * Appends a space
     * 
     * @return This object for chaining purpose
     */
    public T space() {
        return append(" ");
    }

	/**
	 * Appends the specified number of spaces
	 * 
	 * @param times
	 *            Number of spaces
	 * @return This object for chaining purpose
	 */
    public T space(final int times) {
        for (int i = 0; i < times; i++) {
            space();
        }
        return self();
    }

    /**
     * Appends a dot
     * 
     * @return This object for chaining purpose
     */
    public T dot() {
        return append(".");
    }

    /**
     * Appends a quote
     * 
     * @return This object for chaining purpose
     */
    public T quote() {
        return append("\"");
    }

    /**
     * Appends the name of the specified type including any generic type
     * variables
     * 
     * @param type
     *            The type to be rendered
     * @return This object for chaining purpose
     */
    public T append(final Class<?> type) {
        return append(type, true);
    }

    /**
     * Appends the name of the specified type
     * 
     * @param type
     *            The type to be rendered
     * @param includeTypeVariables
     *            Indicates whether type variables should be included
     * @return This object for chaining purpose
     */
    public T append(final Class<?> type, final boolean includeTypeVariables) {
        return append(getStringOfType(type, includeTypeVariables));
    }
    
    /**
     * Appends the name of the specified type including any generic type
     * variables
     * 
     * @param type
     *            The type to be rendered
     * @return This object for chaining purpose
     */
    public T append(final TypeInfo<?> type) {
        return append(type, true);
    }
    
    /**
     * Appends the name of the specified type
     * 
     * @param type
     *            The type to be rendered
     * @param includeTypeVariables
     *            Indicates whether type variables should be included
     * @return This object for chaining purpose
     */
    public T append(final TypeInfo<?> type, final boolean includeTypeVariables) {
        return append(getStringOfType(type, includeTypeVariables));
    }
    
	/**
	 * Tries importing the specified class name if possible and appends it
	 * 
	 * @param fullClassName
	 *            The full name of the class to be appended
	 * @return This object for chaining purpose
	 */
    public T appendClassName(final String fullClassName) {
    	return append(tryImportType(fullClassName));
    }
    
	/**
	 * Gets the string representation of the specified type including any
	 * generic type variables
	 * 
	 * @param type
	 *            The type to be rendered
	 * @return The string representation of the specified type
	 */
    public String getStringOfType(final Class<?> type) {
        return getStringOfType(type, true);
    }
    
	/**
	 * Gets the string representation of the specified type, optionally
	 * excluding the type variables if the specified type is a generic type
	 * 
	 * @param type
	 *            The type to be rendered
	 * @param includeTypeVariables
	 *            Indicates whether type variables should be included
	 * @return The string representation of the specified type
	 */
    public String getStringOfType(final Class<?> type, final boolean includeTypeVariables) {
        return getStringOfType(Reflection.getTypeInfo(type), includeTypeVariables);
    }

    /**
	 * Gets the string representation of the specified type including any
	 * generic type variables
	 * 
	 * @param type
	 *            The type to be rendered
	 * @return The string representation of the specified type
	 */
    public String getStringOfType(final TypeInfo<?> type) {
        return getStringOfType(type, true);
    }
    
    /**
	 * Gets the string representation of the specified type, optionally
	 * excluding the type variables if the specified type is a generic type
	 * 
	 * @param type
	 *            The type to be rendered
	 * @param includeTypeVariables
	 *            Indicates whether type variables should be included
	 * @return The string representation of the specified type
	 */
    public String getStringOfType(final TypeInfo<?> type, final boolean includeTypeVariables) {
        return type.toString(this, includeTypeVariables);
    }

    /**
     * Renders the specified list of types as generic type variables
     * 
     * @param typeList
     *            The list of type variables to be rendered
     * @return This object for chaining purpose
     */
    public T appendTypeVariables(final List<TypeVariableInfo> typeList) {
        return append(getStringOfTypeVariables(typeList));
    }

    /**
     * Renders the specified list of types as generic type variables to a string
     * 
     * @param typeList
     *            The list of type variables to be rendered
     * @return The render result
     */
    public String getStringOfTypeVariables(final List<TypeVariableInfo> typeList) {
        final StringBuilder sb = new StringBuilder();
        sb.append('<');
        for (int i = 0; i < typeList.size(); ++i) {
            if (i != 0) {
                sb.append(", ");
            }
            sb.append(getStringOfType(typeList.get(i), true));
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
     * Tries importing a class
     * 
     * @param fullClassName
     *            The full class name of the class to import
     * @return The simple name of the class if the class is imported
     *         successfully; or the full name of the class otherwise
     */
    public String tryImportType(final String fullClassName) {
        if (!isImported(fullClassName)) {
            return fullClassName;
        }
        final int indexOfLastDot = fullClassName.lastIndexOf('.');
        if (indexOfLastDot != -1) {
            return fullClassName.substring(indexOfLastDot + 1);
        }
        return fullClassName;
    }

    @Override
    public boolean isTypeVariableDefined(final String typeVariableId) {
        if (definedTypeVariables.contains(typeVariableId)) {
            return true;
        }
        if (parent != null) {
            return parent.isTypeVariableDefined(typeVariableId);
        }
        return false;
    }

    @Override
    public void defineTypeVariable(final String typeVariableId) {
        definedTypeVariables.add(typeVariableId);
    }

    /**
     * Gets the content
     */
    @Override
    public String toString() {
        return Joiner.on("").join(segments);
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
        public boolean isImported(final String canonicalClassName) {
            return getParent().isImported(canonicalClassName);
        }
    }
}
