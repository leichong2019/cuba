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

import com.google.common.base.Preconditions;
import com.haulmont.bali.events.EventHub;
import com.haulmont.bali.events.Subscription;
import com.haulmont.cuba.gui.ComponentsHelper;
import com.haulmont.cuba.gui.components.AttachEvent;
import com.haulmont.cuba.gui.components.Attachable;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.DetachEvent;
import com.haulmont.cuba.gui.components.Frame;
import com.haulmont.cuba.gui.components.SizeUnit;
import com.haulmont.cuba.gui.components.Window;
import com.haulmont.cuba.gui.components.sys.FrameImplementation;

import java.util.EventObject;
import java.util.Objects;
import java.util.function.Consumer;

public class CompositeComponent<T extends Component & Attachable>
        implements Component, Component.BelongToFrame, Attachable {

    protected String id;
    protected T root;
    protected Frame frame;
    protected boolean attached = false;

    // private, lazily initialized
    private EventHub eventHub = null;

    protected EventHub getEventHub() {
        if (eventHub == null) {
            eventHub = new EventHub();
        }
        return eventHub;
    }

    public T getComposition() {
        return root;
    }

    protected T getCompositionNN() {
        Preconditions.checkState(root != null, "Composition root is not initialized");
        return root;
    }

    protected void setComposition(T composition) {
        Preconditions.checkState(root == null, "Composition root is already initialized");
        this.root = composition;
    }

    protected <E> void fireEvent(Class<E> eventType, E event) {
        getEventHub().publish(eventType, event);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        if (!Objects.equals(this.id, id)) {
            if (frame != null) {
                ((FrameImplementation) frame).unregisterComponent(this);
            }

            this.id = id;

            // TODO: gg, setCubaId
            // TODO: gg, assignDebugId

            if (frame != null) {
                ((FrameImplementation) frame).registerComponent(this);
            }
        }
    }

    @Override
    public Component getParent() {
        return getCompositionNN().getParent();
    }

    @Override
    public void setParent(Component parent) {
        if (getCompositionNN().getParent() != parent) {
            getCompositionNN().setParent(parent);

            if (isAttached()) {
                detach();
            }

            if (parent != null
                    && ComponentsHelper.isParentAttached(parent)) {
                attach();
            }
        }
    }

    @Override
    public boolean isAttached() {
        return getCompositionNN().isAttached();
    }

    @Override
    public void attach() {
        attached = true;

        getCompositionNN().attach();

        getEventHub().publish(AttachEvent.class, new AttachEvent(this));
    }

    @Override
    public void detach() {
        attached = false;

        getCompositionNN().detach();

        getEventHub().publish(DetachEvent.class, new DetachEvent(this));
    }

    @Override
    public Subscription addAttachListener(Consumer<AttachEvent> listener) {
        return getEventHub().subscribe(AttachEvent.class, listener);
    }

    @Override
    public Subscription addDetachListener(Consumer<DetachEvent> listener) {
        return getEventHub().subscribe(DetachEvent.class, listener);
    }

    @Override
    public boolean isEnabled() {
        return getCompositionNN().isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        getCompositionNN().setEnabled(enabled);
    }

    @Override
    public boolean isResponsive() {
        return getCompositionNN().isResponsive();
    }

    @Override
    public void setResponsive(boolean responsive) {
        getCompositionNN().setResponsive(responsive);
    }

    @Override
    public boolean isVisible() {
        return getCompositionNN().isVisible();
    }

    @Override
    public void setVisible(boolean visible) {
        getCompositionNN().setVisible(visible);
    }

    @Override
    public boolean isVisibleRecursive() {
        return getCompositionNN().isVisibleRecursive();
    }

    @Override
    public boolean isEnabledRecursive() {
        return getCompositionNN().isEnabledRecursive();
    }

    @Override
    public float getHeight() {
        return getCompositionNN().getHeight();
    }

    @Override
    public SizeUnit getHeightSizeUnit() {
        return getCompositionNN().getHeightSizeUnit();
    }

    @Override
    public void setHeight(String height) {
        getCompositionNN().setHeight(height);
    }

    @Override
    public float getWidth() {
        return getCompositionNN().getWidth();
    }

    @Override
    public SizeUnit getWidthSizeUnit() {
        return getCompositionNN().getWidthSizeUnit();
    }

    @Override
    public void setWidth(String width) {
        getCompositionNN().setWidth(width);
    }

    @Override
    public Alignment getAlignment() {
        return getCompositionNN().getAlignment();
    }

    @Override
    public void setAlignment(Alignment alignment) {
        getCompositionNN().setAlignment(alignment);
    }

    @Override
    public String getStyleName() {
        return getCompositionNN().getStyleName();
    }

    @Override
    public void setStyleName(String styleName) {
        getCompositionNN().setStyleName(styleName);
    }

    @Override
    public void addStyleName(String styleName) {
        getCompositionNN().addStyleName(styleName);
    }

    @Override
    public void removeStyleName(String styleName) {
        getCompositionNN().removeStyleName(styleName);
    }

    @Override
    public <X> X unwrap(Class<X> internalComponentClass) {
        return getCompositionNN().unwrap(internalComponentClass);
    }

    @Override
    public <X> X unwrapComposition(Class<X> internalCompositionClass) {
        return getCompositionNN().unwrapComposition(internalCompositionClass);
    }

    @Override
    public Frame getFrame() {
        return frame;
    }

    @Override
    public void setFrame(Frame frame) {
        this.frame = frame;

        if (frame instanceof FrameImplementation) {
            ((FrameImplementation) frame).registerComponent(this);
        }

        if (getComposition() instanceof BelongToFrame) {
            ((BelongToFrame) getComposition()).setFrame(frame);
        }

        // TODO: gg, assignDebugId
    }

    public static class CreateEvent extends EventObject {

        public CreateEvent(CompositeComponent source) {
            super(source);
        }

        @Override
        public CompositeComponent getSource() {
            return (CompositeComponent) super.getSource();
        }
    }
}
