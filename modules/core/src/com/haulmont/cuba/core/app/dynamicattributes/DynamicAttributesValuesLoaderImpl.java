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

package com.haulmont.cuba.core.app.dynamicattributes;


import com.google.common.base.Strings;
import com.haulmont.cuba.core.entity.BaseGenericIdEntity;
import com.haulmont.cuba.core.entity.CategoryAttribute;
import com.haulmont.cuba.core.entity.CategoryAttributeConfiguration;
import com.haulmont.cuba.core.entity.CategoryAttributeValuesLoaderType;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

@Component(DynamicAttributesValuesLoader.NAME)
public class DynamicAttributesValuesLoaderImpl implements DynamicAttributesValuesLoader {

    protected final Map<String, BiFunction<BaseGenericIdEntity, String, List>> loaders = new HashMap<>();

    @PostConstruct
    public void init() {
        loaders.put(CategoryAttributeValuesLoaderType.GROOVY.name(), this::executeGroovyScript);

    }

    @Override
    public List loadValues(CategoryAttribute attribute, BaseGenericIdEntity entity) {
        CategoryAttributeConfiguration configuration = attribute.getConfiguration();
        String loaderScript = configuration.getValuesLoaderScript();
        if (!Strings.isNullOrEmpty(loaderScript)) {
            CategoryAttributeValuesLoaderType loaderType = configuration.getValuesLoaderType();
            BiFunction<> loader =


            if (loaderType == CategoryAttributeValuesLoaderType.GROOVY) {

            } else if (loaderType == CategoryAttributeValuesLoaderType.SQL) {

            } else {
                throw new IllegalStateException(String.format("Unsupported values loader type: %s", loaderType.getId()));
            }
        } else {
            return Collections.emptyList();
        }
    }


    protected List executeSql(BaseGenericIdEntity entity, String query) {

    }

    protected List executeGroovyScript(BaseGenericIdEntity entity, String script) {

    }
}
