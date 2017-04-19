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
package com.vaadin.flow.uitest.ui.webcomponent;

import java.util.ArrayList;

import com.vaadin.flow.html.Button;
import com.vaadin.flow.html.Div;
import com.vaadin.flow.router.View;

/**
 * Example on how to use a web component.
 *
 * @author Vaadin Ltd
 */
public class ProgressBubbleView extends Div implements View {

    private static final String BACKGROUND = "background";
    ArrayList<ProgressBubble> bubbles = new ArrayList<>();

    /**
     * Creates a new view instance.
     */
    public ProgressBubbleView() {
        ProgressBubble bubble = new ProgressBubble(0, 100);
        bubble.getElement().getStyle().set(BACKGROUND, "green");
        bubbles.add(bubble);
        bubble = new ProgressBubble(0, 100);
        bubble.getElement().getStyle().set(BACKGROUND, "red");
        bubbles.add(bubble);
        bubble = new ProgressBubble(0, 100);
        bubble.getElement().getStyle().set(BACKGROUND, "blue");
        bubbles.add(bubble);
        bubble = new ProgressBubble(0, 100);
        bubble.getElement().getStyle().set(BACKGROUND, "purple");
        bubbles.add(bubble);

        Button makeProgress = new Button("Make progress");
        makeProgress.setId("makeProgress");
        makeProgress.addClickListener(e -> {
            bubbles.forEach(pb -> pb.setValue(pb.getValue() + 5));
        });

        Button increaseMax = new Button("Increase max value");
        increaseMax.setId("increaseMax");
        increaseMax.addClickListener(e -> {
            bubbles.forEach(pb -> pb.setMax(pb.getMax() * 2));
        });

        add(makeProgress, increaseMax);
        bubbles.forEach(this::add);
    }
}