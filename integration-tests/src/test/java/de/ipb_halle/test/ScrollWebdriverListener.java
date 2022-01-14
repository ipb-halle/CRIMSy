/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2022 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.test;

import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.events.WebDriverListener;

import com.codeborne.selenide.Selenide;

/**
 * {@link WebDriverListener} implementation that scrolls to the element before
 * clicking it.
 * 
 * @author flange
 */
/*
 * Implementation from
 * https://github.com/selenide/selenide/issues/1692#issuecomment-1010965030
 */
public class ScrollWebdriverListener implements WebDriverListener {
    private final int yOffset;

    /**
     * @param yOffset vertical offset for scrolling in pixels
     */
    public ScrollWebdriverListener(int yOffset) {
        this.yOffset = yOffset;
    }

    /**
     * Scrolls to the element.
     */
    @Override
    public void beforeClick(WebElement element) {
        Point location = element.getLocation();
        Selenide.executeJavaScript("window.scrollTo(" + location.getX() + ", "
                + location.getY() + yOffset + ')');
    }
}