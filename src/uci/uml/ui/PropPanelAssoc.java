// Copyright (c) 1996-98 The Regents of the University of California. All
// Rights Reserved. Permission to use, copy, modify, and distribute this
// software and its documentation for educational, research and non-profit
// purposes, without fee, and without a written agreement is hereby granted,
// provided that the above copyright notice and this paragraph appear in all
// copies. Permission to incorporate this software into commercial products may
// be obtained by contacting the University of California. David F. Redmiles
// Department of Information and Computer Science (ICS) University of
// California Irvine, California 92697-3425 Phone: 714-824-3823. This software
// program and documentation are copyrighted by The Regents of the University
// of California. The software program and documentation are supplied "as is",
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

//import jargo.kernel.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.beans.*;
import com.sun.java.swing.*;
import com.sun.java.swing.event.*;
import com.sun.java.swing.tree.*;
import com.sun.java.swing.text.*;
import com.sun.java.swing.border.*;

import uci.util.*;
import uci.uml.Foundation.Core.*;
import uci.uml.Foundation.Data_Types.*;
import uci.uml.Model_Management.*;

public class PropPanelAssoc extends PropPanel
implements DocumentListener, ItemListener, ChangeListener {

  ////////////////////////////////////////////////////////////////
  // constants
//   public static final VisibilityKind VISIBILITIES[] = {
//     VisibilityKind.PUBLIC, VisibilityKind.PRIVATE,
//     VisibilityKind.PROTECTED, VisibilityKind.PACKAGE };

  public static final Multiplicity MULTS[] = {
    Multiplicity.ONE, Multiplicity.ONE_OR_ZERO,
    Multiplicity.ONE_OR_MORE, Multiplicity.ZERO_OR_MORE };

  public static final AggregationKind AGGS[] = {
    AggregationKind.NONE, AggregationKind.AGG,
    AggregationKind.COMPOSITE };
  
  
  ////////////////////////////////////////////////////////////////
  // instance vars
  JLabel _roleALabel = new JLabel("Role Name: ");
  JTextField _roleAField = new JTextField();
  JLabel _multALabel = new JLabel("Multiplicity: ");
  JComboBox _multAField = new JComboBox(MULTS);
  JLabel _aggALabel = new JLabel("Aggregation: ");
  JComboBox _aggAField = new JComboBox(AGGS);
  JLabel _navALabel = new JLabel("Navigable: ");
  JCheckBox _navAField = new JCheckBox("");
//   JLabel _visALabel = new JLabel("Visibility: ");
//   JComboBox _visAField = new JComboBox(VISIBILITIES);

  JLabel _roleBLabel = new JLabel("Role Name: ");
  JTextField _roleBField = new JTextField();
  JLabel _multBLabel = new JLabel("Multiplicity: ");
  JComboBox _multBField = new JComboBox(MULTS);
  JLabel _aggBLabel = new JLabel("Aggregation: ");
  JComboBox _aggBField = new JComboBox(AGGS);
//   JLabel _visBLabel = new JLabel("Visibility: ");
//   JComboBox _visBField = new JComboBox(VISIBILITIES);
  JLabel _navBLabel = new JLabel("Navigable: ");
  JCheckBox _navBField = new JCheckBox("");

  SpacerPanel _spacer = new SpacerPanel();

  ////////////////////////////////////////////////////////////////
  // contructors
  public PropPanelAssoc() {
    super("Association Properties");
    GridBagLayout gb = (GridBagLayout) getLayout();
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.BOTH;
    c.weightx = 0.0;
    c.ipadx = 0; c.ipady = 0;


//     _multAField.setEditable(true);
//     _multBField.setEditable(true);
//     _multAField.getEditor().getEditorComponent().setBackground(Color.white);
//     _multBField.getEditor().getEditorComponent().setBackground(Color.white);


    c.gridx = 0;
    c.gridwidth = 1;
    c.gridy = 5;
    gb.setConstraints(_roleALabel, c);
    add(_roleALabel);
    c.gridy = 6;
    gb.setConstraints(_multALabel, c);
    add(_multALabel);
    c.gridy = 7;
    gb.setConstraints(_aggALabel, c);
    add(_aggALabel);
//     c.gridy = 8;
//     gb.setConstraints(_visALabel, c);
//     add(_visALabel);
    c.gridy = 9;
    gb.setConstraints(_navALabel, c);
    add(_navALabel);

    c.gridx = 1;
    c.gridwidth = 1;
    c.gridy = 5;
    c.weightx = 1.0;
    gb.setConstraints(_roleAField, c);
    add(_roleAField);
    c.gridy = 6;
    gb.setConstraints(_multAField, c);
    add(_multAField);
    c.gridy = 7;
    gb.setConstraints(_aggAField, c);
    add(_aggAField);
//     c.gridy = 8;
//     gb.setConstraints(_visAField, c);
//     add(_visAField);
    c.gridy = 9;
    gb.setConstraints(_navAField, c);
    add(_navAField);

    

    c.weightx = 0.0;
    c.gridx = 2;
    c.gridy = 0;
    gb.setConstraints(_spacer, c);
    add(_spacer);
    

    c.weightx = 0.0;
    c.gridx = 3;
    c.gridwidth = 1;
    c.gridy = 5;
    gb.setConstraints(_roleBLabel, c);
    add(_roleBLabel);
    c.gridy = 6;
    gb.setConstraints(_multBLabel, c);
    add(_multBLabel);
    c.gridy = 7;
    gb.setConstraints(_aggBLabel, c);
    add(_aggBLabel);
//     c.gridy = 8;
//     gb.setConstraints(_visBLabel, c);
//     add(_visBLabel);
    c.gridy = 9;
    gb.setConstraints(_navBLabel, c);
    add(_navBLabel);

    c.gridx = 4;
    c.gridwidth = 1;
    c.gridy = 5;
    c.weightx = 1.0;
    gb.setConstraints(_roleBField, c);
    add(_roleBField);
    c.gridy = 6;
    gb.setConstraints(_multBField, c);
    add(_multBField);
    c.gridy = 7;
    gb.setConstraints(_aggBField, c);
    add(_aggBField);
//     c.gridy = 8;
//     gb.setConstraints(_visBField, c);
//     add(_visBField);
    c.gridy = 9;
    gb.setConstraints(_navBField, c);
    add(_navBField);


//     Component ed = _multAField.getEditor().getEditorComponent();
//     Document typeDoc = ((JTextField)ed).getDocument();
//     typeDoc.addDocumentListener(this);

//     ed = _multBField.getEditor().getEditorComponent();
//     typeDoc = ((JTextField)ed).getDocument();
//     typeDoc.addDocumentListener(this);

    _roleAField.getDocument().addDocumentListener(this);
    _roleBField.getDocument().addDocumentListener(this);

//     _visAField.addItemListener(this);
//     _visBField.addItemListener(this);
    _multAField.addItemListener(this);
    _multBField.addItemListener(this);

    _aggAField.addItemListener(this);
    _aggBField.addItemListener(this);

    _navAField.addChangeListener(this);
    _navBField.addChangeListener(this);


  }

  ////////////////////////////////////////////////////////////////
  // accessors

  public void setTarget(Object t) {
    super.setTarget(t);
    Association asc = (Association) t;
    Vector conns = asc.getConnection();
    if (conns == null) return; //set fields to blanks?
    if (conns.size() != 2) return; //set fields to blanks?
    AssociationEnd endA = (AssociationEnd) conns.elementAt(0);
    AssociationEnd endB = (AssociationEnd) conns.elementAt(1);

    Name roleA = endA.getName();
    String roleAStr = "(anon)";
    if (roleA != null) roleAStr = roleA.getBody();
    Name roleB = endB.getName();
    String roleBStr = "(anon)";
    if (roleB != null) roleBStr = roleB.getBody();
    
//     VisibilityKind vkA = endA.getVisibility();
//     _visAField.setSelectedItem(vkA);
//     VisibilityKind vkB = endB.getVisibility();
//     _visBField.setSelectedItem(vkB);

    Multiplicity mA = endA.getMultiplicity();
    Multiplicity mB = endB.getMultiplicity();

    AggregationKind akA = endA.getAggregation();
    AggregationKind akB = endB.getAggregation();

    boolean navA = endA.getIsNavigable();
    boolean navB = endB.getIsNavigable();

    _roleAField.setText(roleAStr);
    _roleBField.setText(roleBStr);
    _multAField.setSelectedItem(mA);
    _multBField.setSelectedItem(mB);
    _aggAField.setSelectedItem(akA);
    _aggBField.setSelectedItem(akB);
    _navAField.getModel().setSelected(navA);
    _navBField.getModel().setSelected(navB);
    
  }



  public void setTargetVisibility() {
    if (_target == null) return;
//     VisibilityKind vk = (VisibilityKind) _visField.getSelectedItem();
//     Attribute attr = (Attribute) _target;
//     attr.setVisibility(vk);
  }

  public void setMult() {
    if (_target == null) return;
    Multiplicity mA, mB;
    Object mAs = _multAField.getSelectedItem();
    Object mBs = _multBField.getSelectedItem();

    if (mAs == null || mBs == null) return;
    
    // will be a String if I allow editing, needs-more-work: implement
    // parsing of multiplicities
    //System.out.println("a mult:" + _multAField.getSelectedItem().getClass());
    //System.out.println("b mult:" + _multBField.getSelectedItem().getClass());

    if (mAs instanceof Multiplicity) mA = (Multiplicity) mAs;
    else mA = Multiplicity.ONE; // needs-more-work: parse

    if (mBs instanceof Multiplicity) mB = (Multiplicity) mBs;
    else mB = Multiplicity.ONE; // needs-more-work: parse

    
    Association asc = (Association) _target;
    Vector conns = asc.getConnection();
    if (conns == null || conns.size() != 2) return;
    AssociationEnd endA = (AssociationEnd) conns.elementAt(0);
    AssociationEnd endB = (AssociationEnd) conns.elementAt(1);
    try {
      endA.setMultiplicity(mA);
      endB.setMultiplicity(mB);
    }
    catch (PropertyVetoException pve) { }
  }

  public void setAgg() {
    System.out.println("setagg");
    if (_target == null) return;
    AggregationKind aggA = (AggregationKind) _aggAField.getSelectedItem();
    AggregationKind aggB = (AggregationKind) _aggBField.getSelectedItem();
    //if (aggA == null || aggB == null) return;
    Association asc = (Association) _target;
    Vector conns = asc.getConnection();
    if (conns == null || conns.size() != 2) return;
    AssociationEnd endA = (AssociationEnd) conns.elementAt(0);
    AssociationEnd endB = (AssociationEnd) conns.elementAt(1);
    try {
      endA.setAggregation(aggA);
      endB.setAggregation(aggB);
    }
    catch (PropertyVetoException pve) { }
  }

  public void setNav() {
    if (_target == null) return;
    boolean navA = _navAField.getModel().isSelected();
    boolean navB = _navBField.getModel().isSelected();
    //System.out.println("navA=" + navA + " navB=" + navB);
    Association asc = (Association) _target;
    Vector conns = asc.getConnection();
    if (conns == null || conns.size() != 2) return;
    AssociationEnd endA = (AssociationEnd) conns.elementAt(0);
    AssociationEnd endB = (AssociationEnd) conns.elementAt(1);
    try {
      endA.setIsNavigable(navA);
      endB.setIsNavigable(navB);
    }
    catch (PropertyVetoException pve) { }
  }
  
  public void setRoleNames() {
    if (_target == null) return;
    String roleAStr = _roleAField.getText();
    String roleBStr = _roleBField.getText();
    if (roleBStr == null || roleBStr == null) return;
    //System.out.println("roleA=" + roleAStr + " roleB=" + roleBStr);
    Association asc = (Association) _target;
    Vector conns = asc.getConnection();
    if (conns == null || conns.size() != 2) return;
    AssociationEnd endA = (AssociationEnd) conns.elementAt(0);
    AssociationEnd endB = (AssociationEnd) conns.elementAt(1);
    try {
      endA.setName(new Name(roleAStr));
      endB.setName(new Name(roleBStr));
    }
    catch (PropertyVetoException pve) { }
  }

  

  
  ////////////////////////////////////////////////////////////////
  // event handlers


  public void insertUpdate(DocumentEvent e) {
    //System.out.println(getClass().getName() + " insert");
//     Component ed = _typeField.getEditor().getEditorComponent();
//     Document typeDoc = ((JTextField)ed).getDocument();

    Document roleADoc = _roleAField.getDocument();
    Document roleBDoc = _roleBField.getDocument();

    if (e.getDocument() == roleADoc) setRoleNames();
    else if (e.getDocument() == roleBDoc) setRoleNames();
//     else if (e.getDocument() == initDoc) setTargetInit();
    super.insertUpdate(e);
  }

  public void removeUpdate(DocumentEvent e) { insertUpdate(e); }

  public void changedUpdate(DocumentEvent e) {
    //System.out.println(getClass().getName() + " changed");
    // Apparently, this method is never called.
  }

  
  
  public void stateChanged(ChangeEvent e) {
    //System.out.println("got stateChanged");
    Object src = e.getSource();
    if (src == _navAField || src == _navBField) setNav();
  }

  public void itemStateChanged(ItemEvent e) {
    Object src = e.getSource();
//     if (src == _visAField) {
//       System.out.println("assoc vis A now is " +
// 			 _visAField.getSelectedItem());
//       //setTargetKeywords();
//     }
//     else if (src == _visBField) {
//       System.out.println("assoc vis B now is " +
// 			 _visBField.getSelectedItem());
//       //setTargetKeywords();
//     }
    if (src == _multAField) {
      //System.out.println("assoc mult A now is " +
      //_multAField.getSelectedItem());
      setMult();
    }
    else if (src == _multBField) {
      //System.out.println("assoc mult B now is " +
      //_multBField.getSelectedItem());
      setMult();
    }
    if (src == _aggAField) {
      //System.out.println("assoc agg A now is " +
      //_aggAField.getSelectedItem());
      setAgg();
    }
    else if (src == _aggBField) {
      //System.out.println("assoc agg B now is " +
      //_aggBField.getSelectedItem());
      setAgg();
    }
    
  }


} /* end class PropPanelAssoc */
