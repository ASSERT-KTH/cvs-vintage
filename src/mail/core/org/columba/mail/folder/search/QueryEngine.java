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

package org.columba.mail.folder.search;

import org.columba.mail.filter.FilterRule;

import java.util.List;

/**
 * Custom implementation for performing optimized search requests.
 *
 * @author fdietz
 */
public interface QueryEngine {
    /**
 * Get list of capabilities.
 * <p>
 * All supported search requests as for example:
 * "Body", "Subject", etc.
 *
 * @return                capability list
 */
    String[] getCaps();

    /**
 * Sync engine with folder data.
 * <p>
 * This can become necessary if the folder data is
 * inconsistent.
 *
 * @throws Exception
 */
    void sync() throws Exception;

    /**
 * Execute a list of {@link FilterCriteria}.
 *
 * @param filter                list of filter criteria
 * @return                                list of matching message UIDs
 * @throws Exception
 */
    List queryEngine(FilterRule filter) throws Exception;

    /**
 * Execute a list of {@link FilterCriteria}.
 * <p>
 * Perform search request on a subset of messages only.
 *
 * @param filter                list of filter criteria
 * @param uids                        list of UIDs to perform search request
 * @return                                list of matching message UIDs
 * @throws Exception
 */
    List queryEngine(FilterRule filter, Object[] uids)
        throws Exception;

    /**
 * Notify search engine that a message was added.
 *
 * @param message                message
 * @throws Exception
 */
    void messageAdded(Object uid) throws Exception;

    /**
 * Notify search engine that a message was removed
 *
 * @param uid                        message UID
 * @throws Exception
 */
    void messageRemoved(Object uid) throws Exception;

    /**
 * Reset search engine.
 *
 * @throws Exception
 */
    void reset() throws Exception;
}
