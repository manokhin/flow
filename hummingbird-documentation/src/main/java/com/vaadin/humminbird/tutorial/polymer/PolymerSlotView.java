/*
 * Copyright 2000-2017 Vaadin Ltd.
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
package com.vaadin.humminbird.tutorial.polymer;

import com.vaadin.annotations.HtmlImport;
import com.vaadin.annotations.Tag;
import com.vaadin.humminbird.tutorial.annotations.CodeFor;
import com.vaadin.humminbird.tutorial.routing.Routing.CompanyView;
import com.vaadin.humminbird.tutorial.routing.Routing.HomeView;
import com.vaadin.hummingbird.dom.Element;
import com.vaadin.hummingbird.dom.ElementFactory;
import com.vaadin.hummingbird.router.HasChildView;
import com.vaadin.hummingbird.router.RouterConfiguration;
import com.vaadin.hummingbird.router.RouterConfigurator;
import com.vaadin.hummingbird.router.View;
import com.vaadin.hummingbird.template.PolymerTemplate;
import com.vaadin.ui.AngularTemplate;

@CodeFor("tutorial-template-components-in-slot.asciidoc")
public class PolymerSlotView {
    @Tag("component-container")
    @HtmlImport("/com/vaadin/hummingbird/uitest/ui/template/ComponentContainer.html")
    public class ComponentContainer extends PolymerTemplate {

        public ComponentContainer() {
            Element label = ElementFactory.createLabel("Main layout header");
            getElement().appendChild(label);
        }
    }

    @Tag("main-layout")
    @HtmlImport("/com/vaadin/hummingbird/uitest/ui/template/MainLayout.html")
    public class MainLayout extends PolymerTemplate implements HasChildView {

        private View childView;

        @Override
        public void setChildView(View childView) {
            if(this.childView != null) {
                getElement().removeChild(this.childView.getElement());
            }
            getElement().appendChild(childView.getElement());
            this.childView = childView;
        }
    }

    public class MyRouterConfigurator implements RouterConfigurator {
        @Override
        public void configure(RouterConfiguration configuration) {
            //@formatter:off - custom line wrapping
            configuration.setRoute("", HomeView.class, MainLayout.class);
            configuration.setRoute("company", CompanyView.class, MainLayout.class);
            //@formatter:on
        }
    }
}