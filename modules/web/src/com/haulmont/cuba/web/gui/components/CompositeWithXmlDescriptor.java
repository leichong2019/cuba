/*
 * Copyright (c) 2008-2019 Haulmont.
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
 */

package com.haulmont.cuba.web.gui.components;

import com.haulmont.cuba.gui.components.Component;
import org.dom4j.Element;

/**
 * {@link CompositeComponent} having an XML descriptor attached.
 * Default implementations delegate their execution to {@link CompositeComponent#getComposition()}.
 */
public interface CompositeWithXmlDescriptor extends Component.HasXmlDescriptor {

    @Override
    default Element getXmlDescriptor() {
        Component.HasXmlDescriptor hasXmlDescriptor =
                (Component.HasXmlDescriptor) ((CompositeComponent) this).getCompositionNN();
        return hasXmlDescriptor.getXmlDescriptor();
    }

    @Override
    default void setXmlDescriptor(Element element) {
        Component.HasXmlDescriptor hasXmlDescriptor =
                (Component.HasXmlDescriptor) ((CompositeComponent) this).getCompositionNN();
        hasXmlDescriptor.setXmlDescriptor(element);
    }
}
