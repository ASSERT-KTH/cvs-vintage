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
 * Search for multiple whitespaces in Subject.
 * <p>
 * Example: "Buy this             sdf675sv758"
 * <p>
 * See the random characters add the end.
 * 
 * @author fdietz
 *
 */
public class SubjectWhitespaceRule extends AbstractRule{
    
    public SubjectWhitespaceRule() {
        super("SubjectWhitespaceRule");
    }
    /**
     * @see org.columba.mail.spam.rules.Rule#score(org.columba.mail.folder.Folder, java.lang.Object)
     */
    public float score(MessageFolder folder, Object uid) throws Exception {
        Header header = folder.getHeaderFields(uid, new String[]{"Subject"});
        String subject = header.get("Subject");
        if ( subject == null ) return NEARLY_ZERO;
        if ( subject.length() == 0 ) return NEARLY_ZERO;
        
        int count=0;
        boolean whitespace = false;
        for ( int i=0; i<subject.length(); i++) {
            char ch = subject.charAt(i);
            
            if ( ch == ' ') {
                // whitespace detected
                
                // if already detected before
                if ( whitespace ) count++;
                
                whitespace = true;
            } else {
                whitespace = false;
            }
        }
        
        System.out.println("count="+count);
        
        // check for at least 10 whitespaces
        if ( count > 10 ) return MAX_PROBABILITY;
        
        return NEARLY_ZERO;
    }
}
