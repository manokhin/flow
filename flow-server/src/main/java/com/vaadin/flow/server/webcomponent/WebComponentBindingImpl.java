/*
 * Copyright 2000-2018 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.vaadin.flow.server.webcomponent;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.webcomponent.WebComponentBinding;
import com.vaadin.flow.component.webcomponent.WebComponentConfiguration;
import com.vaadin.flow.di.Instantiator;

/**
 * Implementation of {@link WebComponentBinding}.
 *
 * @param <C> {@code component} exported as embeddable web component
 */
public class WebComponentBindingImpl<C extends Component>
        implements WebComponentBinding<C>, Serializable {
    private C component;
    private Map<String, PropertyBinding<? extends Serializable>> properties;

    /**
     * Constructs a {@link WebComponentBinding} which consists of a
     * {@link Component} instance exposed as an embeddable web component and
     * set of {@link PropertyBinding PropertyBindindings} that the component
     * exposes as a web component.
     *
     * @param component     component which exposes {@code properties} as web
     *                      component. Not null
     * @param properties    set of {@code property bindings}. Each binding
     *                      knows how it is written to the {@code component}.
     *                      Not null
     */
    public WebComponentBindingImpl(C component,
                               Set<PropertyBinding<? extends Serializable>> properties) {
        Objects.requireNonNull(component, "Parameter 'component' must not be " +
                "null!");
        Objects.requireNonNull(properties, "Parameter 'properties' must not " +
                "be null!");

        this.component = component;
        this.properties = properties.stream().collect(Collectors.toMap(
                PropertyBinding::getName, p -> p));
    }

    /**
     * {@inheritDoc}
     * @param propertyName  name of the property
     * @param value         new value to set for the property
     */
    @Override
    public void updateProperty(String propertyName, Serializable value) {
        Objects.requireNonNull(propertyName, "Parameter 'propertyName' must " +
                "not be null!");

        PropertyBinding<?> propertyBinding = properties.get(propertyName);

        if (propertyBinding == null) {
            throw new IllegalArgumentException(
                    String.format("No %s found for propertyName '%s'!",
                            PropertyData.class.getSimpleName(), propertyName));
        }

        propertyBinding.updateValue(value);
    }

    /**
     * {@inheritDoc}
     * @return bound component
     */
    @Override
    public C getComponent() {
        return component;
    }

    /**
     * {@inheritDoc}
     * @param propertyName  name of the property
     * @return type of property matching {@code propertyName}
     */
    @Override
    public Class<? extends Serializable> getPropertyType(String propertyName) {
        if (hasProperty(propertyName)) {
            return properties.get(propertyName).getType();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * @param propertyName  name of the property
     * @return has property
     */
    @Override
    public boolean hasProperty(String propertyName) {
        return properties.containsKey(propertyName);
    }

    /**
     * Called by
     * {@link WebComponentConfiguration#createWebComponentBinding(Instantiator, com.vaadin.flow.dom.Element)}
     * once the instance has been successfully constructed. Reports the current
     * (default) values to the bound component.
     */
    void updatePropertiesToComponent() {
        properties.forEach((key, value) -> value.notifyValueChange());
    }
}
