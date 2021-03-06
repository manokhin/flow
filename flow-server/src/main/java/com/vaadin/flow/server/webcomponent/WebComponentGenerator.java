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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.WebComponentExporter;
import com.vaadin.flow.component.webcomponent.WebComponentConfiguration;
import com.vaadin.flow.internal.AnnotationReader;
import com.vaadin.flow.shared.util.SharedUtil;

import elemental.json.Json;
import elemental.json.JsonObject;
import elemental.json.JsonValue;

/**
 * Generates a client-side web component from a Java class.
 * <p>
 * Current implementation will create a Polymer 2 component that can be served
 * to the client.
 */
public class WebComponentGenerator {

    private static final String INDENTATION = "    ";

    private WebComponentGenerator() {
    }

    private static String getTemplate() {
        try {
            return IOUtils.toString(
                    WebComponentGenerator.class
                            .getResourceAsStream("webcomponent-template.html"),
                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Couldn't load the template class", e);
        }
    }

    /**
     * Generate web component html/JS for given exporter class.
     *
     * @param exporterType
     *            web component exporter class, not {@code null}
     * @param frontendURI
     *            the frontend resources URI, not {@code null}
     * @return generated web component html/JS to be served to the client
     */
    public static String generateModule(
            Class<? extends WebComponentExporter<? extends Component>> exporterType,
            String frontendURI) {
        Objects.requireNonNull(exporterType);
        Objects.requireNonNull(frontendURI);

        WebComponentConfiguration<? extends Component> config = new WebComponentConfigurationFactory()
                .apply(exporterType);
        return generateModule(getTag(exporterType), config, frontendURI, false);
    }

    /**
     * Generate web component html/JS for given tag and class.
     *
     * @param tag
     *            web component tag, not {@code null}
     * @param webComponentConfiguration
     *            web component class implementation, not {@code null}
     * @param frontendURI
     *            the frontend resources URI, not {@code null}
     * @return generated web component html/JS to be served to the client
     */
    public static String generateModule(String tag,
            WebComponentConfiguration<? extends Component> webComponentConfiguration,
            String frontendURI) {
        Objects.requireNonNull(tag);
        Objects.requireNonNull(webComponentConfiguration);
        Objects.requireNonNull(frontendURI);

        Set<PropertyData<?>> propertyDataSet = webComponentConfiguration
                .getPropertyDataSet();

        Map<String, String> replacements = getReplacementsMap(tag,
                propertyDataSet, frontendURI, true);

        String template = getTemplate();
        for (Map.Entry<String, String> replacement : replacements.entrySet()) {
            template = template.replace("_" + replacement.getKey() + "_",
                    replacement.getValue());
        }
        return template;
    }

    static Map<String, String> getReplacementsMap(String tag,
            Set<PropertyData<?>> propertyDataSet, String frontendURI,
            boolean generateUiImport) {
        Map<String, String> replacements = new HashMap<>();

        replacements.put("TagDash", tag);
        replacements.put("TagCamel", SharedUtil
                .capitalize(SharedUtil.dashSeparatedToCamelCase(tag)));

        replacements.put("PropertyMethods", getPropertyMethods(
                propertyDataSet.stream().map(PropertyData::getName)));

        replacements.put("Properties", getPropertyDefinitions(propertyDataSet));

        replacements.put("frontend_resources", frontendURI);

        replacements.put("ui_import",
                generateUiImport
                        ? "<link rel='import' href='web-component-ui.html'>"
                        : "");

        return replacements;
    }

    private static String generateModule(String tag,
            WebComponentConfiguration<? extends Component> webComponentConfiguration,
            String frontendURI, boolean generateUiImport) {
        Objects.requireNonNull(tag);
        Objects.requireNonNull(webComponentConfiguration);
        Objects.requireNonNull(frontendURI);

        Set<PropertyData<?>> propertyDataSet = webComponentConfiguration
                .getPropertyDataSet();

        Map<String, String> replacements = getReplacementsMap(tag,
                propertyDataSet, frontendURI, generateUiImport);

        String template = getTemplate();
        for (Map.Entry<String, String> replacement : replacements.entrySet()) {
            template = template.replace("_" + replacement.getKey() + "_",
                    replacement.getValue());
        }
        return template;
    }

    private static String getPropertyDefinitions(
            Set<PropertyData<?>> properties) {
        JsonObject props = Json.createObject();

        for (PropertyData<?> property : properties) {
            JsonObject prop = createPropertyDefinition(property);
            props.put(property.getName(), prop);
        }
        return props.toJson();
    }

    private static JsonObject createPropertyDefinition(
            PropertyData<?> property) {
        JsonObject prop = Json.createObject();

        prop.put("type", property.getType().getSimpleName());

        if (property.getDefaultValue() != null) {
            String propertyValue = "value";
            if (property.getType() == Boolean.class) {
                prop.put(propertyValue, (Boolean) property.getDefaultValue());
            } else if (property.getType() == Double.class) {
                prop.put(propertyValue, (Double) property.getDefaultValue());
            } else if (property.getType() == Integer.class) {
                prop.put(propertyValue, (Integer) property.getDefaultValue());
            } else if (property.getType() == String.class) {
                prop.put(propertyValue, (String) property.getDefaultValue());
            } else if (JsonValue.class.isAssignableFrom(property.getType())) {
                prop.put(propertyValue, (JsonValue) property.getDefaultValue());
            } else {
                throw new UnsupportedPropertyTypeException(String.format("%s "
                        + "is not a currently supported type for a Property. "
                        + "Please use %s instead.",
                        property.getType().getSimpleName(),
                        JsonValue.class.getSimpleName()));
            }
        }
        prop.put("observer", getSyncMethod(property.getName()));
        prop.put("notify", true);
        prop.put("reflectToAttribute", false);

        return prop;
    }

    private static String getSyncMethod(String property) {
        return "_sync_" + SharedUtil.dashSeparatedToCamelCase(property);
    }

    private static String getPropertyMethods(Stream<String> properties) {
        StringBuilder methods = new StringBuilder();
        properties.forEach(property -> {
            methods.append(INDENTATION);
            methods.append(getSyncMethod(property));
            methods.append("(newValue, oldValue) { ");
            methods.append("this._sync('").append(property)
                    .append("', newValue);");
            methods.append("}\n");
        });
        return methods.toString();
    }

    private static String getTag(
            Class<? extends WebComponentExporter<? extends Component>> exporterType) {
        Optional<Tag> tag = AnnotationReader.getAnnotationFor(exporterType,
                Tag.class);
        assert tag.isPresent();
        return tag.get().value();
    }
}
