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
package org.columba.core.gui.logdisplay;

import java.awt.Color;
import java.awt.Component;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * A list cell renderer for the LogRecordList.
 *
 * @author redsolo
 */
public class LogRecordListRenderer extends JLabel implements ListCellRenderer {

    /**
     * Creates the list cell renderer.
     */
    public LogRecordListRenderer() {
        super();
        setOpaque(true);
    }

    /** {@inheritDoc} */
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

        LogRecord record = (LogRecord) value;

        if (record.getLevel() == Level.SEVERE) {
            setForeground(Color.RED);
        } else if (record.getLevel() == Level.WARNING) {
            setForeground(Color.ORANGE);
        } else {
            setForeground(list.getForeground());
        }

        if (isSelected) {
            setBackground(list.getSelectionBackground());
        } else {
            setBackground(list.getBackground());
        }

        StringBuffer buffer = new StringBuffer();
        /*
         * buffer.append(getShortClassName(record.getSourceClassName()));
         * buffer.append("."); buffer.append(record.getSourceMethodName());
         * buffer.append(" - ");
         */
        buffer.append(record.getMessage());

        setText(buffer.toString());

        return this;
    }

    /*private String getShortClassName(String className) {
        String name = "";

        if (className != null) {
            int lastPos = className.lastIndexOf('.');
            if (lastPos != -1) {
                name = className.substring(lastPos + 1);
            }
        }

        return name;
    }*/

    // The following methods override the defaults for performance reasons

    /** {@inheritDoc} */
    public void validate() {
    }

    /** {@inheritDoc} */
    public void revalidate() {
    }

    /** {@inheritDoc} */
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
    }

    /** {@inheritDoc} */
    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
    }

}
