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
package org.columba.mail.pgp;

import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import org.columba.mail.gui.util.PGPPassphraseDialog;
import org.waffel.jscf.JSCFConnection;
import org.waffel.jscf.JSCFException;
import org.waffel.jscf.JSCFStatement;


/**
 * Checks via a dialog if a passphrase (given from the user over the dialog) can be used to sign a testmessage. The dialog-test is
 * a while loop, which breaks only if the user cancels the dialog or the passphrase is correct.
 * @author waffel
 *
 */
public class PGPPassChecker {
    private static PGPPassChecker myInstance = null;
    private Map passwordMap = new Hashtable();

    /**
 * Returns the instance of the class. If no instance is created, a new instance are created.
 * @return a instance of this class.
 */
    public static PGPPassChecker getInstance() {
        if (myInstance == null) {
            myInstance = new PGPPassChecker();
        }

        //System.out.println("return Instance");
        return myInstance;
    }

    /**
 * Checks with a test string if the test String can be signed. The user is ask for his passphrase until the passphrase is ok or
 * the user cancels the dialog. If the user cancels the dialog the method returns false.
 * This method returned normal only if the user give the right passphrase-
 * @param con JSCFConnection used to check a passphrase given by a dialog 
 * @return Returns true if the given passphrase (via a dialog) is correct and the user can sign a teststring with the passphrase from
 * the dialog. Returns false if the user cancels the dialog. 
 * @exception JSCFException if the concrete JSCF implementation has real probelms (for example a extern tool cannot be found)     
 */
    public boolean checkPassphrase(JSCFConnection con)
        throws JSCFException {
        boolean stmtCheck = false;
        JSCFStatement stmt = con.createStatement();

        // loop until signing was sucessful or the user cancels the passphrase dialog
        Properties props = con.getProperties();

        while (!stmtCheck && (this.checkPassphraseDialog(con) == true)) {
            stmtCheck = stmt.checkPassphrase();

            if (!stmtCheck) {
                passwordMap.remove(props.get("USERID"));
            }
        }

        return stmtCheck;
    }

    private boolean checkPassphraseDialog(JSCFConnection con) {
        String passphrase = "";
        Properties props = con.getProperties();

        if (passwordMap.containsKey(props.get("USERID"))) {
            passphrase = (String) passwordMap.get(props.get("USERID"));
        }

        props.put("PASSWORD", passphrase);

        boolean ret = true;

        PGPPassphraseDialog dialog = new PGPPassphraseDialog();

        if (passphrase.length() == 0) {
            dialog.showDialog((String) props.get("USERID"),
                (String) props.get("PASSWORD"), false);

            if (dialog.success()) {
                passphrase = new String(dialog.getPassword(), 0,
                        dialog.getPassword().length);
                props.put("PASSWORD", passphrase);

                boolean save = dialog.getSave();

                // save passphrase in hash map
                if (save) {
                    passwordMap.put(props.get("USERID"), passphrase);
                }

                ret = true;
            } else {
                ret = false;
            }
        }

        con.setProperties(props);

        return ret;
    }
}
