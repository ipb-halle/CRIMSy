/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2020 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.util;

import de.ipb_halle.lbac.util.HTMLInputFilter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;

/**
 *
 * http://josephoconnell.com/java/xss-html-filter/
 *
 * @author Joseph O'Connell <joe.oconnell at gmail dot com>
 * @version 1.0
 *
 * KICKS MODIFICATIONS: - Limited to plain physical markup with the following
 * tags: b, i, u, sub, sup (no links, no images) by F. Broda
 * <fbroda at ipb-halle dot de>; 2010-04-23 - Introduced HTML entities &micro;
 * and &deg; 2014-04-14 - Separated into HTMLInputFilter and
 * HTMLInputFilterTest; 2016-04-15
 *
 */
public class HTMLInputFilterTest {

    protected HTMLInputFilter vFilter;

    @Before
    public void setUp() {
        vFilter = new HTMLInputFilter(true, true);
    }

    @After
    public void tearDown() {
        vFilter = null;
    }

    private void t(String input, String result) {
        Assert.assertEquals(result, vFilter.filter(input));
    }

    @Test
    public void test_basics() {
        t("", "");
        t("hello", "hello");
    }

    @Test
    public void test_balancing_tags() {
        t("<b>hello", "<b>hello</b>");
        t("<b>hello", "<b>hello</b>");
        t("hello<b>", "hello");
        t("hello</b>", "hello");
        t("hello<b/>", "hello");
        t("<b><b><b>hello", "<b><b><b>hello</b></b></b>");
        t("</b><b>", "");
    }

    @Test
    public void test_end_slashes() {
        t("<img>", "<img />");
        t("<img/>", "<img />");
        t("<b/></b>", "");
    }

    @Test
    public void test_balancing_angle_brackets() {
        if (HTMLInputFilter.ALWAYS_MAKE_TAGS) {
            t("<img src=\"foo\"", "<img src=\"foo\" />");
            t("i>", "");
            t("<img src=\"foo\"/", "<img src=\"foo\" />");
            t(">", "");
            t("foo<b", "foo");
            t("b>foo", "<b>foo</b>");
            t("><b", "");
            t("b><", "");
            t("><b>", "");
        } else {
            t("<img src=\"foo\"", "&lt;img src=\"foo\"");
            t("b>", "b&gt;");
            t("<img src=\"foo\"/", "&lt;img src=\"foo\"/");
            t(">", "&gt;");
            t("foo<b", "foo&lt;b");
            t("b>foo", "b&gt;foo");
            t("><b", "&gt;&lt;b");
            t("b><", "b&gt;&lt;");
            t("><b>", "&gt;");
        }
    }

    @Test
    public void test_attributes() {
        t("<img src=foo>", "<img src=\"foo\" />");
        t("<img asrc=foo>", "<img />");
        t("<img src=test test>", "<img src=\"test\" />");
    }

    @Test
    public void test_disallow_script_tags() {
        t("<script>", "");
        if (HTMLInputFilter.ALWAYS_MAKE_TAGS) {
            t("<script", "");
        } else {
            t("<script", "&lt;script");
        }
        t("<script/>", "");
        t("</script>", "");
        t("<script woo=yay>", "");
        t("<script woo=\"yay\">", "");
        t("<script woo=\"yay>", "");
        t("<script woo=\"yay<b>", "");
        t("<script<script>>", "");
        t("<<script>script<script>>", "script");
        t("<<script><script>>", "");
        t("<<script>script>>", "");
        t("<<script<script>>", "");
    }

    @Test
    public void test_protocols() {
        t("<a href=\"http://foo\">bar</a>", "<a href=\"http://foo\">bar</a>");
        // we don't allow ftp. t("<a href=\"ftp://foo\">bar</a>", "<a href=\"ftp://foo\">bar</a>");
        t("<a href=\"mailto:foo\">bar</a>", "<a href=\"mailto:foo\">bar</a>");
        t("<a href=\"javascript:foo\">bar</a>", "<a href=\"#foo\">bar</a>");
        t("<a href=\"java script:foo\">bar</a>", "<a href=\"#foo\">bar</a>");
        t("<a href=\"java\tscript:foo\">bar</a>", "<a href=\"#foo\">bar</a>");
        t("<a href=\"java\nscript:foo\">bar</a>", "<a href=\"#foo\">bar</a>");
        t("<a href=\"java" + HTMLInputFilter.chr(1) + "script:foo\">bar</a>", "<a href=\"#foo\">bar</a>");
        t("<a href=\"jscript:foo\">bar</a>", "<a href=\"#foo\">bar</a>");
        t("<a href=\"vbscript:foo\">bar</a>", "<a href=\"#foo\">bar</a>");
        t("<a href=\"view-source:foo\">bar</a>", "<a href=\"#foo\">bar</a>");
    }

    @Test
    public void test_self_closing_tags() {
        t("<img src=\"a\">", "<img src=\"a\" />");
        t("<img src=\"a\">foo</img>", "<img src=\"a\" />foo");
        t("</img>", "");
    }

    @Test
    public void test_comments() {
        if (HTMLInputFilter.STRIP_COMMENTS) {
            t("<!-- a<b --->", "");
        } else {
            t("<!-- a<b --->", "<!-- a&lt;b --->");
        }
    }
}
