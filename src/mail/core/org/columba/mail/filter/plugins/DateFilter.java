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

import java.util.Date;
import java.util.logging.Logger;


/**
 *
 * Search for a certain absolute Date
 *
 * @author fdietz
 */
public class DateFilter extends AbstractFilter {

    /** JDK 1.4+ logging framework logger, used for logging. */
    private static final Logger LOG = Logger.getLogger("org.columba.mail.filter.plugins");

    private String criteria;
    private String pattern;

    protected Date transformDate(String pattern) {
        java.text.DateFormat df = java.text.DateFormat.getDateInstance();
        Date searchPattern = null;

        try {
            searchPattern = df.parse(pattern);
        } catch (java.text.ParseException ex) {
            System.out.println("exception: " + ex.getMessage());
            ex.printStackTrace();

            //return new Vector();
        }

        return searchPattern;
    }

    /**
     * @see org.columba.mail.filter.plugins.AbstractFilter#process(java.lang.Object,
     *      org.columba.mail.folder.Folder, java.lang.Object,
     *      org.columba.core.command.WorkerStatusController)
     */
    public boolean process(MessageFolder folder, Object uid) throws Exception {
        // convert criteria into int-value
        int condition = FilterCriteria.getCriteria(criteria);

        // transform string to Date representation
        Date date = transformDate(pattern);

        boolean result = false;

        // get date
        Date d = (Date) folder.getAttribute(uid, "columba.date");

        if (d == null) {
            LOG.fine("field date not found");

            return false;
        }

        switch (condition) {
        case FilterCriteria.DATE_BEFORE:

            if (d.before(date)) {
                result = true;
            }

            break;

        case FilterCriteria.DATE_AFTER:

            if (d.after(date)) {
                result = true;
            }

            break;
        }

        return result;
    }

    /**
     * @see org.columba.mail.filter.plugins.AbstractFilter#setUp(org.columba.mail.filter.FilterCriteria)
     */
    public void setUp(FilterCriteria f) {
        // before/after
        criteria = f.get("criteria");

        // string to search
        pattern = f.get("pattern");
    }
}
