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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import net.javaprog.ui.wizard.plaf.basic.SingleSideEtchedBorder;

import org.columba.core.scripting.model.ColumbaScript;


public class ScriptManager
  extends JDialog
  implements  ActionListener
{

  private static final Logger LOG = 
    Logger.getLogger(ScriptManager.class.getName());
  

  /*TODO move resources to a resource file */
  private static final String 
    RES_WINDOW_TITLE = "Macros",
    RES_EDIT_BUTTON = "Edit",
    RES_REFRESH_BUTTON = "Refresh",
    RES_REMOVE_BUTTON = "Remove",
    RES_CLOSE_BUTTON = "Close";
  
  private static final String 
    EDIT_SCRIPT_ACTION = "edit",
    REMOVE_SCRIPT_ACTION = "remove",
    REFRESH_LIST_ACTION = "refresh",
    CLOSE_ACTION = "close";
  
  private JTable scriptsList;
  private JButton editScriptButton,
                  removeScriptButton,
                  refreshListButton;
    
  
  private ScriptManagerDocument document;
  
  public ScriptManager(Frame parent,ScriptManagerDocument doc)
  {
    super(parent,RES_WINDOW_TITLE,true);
    document = doc;
    
    initGui();
    setLocationRelativeTo(getParent());
    
  }
  
  private void initGui()
  {
    
    JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT,0,0));
    bottomPanel.setBorder(new SingleSideEtchedBorder(SwingConstants.TOP));
    bottomPanel.add(createDialogActionsPanel());
    
    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(createMainPanel(),BorderLayout.CENTER);
    getContentPane().add(bottomPanel,BorderLayout.SOUTH);
    
    pack();
  }

  private JPanel createMainPanel()
  {
  
    JPanel mainPanel = new JPanel(new BorderLayout(10,0));
    mainPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
    mainPanel.add(createScriptList(),BorderLayout.CENTER);
    mainPanel.add(createScriptActionsPanel(),BorderLayout.EAST);  
    
    return mainPanel;
    
  }
  
  private JPanel createScriptList()
  {
    JPanel panel = new JPanel(new BorderLayout());
    
    scriptsList = new JTable(new ScriptsTableModel(document));
    
    scriptsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    scriptsList.setShowGrid(false);
    scriptsList.sizeColumnsToFit(JTable.AUTO_RESIZE_NEXT_COLUMN);
    
    scriptsList.getSelectionModel()
      .addListSelectionListener(new ListSelectionListener()
      {
        public void valueChanged(ListSelectionEvent e)
        {
          if (e.getValueIsAdjusting()) 
            return;
            
          setScriptActionStatus();
        }
      });
      
    TableColumnModel tcm = scriptsList.getColumnModel();
    tcm.getColumn(ScriptsTableModel.NAME_COLUMN).setPreferredWidth(100);
    tcm.getColumn(ScriptsTableModel.NAME_COLUMN).setCellRenderer(new TableNameRenderer());
    
    tcm.getColumn(ScriptsTableModel.AUTHOR_COLUMN).setPreferredWidth(100);
    tcm.getColumn(ScriptsTableModel.DESCRIPTION_COLUMN).setPreferredWidth(250);
    
    JScrollPane scrollPane = new JScrollPane(scriptsList);
    scrollPane.setPreferredSize(new Dimension(450, 200));
    scrollPane.getViewport().setBackground(Color.white);
    panel.add(scrollPane,BorderLayout.CENTER);
    
    return panel;
  }
  
  private JPanel createScriptActionsPanel()
  {
  
    JPanel scriptActionsPanel = new JPanel(new GridBagLayout());

    editScriptButton = new JButton(RES_EDIT_BUTTON);
    removeScriptButton = new JButton(RES_REMOVE_BUTTON);
    refreshListButton = new JButton(RES_REFRESH_BUTTON);
    
    editScriptButton.setActionCommand(EDIT_SCRIPT_ACTION);
    removeScriptButton.setActionCommand(REMOVE_SCRIPT_ACTION);
    refreshListButton.setActionCommand(REFRESH_LIST_ACTION);
    
    editScriptButton.addActionListener(this);
    removeScriptButton.addActionListener(this);
    refreshListButton.addActionListener(this);

    setScriptActionStatus();
    
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.gridx = 0;
    gbc.gridy = 0;
    scriptActionsPanel.add(editScriptButton,gbc);
    
    gbc = (GridBagConstraints)gbc.clone();
    gbc.gridy = 1;
    scriptActionsPanel.add(removeScriptButton,gbc);
    
    gbc = (GridBagConstraints)gbc.clone();
    gbc.gridy = 2;
    scriptActionsPanel.add(refreshListButton,gbc);
    
    JPanel container = new JPanel(new BorderLayout());
    container.add(scriptActionsPanel,BorderLayout.NORTH);
    
    return container;
  }
  
  private void setScriptActionStatus()
  {
    boolean hasSelection = scriptsList.getSelectedRowCount() > 0;
    
    editScriptButton.setEnabled(hasSelection);
    removeScriptButton.setEnabled(hasSelection);
    refreshListButton.setEnabled(hasSelection);
    
  }
  
  private JPanel createDialogActionsPanel()
  {
  
    JButton closeButton = new JButton(RES_CLOSE_BUTTON);
    JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT,0,0));
    
    closeButton.setActionCommand(CLOSE_ACTION);
    closeButton.addActionListener(this);
    actionsPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
    actionsPanel.add(closeButton);
    
    return actionsPanel;
    
  }
  
  public void actionPerformed(ActionEvent event)
  {
    String command = event.getActionCommand();
    
    if (command.equals(CLOSE_ACTION))
    {
      close();
    }
    else if (command.equals(REMOVE_SCRIPT_ACTION))
    {
      
      if (JOptionPane.showConfirmDialog(this,
            "The selected scripts will be removed from disk.\nContinue?",
            "Remove scripts",
            JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
      {
        return;
      }
      
      int[] selectedIndexes = scriptsList.getSelectedRows();
      TableModel model = scriptsList.getModel();
      
      ColumbaScript[] scripts = new ColumbaScript[selectedIndexes.length];
      for(int i=0;i<selectedIndexes.length;i++)
      {
        scripts[i] = (ColumbaScript)model.getValueAt(selectedIndexes[i],
                                                 ScriptsTableModel.NAME_COLUMN);
      }                                          
      document.removeScript(scripts);
    }
    else if (command.equals(REFRESH_LIST_ACTION))
    {
      document.refreshScriptList();
    }
    else if (command.equals(EDIT_SCRIPT_ACTION))
    {
      JOptionPane.showMessageDialog(this,
        "Ha ha, wouldn't it be really sweet if this were implemented?",
        "TODO",
        JOptionPane.INFORMATION_MESSAGE);
    }
    else
    {
      LOG.warning("Not handling: " + event.getActionCommand());
    }
  }

  private void close()
  {
    setVisible(false);
    dispose();
  }
  
}
