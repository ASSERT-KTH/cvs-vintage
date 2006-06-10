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

import org.columba.core.filter.AbstractFilter;
import org.columba.core.filter.FilterCriteria;
import org.columba.core.folder.IFolder;


/**
 * Simple filter matching all messages.
 *
 * @author fdietz
 */
public class MatchAllFilter extends AbstractFilter {
    /**
 * 
 */
    public MatchAllFilter() {
        super();
    }

    /**
 * @see org.columba.core.filter.AbstractFilter#setUp(org.columba.mail.filter.FilterCriteria)
 */
    public void setUp(FilterCriteria f) {
        // we don't need any configuration here
    }

    /**
 * @see org.columba.core.filter.AbstractFilter#process(IFolder, java.lang.Object)
 */
    public boolean process(IFolder folder, Object uid) throws Exception {
        // match all matches
        return true;
    }
}
