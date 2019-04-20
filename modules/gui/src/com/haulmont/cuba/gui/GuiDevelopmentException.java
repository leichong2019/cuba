/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */

package com.haulmont.cuba.gui;

import com.haulmont.cuba.core.global.DevelopmentException;

import java.util.Map;

public class GuiDevelopmentException extends DevelopmentException {

    protected Object contextId;

    public GuiDevelopmentException(String message, Object contextId) {
        super(message);
        this.contextId = contextId;
    }

    public GuiDevelopmentException(String message, Object contextId, String paramKey, Object paramValue) {
        super(message, paramKey, paramValue);
        this.contextId = contextId;
    }

    public GuiDevelopmentException(String message, Object contextId, Map<String, Object> params) {
        super(message, params);
        this.contextId = contextId;
    }

    public Object getContextId() {
        return contextId;
    }

    @Override
    public String toString() {
        return super.toString() +
                (contextId instanceof String ? ", frameId=" + contextId
                : contextId instanceof Class ? ", componentClass=" + contextId
                : "");
    }
}