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
package org.columba.mail.folder.outbox;

import java.util.List;

import org.columba.mail.folder.AbstractHeaderListStorage;
import org.columba.mail.folder.LocalFolder;
import org.columba.mail.folder.headercache.AbstractHeaderCache;
import org.columba.mail.folder.headercache.LocalHeaderCache;
import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.SendableHeader;

/**
 * @author fdietz
 *  
 */
public class OutboxHeaderListStorage extends AbstractHeaderListStorage {

    protected AbstractHeaderCache headerCache;

    protected LocalFolder folder;

    /**
     *  
     */
    public OutboxHeaderListStorage(LocalFolder folder) {
        super();

        this.folder = folder;
    }

    /**
     * @see org.columba.mail.folder.AbstractHeaderListStorage#getHeaderCacheInstance()
     */
    public AbstractHeaderCache getHeaderCacheInstance() {
        if (headerCache == null) {
            headerCache = new OutboxHeaderCache(folder);
        }

        return headerCache;
    }

    class OutboxHeaderCache extends LocalHeaderCache {

        public OutboxHeaderCache(LocalFolder folder) {
            super(folder);
        }

        public ColumbaHeader createHeaderInstance() {
            return new SendableHeader();
        }

        protected void loadHeader(ColumbaHeader h) throws Exception {
            super.loadHeader(h);

            Integer accountUid = (Integer) reader.readObject();
            h.getAttributes().put("columba.accountuid", accountUid);

            List recipients = (List) reader.readObject();
            h.getAttributes().put("columba.recipients", recipients);
            ;
        }

        protected void saveHeader(ColumbaHeader h) throws Exception {
            super.saveHeader(h);

            writer.writeObject(h.getAttributes().get("columba.accountuid"));

            writer.writeObject(h.getAttributes().get("columba.recipients"));
        }
    }

}