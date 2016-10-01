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

import java.util.ArrayList;
import java.util.List;

import com.strobel.reflection.Type;

/**
 * Holds context information for WrapperGenerator
 * 
 * @author xerxes
 *
 */
public class WrapperGeneratorContext {
    private final WrapperGeneratorContext parentFrame;
    private final Type<?> wrappeeType;
    private final List<String> definedGenericParameters;

    public WrapperGeneratorContext(final Type<?> wrappeeType) {
        this(null, wrappeeType);
    }

    protected WrapperGeneratorContext(final WrapperGeneratorContext parentFrame, final Type<?> wrappeeType) {
        this.parentFrame = parentFrame;
        this.wrappeeType = wrappeeType;
        this.definedGenericParameters = new ArrayList<String>();
    }

    public WrapperGeneratorContext newFrame() {
        return new WrapperGeneratorContext(this, getWrappeeType());
    }

    public WrapperGeneratorContext getParentFrame() {
        return parentFrame;
    }

    public Type<?> getWrappeeType() {
        return wrappeeType;
    }

    public void addGenericParameter(final String name) {
        definedGenericParameters.add(name);
    }

    public boolean isGenericParameterDefined(final String name) {
        if (definedGenericParameters.contains(name)) {
            return true;
        }
        if (parentFrame != null) {
            return parentFrame.isGenericParameterDefined(name);
        }
        return false;
    }
}
