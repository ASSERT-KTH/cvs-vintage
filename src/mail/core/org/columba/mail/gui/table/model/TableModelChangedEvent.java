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
package org.columba.mail.gui.table.model;

import org.columba.mail.folder.AbstractFolder;
import org.columba.mail.message.HeaderList;


/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class TableModelChangedEvent {
    public final static int UPDATE = 0;
    public final static int SET = 1;
    public final static int REMOVE = 2;
    public final static int MARK = 3;
    protected AbstractFolder srcFolder;
    protected Object[] uids;
    protected int markVariant;
    protected int eventType;
    protected HeaderList headerList;

    /**
 * Constructor for TableChangedEvent.
 */
    public TableModelChangedEvent(int eventType) {
        this.eventType = eventType;
    }

    public TableModelChangedEvent(int eventType, AbstractFolder srcFolder) {
        this.eventType = eventType;
        this.srcFolder = srcFolder;
    }

    public TableModelChangedEvent(int eventType, AbstractFolder srcFolder,
        Object[] uids) {
        this.eventType = eventType;
        this.srcFolder = srcFolder;
        this.uids = uids;
    }

    public TableModelChangedEvent(int eventType, AbstractFolder srcFolder,
        HeaderList headerList) {
        this.eventType = eventType;
        this.srcFolder = srcFolder;
        this.headerList = headerList;
    }

    public TableModelChangedEvent(int eventType, AbstractFolder srcFolder,
        Object[] uids, int markVariant) {
        this.eventType = eventType;
        this.srcFolder = srcFolder;
        this.uids = uids;
        this.markVariant = markVariant;
    }

    /**
 * Returns the markVariant.
 * @return int
 */
    public int getMarkVariant() {
        return markVariant;
    }

    /**
 * Returns the srcFolder.
 * @return FolderTreeNode
 */
    public AbstractFolder getSrcFolder() {
        return srcFolder;
    }

    /**
 * Returns the uids.
 * @return Object[]
 */
    public Object[] getUids() {
        return uids;
    }

    /**
 * Returns the eventType.
 * @return int
 */
    public int getEventType() {
        return eventType;
    }

    /**
 * Returns the headerList.
 * @return HeaderInterface[]
 */
    public HeaderList getHeaderList() {
        return headerList;
    }
}
