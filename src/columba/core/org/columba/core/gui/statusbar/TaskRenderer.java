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
package org.columba.core.gui.statusbar;

import org.columba.core.command.TaskManager;
import org.columba.core.command.Worker;
import org.columba.core.main.MainInterface;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;


/**
 * @author fdietz
 */
public class TaskRenderer extends DefaultListCellRenderer {
    private TaskManager tm;

    public TaskRenderer() {
        super();

        tm = MainInterface.processor.getTaskManager();
    }

    /* (non-Javadoc)
 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
 */
    public Component getListCellRendererComponent(JList arg0, Object arg1,
        int arg2, boolean arg3, boolean arg4) {
        super.getListCellRendererComponent(arg0, arg1, arg2, arg3, arg4);

        Worker worker = (Worker) arg1;
        setText(worker.getDisplayText());

        //return super.getListCellRendererComponent(arg0, arg1, arg2, arg3, arg4);
        return this;
    }
}
