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

package com.haulmont.cuba.gui.app.core.categories;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.haulmont.bali.util.Dom4j;
import com.haulmont.bali.util.ParamsMap;
import com.haulmont.chile.core.datatypes.Datatype;
import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.client.ClientConfig;
import com.haulmont.cuba.core.app.dynamicattributes.PropertyType;
import com.haulmont.cuba.core.entity.CategoryAttribute;
import com.haulmont.cuba.core.entity.CategoryAttributeConfiguration;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.entity.HasUuid;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.core.global.filter.SecurityJpqlGenerator;
import com.haulmont.cuba.gui.UiComponents;
import com.haulmont.cuba.gui.WindowManager.OpenType;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.BaseAction;
import com.haulmont.cuba.gui.components.actions.RemoveAction;
import com.haulmont.cuba.gui.components.autocomplete.JpqlSuggestionFactory;
import com.haulmont.cuba.gui.components.autocomplete.Suggestion;
import com.haulmont.cuba.gui.components.filter.ConditionsTree;
import com.haulmont.cuba.gui.components.filter.FakeFilterSupport;
import com.haulmont.cuba.gui.components.filter.FilterParser;
import com.haulmont.cuba.gui.components.filter.Param;
import com.haulmont.cuba.gui.components.filter.edit.FilterEditor;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.impl.AbstractDatasource;
import com.haulmont.cuba.gui.data.impl.DatasourceImplementation;
import com.haulmont.cuba.gui.dynamicattributes.DynamicAttributesGuiTools;
import com.haulmont.cuba.gui.icons.CubaIcon;
import com.haulmont.cuba.gui.icons.Icons;
import com.haulmont.cuba.gui.sys.ScreensHelper;
import com.haulmont.cuba.gui.theme.ThemeConstants;
import com.haulmont.cuba.security.entity.FilterEntity;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.TextStringBuilder;
import org.dom4j.Element;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Consumer;

import static java.lang.String.format;

/**
 * Class that encapsulates editing of {@link CategoryAttribute} entities.
 */
public class AttributeEditor extends AbstractEditor<CategoryAttribute> {

    protected static final Multimap<PropertyType, String> FIELDS_VISIBLE_FOR_DATATYPES = ArrayListMultimap.create();
    protected static final Set<String> ALWAYS_VISIBLE_FIELDS = ImmutableSet.of("name", "code", "required", "dataType",
            "description", "configuration.validatorGroovyScript");
    protected static final String WHERE = " where ";

    static {
        FIELDS_VISIBLE_FOR_DATATYPES.put(PropertyType.BOOLEAN, "defaultBoolean");
        FIELDS_VISIBLE_FOR_DATATYPES.put(PropertyType.STRING, "defaultString");
        FIELDS_VISIBLE_FOR_DATATYPES.put(PropertyType.STRING, "width");
        FIELDS_VISIBLE_FOR_DATATYPES.put(PropertyType.STRING, "rowsCount");
        FIELDS_VISIBLE_FOR_DATATYPES.put(PropertyType.STRING, "isCollection");
        FIELDS_VISIBLE_FOR_DATATYPES.put(PropertyType.DOUBLE, "defaultDouble");
        FIELDS_VISIBLE_FOR_DATATYPES.put(PropertyType.DOUBLE, "configuration.minDouble");
        FIELDS_VISIBLE_FOR_DATATYPES.put(PropertyType.DOUBLE, "configuration.maxDouble");
        FIELDS_VISIBLE_FOR_DATATYPES.put(PropertyType.DOUBLE, "width");
        FIELDS_VISIBLE_FOR_DATATYPES.put(PropertyType.DOUBLE, "isCollection");
        FIELDS_VISIBLE_FOR_DATATYPES.put(PropertyType.DECIMAL, "defaultDecimal");
        FIELDS_VISIBLE_FOR_DATATYPES.put(PropertyType.DECIMAL, "configuration.minDecimal");
        FIELDS_VISIBLE_FOR_DATATYPES.put(PropertyType.DECIMAL, "configuration.maxDecimal");
        FIELDS_VISIBLE_FOR_DATATYPES.put(PropertyType.DECIMAL, "width");
        FIELDS_VISIBLE_FOR_DATATYPES.put(PropertyType.DECIMAL, "isCollection");
        FIELDS_VISIBLE_FOR_DATATYPES.put(PropertyType.DECIMAL, "configuration.numberFormatPattern");
        FIELDS_VISIBLE_FOR_DATATYPES.put(PropertyType.INTEGER, "defaultInt");
        FIELDS_VISIBLE_FOR_DATATYPES.put(PropertyType.INTEGER, "configuration.minInt");
        FIELDS_VISIBLE_FOR_DATATYPES.put(PropertyType.INTEGER, "configuration.maxInt");
        FIELDS_VISIBLE_FOR_DATATYPES.put(PropertyType.INTEGER, "width");
        FIELDS_VISIBLE_FOR_DATATYPES.put(PropertyType.INTEGER, "isCollection");
        FIELDS_VISIBLE_FOR_DATATYPES.put(PropertyType.DATE, "defaultDate");
        FIELDS_VISIBLE_FOR_DATATYPES.put(PropertyType.DATE, "defaultDateIsCurrent");
        FIELDS_VISIBLE_FOR_DATATYPES.put(PropertyType.DATE, "width");
        FIELDS_VISIBLE_FOR_DATATYPES.put(PropertyType.DATE, "isCollection");
        FIELDS_VISIBLE_FOR_DATATYPES.put(PropertyType.DATE_WITHOUT_TIME, "defaultDateWithoutTime");
        FIELDS_VISIBLE_FOR_DATATYPES.put(PropertyType.DATE_WITHOUT_TIME, "defaultDateIsCurrent");
        FIELDS_VISIBLE_FOR_DATATYPES.put(PropertyType.DATE_WITHOUT_TIME, "width");
        FIELDS_VISIBLE_FOR_DATATYPES.put(PropertyType.DATE_WITHOUT_TIME, "isCollection");
        FIELDS_VISIBLE_FOR_DATATYPES.put(PropertyType.ENUMERATION, "enumeration");
        FIELDS_VISIBLE_FOR_DATATYPES.put(PropertyType.ENUMERATION, "defaultString");
        FIELDS_VISIBLE_FOR_DATATYPES.put(PropertyType.ENUMERATION, "width");
        FIELDS_VISIBLE_FOR_DATATYPES.put(PropertyType.ENUMERATION, "isCollection");
        FIELDS_VISIBLE_FOR_DATATYPES.put(PropertyType.ENTITY, "entityClass");
        FIELDS_VISIBLE_FOR_DATATYPES.put(PropertyType.ENTITY, "screen");
        FIELDS_VISIBLE_FOR_DATATYPES.put(PropertyType.ENTITY, "lookup");
        FIELDS_VISIBLE_FOR_DATATYPES.put(PropertyType.ENTITY, "defaultEntityId");
        FIELDS_VISIBLE_FOR_DATATYPES.put(PropertyType.ENTITY, "width");
        FIELDS_VISIBLE_FOR_DATATYPES.put(PropertyType.ENTITY, "joinClause");
        FIELDS_VISIBLE_FOR_DATATYPES.put(PropertyType.ENTITY, "whereClause");
        FIELDS_VISIBLE_FOR_DATATYPES.put(PropertyType.ENTITY, "constraintWizard");
        FIELDS_VISIBLE_FOR_DATATYPES.put(PropertyType.ENTITY, "isCollection");
    }

    protected CategoryAttribute attribute;

    @Inject
    protected FieldGroup attributeFieldGroup;

    @Inject
    protected FieldGroup columnSettingsFieldGroup;

    @Inject
    protected FieldGroup recalculationSettingsFieldGroup;

    protected LookupField<PropertyType> dataTypeField;
    protected LookupField<String> screenField;
    protected LookupField<String> entityTypeField;
    protected PickerField<Entity> defaultEntityField;
    protected TextArea<String> descriptionField;
    protected LookupField<String> columnAlignmentField;

    protected String fieldWidth;

    @Inject
    protected Datasource<CategoryAttribute> attributeDs;

    @Inject
    protected Datasource<CategoryAttributeConfiguration> configurationDs;

    @Inject
    protected UiComponents uiComponents;

    @Inject
    protected Metadata metadata;

    @Inject
    protected MetadataTools metadataTools;

    @Inject
    protected MessageTools messageTools;

    @Inject
    protected DatatypeFormatter datatypeFormatter;

    @Inject
    protected ThemeConstants themeConstants;

    @Inject
    protected ScreensHelper screensHelper;

    @Inject
    protected DynamicAttributesGuiTools dynamicAttributesGuiTools;

    @Inject
    protected DataManager dataManager;

    @Inject
    protected ReferenceToEntitySupport referenceToEntitySupport;

    @Inject
    protected Table<ScreenAndComponent> targetScreensTable;

    @Inject
    protected TabSheet tabsheet;

    @Inject
    protected CollectionDatasource<ScreenAndComponent, UUID> screensDs;

    @Inject
    protected GlobalConfig globalConfig;

    @Inject
    protected ClientConfig clientConfig;

    @Inject
    protected Icons icons;

    protected LocalizedNameAndDescriptionFrame localizedFrame;

    protected ListEditor<String> enumerationListEditor;
    protected SourceCodeEditor joinField;
    protected SourceCodeEditor whereField;

    protected HBoxLayout validatorHBoxLayoutField;
    protected SourceCodeEditor validatorGroovyScriptField;
    protected LinkButton validatorHelpLinkBtn;

    protected HBoxLayout recalculationHBoxLayoutField;
    protected SourceCodeEditor recalculationGroovyScriptField;
    protected LinkButton recalculationHelpLinkBtn;
    protected ListEditor<CategoryAttribute> dependentAttributesField;

    protected TextField defaultDecimalField;
    protected TextField minDecimalField;
    protected TextField maxDecimalField;

    @Inject
    protected FilterParser filterParser;

    @Override
    public void init(Map<String, Object> params) {
        getDialogOptions().setWidth(themeConstants.get("cuba.gui.AttributeEditor.width"));

        fieldWidth = themeConstants.get("cuba.gui.AttributeEditor.field.width");

        initLocalizedFrame();
        initFieldGroup();
        initColumnSettingsFieldGroup();
        initRecalculationSettingsFieldGroup();

        Action createAction = initCreateScreenAndComponentAction();
        targetScreensTable.addAction(createAction);
        Action removeAction = new RemoveAction(targetScreensTable);
        removeAction.setCaption(getMessage("targetScreensTable.remove"));
        targetScreensTable.addAction(removeAction);
    }

    @Override
    public void ready() {
        defaultDecimalField = (TextField) attributeFieldGroup.getFieldNN("defaultDecimal").getComponentNN();
        minDecimalField = (TextField) attributeFieldGroup.getFieldNN("configuration.minDecimal").getComponentNN();
        maxDecimalField = (TextField) attributeFieldGroup.getFieldNN("configuration.maxDecimal").getComponentNN();

        setupBigDecimalFormat();

        dependentAttributesField.setOptionsList(getCategoryAttributes());
    }

    protected Action initCreateScreenAndComponentAction() {
        return new BaseAction("create")
                .withCaption(getMessage("targetScreensTable.create"))
                .withIcon(icons.get(CubaIcon.CREATE_ACTION))
                .withShortcut(clientConfig.getTableInsertShortcut())
                .withHandler(e ->
                        screensDs.addItem(metadata.create(ScreenAndComponent.class))
                );
    }

    protected void initLocalizedFrame() {
        if (globalConfig.getAvailableLocales().size() > 1) {
            tabsheet.getTab("localization").setVisible(true);

            localizedFrame = (LocalizedNameAndDescriptionFrame) openFrame(
                    tabsheet.getTabComponent("localization"), "localizedNameAndDescriptionFrame");
            localizedFrame.setWidth("100%");
            localizedFrame.setHeight("250px");
        }
    }

    @SuppressWarnings("unchecked")
    protected void initFieldGroup() {
        attributeFieldGroup.addCustomField("defaultBoolean", (datasource, propertyId) -> {
            LookupField<Boolean> lookupField = uiComponents.create(LookupField.NAME);

            Map<String, Boolean> options = new TreeMap<>();
            options.put(datatypeFormatter.formatBoolean(true), true);
            options.put(datatypeFormatter.formatBoolean(false), false);
            lookupField.setOptionsMap(options);
            lookupField.setDatasource(attributeDs, "defaultBoolean");
            return lookupField;
        });

        attributeFieldGroup.addCustomField("dataType", (datasource, propertyId) -> {
            dataTypeField = uiComponents.create(LookupField.NAME);
            Map<String, PropertyType> options = new TreeMap<>();
            PropertyType[] types = PropertyType.values();
            for (PropertyType propertyType : types) {
                options.put(getMessage(propertyType.toString()), propertyType);
            }
            dataTypeField.setWidth(fieldWidth);

            dataTypeField.setNewOptionAllowed(false);
            dataTypeField.setRequired(true);
            dataTypeField.setRequiredMessage(getMessage("dataTypeRequired"));
            dataTypeField.setOptionsMap(options);
            dataTypeField.setCaption(getMessage("dataType"));
            dataTypeField.setFrame(frame);
            dataTypeField.setDatasource(datasource, propertyId);

            return dataTypeField;
        });

        attributeFieldGroup.addCustomField("description", (datasource, propertyId) -> {
            descriptionField = uiComponents.create(TextArea.TYPE_STRING);
            descriptionField.setMaxLength(1000);
            descriptionField.setRows(3);
            descriptionField.setDatasource(datasource, propertyId);

            return descriptionField;
        });

        attributeFieldGroup.addCustomField("screen", (datasource, propertyId) -> {
            screenField = uiComponents.create(LookupField.NAME);
            screenField.setDatasource(datasource, propertyId);
            screenField.setId("screenField");
            screenField.setCaption(getMessage("screen"));
            screenField.setWidth(fieldWidth);
            screenField.setRequiredMessage(getMessage("entityScreenRequired"));
            screenField.setFrame(frame);

            return screenField;
        });

        attributeFieldGroup.addCustomField("entityClass", (datasource, propertyId) -> {
            entityTypeField = uiComponents.create(LookupField.NAME);
            entityTypeField.setId("entityClass");
            entityTypeField.setCaption(getMessage("entityType"));
            entityTypeField.setRequired(true);
            entityTypeField.setRequiredMessage(getMessage("entityTypeRequired"));
            entityTypeField.setWidth(fieldWidth);
            entityTypeField.setFrame(frame);

            Map<String, String> options = new TreeMap<>();
            MetaClass entityType = null;
            for (MetaClass metaClass : metadataTools.getAllPersistentMetaClasses()) {
                if (!metadataTools.isSystemLevel(metaClass)) {
                    if (metadata.getTools().hasCompositePrimaryKey(metaClass) && !HasUuid.class.isAssignableFrom(metaClass.getJavaClass())) {
                        continue;
                    }
                    options.put(messageTools.getDetailedEntityCaption(metaClass), metaClass.getJavaClass().getName());

                    if (attribute != null && metaClass.getJavaClass().getName().equals(attribute.getEntityClass())) {
                        entityType = metaClass;
                    }
                }
            }
            entityTypeField.setOptionsMap(options);
            if (entityType != null) {
                entityTypeField.setValue(entityType.getName());
            }
            entityTypeField.setDatasource(datasource, propertyId);

            return entityTypeField;
        });

        attributeFieldGroup.addCustomField("defaultEntityId", (datasource, propertyId) -> {
            defaultEntityField = uiComponents.create(PickerField.NAME);
            defaultEntityField.setCaption(messages.getMessage(CategoryAttribute.class, "CategoryAttribute.defaultEntityId"));
            defaultEntityField.addValueChangeListener(e -> {
                Entity entity = e.getValue();
                if (entity != null) {
                    attribute.setObjectDefaultEntityId(referenceToEntitySupport.getReferenceId(entity));
                } else {
                    attribute.setObjectDefaultEntityId(null);
                }
                ((AbstractDatasource) attributeDs).modified(attribute);
            });
            defaultEntityField.addLookupAction();
            defaultEntityField.addClearAction();

            return defaultEntityField;
        });

        attributeFieldGroup.addCustomField("enumeration", (datasource, propertyId) -> {
            enumerationListEditor = uiComponents.create(ListEditor.NAME);
            enumerationListEditor.setWidth("100%");
            enumerationListEditor.setItemType(ListEditor.ItemType.STRING);
            enumerationListEditor.setRequired(true);
            enumerationListEditor.setRequiredMessage(getMessage("enumRequired"));
            enumerationListEditor.addValueChangeListener(e -> {
                List<?> list = e.getValue() != null ? e.getValue() : Collections.emptyList();
                attribute.setEnumeration(Joiner.on(",").join(list));
            });

            if (localizedFrame != null) {
                enumerationListEditor.setEditorWindowId("localizedEnumerationWindow");
                enumerationListEditor.setEditorParamsSupplier(() ->
                        ParamsMap.of("enumerationLocales", attribute.getEnumerationLocales()));
                enumerationListEditor.addEditorCloseListener(closeEvent -> {
                    if (closeEvent.getActionId().equals(COMMIT_ACTION_ID)) {
                        LocalizedEnumerationWindow enumerationWindow = (LocalizedEnumerationWindow) closeEvent.getWindow();
                        attribute.setEnumerationLocales(enumerationWindow.getLocalizedValues());
                    }
                });
            }
            return enumerationListEditor;
        });

        attributeFieldGroup.addCustomField("whereClause", (datasource, propertyId) -> {
            whereField = uiComponents.create(SourceCodeEditor.class);
            whereField.setDatasource(attributeDs, "whereClause");
            whereField.setWidthFull();
            whereField.setHeight(themeConstants.get("cuba.gui.customConditionFrame.whereField.height"));
            whereField.setSuggester((source, text, cursorPosition) ->
                    requestHint(whereField, text, cursorPosition)
            );
            whereField.setHighlightActiveLine(false);
            whereField.setShowGutter(false);
            return whereField;
        });

        attributeFieldGroup.addCustomField("joinClause", (datasource, propertyId) -> {
            joinField = uiComponents.create(SourceCodeEditor.class);
            joinField.setDatasource(attributeDs, "joinClause");
            joinField.setWidthFull();
            joinField.setHeight(themeConstants.get("cuba.gui.customConditionFrame.joinField.height"));
            joinField.setSuggester((source, text, cursorPosition) -> requestHint(joinField, text, cursorPosition));
            joinField.setHighlightActiveLine(false);
            joinField.setShowGutter(false);
            return joinField;
        });

        attributeFieldGroup.addCustomField("constraintWizard", (datasource, propertyId) -> {
            LinkButton linkButton = uiComponents.create(LinkButton.class);
            linkButton.setAction(new BaseAction("constraintWizard")
                    .withHandler(event ->
                            openConstraintWizard()
                    ));
            linkButton.setCaption(getMessage("constraintWizard"));
            linkButton.setAlignment(Alignment.MIDDLE_LEFT);

            HBoxLayout hbox = uiComponents.create(HBoxLayout.class);
            hbox.setWidthFull();
            hbox.add(linkButton);
            return hbox;
        });

        attributeDs.addItemPropertyChangeListener(e -> {
            String property = e.getProperty();
            CategoryAttribute attribute = getItem();
            if ("dataType".equalsIgnoreCase(property)
                    || "lookup".equalsIgnoreCase(property)
                    || "defaultDateIsCurrent".equalsIgnoreCase(property)
                    || "entityClass".equalsIgnoreCase(property)) {
                setupVisibility();
            }
            if ("name".equalsIgnoreCase(property)) {
                fillAttributeCode();
            }
            if ("screen".equalsIgnoreCase(property) || "joinClause".equals(property) || "whereClause".equals(property)) {
                dynamicAttributesGuiTools.initEntityPickerField(defaultEntityField, attribute);
            }
        });

        configurationDs.addItemPropertyChangeListener(e -> {
            ((DatasourceImplementation) attributeDs).modified(attribute);

            if ("numberFormatPattern".equalsIgnoreCase(e.getProperty())) {
                setupBigDecimalFormat();
            }
        });

        attributeFieldGroup.addCustomField("configuration.validatorGroovyScript", (datasource, propertyId) -> {

            validatorGroovyScriptField = uiComponents.create(SourceCodeEditor.class);
            validatorGroovyScriptField.setMode(SourceCodeEditor.Mode.Groovy);
            validatorGroovyScriptField.setDatasource(attributeDs, "configuration.validatorGroovyScript");
            validatorGroovyScriptField.setWidthFull();
            validatorGroovyScriptField.setHeight(themeConstants.get("cuba.gui.AttributeEditor.validatorGroovyScriptField.height"));
            validatorGroovyScriptField.setHighlightActiveLine(false);
            validatorGroovyScriptField.setShowGutter(false);

            validatorHelpLinkBtn = uiComponents.create(LinkButton.class);
            validatorHelpLinkBtn.setIcon("icons/question-white.png");
            validatorHelpLinkBtn.addClickListener(event -> showMessageDialog(getMessage("validatorScript"), getMessage("validatorScriptHelp"),
                    MessageType.CONFIRMATION_HTML
                            .modal(false)
                            .width(560f)));

            validatorHBoxLayoutField = uiComponents.create(HBoxLayout.class);
            validatorHBoxLayoutField.setWidthFull();
            validatorHBoxLayoutField.add(validatorGroovyScriptField, validatorHelpLinkBtn);
            validatorHBoxLayoutField.expand(validatorGroovyScriptField);

            return validatorHBoxLayoutField;
        });
    }

    protected void initColumnSettingsFieldGroup() {
        columnSettingsFieldGroup.addCustomField("configuration.columnAlignment", (datasource, propertyId) -> {
            columnAlignmentField = uiComponents.create(LookupField.NAME);
            columnAlignmentField.setDatasource(datasource, "configuration.columnAlignment");
            columnAlignmentField.setWidth(fieldWidth);
            columnAlignmentField.setFrame(frame);

            List<String> options = new ArrayList<>();
            for (Table.ColumnAlignment alignment : Table.ColumnAlignment.values()) {
                options.add(alignment.name());
            }

            columnAlignmentField.setOptionsList(options);

            return columnAlignmentField;
        });
    }

    protected void initRecalculationSettingsFieldGroup() {
        recalculationSettingsFieldGroup.addCustomField("configuration.recalculationGroovyScript", (datasource, propertyId) -> {

            recalculationGroovyScriptField = uiComponents.create(SourceCodeEditor.class);
            recalculationGroovyScriptField.setMode(SourceCodeEditor.Mode.Groovy);
            recalculationGroovyScriptField.setDatasource(attributeDs, "configuration.recalculationGroovyScript");
            recalculationGroovyScriptField.setWidthFull();
            recalculationGroovyScriptField.setHeight(themeConstants.get("cuba.gui.AttributeEditor.recalculationGroovyScriptField.height"));
            recalculationGroovyScriptField.setHighlightActiveLine(false);
            recalculationGroovyScriptField.setShowGutter(false);

            recalculationHelpLinkBtn = uiComponents.create(LinkButton.class);
            recalculationHelpLinkBtn.setIcon("icons/question-white.png");
            recalculationHelpLinkBtn.addClickListener(event -> showMessageDialog(getMessage("recalculationScript"), getMessage("recalculationScriptHelp"),
                    MessageType.CONFIRMATION_HTML
                            .modal(false)
                            .width(560f)));

            recalculationHBoxLayoutField = uiComponents.create(HBoxLayout.class);
            recalculationHBoxLayoutField.setWidthFull();
            recalculationHBoxLayoutField.add(recalculationGroovyScriptField, recalculationHelpLinkBtn);
            recalculationHBoxLayoutField.expand(recalculationGroovyScriptField);

            return recalculationHBoxLayoutField;
        });

        recalculationSettingsFieldGroup.addCustomField("configuration.dependentCategoryAttributes", (datasource, propertyId) -> {
            dependentAttributesField = uiComponents.create(ListEditor.NAME);
            dependentAttributesField.setDatasource(datasource, "configuration.dependentCategoryAttributes");
            dependentAttributesField.setWidth(fieldWidth);
            dependentAttributesField.setFrame(frame);
            dependentAttributesField.setItemType(ListEditor.ItemType.ENTITY);
            dependentAttributesField.setEntityName("sys$CategoryAttribute");

            return dependentAttributesField;
        });
    }

    public void openConstraintWizard() {
        Class entityClass = attribute.getJavaClassForEntity();

        if (entityClass == null) {
            showNotification(getMessage("selectEntityType"));
            return;
        }

        MetaClass metaClass = metadata.getClassNN(entityClass);
        FakeFilterSupport fakeFilterSupport = new FakeFilterSupport(this, metaClass);

        Filter fakeFilter = fakeFilterSupport.createFakeFilter();
        FilterEntity filterEntity = fakeFilterSupport.createFakeFilterEntity(attribute.getFilterXml());
        ConditionsTree conditionsTree = fakeFilterSupport.createFakeConditionsTree(fakeFilter, filterEntity);

        Map<String, Object> params = new HashMap<>();
        params.put("filter", fakeFilter);
        params.put("filterEntity", filterEntity);
        params.put("conditionsTree", conditionsTree);
        params.put("useShortConditionForm", true);

        FilterEditor filterEditor = (FilterEditor) openWindow("filterEditor", OpenType.DIALOG, params);
        filterEditor.addCloseListener(actionId -> {
            if (!COMMIT_ACTION_ID.equals(actionId)) {
                return;
            }

            filterEntity.setXml(filterParser.getXml(filterEditor.getConditions(), Param.ValueProperty.DEFAULT_VALUE));

            if (filterEntity.getXml() != null) {
                Element element = Dom4j.readDocument(filterEntity.getXml()).getRootElement();
                com.haulmont.cuba.core.global.filter.FilterParser filterParser =
                        new com.haulmont.cuba.core.global.filter.FilterParser(element);
                String jpql = new SecurityJpqlGenerator().generateJpql(filterParser.getRoot());
                attribute.setWhereClause(jpql);
                Set<String> joins = filterParser.getRoot().getJoins();
                if (!joins.isEmpty()) {
                    String joinsStr = new TextStringBuilder().appendWithSeparators(joins, " ").toString();
                    attribute.setJoinClause(joinsStr);
                }
                attribute.setFilterXml(filterEntity.getXml());
            }
        });
    }

    private void setupVisibility() {
        for (FieldGroup.FieldConfig fieldConfig : attributeFieldGroup.getFields()) {
            if (!ALWAYS_VISIBLE_FIELDS.contains(fieldConfig.getId())) {
                attributeFieldGroup.setVisible(fieldConfig.getId(), false);
            }
        }

        Collection<String> componentIds = FIELDS_VISIBLE_FOR_DATATYPES.get(attribute.getDataType());
        if (componentIds != null) {
            for (String componentId : componentIds) {
                attributeFieldGroup.setVisible(componentId, true);
            }
        }

        if (attribute.getDataType() == PropertyType.ENTITY) {
            if (StringUtils.isNotBlank(attribute.getEntityClass())) {
                defaultEntityField.setEditable(true);
                whereField.setEnabled(true);
                joinField.setEnabled(true);
                Class entityClass = attribute.getJavaClassForEntity();
                MetaClass metaClass = metadata.getClass(entityClass);
                defaultEntityField.setMetaClass(metaClass);
                fillDefaultEntities(entityClass);
                fillSelectEntityScreens(entityClass);

                dynamicAttributesGuiTools.initEntityPickerField(defaultEntityField, attribute);
            } else {
                defaultEntityField.setEditable(false);
                whereField.setEnabled(false);
                joinField.setEnabled(false);
            }

            if (Boolean.TRUE.equals(attribute.getLookup())) {
                attributeFieldGroup.setVisible("screen", false);
            } else {
                attributeFieldGroup.setVisible("screen", true);
            }

            getDialogOptions().center();
        }

        if (attribute.getDataType() == PropertyType.DATE) {
            if (Boolean.TRUE.equals(attribute.getDefaultDateIsCurrent())) {
                attributeFieldGroup.setVisible("defaultDate", false);
                attributeFieldGroup.setFieldValue("defaultDate", null);
            } else {
                attributeFieldGroup.setVisible("defaultDate", true);
            }
        }

        if (attribute.getDataType() == PropertyType.DATE_WITHOUT_TIME) {
            if (Boolean.TRUE.equals(attribute.getDefaultDateIsCurrent())) {
                attributeFieldGroup.setVisible("defaultDateWithoutTime", false);
                attributeFieldGroup.setFieldValue("defaultDateWithoutTime", null);
            } else {
                attributeFieldGroup.setVisible("defaultDateWithoutTime", true);
            }
        }

        if (attribute.getDataType() == PropertyType.BOOLEAN) {
            attributeFieldGroup.setFieldValue("isCollection", null);
        }
    }

    protected void fillSelectEntityScreens(Class entityClass) {
        Map<String, String> screensMap = screensHelper.getAvailableBrowserScreens(entityClass);
        screenField.setOptionsMap(screensMap);
        String value = attribute.getScreen();
        screenField.setValue(screensMap.containsValue(value) ? value : null);
    }

    @SuppressWarnings("unchecked")
    protected void fillDefaultEntities(Class entityClass) {
        MetaClass metaClass = metadata.getClassNN(entityClass);
        if (attribute.getObjectDefaultEntityId() != null) {
            LoadContext<Entity> lc = new LoadContext<>(entityClass).setView(View.MINIMAL);
            String pkName = referenceToEntitySupport.getPrimaryKeyForLoadingEntity(metaClass);
            lc.setQueryString(format("select e from %s e where e.%s = :entityId", metaClass.getName(), pkName))
                    .setParameter("entityId", attribute.getObjectDefaultEntityId());

            Entity entity = dataManager.load(lc);
            if (entity != null) {
                defaultEntityField.setValue(entity);
            } else {
                defaultEntityField.setValue(null);
            }
        }
    }

    protected void fillAttributeCode() {
        CategoryAttribute attribute = getItem();
        if (StringUtils.isBlank(attribute.getCode()) && StringUtils.isNotBlank(attribute.getName())) {
            String categoryName = StringUtils.EMPTY;
            if (attribute.getCategory() != null) {
                categoryName = StringUtils.defaultString(attribute.getCategory().getName());
            }
            attribute.setCode(StringUtils.deleteWhitespace(categoryName + attribute.getName()));
        }
    }

    @Override
    public boolean preCommit() {
        Collection<ScreenAndComponent> screens = screensDs.getItems();
        StringBuilder stringBuilder = new StringBuilder();
        for (ScreenAndComponent screenAndComponent : screens) {
            if (StringUtils.isNotBlank(screenAndComponent.getScreen())) {
                stringBuilder.append(screenAndComponent.getScreen());
                if (StringUtils.isNotBlank(screenAndComponent.getComponent())) {
                    stringBuilder.append("#");
                    stringBuilder.append(screenAndComponent.getComponent());
                }
                stringBuilder.append(",");
            }
        }

        if (stringBuilder.length() > 0) {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }
        attribute.setTargetScreens(stringBuilder.toString());

        if (localizedFrame != null) {
            attribute.setLocaleNames(localizedFrame.getNamesValue());
            attribute.setLocaleDescriptions(localizedFrame.getDescriptionsValue());
        }

        if (getDsContext().isModified()) {
            attribute.setAttributeConfigurationJson(new Gson().toJson(configurationDs.getItem()));
        }

        return true;
    }

    @Override
    public void postValidate(ValidationErrors errors) {
        if (attribute.getDataType() == PropertyType.INTEGER
                || attribute.getDataType() == PropertyType.DOUBLE
                || attribute.getDataType() == PropertyType.DECIMAL) {
            if (attribute.getConfiguration().getMinValue() != null &&
                    attribute.getConfiguration().getMaxValue() != null &&
                    compareNumbers(attribute.getDataType(),
                            attribute.getConfiguration().getMinValue(),
                            attribute.getConfiguration().getMaxValue()) > 0) {

                errors.add(getMessage("minGreaterThanMax"));

            } else if (attribute.getDefaultValue() != null) {
                if (attribute.getConfiguration().getMinValue() != null &&
                        compareNumbers(attribute.getDataType(), attribute.getConfiguration().getMinValue(),
                                (Number) attribute.getDefaultValue()) > 0) {

                    errors.add(getMessage("defaultLessThanMin"));
                }

                if (attribute.getConfiguration().getMaxValue() != null &&
                        compareNumbers(attribute.getDataType(),
                                attribute.getConfiguration().getMaxValue(), (Number) attribute.getDefaultValue()) < 0) {

                    errors.add(getMessage("defaultGreaterThanMax"));
                }
            }
        }

        @SuppressWarnings("unchecked")
        CollectionDatasource<CategoryAttribute, UUID> parent
                = (CollectionDatasource<CategoryAttribute, UUID>) ((DatasourceImplementation) attributeDs).getParent();
        if (parent != null) {
            CategoryAttribute categoryAttribute = getItem();
            for (UUID id : parent.getItemIds()) {
                CategoryAttribute ca = parent.getItemNN(id);
                if (ca.getName().equals(categoryAttribute.getName())
                        && (!ca.equals(categoryAttribute))) {
                    errors.add(getMessage("uniqueName"));
                    return;
                } else if (ca.getCode() != null && ca.getCode().equals(categoryAttribute.getCode())
                        && (!ca.equals(categoryAttribute))) {
                    errors.add(getMessage("uniqueCode"));
                    return;
                }
            }
        }
    }

    @Override
    protected void postInit() {
        attribute = getItem();

        Set<String> targetScreens = attribute.targetScreensSet();
        for (String targetScreen : targetScreens) {
            if (targetScreen.contains("#")) {
                String[] split = targetScreen.split("#");
                screensDs.addItem(new ScreenAndComponent(split[0], split[1]));
            } else {
                screensDs.addItem(new ScreenAndComponent(targetScreen, null));
            }
        }

        MetaClass categorizedEntityMetaClass = metadata.getClass(attribute.getCategory().getEntityType());
        Map<String, String> optionsMap = categorizedEntityMetaClass != null ?
                new HashMap<>(screensHelper.getAvailableScreens(categorizedEntityMetaClass.getJavaClass(), true)) :
                new HashMap<>();

        targetScreensTable.addGeneratedColumn(
                "screen",
                entity -> {
                    LookupField<String> lookupField = uiComponents.create(LookupField.NAME);
                    lookupField.setDatasource(targetScreensTable.getItemDatasource(entity), "screen");
                    lookupField.setOptionsMap(optionsMap);
                    lookupField.setNewOptionAllowed(true);
                    //noinspection RedundantCast
                    lookupField.setNewOptionHandler((Consumer<String>) caption -> {
                        if (caption != null && !optionsMap.containsKey(caption)) {
                            optionsMap.put(caption, caption);
                            lookupField.setValue(caption);
                        }
                    });
                    lookupField.setRequired(true);
                    lookupField.setWidth("100%");
                    return lookupField;
                }
        );

        String enumeration = attribute.getEnumeration();
        if (!Strings.isNullOrEmpty(enumeration)) {
            Iterable<String> items = Splitter.on(",").omitEmptyStrings().split(enumeration);
            enumerationListEditor.setValue(Lists.newArrayList(items));
        }

        if (localizedFrame != null) {
            localizedFrame.setNamesValue(attribute.getLocaleNames());
            localizedFrame.setDescriptionsValue(attribute.getLocaleDescriptions());
        }

        setupVisibility();
    }

    protected List<Suggestion> requestHint(SourceCodeEditor sender, String text, int senderCursorPosition) {
        String joinStr = joinField.getValue();
        String whereStr = whereField.getValue();

        // CAUTION: the magic entity name!  The length is three character to match "{E}" length in query
        String entityAlias = "a39";

        int queryPosition = -1;
        Class javaClassForEntity = attribute.getJavaClassForEntity();
        if (javaClassForEntity == null) {
            return new ArrayList<>();
        }

        MetaClass metaClass = metadata.getClassNN(javaClassForEntity);
        String queryStart = String.format("select %s from %s %s ", entityAlias, metaClass.getName(), entityAlias);

        StringBuilder queryBuilder = new StringBuilder(queryStart);
        if (StringUtils.isNotEmpty(joinStr)) {
            if (sender == joinField) {
                queryPosition = queryBuilder.length() + senderCursorPosition - 1;
            }
            if (!StringUtils.containsIgnoreCase(joinStr, "join") && !StringUtils.contains(joinStr, ",")) {
                queryBuilder.append("join ").append(joinStr);
                queryPosition += "join ".length();
            } else {
                queryBuilder.append(joinStr);
            }
        }
        if (StringUtils.isNotEmpty(whereStr)) {
            if (sender == whereField) {
                queryPosition = queryBuilder.length() + WHERE.length() + senderCursorPosition - 1;
            }
            queryBuilder.append(WHERE).append(whereStr);
        }
        String query = queryBuilder.toString();
        query = query.replace("{E}", entityAlias);

        return JpqlSuggestionFactory.requestHint(query, queryPosition, sender.getAutoCompleteSupport(), senderCursorPosition);
    }

    private int compareNumbers(PropertyType type, Number first, Number second) {
        if (type == PropertyType.INTEGER) {
            return Integer.compare((Integer) first, (Integer) second);
        } else if (type == PropertyType.DOUBLE) {
            return Double.compare((Double) first, (Double) second);
        } else if (type == PropertyType.DECIMAL) {
            return ((BigDecimal) first).compareTo((BigDecimal) second);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @SuppressWarnings("unchecked")
    protected void setupBigDecimalFormat() {

        Datatype datatype = dynamicAttributesGuiTools.getCustomNumberDatatype(attribute);

        if (datatype != null) {
            defaultDecimalField.setDatatype(datatype);
            minDecimalField.setDatatype(datatype);
            maxDecimalField.setDatatype(datatype);

            defaultDecimalField.setValue(defaultDecimalField.getValue());
            minDecimalField.setValue(minDecimalField.getValue());
            maxDecimalField.setValue(maxDecimalField.getValue());
        }
    }

    protected List<CategoryAttribute> getCategoryAttributes() {
        return attributeDs.getItem().getCategory().getCategoryAttrs();
    }
}