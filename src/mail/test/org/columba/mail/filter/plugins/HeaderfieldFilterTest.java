// The contents of this file are subject to the Mozilla Public License Version
// 1.1
//(the "License"); you may not use this file except in compliance with the
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.mail.filter.plugins;

import org.columba.core.xml.XmlElement;

import org.columba.mail.filter.FilterCriteria;
import org.columba.mail.folder.MailboxTstFactory;


/**
 * @author fdietz
 *  
 */
public class HeaderfieldFilterTest extends AbstractFilterTestCase {
    /**
 * Constructor for HeaderfieldFilterTest.
 * 
 * @param arg0
 */
    public HeaderfieldFilterTest(MailboxTstFactory factory, String arg0) {
        super(factory, arg0);
        
    }

    /**
     * Check if Subject: contains "test"
     * 
     * @throws Exception
     */
    public void testSubjectContainsFilter() throws Exception {
        // add message to folder
        Object uid = addMessage();

        // !!! Subject: test
        // create filter configuration
        // -> check if <Subject> <contains> pattern <test>
        FilterCriteria criteria = new FilterCriteria(new XmlElement("criteria"));
        criteria.setType("Subject");
        criteria.setPattern("test");
        criteria.setHeaderItem("Subject");
        criteria.setCriteria("contains");

        // create filter
        HeaderfieldFilter filter = new HeaderfieldFilter();

        // init configuration
        filter.setUp(criteria);

        // execute filter
        boolean result = filter.process(getSourceFolder(), uid);
        assertEquals("filter result", true, result);
    }
    
    /**
     * Check if Subject: contains "pudding" fails as expected
     * 
     * @throws Exception
     */
    public void testSubjectContainsFailedFilter() throws Exception {
        // add message to folder
        Object uid = addMessage();

        // !!! Subject: test
        // create filter configuration
        // -> check if <Subject> <contains> pattern <test>
        FilterCriteria criteria = new FilterCriteria(new XmlElement("criteria"));
        criteria.setType("Subject");
        criteria.setPattern("pudding");
        criteria.setHeaderItem("Subject");
        criteria.setCriteria("contains");

        // create filter
        HeaderfieldFilter filter = new HeaderfieldFilter();

        // init configuration
        filter.setUp(criteria);

        // execute filter
        boolean result = filter.process(getSourceFolder(), uid);
        assertEquals("filter result", false, result);
    }
    
    /**
     * Check if Subject: contains not "pudding" works
     * 
     * @throws Exception
     */
    public void testSubjectContainsNotFilter() throws Exception {
        // add message to folder
        Object uid = addMessage();

        // !!! Subject: test
        // create filter configuration
        // -> check if <Subject> <contains> pattern <test>
        FilterCriteria criteria = new FilterCriteria(new XmlElement("criteria"));
        criteria.setType("Subject");
        criteria.setPattern("pudding");
        criteria.setHeaderItem("Subject");
        criteria.setCriteria("contains not");

        // create filter
        HeaderfieldFilter filter = new HeaderfieldFilter();

        // init configuration
        filter.setUp(criteria);

        // execute filter
        boolean result = filter.process(getSourceFolder(), uid);
        assertEquals("filter result", true, result);
    }

    /**
     * Check if From: contains "alice@mail.org"
     * @throws Exception
     */
    public void testFromFilter() throws Exception {
        // add message to folder
        Object uid = addMessage();
        getSourceFolder().setAttribute(uid, "From", "alice@mail.org");

        // !!! From: alice@mail.org
        // create filter configuration
        // -> check if <From> <contains> pattern <alice@mail.org>
        FilterCriteria criteria = new FilterCriteria(new XmlElement("criteria"));
        criteria.setType("From");
        criteria.setPattern("alice@mail.org");
        criteria.setHeaderItem("From");
        criteria.setCriteria("contains");

        // create filter
        HeaderfieldFilter filter = new HeaderfieldFilter();

        // init configuration
        filter.setUp(criteria);

        // execute filter
        boolean result = filter.process(getSourceFolder(), uid);
        assertEquals("filter result", true, result);
    }
    
    /**
     * Check if X-Mailer contains "Columba" using the custom headerfield filter.
     * 
     * @throws Exception
     */
    public void testCustomHeaderFilter() throws Exception {
        // add message to folder
        Object uid = addMessage();

        // !!! From: alice@mail.org
        // create filter configuration
        // -> check if <From> <contains> pattern <alice@mail.org>
        FilterCriteria criteria = new FilterCriteria(new XmlElement("criteria"));
        criteria.setType("Custom Headerfield");
        criteria.setPattern("Columba");
        criteria.setHeaderItem("X-Mailer");
        criteria.setCriteria("contains");

        // create filter
        HeaderfieldFilter filter = new HeaderfieldFilter();

        // init configuration
        filter.setUp(criteria);

        // execute filter
        boolean result = filter.process(getSourceFolder(), uid);
        assertEquals("filter result", true, result);
    }
}
