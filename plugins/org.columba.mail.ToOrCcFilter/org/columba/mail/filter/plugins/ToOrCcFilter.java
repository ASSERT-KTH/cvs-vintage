//The contents of this file are subject to the Mozilla Public License Version 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.

package org.columba.mail.filter.plugins;

import org.columba.mail.filter.FilterCriteria;
import org.columba.mail.folder.MessageFolder;

import org.columba.ristretto.message.Header;

/**
 * This FilterPlugin searches every To and Cc headerfield
 * of an occurence of a search string and combines the result
 * with an logical OR operation
 *
 * @author fdietz
 */
public class ToOrCcFilter extends HeaderfieldFilter {
    private String criteria;
    private String pattern;

    /** {@inheritDoc} */
    public boolean process(MessageFolder folder, Object uid) throws Exception {
        // get the header of the message
        Header header = folder.getHeaderFields(uid, new String[] {"To", "Cc"});

        boolean result = false;
        if (header != null) {
            // convert the condition string to an int which is easier to handle
            int condition = FilterCriteria.getCriteria(criteria);

            // get the "To" headerfield from the header
            String to = (String) header.get("To");

            // get the "Cc" headerfield from the header
            String cc = (String) header.get("Cc");

            // test if our To headerfield contains or contains not the search string
            result = match(to, condition, pattern);

            // do the same for the Cc headerfield and OR the results
            result |= match(cc, condition, pattern);

            // return the result as boolean value true or false
        }
        return result;
    }

    /**
     *  {@inheritDoc}
     */
    public void setUp(FilterCriteria filterCriteria) {
        //  before/after
        criteria = filterCriteria.get("criteria");

        // string to search
        pattern = filterCriteria.get("pattern");
    }
}
