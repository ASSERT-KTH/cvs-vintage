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

import org.columba.mail.folder.MessageFolder;
import org.columba.ristretto.message.Header;

/**
 * Check if From: header contains many digits.
 * 
 * @author fdietz
 *  
 */
public class MixedCharactersAddressRule extends AbstractRule {

    public MixedCharactersAddressRule() {
        super("MixedCharactersAddressRule");

    }

    /**
     * @see org.columba.mail.spam.rules.Rule#score(org.columba.mail.folder.MessageFolder,
     *      java.lang.Object)
     */
    public float score(MessageFolder folder, Object uid) throws Exception {
        Header header = folder.getHeaderFields(uid, new String[] { "From"});
        String from = header.get("From");
        if (from == null) return NEARLY_ZERO;
        if (from.length() == 0) return NEARLY_ZERO;

        int count = 0;
        for (int i = 0; i < from.length(); i++) {
            if (Character.isDigit(from.charAt(i))) count++;
        }

        // if 1/3 of all characters are digits
        if ( count > from.length()/3) return MAX_PROBABILITY;
            
        return NEARLY_ZERO;
    }

}