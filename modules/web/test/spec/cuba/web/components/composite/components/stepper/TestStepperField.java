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

package spec.cuba.web.components.composite.components.stepper;

import com.haulmont.bali.events.Subscription;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.data.ValueSource;
import com.haulmont.cuba.gui.icons.Icons;
import com.haulmont.cuba.web.gui.components.CompositeComponent;
import com.haulmont.cuba.web.gui.components.CompositeDescriptor;
import com.haulmont.cuba.web.widgets.CubaTextField;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;

import java.util.Collection;
import java.util.function.Consumer;

@CompositeDescriptor("spec/cuba/web/components/composite/components/stepper/stepper-field.xml")
public class TestStepperField extends CompositeComponent<CssLayout> implements Field<Integer> {

    public static final String NAME = "testStepperField";

    /* Nested Components */
    private TextField<Integer> valueField;
    private Button upBtn;
    private Button downBtn;

    @SuppressWarnings("unchecked")
    @Override
    protected void setComposition(CssLayout composition) {
        super.setComposition(composition);

        valueField = (TextField<Integer>) composition.getComponentNN("stepper_valueField");
        CubaTextField cubaTextField = valueField.unwrap(CubaTextField.class);
        cubaTextField.addShortcutListener(createAdjustmentShortcut(ShortcutAction.KeyCode.ARROW_UP, 1));
        cubaTextField.addShortcutListener(createAdjustmentShortcut(ShortcutAction.KeyCode.ARROW_DOWN, -1));

        upBtn = (Button) composition.getComponentNN("stepper_upBtn");
        downBtn = (Button) composition.getComponentNN("stepper_downBtn");

        upBtn.addClickListener(clickEvent -> updateValue(1));
        downBtn.addClickListener(clickEvent -> updateValue(-1));
    }

    private ShortcutListener createAdjustmentShortcut(int keyCode, int adjustment) {
        return new ShortcutListener(null, keyCode, (int[]) null) {
            @Override
            public void handleAction(Object sender, Object target) {
                updateValue(adjustment);
            }
        };
    }

    private void updateValue(int adjustment) {
        Integer currentValue = getValue();
        setValue(currentValue != null ? currentValue + adjustment : adjustment);
    }

    @Override
    public boolean isRequired() {
        return valueField.isRequired();
    }

    @Override
    public void setRequired(boolean required) {
        valueField.setRequired(required);
        getCompositionNN().setRequiredIndicatorVisible(required);
    }

    @Override
    public String getRequiredMessage() {
        return valueField.getRequiredMessage();
    }

    @Override
    public void setRequiredMessage(String msg) {
        valueField.setRequiredMessage(msg);
    }

    @Override
    public void addValidator(Consumer<? super Integer> validator) {
        valueField.addValidator(validator);
    }

    @Override
    public void removeValidator(Consumer<Integer> validator) {
        valueField.removeValidator(validator);
    }

    @Override
    public Collection<Consumer<Integer>> getValidators() {
        return valueField.getValidators();
    }

    @Override
    public boolean isEditable() {
        return valueField.isEditable();
    }

    @Override
    public void setEditable(boolean editable) {
        valueField.setEditable(editable);
        upBtn.setEnabled(editable);
        downBtn.setEnabled(editable);
    }

    @Override
    public String getCaption() {
        return getCompositionNN().getCaption();
    }

    @Override
    public void setCaption(String caption) {
        getCompositionNN().setCaption(caption);
    }

    @Override
    public String getDescription() {
        return getCompositionNN().getDescription();
    }

    @Override
    public void setDescription(String description) {
        getCompositionNN().setDescription(description);
    }

    @Override
    public String getIcon() {
        return getCompositionNN().getIcon();
    }

    @Override
    public void setIcon(String icon) {
        getCompositionNN().setIcon(icon);
    }

    @Override
    public void setIconFromSet(Icons.Icon icon) {
        getCompositionNN().setIconFromSet(icon);
    }

    @Override
    public Integer getValue() {
        return valueField.getValue();
    }

    @Override
    public void setValue(Integer value) {
        valueField.setValue(value);
    }

    @Override
    public Subscription addValueChangeListener(Consumer<ValueChangeEvent<Integer>> listener) {
        return valueField.addValueChangeListener(listener);
    }

    @Override
    public void removeValueChangeListener(Consumer<ValueChangeEvent<Integer>> listener) {
        valueField.removeValueChangeListener(listener);
    }

    @Override
    public String getContextHelpText() {
        return getCompositionNN().getContextHelpText();
    }

    @Override
    public void setContextHelpText(String contextHelpText) {
        getCompositionNN().setContextHelpText(contextHelpText);
    }

    @Override
    public boolean isContextHelpTextHtmlEnabled() {
        return getCompositionNN().isContextHelpTextHtmlEnabled();
    }

    @Override
    public void setContextHelpTextHtmlEnabled(boolean enabled) {
        getCompositionNN().setContextHelpTextHtmlEnabled(enabled);
    }

    @Override
    public Consumer<ContextHelpIconClickEvent> getContextHelpIconClickHandler() {
        return getCompositionNN().getContextHelpIconClickHandler();
    }

    @Override
    public void setContextHelpIconClickHandler(Consumer<ContextHelpIconClickEvent> handler) {
        getCompositionNN().setContextHelpIconClickHandler(handler);
    }

    @Override
    public boolean isCaptionAsHtml() {
        return getCompositionNN().isCaptionAsHtml();
    }

    @Override
    public void setCaptionAsHtml(boolean captionAsHtml) {
        getCompositionNN().setCaptionAsHtml(captionAsHtml);
    }

    @Override
    public boolean isDescriptionAsHtml() {
        return getCompositionNN().isDescriptionAsHtml();
    }

    @Override
    public void setDescriptionAsHtml(boolean descriptionAsHtml) {
        getCompositionNN().setDescriptionAsHtml(descriptionAsHtml);
    }

    @Override
    public boolean isValid() {
        return valueField.isValid();
    }

    @Override
    public void validate() throws ValidationException {
        valueField.validate();
    }

    @Override
    public void setValueSource(ValueSource<Integer> valueSource) {
        valueField.setValueSource(valueSource);
        getCompositionNN().setRequiredIndicatorVisible(valueField.isRequired());
    }

    @Override
    public ValueSource<Integer> getValueSource() {
        return valueField.getValueSource();
    }
}
