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
 * DebugFormatter is a LogFormatter for the Java.util.logging logging framework.
 * Using this class outputs a oneline log that looks like this:
 * "01/12/2002 22:00 [Classname.Methoname()] MESSAGE"
 *
 * @author redsolo
 */
public class DebugFormatter extends Formatter {

    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd/MM/yyyy kk:mm:ss");

    /**
     * Format the given log record and return the formatted string.
     *
     * @param record the log record to be formatted
     * @return the formatted log record
     */
    public String format(LogRecord record) {
        StringBuffer string = new StringBuffer();

        String className = record.getSourceClassName();
        if (className != null) {
            int lastPos = className.lastIndexOf('.');
            if (lastPos != -1) {
                className = className.substring(lastPos + 1);
            }
        } else {
            className = "unknown";
        }

        string.append(DATE_FORMATTER.format(new Date(record.getMillis())));
        string.append(" [");
        string.append(className);
        string.append(".");
        string.append(record.getSourceMethodName());

        string.append("()] ");
        string.append(record.getMessage());
        string.append("\n");

        return string.toString();
    }
}
