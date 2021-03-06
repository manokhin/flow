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

package com.vaadin.flow.component;

import java.io.Serializable;

import com.vaadin.flow.component.webcomponent.WebComponent;
import com.vaadin.flow.component.webcomponent.WebComponentDefinition;

/**
 * Provides a way to exporter a class which extends {@link Component} as an
 * embeddable web component The tag of the exporter web component <b>must be</b>
 * defined using {@link Tag} annotation - otherwise, an exception will be
 * thrown.
 * <p>
 * Limitations regarding the tag are:
 * <ul>
 * <li>The tag must be a non-null, non-empty string with dash-separated words,
 * i.e. "dash-separated".</li>
 * <li>Exporter cannot share the tag with the component being exported. If they
 * do, an exception will be thrown during run-time.</li>
 * </ul>
 * <p>
 * The exported web components can be embedded into non-Vaadin applications.
 * <p>
 * Example of exporting {@code MyComponent} component:
 *
 * <pre>
 * &#064;Tag("my-component")
 * public class Exporter implements WebComponentExporter&lt;MyComponent&gt;() {
 *     &#064;Override
 *     public void define(WebComponentDefinition&lt;MyComponent&gt;
 *              definition) {
 *         definition.addProperty("name", "John Doe")
 *                 .onChange(MyComponent::setName);
 *     }
 *
 *     &#064;Override
 *     public void configure(WebComponent&lt;MyComponent&gt;
 *              webComponent, MyComponent component) {
 *          // add e.g. a listener to the {@code component}
 *          // and do something with {@code webComponent}
 *     }
 * }
 * </pre>
 *
 * @param <C>
 *            type of the component to export
 */
public interface WebComponentExporter<C extends Component>
        extends Serializable {
    /**
     * Called by the web component export process. Use the given
     * {@link WebComponentDefinition} to define web component's properties, and
     * how the properties interact with the {@link Component} being exported.
     * <p>
     * If the component instance needs to be configured further after its
     * creation, or property updates need to be pushed to the client, implement
     * {@link #configure(WebComponent, Component)}.
     *
     * @see #configure(WebComponent, Component)
     *
     * @param definition
     *            instance used to define the component.
     */
    void define(WebComponentDefinition<C> definition);

    /**
     * If custom initialization for the created {@link Component} instance is
     * needed, it can be done here. It is also possible to configure custom
     * communication between the {@code component} instance and client-side web
     * component using the {@link WebComponent} instance. The {@code
     * webComponent} and {@code component} are in 1-to-1 relation.
     *
     * @param webComponent
     *            instance representing the client-side web component instance
     *            matching the component
     * @param component
     *            exported component instance
     */
    void configure(WebComponent<C> webComponent, C component);
}
