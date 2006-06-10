/*
 The contents of this file are subject to the Mozilla Public License Version 1.1
 (the "License"); you may not use this file except in compliance with the License.
 You may obtain a copy of the License at http://www.mozilla.org/MPL/

 Software distributed under the License is distributed on an "AS IS" basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 for the specific language governing rights and limitations under the License.

 The Original Code is "The Columba Project"

 The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
 Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.

 All Rights Reserved.
 */
package org.columba.core.gui.scripting;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import org.columba.core.scripting.ScriptLogger;

/**
    @author Celso Pinto (cpinto@yimports.com)
 */
public class ScriptLogTableModel
    extends AbstractTableModel
    implements Observer
{

    private static final String
        RES_MESSAGE_COLUMN = "Message";

    private String[] columns = new String[]
        {
            RES_MESSAGE_COLUMN
        };

    public static final int
        MESSAGE_COLUMN = 0;

    private List<ScriptLogger.LogEntry> logList;

    public ScriptLogTableModel()
    {
        logList = new ArrayList<ScriptLogger.LogEntry>();
        logList.addAll(ScriptLogger.getInstance().dumpCurrentLog());
        ScriptLogger.getInstance().addObserver(this);
    }

    public void clearLog()
    {
        logList.clear();
        ScriptLogger.getInstance().clear();
        fireTableChangedEv();
    }

    public int getColumnCount()
    {
        return columns.length;
    }

    public int getRowCount()
    {
        return logList.size();
    }

    public String getColumnName(int col)
    {
        return columns[col];
    }

    public Class getColumnClass(int columnIndex)
    {
        if (columnIndex == 0) return ScriptLogger.LogEntry.class;
        else return super.getColumnClass(columnIndex);
    }

    public Object getValueAt(int row, int col)
    {
        switch (col)
        {
            case MESSAGE_COLUMN:
                return logList.get(row);
            default:
                return "";
        }

    }

    private void fireTableChangedEv()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                fireTableDataChanged();
            }
        });
    }

    public void update(Observable o, Object arg)
    {
        fireTableChangedEv();
    }

    public void dispose()
    {
        ScriptLogger.getInstance().deleteObserver(this);
    }

}
