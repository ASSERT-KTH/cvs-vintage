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
 * Find messages with certain size.
 *
 * @author fdietz
 */
public class SizeFilter extends AbstractFilter {
    private String criteria;
    private String pattern;

    /**
 * Constructor for SizeFilter.
 */
    public SizeFilter() {
        super();
    }

    /**
 * Transform string to integer representation
 *
 * @param p             string containing priority
 *
 * @return              integer representation of string
 */
    protected Integer transformSize(String p) {
        Integer searchPattern = Integer.valueOf(p);

        return searchPattern;
    }

    /**
 * @see org.columba.mail.filter.plugins.AbstractFilter#process(java.lang.Object,
 *      org.columba.mail.folder.Folder, java.lang.Object,
 *      org.columba.core.command.WorkerStatusController)
 */
    public boolean process(MessageFolder folder, Object uid) throws Exception {
        boolean result = false;

        int condition = FilterCriteria.getCriteria(criteria);
        Integer size = transformSize((String) pattern);

        Integer s = (Integer) folder.getAttribute(uid, "columba.size");

        if (s == null) {
            return false;
        }

        switch (condition) {
        case FilterCriteria.SIZE_SMALLER:

            if (size.compareTo(s) > 0) {
                result = true;
            }

            break;

        case FilterCriteria.SIZE_BIGGER:

            if (size.compareTo(s) < 0) {
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
        //      bigger/smaller
        criteria = f.get("criteria");

        // string to search
        pattern = f.get("pattern");
    }
}
