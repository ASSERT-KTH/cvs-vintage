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


/**
 * Filter for account's uid.
 *
 * @author redsolo
 */
public class AccountFilter extends AbstractFilter {
    int criteriaCondition;
    int criteriaAccountUid;

    /**
 * @param f the filter criteria
 */
    public AccountFilter() {
        super();
    }

    /** {@inheritDoc} */
    public boolean process(MessageFolder folder, Object uid) throws Exception {
        boolean result = false;

        Integer messageAccountUid = (Integer) folder.getAttribute(uid,
                "columba.accountuid");

        if ((messageAccountUid != null) && (criteriaAccountUid != -1)) {
            int id = messageAccountUid.intValue();

            if ((criteriaCondition == FilterCriteria.IS) &&
                    (criteriaAccountUid == id)) {
                result = true;
            } else if ((criteriaCondition == FilterCriteria.IS_NOT) &&
                    (criteriaAccountUid != id)) {
                result = true;
            }
        }

        return result;
    }

    /**
 * @see org.columba.mail.filter.plugins.AbstractFilter#setUp(org.columba.mail.filter.FilterCriteria)
 */
    public void setUp(FilterCriteria f) {
        criteriaCondition = FilterCriteria.getCriteria(f.getCriteriaString());
        criteriaAccountUid = f.getInteger("account.uid", -1);
    }
}
