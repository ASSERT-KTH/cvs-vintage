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
import org.columba.mail.folder.Folder;

import org.columba.ristretto.message.Header;


/**
 * Search for a string in a certain headerfield.
 * <p>
 * "headerfield" is for example Subject <br>
 * "criteria" can be "contains" or "contains not" <br>
 * "pattern" specifies the search string <br>
 *
 * @author fdietz
 */
public class HeaderfieldFilter extends AbstractFilter {
    /**
     * Constructor for HeaderfieldFilter.
     *
     * @param filter
     */
    public HeaderfieldFilter(FilterCriteria filter) {
        super(filter);
    }

    /**
     *
     * Check if the requested headerfield contains the search string and return
     * true if match was found, otherwise return false
     *
     * @see org.columba.mail.filter.plugins.AbstractFilter#process(org.columba.mail.folder.Folder,
     *      java.lang.Object, org.columba.mail.filter.Filter)
     */
    public boolean process(Folder folder, Object uid) throws Exception {
        // contains/contains not
        String criteria = getFilterCriteria().get("criteria");

        // get headerfield to search in (for example: Subject)
        String headerfield = getFilterCriteria().get("headerfield");

        // string to search
        String pattern = getFilterCriteria().get("pattern");

        // get message header
        Header header = folder.getHeaderFields(uid, new String[] { headerfield });

        if (header == null) {
            return false;
        }

        String headerItem = (String) header.get(headerfield);

        // get condition and convert it to constant as defined in
        // FilterCriteria
        int condition = FilterCriteria.getCriteria(criteria);

        // see if theirs a match
        boolean result = match(headerItem, condition, pattern);

        return result;
    }

    /**
     *
     * check if a match exists in the requested headerfield
     *
     * @param headerItem
     *            String to specify headerfield (example:Subject)
     * @param condition
     *            contains, contains not
     * @param pattern
     *            search string
     *
     * @return boolean return true if match was found, otherwise return false
     */
    protected boolean match(String headerItem, int condition, String pattern) {
        boolean result = false;

        // skip if message doesn't contain the requested headerfield
        if (headerItem == null) {
            return false;
        }

        switch (condition) {
        case FilterCriteria.CONTAINS:

            if (headerItem.toLowerCase().indexOf(pattern.toLowerCase()) != -1) {
                result = true;
            }

            break;

        case FilterCriteria.CONTAINS_NOT:

            if (headerItem.toLowerCase().indexOf(pattern.toLowerCase()) == -1) {
                result = true;
            }

            break;

        case FilterCriteria.IS:

            if (headerItem.equalsIgnoreCase(pattern)) {
                result = true;
            }

            break;

        case FilterCriteria.IS_NOT:

            if (!headerItem.equalsIgnoreCase(pattern)) {
                result = true;
            }

            break;

        case FilterCriteria.BEGINS_WITH:

            if (headerItem.toLowerCase().startsWith(pattern.toLowerCase())) {
                result = true;
            }

            break;

        case FilterCriteria.ENDS_WITH:

            if (headerItem.toLowerCase().endsWith(pattern.toLowerCase())) {
                result = true;
            }

            break;
        }

        return result;
    }
}
