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
package org.columba.mail.spam.rules;

import java.util.Iterator;

import org.columba.mail.folder.MessageFolder;
import org.columba.ristretto.message.MimeHeader;
import org.columba.ristretto.message.MimePart;
import org.columba.ristretto.message.MimeTree;
import org.columba.ristretto.message.MimeType;

/**
 * Check if message contains only HTML mimeparts.
 * 
 * @author fdietz
 */
public class OnlyHTMLMimepartRule extends AbstractRule {

    public OnlyHTMLMimepartRule() {
        super("OnlyHTMLMimepartRule");

    }

    /**
     * @see org.columba.mail.spam.rules.Rule#score(org.columba.mail.folder.MessageFolder,
     *      java.lang.Object)
     */
    public float score(MessageFolder folder, Object uid) throws Exception {
        MimeTree tree = folder.getMimePartTree(uid);

        Iterator it = tree.getAllLeafs().iterator();
        boolean onlyHTML = true;
        while (it.hasNext()) {
            MimePart mp = (MimePart) it.next();
            MimeHeader header = mp.getHeader();
            MimeType mimetype = header.getMimeType();

            if ((mimetype.getType().equals("text"))
                    && (mimetype.getSubtype().equals("html"))) {

            } else {
                onlyHTML = false;
            }
        }

        if (onlyHTML) return MAX_PROBABILITY;

        return NEARLY_ZERO;
    }

}