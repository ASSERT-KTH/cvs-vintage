// Copyright (c) 1996-98 The Regents of the University of California. All
// Rights Reserved. Permission to use, copy, modify, and distribute this
// software and its documentation for educational, research and non-profit
// purposes, without fee, and without a written agreement is hereby granted,
// provided that the above copyright notice and this paragraph appear in all
// copies. Permission to incorporate this software into commercial products
// must be negotiated with University of California. This software program and
// documentation are copyrighted by The Regents of the University of
// California. The software program and documentation are supplied "as is",
// without any accompanying services from The Regents. The Regents do not
// warrant that the operation of the program will be uninterrupted or
// error-free. The end-user understands that the program was developed for
// research purposes and is advised not to rely exclusively on the program for
// any reason. IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY
// PARTY FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES,
// INCLUDING LOST PROFITS, ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS
// DOCUMENTATION, EVEN IF THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGE. THE UNIVERSITY OF CALIFORNIA SPECIFICALLY
// DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
// WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE
// SOFTWARE PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
// ENHANCEMENTS, OR MODIFICATIONS.




package uci.uml.ui;

import java.beans.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import com.sun.java.swing.*;
import com.sun.java.swing.event.*;
import com.sun.java.swing.table.*;
import com.sun.java.swing.text.*;
import com.sun.java.swing.plaf.metal.MetalLookAndFeel;

import uci.util.*;
import uci.argo.kernel.*;

/** Dialog box to list all critics and allow editing of some of their
 *  properties.  Needs-More-Work: knowledge type, supported goals,
 *  supported decisions, critic network. */
public class CriticBrowserDialog extends JFrame
implements ActionListener, ListSelectionListener, ItemListener, DocumentListener {


  ////////////////////////////////////////////////////////////////
  // constants
  public static final String PRIORITIES[] = { "High", "Medium", "Low" };
  public static final String USE_CLAR[] = { "Always", "If Only One", "Never" };


  ////////////////////////////////////////////////////////////////
  // instance variables
  
  protected JLabel _criticsLabel   = new JLabel("Critics");
  protected JLabel _clsNameLabel   = new JLabel("Critic Class: ");
  protected JLabel _headlineLabel  = new JLabel("Headline: ");
  protected JLabel _priorityLabel  = new JLabel("Priority: ");
  protected JLabel _moreInfoLabel  = new JLabel("MoreInfo: ");
  protected JLabel _descLabel      = new JLabel("Description: ");
  protected JLabel _clarifierLabel = new JLabel("Use Clarifier: ");

  TableModelCritics _tableModel  = new TableModelCritics();
  protected JTable _table        = new JTable(30, 3);
  protected JLabel _className    = new JLabel("Classname goes here");
  protected JTextField _headline = new JTextField("Headline goes here", 40);
  protected JComboBox _priority  = new JComboBox(PRIORITIES);
  protected JTextField _moreInfo = new JTextField("URL goes here", 35);
  protected JTextArea _desc      = new JTextArea("Desc goes here", 6, 40);
  protected JComboBox _useClar   = new JComboBox(USE_CLAR);

  
  protected JButton _okButton      = new JButton("OK");
  protected JButton _wakeButton    = new JButton("Wake");
  protected JButton _configButton  = new JButton("Configure");
  protected JButton _networkButton = new JButton("Edit Network");
  protected JButton _goButton      = new JButton("Go");
  
  protected Critic _target;

  ////////////////////////////////////////////////////////////////
  // constructors
  
  public CriticBrowserDialog() {
    super("Critics");

    Container mainContent = getContentPane();
    GridBagLayout gb = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.BOTH;
    c.weightx = 0.0;
    c.ipadx = 3; c.ipady = 3;


    JPanel content = new JPanel();
    mainContent.add(content, BorderLayout.CENTER);
    content.setLayout(gb);

    _tableModel.setTarget(Agency.getCritics());
    _table.setModel(_tableModel);
    _table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    Font labelFont = MetalLookAndFeel.getSubTextFont();
    _table.setFont(labelFont);

//     _table.setMinimumSize(new Dimension(150, 80));
//     _table.setPreferredSize(new Dimension(200, 150));
//     _table.setSize(new Dimension(200, 150));

    //_table.setRowSelectionAllowed(false);
    _table.setIntercellSpacing(new Dimension(0, 1));
    _table.setShowVerticalLines(false);
    _table.getSelectionModel().addListSelectionListener(this);
    _table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

    TableColumn checkCol = _table.getColumnModel().getColumn(0);
    TableColumn descCol = _table.getColumnModel().getColumn(1);
    TableColumn actCol = _table.getColumnModel().getColumn(2);
    checkCol.setMinWidth(30);
    checkCol.setMaxWidth(30);
    checkCol.setWidth(30);
    descCol.setMinWidth(200);
    descCol.setWidth(200);
    actCol.setMinWidth(80);
    actCol.setMaxWidth(80);
    actCol.setWidth(80);

//     JPanel westPanel = new JPanel();
//     westPanel.setLayout(new BorderLayout());

//     westPanel.add(_criticsLabel, BorderLayout.NORTH);
//     JScrollPane tableSP = new JScrollPane(_table,
// 					  JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
// 					  JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
//     tableSP.setSize(100, 50);
//     tableSP.setMinimumSize(new Dimension(100, 50));
//     tableSP.setMaximumSize(new Dimension(100, 50));
//     tableSP.setPreferredSize(new Dimension(100, 50));
//     westPanel.add(tableSP, BorderLayout.CENTER);
//     mainContent.add(westPanel, BorderLayout.WEST);
    
    c.gridx = 0;
    c.gridy = 0;
    gb.setConstraints(_criticsLabel, c);
    content.add(_criticsLabel);
    c.gridy = 1;
    c.gridheight = 11; //GridBagConstraints.REMAINDER
    JScrollPane tableSP = new JScrollPane(_table);
    JPanel p = new JPanel();
    p.setLayout(new BorderLayout());
    p.setPreferredSize(new Dimension(310, 150));
    p.setSize(new Dimension(310, 150));
    p.setMaximumSize(new Dimension(310, 150));
    p.add(tableSP, BorderLayout.CENTER);
//     tableSP.setPreferredSize(new Dimension(310, 100));
//     tableSP.setSize(new Dimension(310, 100));
//     tableSP.setMaximumSize(new Dimension(310, 100));
    gb.setConstraints(p, c);
    content.add(p);

    c.gridx = 1;
    c.gridy = 0;
    c.gridwidth = 1;
    c.gridheight = 1;
    SpacerPanel spacer = new SpacerPanel();
    gb.setConstraints(spacer, c);
    content.add(spacer);

    c.weightx = 0.0;
    c.gridx = 2;
    c.gridy = 1;
    gb.setConstraints(_clsNameLabel, c);
    content.add(_clsNameLabel);

    c.gridy = 2;
    gb.setConstraints(_headlineLabel, c);
    content.add(_headlineLabel);

    c.gridy = 3;
    gb.setConstraints(_priorityLabel, c);
    content.add(_priorityLabel);

    c.gridy = 4;
    gb.setConstraints(_moreInfoLabel, c);
    content.add(_moreInfoLabel);

    c.gridy = 5;
    gb.setConstraints(_descLabel, c);
    content.add(_descLabel);

    c.gridy = 8;
    gb.setConstraints(_clarifierLabel, c);
    content.add(_clarifierLabel);


    c.weightx = 1.0;
    c.gridx = 3;
    c.gridy = 1;
    c.gridwidth = 2;
    gb.setConstraints(_className, c);
    content.add(_className);

    c.gridy = 2;
    gb.setConstraints(_headline, c);
    content.add(_headline);

    c.gridy = 3;
    gb.setConstraints(_priority, c);
    content.add(_priority);

    c.gridy = 4;
    c.gridwidth = 1;
    gb.setConstraints(_moreInfo, c);
    content.add(_moreInfo);

    c.weightx = 0.0;
    c.gridx = 4;
    c.gridy = 4;
    c.gridwidth = 1;
    gb.setConstraints(_goButton, c);
    content.add(_goButton);

    c.weightx = 1.0;
    c.gridx = 3;
    c.gridy = 5;
    c.gridwidth = 2;
    JScrollPane descSP = new JScrollPane(_desc);
    gb.setConstraints(descSP, c);
    content.add(descSP);

    c.gridy = 8;
    gb.setConstraints(_useClar, c);
    content.add(_useClar);

    c.gridy = 9;
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new GridLayout(1, 2));
    buttonPanel.add(_wakeButton);
    buttonPanel.add(_configButton);
    buttonPanel.add(_networkButton);
    gb.setConstraints(buttonPanel, c);
    content.add(buttonPanel);

    c.gridx = 2;
    c.gridy = 10;
    c.gridwidth = GridBagConstraints.REMAINDER;
    JPanel buttonPane = new JPanel();
    buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
    buttonPane.add(_okButton);
    gb.setConstraints(buttonPane, c);
    content.add(buttonPane);

    _goButton.addActionListener(this);
    _okButton.addActionListener(this);
    _networkButton.addActionListener(this);
    _wakeButton.addActionListener(this);
    _configButton.addActionListener(this);
    _headline.getDocument().addDocumentListener(this);
    _moreInfo.getDocument().addDocumentListener(this);
    _desc.getDocument().addDocumentListener(this);
    _priority.addItemListener(this);
    _useClar.addItemListener(this);

    _desc.setLineWrap(true);
    _desc.setWrapStyleWord(true);

    setLocation(100, 150);
    pack();
    setResizable(false);
  }

  public void setTarget(Object t) {
    _target = (Critic) t;
    _goButton.setEnabled(false);
    _networkButton.setEnabled(false);
    _wakeButton.setEnabled(_target != null &&
			   _target.snoozeOrder().getSnoozed());
    _configButton.setEnabled(false);
    _className.setText(_target.getClass().getName());
    _headline.setText(_target.getHeadline());

    int p = _target.getPriority();
    if (p == ToDoItem.HIGH_PRIORITY) _priority.setSelectedIndex(0);
    else if (p == ToDoItem.MED_PRIORITY) _priority.setSelectedIndex(1);
    else _priority.setSelectedIndex(2);

    _moreInfo.setText(_target.getMoreInfoURL());
    _desc.setText(_target.getDescriptionTemplate());
    _desc.setCaretPosition(0);
    _useClar.setSelectedIndex(0);
  }

  public void setTargetHeadline() {
    if (_target == null) return;
    String h = _headline.getText();
    _target.setHeadline(h);
  }

  public void setTargetPriority() {
    if (_target == null) return;
    String p = (String) _priority.getSelectedItem();
    if (p == null) return;
    if (p.equals(PRIORITIES[0])) _target.setPriority(ToDoItem.HIGH_PRIORITY);
    if (p.equals(PRIORITIES[1])) _target.setPriority(ToDoItem.MED_PRIORITY);
    if (p.equals(PRIORITIES[2])) _target.setPriority(ToDoItem.LOW_PRIORITY);
  }

  public void setTargetMoreInfo() {
    if (_target == null) return;
    String mi = _moreInfo.getText();
    _target.setMoreInfoURL(mi);
  }

  public void setTargetDesc() {
    if (_target == null) return;
    String d = _desc.getText();
    _target.setDescription(d);
  }

  public void setTargetUseClarifiers() {
    System.out.println("setting clarifier usage rule");
  }
  
  
  ////////////////////////////////////////////////////////////////
  // event handlers


  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == _okButton) {
      hide();
      dispose();
      return;
    }
    if (e.getSource() == _goButton) {
      System.out.println("needs-more-work go!");
      return;
    }
    if (e.getSource() == _networkButton) {
      System.out.println("needs-more-work network!");
      return;
    }
    if (e.getSource() == _configButton) {
      System.out.println("needs-more-work config!");
      return;
    }
    if (e.getSource() == _wakeButton) {
      _target.unsnooze();
      return;
    }
    System.out.println("unknown src in CriticBrowserDialog: " + e.getSource());
  }

  public void valueChanged(ListSelectionEvent lse) {
    if (lse.getValueIsAdjusting()) return;
    Object src = lse.getSource();
    if (src != _table.getSelectionModel()) {
      System.out.println("src = " + src);
      return;
    }
    //System.out.println("got valueChanged from " + src);
    int row = lse.getFirstIndex();
    Vector critics = Agency.getCritics();
    setTarget(critics.elementAt(row));
  }

  public void insertUpdate(DocumentEvent e) {
    //System.out.println(getClass().getName() + " insert");
    Document hDoc = _headline.getDocument();
    Document miDoc = _moreInfo.getDocument();
    Document dDoc = _desc.getDocument();
    if (e.getDocument() == hDoc) setTargetHeadline();
    if (e.getDocument() == miDoc) setTargetMoreInfo();
    if (e.getDocument() == dDoc) setTargetDesc();
  }

  public void removeUpdate(DocumentEvent e) { insertUpdate(e); }

  public void changedUpdate(DocumentEvent e) {
    System.out.println(getClass().getName() + " changed");
    // Apparently, this method is never called.
  }

  public void itemStateChanged(ItemEvent e) {
    Object src = e.getSource();
    if (src == _priority) {
      //System.out.println("class keywords now is " +
      //_keywordsField.getSelectedItem());
      setTargetPriority();
    }
    else if (src == _useClar) {
      //System.out.println("class VisibilityKind now is " +
      //_visField.getSelectedItem());
      setTargetUseClarifiers();
    }
    else 
      System.out.println("unknown itemStateChanged src: "+ src);
  }

  
} /* end class CriticBrowserDialog */




class TableModelCritics extends AbstractTableModel
implements VetoableChangeListener, DelayedVetoableChangeListener {
  ////////////////
  // instance varables
  Vector _target;

  ////////////////
  // constructor
  public TableModelCritics() { }

  ////////////////
  // accessors
  public void setTarget(Vector critics) {
    _target = critics;
    //fireTableStructureChanged();
  }

  ////////////////
  // TableModel implemetation
  public int getColumnCount() { return 3; }

  public String  getColumnName(int c) {
    if (c == 0) return "X";
    if (c == 1) return "Headline";
    if (c == 2) return "Active";
    return "XXX";
  }

  public Class getColumnClass(int c) {
    if (c == 0) return Boolean.class;
    if (c == 1) return String.class;
    if (c == 2) return String.class;
    return String.class;
  }

  public boolean isCellEditable(int row, int col) {
    return col == 0;
  }

  public int getRowCount() {
    if (_target == null) return 0;
    return _target.size();
  }

  public Object getValueAt(int row, int col) {
    Critic cr = (Critic) _target.elementAt(row);
    if (col == 0) return cr.isEnabled() ? Boolean.TRUE : Boolean.FALSE;
    if (col == 1) return cr.getHeadline();
    if (col == 2) return cr.isActive() ? "Active" : "Inactive";
    return "CR-" + row*2+col; // for debugging
  }

  public void setValueAt(Object aValue, int rowIndex, int columnIndex)  {
    //System.out.println("setting table value " + rowIndex + ", " + columnIndex);
    if (columnIndex != 0) return;
    if (!(aValue instanceof Boolean)) return;
    Boolean enable = (Boolean) aValue;
    Critic cr = (Critic) _target.elementAt(rowIndex);
    cr.setEnabled(enable.booleanValue());
    fireTableRowsUpdated(rowIndex, rowIndex); //needs-more-work
  }

  ////////////////
  // event handlers

  public void vetoableChange(PropertyChangeEvent pce) {
    DelayedChangeNotify delayedNotify = new DelayedChangeNotify(this, pce);
    SwingUtilities.invokeLater(delayedNotify);
  }

  public void delayedVetoableChange(PropertyChangeEvent pce) {
    fireTableStructureChanged();
  }


} /* end class TableModelCritics */
