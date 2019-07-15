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

package com.haulmont.cuba.gui.components.filter;

import com.haulmont.bali.datastruct.Node;
import com.haulmont.bali.events.Subscription;
import com.haulmont.cuba.gui.components.filter.condition.AbstractCondition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages subscriptions of conditions and parameters. It allows to subscribe and remove listeners.
 */
@Component(ImmediateModeHelper.NAME)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ImmediateModeHelper {
    public static final String NAME = "cuba_ImmediateModeHelper";

    protected List<Subscription> paramValueChangeSubscriptions;
    protected Map<AbstractCondition, AbstractCondition.Listener> conditionListeners;

    protected Runnable applyFilterHandler;

    /**
     * Sets apply filter handler. It will be invoked if param value is changed or param is changed itself.
     *
     * @param applyFilterHandler apply filter handler
     */
    public void setApplyFilterHandler(Runnable applyFilterHandler) {
        this.applyFilterHandler = applyFilterHandler;
    }

    /**
     * @return apply filter handler
     */
    @Nullable
    public Runnable getApplyFilterHandler() {
        return applyFilterHandler;
    }

    /**
     * Clears all param value change subscriptions and condition listeners.
     */
    public void clearParamValueChangeSubscriptions() {
        if (paramValueChangeSubscriptions != null) {
            paramValueChangeSubscriptions.forEach(Subscription::remove);
            paramValueChangeSubscriptions.clear();
        }

        if (conditionListeners != null) {
            for (Map.Entry<AbstractCondition, AbstractCondition.Listener> item : conditionListeners.entrySet()) {
                item.getKey().removeListener(item.getValue());
            }
            conditionListeners.clear();
        }
    }

    /**
     * Subscribes to change param value change in condition recursively and add listener to condition.
     *
     * @param conditions list of node with conditions
     */
    public void subscribeToParamValueChangeEventRecursively(List<Node<AbstractCondition>> conditions) {
        if (paramValueChangeSubscriptions == null) {
            paramValueChangeSubscriptions = new ArrayList<>();
        }

        for (Node<AbstractCondition> node : conditions) {
            AbstractCondition condition = node.getData();
            if (condition.isGroup()) {
                subscribeToParamValueChangeEventRecursively(node.getChildren());
            } else {
                Subscription subscription = condition.getParam()
                        .addParamValueChangeListener(event -> applyFilter());
                paramValueChangeSubscriptions.add(subscription);

                addConditionListener(condition, subscription);
            }
        }
    }

    protected void addConditionListener(AbstractCondition condition, Subscription current) {
        if (conditionListeners == null) {
            conditionListeners = new HashMap<>();
        }

        AbstractCondition.Listener listener = new AbstractCondition.Listener() {
            protected Subscription previous = current;

            @Override
            public void captionChanged() {
                // do nothing
            }

            @Override
            public void paramChanged(Param oldParam, Param newParam) {
                previous.remove();
                paramValueChangeSubscriptions.remove(previous);

                Subscription newSubscription = newParam.addParamValueChangeListener(event -> applyFilter());
                paramValueChangeSubscriptions.add(newSubscription);

                previous = newSubscription;

                applyFilter();
            }
        };

        condition.addListener(listener);
        conditionListeners.put(condition, listener);
    }

    protected void applyFilter() {
        if (applyFilterHandler != null) {
            applyFilterHandler.run();
        }
    }
}
