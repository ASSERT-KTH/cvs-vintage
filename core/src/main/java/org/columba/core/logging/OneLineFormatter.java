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
package org.columba.core.logging;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;



/**
 * OneLineFormatter is a LogFormatter for the Java.util.logging logging framework.
 * The class formts incoming LogRecords and displays only the message and the timestamp.
 * Using this class outputs a oneline log that looks like this:
 * "01/12/2002 22:00 - MESSAGE"
 *
 * @author redsolo
 */
public class OneLineFormatter extends Formatter {

    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd/MM/yyyy kk:mm");

    /**
     * Formatting the LogRecord into "dd/mm/yyyy hh:mm - MESSAGE"
     * @param rec The LogRecord to format.
     * @return The LogRecord as a formatted String
     */
    public String format(LogRecord rec) {
        StringBuffer buf = new StringBuffer(1000);
        buf.append(DATE_FORMATTER.format(new Date(rec.getMillis())));
        buf.append(" - ");
        buf.append(rec.getMessage());
        buf.append('\n');
        return buf.toString();
    }
}
