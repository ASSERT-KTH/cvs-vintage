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

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.ListCellRenderer;

import org.columba.core.command.TaskManager;
import org.columba.core.command.Worker;
import org.columba.core.main.MainInterface;


/**
 * Custom renderer using a JLabel for status messages and
 * a JProgressBar for worker progress.
 * 
 * @author fdietz
 */
public class TaskRenderer extends JPanel implements ListCellRenderer {
    private TaskManager tm;

    private JLabel label;
    private JProgressBar progressBar;
    private JPanel progressPanel;
    
    public TaskRenderer() {
        super();

        tm = MainInterface.processor.getTaskManager();
        
        setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
        
        label = new JLabel();
        progressBar = new JProgressBar();
        progressPanel = new JPanel();
        progressPanel.setLayout(new BorderLayout());
        progressPanel.setBorder(BorderFactory.createEmptyBorder(2,10,2,10));
        progressPanel.add(progressBar, BorderLayout.CENTER);
        
        setLayout(new BorderLayout());
        add(label, BorderLayout.NORTH);
        add(progressPanel, BorderLayout.CENTER);
    }

    /* (non-Javadoc)
 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
 */
    public Component getListCellRendererComponent(JList list, Object value,
        int index, boolean isSelected, boolean hasFocus) {
       
    	if ( isSelected) {
    		setBackground(list.getSelectionBackground());
    		setForeground(list.getSelectionForeground());
    		progressPanel.setBackground(list.getSelectionBackground());
    		progressPanel.setForeground(list.getSelectionForeground());
    	} else {
    		setBackground(list.getBackground());
    		setForeground(list.getForeground());
    		progressPanel.setBackground(list.getBackground());
			progressPanel.setForeground(list.getForeground());
    	}

        Worker worker = (Worker) value;
        label.setText(worker.getDisplayText());
        progressBar.setMaximum(worker.getProgessBarMaximum());
        progressBar.setValue(worker.getProgressBarValue());

        //return super.getListCellRendererComponent(arg0, arg1, arg2, arg3, arg4);
        return this;
    }
}
