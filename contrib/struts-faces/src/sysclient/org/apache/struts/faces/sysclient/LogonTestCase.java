/*
 * Copyright 2002,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.struts.faces.sysclient;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlBase;
import com.gargoylesoftware.htmlunit.html.HtmlBody;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlHead;
import com.gargoylesoftware.htmlunit.html.HtmlLink;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlResetInput;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * <p>Test case for a logon form that accepts a username and password.</p>
 *
 * @version $Revision: 1.1 $ $Date: 2004/08/08 03:28:35 $
 */

public class LogonTestCase extends AbstractTestCase {


    // ------------------------------------------------------------ Constructors


    /**
     * <p>Construct a new instance of this test case.</p>
     *
     * @param name Name of the new test case
     */
    public LogonTestCase(String name) {

        super(name);

    }


    // ------------------------------------------------------ Instance Variables


    // ------------------------------------------------------ Test Setup Methods


    /**
     * <p>Set up the instance variables required for this test case.</p>
     */
    public void setUp() throws Exception {

        super.setUp();
        page("/logon.faces");

    }


    /**
     * <p>Return the set of tests included in this test suite.</p>
     */
    public static Test suite() {

        return (new TestSuite(LogonTestCase.class));

    }


    /**
     * <p>Tear down instance variables required by this test case.</p>
     */
    public void tearDown() {

        super.tearDown();

    }



    // ------------------------------------------------- Individual Test Methods


    /**
     * <p>Submit incorrect input fields and verify the correct response.</p>
     */
    public void testIncorrect() throws Exception {

        HtmlSpan span = null;
        HtmlTextInput username = (HtmlTextInput) element("form:username");
        HtmlPasswordInput password = (HtmlPasswordInput)
            element("form:password");
        HtmlSubmitInput submit = (HtmlSubmitInput) element("form:submit");

        username.setValueAttribute("bb");
        password.setValueAttribute("");
        submit(submit);

        assertEquals("logon", title());

        span = (HtmlSpan) element("globalErrors");
        assertNotNull(span);
        assertEquals("", span.asText());

        username = (HtmlTextInput) element("form:username");
        assertNotNull(username);
        assertEquals("bb", username.getValueAttribute());

        span = (HtmlSpan) element("form:usernameErrors");
        assertNotNull(span);
        // FIXME:  response string should really include "Username:"
        assertEquals("[EH][EP] can not be less than 3 characters.[ES][EF]",
                     span.asText());

        password = (HtmlPasswordInput) element("form:password");
        assertNotNull(password);
        assertEquals("", password.getValueAttribute());

        span = (HtmlSpan) element("form:passwordErrors");
        assertNotNull(span);
        // FIXME:  response string should really include "Password:"
        assertEquals("[EH][EP] is required.[ES][EF]", span.asText());

    }


    /**
     * <p>Verify the content of a pristine page returned when executing this
     * view for the first time.</p>
     */
    public void testPristine() throws Exception {

        HtmlSpan span = null;

        HtmlElement html = (HtmlElement) page;
        assertEquals("html", html.getTagName());
        assertEquals("http://www.w3.org/1999/xhtml", html.getAttributeValue("xmlns"));

        assertEquals("logon", title());

        HtmlForm form = (HtmlForm) element("form");
        assertNotNull(form);
        assertEquals("", form.getAcceptAttribute());
        assertEquals("", form.getAcceptCharsetAttribute());
        String url = this.url.toString();
        url = url.substring(0, url.length() - 1);
        url = url.substring(url.lastIndexOf('/'));
        String action = form.getActionAttribute();
        int semicolon = action.indexOf(';');
        if (semicolon >= 0) {
            action = action.substring(0, semicolon);
        }
        assertEquals(url + "/logon.faces", action);
        assertEquals("", form.getEnctypeAttribute());
        assertEquals("post", form.getMethodAttribute());
        assertEquals("", form.getNameAttribute());
        assertEquals("", form.getOnResetAttribute());
        assertEquals("", form.getOnSubmitAttribute());
        assertEquals("", form.getTargetAttribute());

        span = (HtmlSpan) element("form:usernamePrompt");
        assertNotNull(span);
        assertEquals("Username:", span.asText());

        HtmlTextInput username = (HtmlTextInput) element("form:username");
        assertNotNull(username);
        assertEquals("", username.getLangAttribute());
        assertEquals("form:username", username.getNameAttribute());
        assertEquals("", username.getOnClickAttribute());
        assertEquals("", username.getOnDblClickAttribute());
        assertEquals("", username.getOnKeyDownAttribute());
        assertEquals("", username.getOnKeyPressAttribute());
        assertEquals("", username.getOnKeyUpAttribute());
        assertEquals("", username.getOnMouseDownAttribute());
        assertEquals("", username.getOnMouseMoveAttribute());
        assertEquals("", username.getOnMouseOutAttribute());
        assertEquals("", username.getOnMouseOverAttribute());
        assertEquals("", username.getOnMouseUpAttribute());
        assertEquals("text", username.getTypeAttribute());
        assertEquals("", username.getValueAttribute());

        span = (HtmlSpan) element("form:passwordPrompt");
        assertNotNull(span);
        assertEquals("Password:", span.asText());

        HtmlPasswordInput password = (HtmlPasswordInput)
            element("form:password");
        assertNotNull(password);
        assertEquals("", password.getLangAttribute());
        assertEquals("form:password", password.getNameAttribute());
        assertEquals("", password.getOnClickAttribute());
        assertEquals("", password.getOnDblClickAttribute());
        assertEquals("", password.getOnKeyDownAttribute());
        assertEquals("", password.getOnKeyPressAttribute());
        assertEquals("", password.getOnKeyUpAttribute());
        assertEquals("", password.getOnMouseDownAttribute());
        assertEquals("", password.getOnMouseMoveAttribute());
        assertEquals("", password.getOnMouseOutAttribute());
        assertEquals("", password.getOnMouseOverAttribute());
        assertEquals("", password.getOnMouseUpAttribute());
        assertEquals("password", password.getTypeAttribute());
        assertEquals("", password.getValueAttribute());

        HtmlSubmitInput submit = (HtmlSubmitInput) element("form:submit");
        assertNotNull(submit);
        assertEquals("", submit.getLangAttribute());
        assertEquals("form:submit", submit.getNameAttribute());
        assertEquals("", submit.getOnClickAttribute());
        assertEquals("", submit.getOnDblClickAttribute());
        assertEquals("", submit.getOnKeyDownAttribute());
        assertEquals("", submit.getOnKeyPressAttribute());
        assertEquals("", submit.getOnKeyUpAttribute());
        assertEquals("", submit.getOnMouseDownAttribute());
        assertEquals("", submit.getOnMouseMoveAttribute());
        assertEquals("", submit.getOnMouseOutAttribute());
        assertEquals("", submit.getOnMouseOverAttribute());
        assertEquals("", submit.getOnMouseUpAttribute());
        assertEquals("submit", submit.getTypeAttribute());
        assertEquals("Logon", submit.getValueAttribute());

        HtmlResetInput reset = (HtmlResetInput) element("form:reset");
        assertNotNull(reset);
        assertEquals("", reset.getLangAttribute());
        assertEquals("form:reset", reset.getNameAttribute());
        assertEquals("", reset.getOnClickAttribute());
        assertEquals("", reset.getOnDblClickAttribute());
        assertEquals("", reset.getOnKeyDownAttribute());
        assertEquals("", reset.getOnKeyPressAttribute());
        assertEquals("", reset.getOnKeyUpAttribute());
        assertEquals("", reset.getOnMouseDownAttribute());
        assertEquals("", reset.getOnMouseMoveAttribute());
        assertEquals("", reset.getOnMouseOutAttribute());
        assertEquals("", reset.getOnMouseOverAttribute());
        assertEquals("", reset.getOnMouseUpAttribute());
        assertEquals("reset", reset.getTypeAttribute());
        assertEquals("Reset", reset.getValueAttribute());

        HtmlSubmitInput cancel = (HtmlSubmitInput) element("form:cancel");
        assertNotNull(cancel);
        assertEquals("", cancel.getLangAttribute());
        assertEquals("form:cancel", cancel.getNameAttribute());
        assertEquals("", cancel.getOnClickAttribute());
        assertEquals("", cancel.getOnDblClickAttribute());
        assertEquals("", cancel.getOnKeyDownAttribute());
        assertEquals("", cancel.getOnKeyPressAttribute());
        assertEquals("", cancel.getOnKeyUpAttribute());
        assertEquals("", cancel.getOnMouseDownAttribute());
        assertEquals("", cancel.getOnMouseMoveAttribute());
        assertEquals("", cancel.getOnMouseOutAttribute());
        assertEquals("", cancel.getOnMouseOverAttribute());
        assertEquals("", cancel.getOnMouseUpAttribute());
        assertEquals("submit", cancel.getTypeAttribute());
        assertEquals("Cancel", cancel.getValueAttribute());


    }


    /**
     * <p>Submit known-bad mismatch and verify the correct response.</p>
     */
    public void testMismatch() throws Exception {

        HtmlSpan span = null;
        HtmlTextInput username = (HtmlTextInput) element("form:username");
        HtmlPasswordInput password = (HtmlPasswordInput)
            element("form:password");
        HtmlSubmitInput submit = (HtmlSubmitInput) element("form:submit");

        username.setValueAttribute("baduser");
        password.setValueAttribute("badpass");
        submit(submit);

        assertEquals("logon", title());

        span = (HtmlSpan) element("globalErrors");
        assertNotNull(span);
        assertEquals("[EH][EP]Invalid username/password combination[ES][EF]",
                     span.asText());

        username = (HtmlTextInput) element("form:username");
        assertNotNull(username);
        assertEquals("baduser", username.getValueAttribute());

        span = (HtmlSpan) element("form:usernameErrors");
        assertNotNull(span);
        assertEquals("", span.asText());

        password = (HtmlPasswordInput) element("form:password");
        assertNotNull(password);
        assertEquals("", password.getValueAttribute());

        span = (HtmlSpan) element("form:passwordErrors");
        assertNotNull(span);
        assertEquals("", span.asText());

    }


    /**
     * <p>Submit known-good username and password values, and
     * verify the correct response.</p>
     */
    public void testSuccessful() throws Exception {

        HtmlTextInput username = (HtmlTextInput) element("form:username");
        HtmlPasswordInput password = (HtmlPasswordInput)
            element("form:password");
        HtmlSubmitInput submit = (HtmlSubmitInput) element("form:submit");

        username.setValueAttribute("gooduser");
        password.setValueAttribute("goodpass");
        submit(submit);

        assertEquals("logon1", title());

    }


}
