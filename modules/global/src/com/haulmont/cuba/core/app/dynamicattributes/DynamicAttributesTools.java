/*
 * Copyright (c) 2008-2018 Haulmont.
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

import com.google.common.base.Joiner;
import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.chile.core.model.MetaProperty;
import com.haulmont.chile.core.model.MetaPropertyPath;
import com.haulmont.cuba.core.entity.CategoryAttribute;
import com.haulmont.cuba.core.entity.CategoryAttributeValue;
import com.haulmont.cuba.core.global.MetadataTools;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static com.haulmont.cuba.core.app.dynamicattributes.DynamicAttributesUtils.decodeAttributeCode;
import static com.haulmont.cuba.core.app.dynamicattributes.DynamicAttributesUtils.getCategoryAttribute;

@Component(DynamicAttributesTools.NAME)
public class DynamicAttributesTools {

    public static final String NAME = "cuba_DynamicAttributesTools";

    @Inject
    protected DynamicAttributes dynamicAttributes;

    @Inject
    protected MetadataTools metadataTools;

    /**
     * Get special meta property path object for dynamic attribute by code
     */
    @Nullable
    public MetaPropertyPath getMetaPropertyPath(MetaClass metaClass, String attributeCode) {
        attributeCode = decodeAttributeCode(attributeCode);
        CategoryAttribute attribute = dynamicAttributes.getAttributeForMetaClass(metaClass, attributeCode);

        if (attribute != null) {
            return DynamicAttributesUtils.getMetaPropertyPath(metaClass, attribute);
        } else {
            return null;
        }
    }

    /**
     * Get special meta property path object for dynamic attribute id
     */
    @Nullable
    public MetaPropertyPath getMetaPropertyPath(MetaClass metaClass, UUID attributeId) {
        Collection<CategoryAttribute> attributes = dynamicAttributes.getAttributesForMetaClass(metaClass);
        CategoryAttribute attribute = null;
        for (CategoryAttribute theAttribute : attributes) {
            if (theAttribute.getId().equals(attributeId)) {
                attribute = theAttribute;
                break;
            }
        }

        if (attribute != null) {
            return DynamicAttributesUtils.getMetaPropertyPath(metaClass, attribute);
        } else {
            return null;
        }
    }

    /**
     * For collection dynamic attributes the method returns a list of formatted collection items joined with the comma,
     * for non-collection dynamic attribute a formatted value is returned
     */
    @SuppressWarnings("unchecked")
    public String getDynamicAttributeValueAsString(MetaProperty metaProperty, Object value) {
        CategoryAttribute categoryAttribute = getCategoryAttribute(metaProperty);
        if (Boolean.TRUE.equals(categoryAttribute.getIsCollection())) {
            if (value instanceof Collection) {
                List<String> valuesList = ((Collection<Object>) value).stream()
                        .map(item -> metadataTools.format(item, metaProperty))
                        .collect(Collectors.toList());
                return Joiner.on(", ").join(valuesList);
            }
        }
        return metadataTools.format(value, metaProperty);
    }

    /**
     * Makes a deep copy of the source dynamic attributes. All CategoryAttributeValues will be copied.
     * For collection CategoryAttributeValues all child CAVs will be copied as well.
     */
    public Map<String, CategoryAttributeValue> copyDynamicAttributes(Map<String, CategoryAttributeValue> source) {

        Map<String, CategoryAttributeValue> copiedDynamicAttributes = new HashMap<>();

        for (Map.Entry<String, CategoryAttributeValue> entry : source.entrySet()) {
            copiedDynamicAttributes.put(entry.getKey(), copyCategoryAttributeValue(entry.getValue()));
        }

        return copiedDynamicAttributes;
    }

    /**
     * Makes a deep copy of the source CategoryAttributeValue. All referenced entities and collections will be copied as well.
     */
    public CategoryAttributeValue copyCategoryAttributeValue(CategoryAttributeValue source) {
        if (Boolean.TRUE.equals(source.getCategoryAttribute().getIsCollection())){
            return copyCollectionCAV(source);
        } else {
            return metadataTools.deepCopy(source);
        }
    }

    protected CategoryAttributeValue copyCollectionCAV(CategoryAttributeValue source) {
        CategoryAttributeValue destination = metadataTools.deepCopy(source);
        List<CategoryAttributeValue> copiedChildValues = null;

        if (destination.getChildValues() != null) {
            copiedChildValues = new ArrayList<>();
            for (CategoryAttributeValue childValue : destination.getChildValues()) {
                CategoryAttributeValue copiedChildValue = metadataTools.deepCopy(childValue);
                copiedChildValue.setParent(destination);
                copiedChildValues.add(copiedChildValue);
            }
        }

        destination.setChildValues(copiedChildValues);
        if (source.getTransientCollectionValue() != null) {
            destination.setTransientCollectionValue(new ArrayList<>(source.getTransientCollectionValue()));
        }
        return destination;
    }
}