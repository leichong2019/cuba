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
import com.haulmont.cuba.core.global.GlobalConfig;
import com.haulmont.cuba.core.global.MetadataTools;
import com.haulmont.cuba.core.global.Scripting;
import groovy.lang.Binding;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.*;

@Component(DynamicAttributesRecalculationTools.NAME)
public class DynamicAttributesRecalculationTools {

    public static final String NAME = "cuba_DynamicAttributesRecalculationTools";

    @Inject
    protected Scripting scripting;

    @Inject
    protected MetadataTools metadataTools;

    @Inject
    protected DynamicAttributesTools dynamicAttributesTools;

    @Inject
    protected GlobalConfig config;

    /**
     * Performs recalculation for all dependent dynamic attributes. Recalculation is performed hierarchically.
     * This method clones {@code entity} and updates this cloned instance only.
     * Updated entity should be persisted manually if needed.
     *
     * Recalculation level limited by
     * {@code cuba.dynamicAttributes.maxRecalculationLevel} application property. If this property is not defined
     * then the default value is used (default value is 10).
     *
     * @param entity entity with loaded dynamic attributes. This instance won't be updated
     * @param attribute an attribute from which the recalculation begins. Value for this attribute won't be changed,
     *                  it is assumed that this attribute was updated before
     * @return copy of {@code entity} with updated dynamic attributes
     */
    public <T extends BaseGenericIdEntity> T recalculateDynamicAttributes(T entity, CategoryAttribute attribute) {

        if (attribute == null || attribute.getConfiguration().getDependentCategoryAttributes() == null
                || attribute.getConfiguration().getDependentCategoryAttributes().isEmpty()) {
            return entity;
        }

        T updatedEntity = copyEntity(entity);

        Set<CategoryAttribute> needToRecalculate = new HashSet<>(attribute.getConfiguration().getDependentCategoryAttributes());
        int recalculationLevel = 1;

        while (!needToRecalculate.isEmpty()) {

            if (recalculationLevel > config.getMaxRecalculationLevel()) {
                throw new IllegalStateException(String.format("Recalculation level has reached the maximum allowable value: %d. " +
                        "Check Dynamic Attributes configuration.", config.getMaxRecalculationLevel()));
            }

            Set<CategoryAttribute> nextLevelAttributes = new HashSet<>();

            for (CategoryAttribute dependentAttribute : needToRecalculate) {
                String groovyScript = dependentAttribute.getConfiguration().getRecalculationGroovyScript();

                if (Strings.isNullOrEmpty(groovyScript)) {
                    continue;
                }

                String attributeCode = DynamicAttributesUtils.encodeAttributeCode(dependentAttribute.getCode());

                Object oldValue = updatedEntity.getValue(attributeCode);
                Object newValue = evaluateGroovyScript(updatedEntity, groovyScript);

                if ((oldValue == null && newValue == null)
                        || (oldValue != null && oldValue.equals(newValue))) {
                    continue;
                }

                updatedEntity.setValue(attributeCode, newValue);

                if (dependentAttribute.getConfiguration().getDependentCategoryAttributes() != null) {
                    nextLevelAttributes.addAll(dependentAttribute.getConfiguration().getDependentCategoryAttributes());
                }
            }

            needToRecalculate = nextLevelAttributes;
            recalculationLevel++;
        }

        T entityToReturn = metadataTools.deepCopy(entity);
        //noinspection unchecked
        entityToReturn.setDynamicAttributes(updatedEntity.getDynamicAttributes());

        return entityToReturn;
    }

    protected Object evaluateGroovyScript(BaseGenericIdEntity entity, String groovyScript) {

        Binding binding = new Binding();
        binding.setVariable("__entity__", entity);
        binding.setVariable("dynamicAttributes", entity.getDynamicAttributes());

        return scripting.evaluateGroovy(groovyScript.replace("{E}", "__entity__"), binding);
    }

    protected <T extends BaseGenericIdEntity> T copyEntity(T entity) {
        T copiedEntity = metadataTools.deepCopy(entity);
        if (entity.getDynamicAttributes() != null) {
            //noinspection unchecked
            copiedEntity.setDynamicAttributes(dynamicAttributesTools.copyDynamicAttributes(entity.getDynamicAttributes()));
        }
        return copiedEntity;
    }
}
