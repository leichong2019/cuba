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
 */

package com.haulmont.cuba.web.widgets.client.richtextarea;

import com.google.gwt.dev.util.HttpHeaders;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.HeadElement;
import com.google.gwt.dom.client.IFrameElement;
import com.google.gwt.dom.client.MetaElement;
import com.google.gwt.user.client.ui.RichTextArea;
import com.vaadin.client.ui.VRichTextArea;

import java.util.Map;

public class CubaRichTextAreaWidget extends VRichTextArea {

    protected AfterAttachedValueSupplier valueSupplier;

    public interface AfterAttachedValueSupplier {
        String getValue();
    }

    public CubaRichTextAreaWidget() {
        super();

        setContentCharset();

        rta.addAttachHandler(event -> {
            if (event.isAttached()) {
                // There are cases when 'ReachTextArea' is not attached but value has already set to 'HTML'
                // that is also not attached. When 'ReachTextArea' is attached we should set value again.
                if (valueSupplier != null) {
                    setValue(valueSupplier.getValue());
                }
            }
        });
    }

    public void setValueSupplier(AfterAttachedValueSupplier valueSupplier) {
        this.valueSupplier = valueSupplier;
    }

    protected void setContentCharset() {
        rta.addInitializeHandler(event -> {
            IFrameElement iFrameElement = IFrameElement.as(rta.getElement());
            HeadElement headElement = iFrameElement.getContentDocument().getHead();

            MetaElement charsetMetaElement = Document.get().createMetaElement();
            charsetMetaElement.setHttpEquiv(HttpHeaders.CONTENT_TYPE);
            charsetMetaElement.setContent(HttpHeaders.CONTENT_TYPE_TEXT_HTML_UTF8);

            headElement.appendChild(charsetMetaElement);
        });
    }

    @Override
    protected void createRichTextToolbar(RichTextArea rta) {
        formatter = new CubaRichTextToolbarWidget(rta);
    }

    public void setLocaleMap(Map<String, String> localeMap) {
        ((CubaRichTextToolbarWidget) formatter).setLocaleMap(localeMap);
    }
}