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

import org.columba.mail.filter.FilterCriteria;
import org.columba.mail.folder.MessageFolder;


/**
 * Search for a certain priority.
 * <p>
 * This can be for example "high", "log", "highest", "lowest"
 *
 * @author fdietz
 */
public class PriorityFilter extends AbstractFilter {
    private String criteria;
    private String searchPattern;

    /**
 * Constructor for PriorityFilter.
 */
    public PriorityFilter() {
        super();
    }

    /**
 * Transform priority to integer value.
 *
 * @param pattern       priority string
 * @return              integer representation of string
 */
    protected Integer transformPriority(String pattern) {
        Integer searchPatternInt = new Integer(3);

        if (pattern.equalsIgnoreCase("Highest")) {
            searchPatternInt = new Integer(1);
        } else if (pattern.equalsIgnoreCase("High")) {
            searchPatternInt = new Integer(2);
        } else if (pattern.equalsIgnoreCase("Normal")) {
            searchPatternInt = new Integer(3);
        } else if (pattern.equalsIgnoreCase("Low")) {
            searchPatternInt = new Integer(4);
        } else if (pattern.equalsIgnoreCase("Lowest")) {
            searchPatternInt = new Integer(5);
        }

        //Integer priority = Integer.valueOf(pattern);
        //return priority;
        return searchPatternInt;
    }

    /**
 * @see org.columba.mail.filter.plugins.AbstractFilter#process(java.lang.Object,
 *      org.columba.mail.folder.Folder, java.lang.Object,
 *      org.columba.core.command.WorkerStatusController)
 */
    public boolean process(MessageFolder folder, Object uid) throws Exception {
        boolean result = false;

        int condition = FilterCriteria.getCriteria(criteria);
        String s = (String) searchPattern;
        Integer searchPatternInt = transformPriority(s);

        Integer priority = (Integer) folder.getAttribute(uid, "columba.priority");

        if (priority == null) {
            return false;
        }

        switch (condition) {
        case FilterCriteria.IS:

            if (priority.compareTo(searchPatternInt) == 0) {
                result = true;
            }

            break;

        case FilterCriteria.IS_NOT:

            if (priority.compareTo(searchPatternInt) != 0) {
                result = true;
            }

            break;
        }

        return result;
    }

    /**
 *
 * @see org.columba.mail.filter.plugins.AbstractFilter#setUp(org.columba.mail.filter.FilterCriteria)
 */
    public void setUp(FilterCriteria f) {
        // is/is not
        criteria = f.get("criteria");

        // string to search
        searchPattern = f.get("pattern");
    }
}
