/**
 * Copyright  2000-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * --------------------------------------------------------------------------
 * $Id: JunitFormatter.java,v 1.1 2005/02/16 14:24:38 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.ant;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.AssertionFailedError;
import junit.framework.Test;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitResultFormatter;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTestRunner;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitVersionHelper;
import org.apache.tools.ant.taskdefs.optional.junit.XMLConstants;
import org.apache.tools.ant.util.DOMElementWriter;

/**
 * Use our own formatter (based on XML formatter). This is to prefix test name
 * as all our tests have the same name but we run it with different
 * configurations Note : cannot extends super class as all is private !!!
 * Version comes from ANT 1.6.2 version Changes made are prefix by : // ###OW
 * changes
 */
public class JunitFormatter implements JUnitResultFormatter, XMLConstants {

    private static DocumentBuilder getDocumentBuilder() {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (Exception exc) {
            throw new ExceptionInInitializerError(exc);
        }
    }

    /**
     * The XML document.
     */
    private Document doc;

    /**
     * The wrapper for the whole testsuite.
     */
    private Element rootElement;

    /**
     * Element for the current test.
     */
    private Hashtable testElements = new Hashtable();

    /**
     * tests that failed.
     */
    private Hashtable failedTests = new Hashtable();

    /**
     * Timing helper.
     */
    private Hashtable testStarts = new Hashtable();

    //  ###OW changes
    /**
     * Name of test (test.name) property
     */
    private String testName = null;

    /**
     * Mode of test (test.mode) property (parallel1 or parallel2)
     */
    private String testMode = null;

    // #OW changes###

    /**
     * Where to write the log to.
     */
    private OutputStream out;

    public JunitFormatter() {
    }

    public void setOutput(OutputStream out) {
        this.out = out;
    }

    public void setSystemOutput(String out) {
        formatOutput(SYSTEM_OUT, out);
    }

    public void setSystemError(String out) {
        formatOutput(SYSTEM_ERR, out);
    }

    /**
     * The whole testsuite started.
     * @param suite the test suite
     */
    public void startTestSuite(JUnitTest suite) {
        doc = getDocumentBuilder().newDocument();
        rootElement = doc.createElement(TESTSUITE);
        rootElement.setAttribute(ATTR_NAME, suite.getName());

        // Output properties
        Element propsElement = doc.createElement(PROPERTIES);
        rootElement.appendChild(propsElement);
        Properties props = suite.getProperties();
        if (props != null) {
            // ###OW changes
            // get test.name property
            testName = props.getProperty("test.name");

            testMode = props.getProperty("test.mode", "parallel");

            if (testName != null) {
                rootElement.setAttribute(ATTR_NAME, suite.getName() + testName.replace('.', '-') + "-" + testMode);
            }
            // OW changes###

            Enumeration e = props.propertyNames();
            while (e.hasMoreElements()) {
                String name = (String) e.nextElement();
                Element propElement = doc.createElement(PROPERTY);
                propElement.setAttribute(ATTR_NAME, name);
                propElement.setAttribute(ATTR_VALUE, props.getProperty(name));
                propsElement.appendChild(propElement);
            }
        }
    }

    /**
     * The whole testsuite ended.
     * @param suite the test suite
     * @throws BuildException if it fails
     */
    public void endTestSuite(JUnitTest suite) throws BuildException {
        rootElement.setAttribute(ATTR_TESTS, "" + suite.runCount());
        rootElement.setAttribute(ATTR_FAILURES, "" + suite.failureCount());
        rootElement.setAttribute(ATTR_ERRORS, "" + suite.errorCount());
        rootElement.setAttribute(ATTR_TIME, "" + (suite.getRunTime() / 1000.0));
        if (out != null) {
            Writer wri = null;
            try {
                wri = new BufferedWriter(new OutputStreamWriter(out, "UTF8"));
                wri.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
                (new DOMElementWriter()).write(rootElement, wri, 0, "  ");
                wri.flush();
            } catch (IOException exc) {
                throw new BuildException("Unable to write log file", exc);
            } finally {
                if (out != System.out && out != System.err) {
                    if (wri != null) {
                        try {
                            wri.close();
                        } catch (IOException e) {
                            // ignore
                        }
                    }
                }
            }
        }
    }

    /**
     * Interface TestListener.
     * <p>
     * A new Test is started.
     * @param t test started
     */
    public void startTest(Test t) {
        testStarts.put(t, new Long(System.currentTimeMillis()));
    }

    /**
     * Interface TestListener.
     * <p>
     * A Test is finished.
     * @param test finished
     */
    public void endTest(Test test) {
        // Fix for bug #5637 - if a junit.extensions.TestSetup is
        // used and throws an exception during setUp then startTest
        // would never have been called
        if (!testStarts.containsKey(test)) {
            startTest(test);
        }

        Element currentTest = null;
        if (!failedTests.containsKey(test)) {
            currentTest = doc.createElement(TESTCASE);

            // ###OW changes
            String tmpTestName = JUnitVersionHelper.getTestCaseName(test);
            if (testName != null) {
                tmpTestName = testName + "-" + testMode + "-" + tmpTestName;
            }
            currentTest.setAttribute(ATTR_NAME, tmpTestName);
            // OW changes###

            // a TestSuite can contain Tests from multiple classes,
            // even tests with the same name - disambiguate them.
            currentTest.setAttribute(ATTR_CLASSNAME, test.getClass().getName());
            rootElement.appendChild(currentTest);
            testElements.put(test, currentTest);
        } else {
            currentTest = (Element) testElements.get(test);
        }

        Long l = (Long) testStarts.get(test);
        currentTest.setAttribute(ATTR_TIME, "" + ((System.currentTimeMillis() - l.longValue()) / 1000.0));
    }

    /**
     * Interface TestListener for JUnit &lt;= 3.4.
     * <p>
     * A Test failed.
     * @param test test which failed
     * @param t exception
     */
    public void addFailure(Test test, Throwable t) {
        formatError(FAILURE, test, t);
    }

    /**
     * Interface TestListener for JUnit &gt; 3.4.
     * <p>
     * A Test failed.
     * @param test test which failed
     * @param t the failure
     */
    public void addFailure(Test test, AssertionFailedError t) {
        addFailure(test, (Throwable) t);
    }

    /**
     * Interface TestListener.
     * <p>
     * An error occurred while running the test.
     * @param test test which failed
     * @param t the exception
     */
    public void addError(Test test, Throwable t) {
        formatError(ERROR, test, t);
    }

    /**
     * Format the given error
     * @param type of error
     * @param test test which made the error
     * @param t the exception
     */
    private void formatError(String type, Test test, Throwable t) {
        if (test != null) {
            endTest(test);
            failedTests.put(test, test);
        }

        Element nested = doc.createElement(type);
        Element currentTest = null;
        if (test != null) {
            currentTest = (Element) testElements.get(test);
        } else {
            currentTest = rootElement;
        }

        currentTest.appendChild(nested);

        String message = t.getMessage();
        if (message != null && message.length() > 0) {
            nested.setAttribute(ATTR_MESSAGE, t.getMessage());
        }
        nested.setAttribute(ATTR_TYPE, t.getClass().getName());

        String strace = JUnitTestRunner.getFilteredTrace(t);
        Text trace = doc.createTextNode(strace);
        nested.appendChild(trace);
    }

    /**
     * Format the output for a given type
     * @param type given type
     * @param output section to add
     */
    private void formatOutput(String type, String output) {
        Element nested = doc.createElement(type);
        rootElement.appendChild(nested);
        nested.appendChild(doc.createCDATASection(output));
    }
}