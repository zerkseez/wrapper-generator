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

import com.strobel.reflection.Type;
import com.strobel.reflection.TypeList;
import com.strobel.reflection.Types;

/**
 * Renders type names
 * 
 * @author xerxes
 *
 */
public class TypeRenderer {
    /**
     * Renders type name
     * 
     * @param type
     *            The type to render
     * @param context
     *            Context object for storing context information
     * @return The name of the type as declared in Java code
     */
    public String renderType(final Type<?> type, final WrapperGeneratorContext context) {
        final StringBuilder sb = new StringBuilder();
        final String typeArgumentFullName = normalizeTypeFullName(type.getFullName());

        if (type.isArray()) {
            sb.append(renderType(type.getElementType(), context));
            sb.append("[]");
        }
        else if (type.isGenericType()) {
            sb.append(typeArgumentFullName);
            sb.append(renderGenericTypeParameters(type.getTypeArguments(), context));
        }
        else if (type.isGenericParameter()) {
            sb.append(typeArgumentFullName);
            if (!context.isGenericParameterDefined(typeArgumentFullName)) {
                context.addGenericParameter(typeArgumentFullName);
                sb.append(renderSuperAndExtendsBounds(type, context));
            }
        }
        else if (type.isCompoundType()) {
            final Type<?> baseType = type.getBaseType();
            final TypeList interfaces = type.getInterfaces();
            if (baseType != Types.Object) {
                sb.append(renderType(baseType, context.newFrame()));
                if (!interfaces.isEmpty()) {
                    sb.append(" & ");
                }
            }
            for (int i = 0, n = interfaces.size(); i < n; i++) {
                if (i != 0) {
                    sb.append(" & ");
                }
                sb.append(renderType(interfaces.get(i), context.newFrame()));
            }
        }
        else if (type.isWildcardType()) {
            sb.append('?');
            sb.append(renderSuperAndExtendsBounds(type, context));
        }
        else {
            sb.append(typeArgumentFullName);
        }

        return sb.toString();
    }

    /**
     * Renders generic type parameters (the portion within &lt; and &gt;)
     * 
     * @param typeList
     *            List of type parameters
     * @param context
     *            Context object for storing context information
     * @return The generic type parameters declaration as in Java code,
     *         including the opening &lt; and the ending &gt; symbol
     */
    public String renderGenericTypeParameters(final TypeList typeList, final WrapperGeneratorContext context) {
        final StringBuilder sb = new StringBuilder();
        sb.append('<');
        for (int i = 0; i < typeList.size(); ++i) {
            if (i != 0) {
                sb.append(", ");
            }
            sb.append(renderType(typeList.get(i), context));
        }
        sb.append('>');
        return sb.toString();
    }

    protected String renderSuperAndExtendsBounds(final Type<?> type, final WrapperGeneratorContext context) {
        final StringBuilder sb = new StringBuilder();
        final Type<?> superBound = type.getSuperBound();
        if (superBound != null && superBound != Types.Object) {
            final String superTypeName = renderType(superBound, context.newFrame());
            if (!"<any>".equals(superTypeName)) {
                sb.append(" super ");
                sb.append(superTypeName);
            }
        }
        final Type<?> upperBound = type.getExtendsBound();
        if (upperBound != null && upperBound != Types.Object) {
            sb.append(" extends ");
            sb.append(renderType(upperBound, context.newFrame()));
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
}
