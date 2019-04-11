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

package com.haulmont.cuba.gui.xml.layout.loaders;

import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.xml.layout.ComponentLoader;

import java.util.ArrayList;
import java.util.List;

public class CompositeComponentLoaderContext implements ComponentLoader.CompositeComponentContext {

    protected Class<? extends Component> componentClass;
    protected String template;

    protected List<ComponentLoader.PostInitTask> postInitTasks = new ArrayList<>();
    protected List<ComponentLoader.InjectTask> injectTasks = new ArrayList<>();
    protected List<ComponentLoader.InitTask> initTasks = new ArrayList<>();

    @Override
    public Class<? extends Component> getComponentClass() {
        return componentClass;
    }

    @Override
    public void setComponentClass(Class<? extends Component> componentClass) {
        this.componentClass = componentClass;
    }

    @Override
    public String getComponentTemplate() {
        return template;
    }

    @Override
    public void setComponentTemplate(String template) {
        this.template = template;
    }

    @Override
    public void addPostInitTask(ComponentLoader.PostInitTask task) {
        postInitTasks.add(task);
    }

    @Override
    public void executePostInitTasks() {
        for (ComponentLoader.PostInitTask postInitTask : postInitTasks) {
            postInitTask.execute(this, null);
        }
        postInitTasks.clear();
    }

    @Override
    public void addInjectTask(ComponentLoader.InjectTask task) {
        injectTasks.add(task);
    }

    @Override
    public void executeInjectTasks() {
        for (ComponentLoader.InjectTask injectTask : injectTasks) {
            injectTask.execute(this, null);
        }
        injectTasks.clear();
    }

    @Override
    public void addInitTask(ComponentLoader.InitTask task) {
        initTasks.add(task);
    }

    @Override
    public void executeInitTasks() {
        for (ComponentLoader.InitTask initTask : initTasks) {
            initTask.execute(this, null);
        }
        initTasks.clear();
    }
}
