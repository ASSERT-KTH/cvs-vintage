/*
 * Created on 2003-nov-20
 */
package org.columba.core.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import junit.framework.TestCase;


/**
 * @author Erik Mattsson
 */
public class XmlIOTest extends TestCase {
    /**
 * Test for writing a Xml Element that has been passed in the constructor XmlIO(XmlElement).
 * @throws IOException thrown if the test fails.
 */
    public void testXmlElement() throws IOException {
        // Setup the XML that is to be written
        XmlElement expected = new XmlElement("big_name");
        expected.addAttribute("anattr", "avalue");
        expected.addAttribute("other", "value");

        XmlElement child1 = new XmlElement("child1");
        child1.addAttribute("othername", "nooname");
        child1.addAttribute("onemore", "ok");
        expected.addElement(child1);
        expected.addElement(new XmlElement("child2"));

        XmlIO xmlIO = new XmlIO(expected);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        xmlIO.write(baos);

        xmlIO = new XmlIO();
        assertTrue("Could not parse the written XML",
            xmlIO.load(new ByteArrayInputStream(baos.toByteArray())));

        XmlElement actual = xmlIO.getRoot().getElement(0);
        assertEquals("The original and the written XML element are not equal",
            expected, actual);
    }

    /**
 * Test the load(InputStream) method.
 */
    public void testReadInputStream() {
        String expected = "<xml attr=\"one\" secAttr=\"two\"><child name=\"other\"/></xml>";
        XmlIO xmlIO = new XmlIO();
        assertTrue("The XML could not be loaded",
            xmlIO.load(new ByteArrayInputStream(expected.getBytes())));

        XmlElement actualXml = xmlIO.getRoot().getElement("xml");
        assertEquals("Name isnt correct", "xml", actualXml.getName());
        assertEquals("The first attribute isnt correct", "one",
            actualXml.getAttribute("attr"));
        assertEquals("The second attribute isnt correct", "two",
            actualXml.getAttribute("secAttr"));

        XmlElement child = actualXml.getElement(0);
        assertEquals("The child name isnt correct", "child", child.getName());
        assertEquals("The childs first attribute isnt correct", "other",
            child.getAttribute("name"));
    }
}
