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
package org.columba.mail.imap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


/**
 * Cuts a list/array of uids in pieces of certain size.
 *
 * @author fdietz
 */
public class MessageSetTokenizer implements Iterator {
    Object[] uids;
    List list;
    int index;
    int sizeOfPieces;
    int count;
    int rest;

    public MessageSetTokenizer(List l, int sizeOfPieces) {
        this.uids = l.toArray();

        this.sizeOfPieces = sizeOfPieces;

        index = 0;

        count = uids.length / sizeOfPieces;
        rest = uids.length % sizeOfPieces;

        list = new ArrayList(Arrays.asList(uids));
    }

    public MessageSetTokenizer(Object[] uids, int sizeOfPieces) {
        this.uids = uids;
        this.sizeOfPieces = sizeOfPieces;

        index = 0;

        count = uids.length / sizeOfPieces;
        rest = uids.length % sizeOfPieces;

        list = new ArrayList(Arrays.asList(uids));
    }

    /* (non-Javadoc)
 * @see java.util.Iterator#hasNext()
 */
    public boolean hasNext() {
        if (index < uids.length) {
            return true;
        }

        return false;
    }

    /* (non-Javadoc)
 * @see java.util.Iterator#next()
 */
    public Object next() {
        int i = sizeOfPieces;

        // calculate rest
        if ((index + sizeOfPieces) > uids.length) {
            i = uids.length - index;
        }

        List sublist = list.subList(index, index + i);

        index = index + i;

        return sublist;
    }

    /* (non-Javadoc)
 * @see java.util.Iterator#remove()
 */
    public void remove() {
        // not needed
    }
}
