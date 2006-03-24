/*

The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License") you may not use this file except in compliance with the License. 

You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.

The Original Code is "BshInterpreter plugin for The Columba Project"

The Initial Developer of the Original Code is Celso Pinto
Portions created by Celso Pinto are Copyright (C) 2005.
Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.

All Rights Reserved.

*/
package org.columba.core.gui.scripting;

import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import org.columba.core.scripting.IScriptsObserver;
import org.columba.core.scripting.model.ColumbaScript;

/**
    @author Celso Pinto (cpinto@yimports.com)
 */
public class ScriptsTableModel
    extends AbstractTableModel
    implements IScriptsObserver
{

    private static final String
        RES_NAME_COLUMN = "Script name",
        RES_AUTHOR_COLUMN = "Author",
        RES_DESCRIPTION_COLUMN = "Description";

    private String[] columns = new String[]
        {
            RES_NAME_COLUMN,
            RES_AUTHOR_COLUMN,
            RES_DESCRIPTION_COLUMN
        };

    public static final int
        NAME_COLUMN = 0,
        AUTHOR_COLUMN = 1,
        DESCRIPTION_COLUMN = 2;

    private List scriptList;

    private ScriptManagerDocument document;

    public ScriptsTableModel(ScriptManagerDocument doc)
    {

        document = doc;
        document.addObserver(this);

        scriptList = document.getScripts();
    }

    public int getColumnCount()
    {
        return columns.length;
    }

    public int getRowCount()
    {
        return scriptList.size();
    }

    public String getColumnName(int col)
    {
        return columns[col];
    }


    public Class getColumnClass(int columnIndex)
    {
        if (columnIndex == 0) return ColumbaScript.class;
        else return super.getColumnClass(columnIndex);
    }

    public Object getValueAt(int row, int col)
    {
        ColumbaScript script = (ColumbaScript) scriptList.get(row);
        switch (col)
        {
            case NAME_COLUMN:
                return script;
            case AUTHOR_COLUMN:
                return script.getAuthor();
            case DESCRIPTION_COLUMN:
                return script.getDescription();
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

    public void scriptsAdded(List scripts)
    {
        scriptList.addAll(scripts);
        fireTableChangedEv();
    }

    public void scriptsRemoved(List scripts)
    {
        scriptList.removeAll(scripts);
        fireTableChangedEv();
    }

    public void scriptsChanged(List scripts)
    {
        fireTableChangedEv();
    }

}
