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

import java.awt.Color;

import org.columba.core.xml.XmlElement;
import org.columba.mail.filter.FilterCriteria;
import org.columba.mail.folder.MailboxTstFactory;


/**
 * @author fdietz
 *
 */
public class ColorFilterTest extends AbstractFilterTestCase {

    /**
     * @param arg0
     */
    public ColorFilterTest(MailboxTstFactory factory, String arg0) {
        super(factory, arg0);
        
    }

    public void test() throws Exception {
        // add message to folder
        Object uid = addMessage();
        getSourceFolder().setAttribute(uid, "columba.color", Color.red);
        
        // create filter configuration
        FilterCriteria criteria = new FilterCriteria(new XmlElement("criteria"));
        criteria.setType("Color");
        criteria.setCriteria("is");
        criteria.set("rgb", Color.red.getRGB());

        // create filter
        ColorFilter filter = new ColorFilter();

        // init configuration
        filter.setUp(criteria);

        // execute filter
        boolean result = filter.process(getSourceFolder(), uid);
        assertEquals("filter result", true, result);
    }
}
