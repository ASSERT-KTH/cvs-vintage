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

import java.io.InputStream;

import org.columba.core.io.StreamUtils;
import org.columba.mail.filter.FilterCriteria;
import org.columba.mail.folder.MessageFolder;


/**
 * Search for a certain string in the message body.
 *
 * @author fdietz
 */
public class BodyFilter extends AbstractFilter {
    private String pattern;
    private String criteria;

    /**
     * @see org.columba.mail.filter.plugins.AbstractFilter#process(java.lang.Object,
     *          org.columba.mail.folder.Folder, java.lang.Object,
     *          org.columba.core.command.WorkerStatusController)
     */
    public boolean process(MessageFolder folder, Object uid) throws Exception {
        // get message body

        InputStream messageSourceStream = folder.getMessageSourceStream(uid);
        StringBuffer body = StreamUtils.readInString(messageSourceStream);
        messageSourceStream.close();

        // convert criteria into int-value
        int condition = FilterCriteria.getCriteria(criteria);

        String bodyText = pattern;

        boolean result = false;

        switch (condition) {
        case FilterCriteria.CONTAINS:

            if (body.indexOf(bodyText) != -1) {
                result = true;
            }

            break;

        case FilterCriteria.CONTAINS_NOT:

            if (body.indexOf(bodyText) == -1) {
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
        // contains/contains not
        criteria = f.get("criteria");

        // string to search
        pattern = f.get("pattern");
    }
}
