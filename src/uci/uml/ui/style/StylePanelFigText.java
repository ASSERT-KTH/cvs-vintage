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




package uci.uml.ui.style;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.beans.*;
import com.sun.java.swing.*;
import com.sun.java.swing.event.*;
import com.sun.java.swing.tree.*;
import com.sun.java.swing.text.*;
import com.sun.java.swing.border.*;
import com.sun.java.swing.plaf.metal.MetalLookAndFeel;

import uci.util.*;
import uci.ui.*;
import uci.gef.*;
import uci.uml.ui.*;


public class StylePanelFigText extends StylePanelFig {

  ////////////////////////////////////////////////////////////////
  // constants
  public static String FONT_NAMES[] = { "dialog", "serif", "sanserif",
					"monospaced" };
  public static Integer COMMON_SIZES[] = {
    new Integer(8), new Integer(9), new Integer(10), new Integer(12),
    new Integer(16), new Integer(18), new Integer(24), new
    Integer(36), new Integer(48), new Integer(72), new Integer(96) }; 
  
  public static String STYLES[] = { "Plain", "Bold", "Italic", "Bold-Italic"};

  ////////////////////////////////////////////////////////////////
  // instance vars
  JLabel _fontLabel = new JLabel("Font: ");
  JComboBox _fontField = new JComboBox(FONT_NAMES);
  JLabel _sizeLabel = new JLabel("Size: ");
  JComboBox _sizeField = new JComboBox(COMMON_SIZES);
  JLabel _styleLabel = new JLabel("Style: ");
  JComboBox _styleField = new JComboBox(STYLES);
  JLabel _textColorLabel = new JLabel("Text Color: ");
  JComboBox _textColorField = new JComboBox();
  JLabel _textFillLabel = new JLabel("Text Fill: ");
  JComboBox _textFillField = new JComboBox();




  ////////////////////////////////////////////////////////////////
  // contructors
  public StylePanelFigText() {
    super();
    GridBagLayout gb = (GridBagLayout) getLayout();    
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.BOTH;
    c.ipadx = 0; c.ipady = 0;

    _fontField.addItemListener(this);
    _sizeField.addItemListener(this);
    _styleField.addItemListener(this);
    _textColorField.addItemListener(this);
    _textFillField.addItemListener(this);

    _textColorField.setRenderer(new ColorRenderer());
    _textFillField.setRenderer(new ColorRenderer());
    
    c.weightx = 0.0;
    c.gridx = 0;
    c.gridwidth = 1;
    c.gridy = 5;
    gb.setConstraints(_textColorLabel, c);
    add(_textColorLabel);
    c.gridy = 6;
    gb.setConstraints(_textFillLabel, c);
    add(_textFillLabel);


    c.gridx = 3;
    c.gridwidth = 1;
    c.gridy = 1;
    gb.setConstraints(_fontLabel, c);
    add(_fontLabel);
    c.gridy = 2;
    gb.setConstraints(_sizeLabel, c);
    add(_sizeLabel);
    c.gridy = 3;
    gb.setConstraints(_styleLabel, c);
    add(_styleLabel);

    
    c.weightx = 1.0;
    c.gridx = 1;
    c.gridy = 5;
    gb.setConstraints(_textColorField, c);
    add(_textColorField);
    c.gridy = 6;
    gb.setConstraints(_textFillField, c);
    add(_textFillField);
    c.gridx = 4;
    c.gridy = 1;
    gb.setConstraints(_fontField, c);
    add(_fontField);
    c.gridy = 2;
    gb.setConstraints(_sizeField, c);
    add(_sizeField);
    c.gridy = 3;
    gb.setConstraints(_styleField, c);
    add(_styleField);
    initChoices2();
  }

  protected void initChoices2() {
    _textColorField.addItem(Color.black);
    _textColorField.addItem(Color.white);
    _textColorField.addItem(Color.gray);
    _textColorField.addItem(Color.lightGray);
    _textColorField.addItem(Color.darkGray);
    _textColorField.addItem(Color.red);
    _textColorField.addItem(Color.blue);
    _textColorField.addItem(Color.green);
    _textColorField.addItem(Color.orange);
    _textColorField.addItem(Color.pink);
    _textColorField.addItem("Custom...");

    _textFillField.addItem("No Fill");
    _textFillField.addItem(Color.black);
    _textFillField.addItem(Color.white);
    _textFillField.addItem(Color.gray);
    _textFillField.addItem(Color.lightGray);
    _textFillField.addItem(Color.darkGray);
    _textFillField.addItem(Color.red);
    _textFillField.addItem(Color.blue);
    _textFillField.addItem(Color.green);
    _textFillField.addItem(Color.orange);
    _textFillField.addItem(Color.pink);
    _textFillField.addItem("Custom...");

  }

  ////////////////////////////////////////////////////////////////
  // accessors

  public void refresh() {
    super.refresh();
    FigText ft = (FigText) _target; 
    String fontName = ft.getFontFamily();
    int size = ft.getFontSize();
    String styleName = STYLES[0];
    
    _fontField.setSelectedItem(fontName);
    _sizeField.setSelectedItem(new Integer(size));
    if (ft.getBold()) styleName = STYLES[1];
    if (ft.getItalic()) styleName = STYLES[2];
    if (ft.getBold() && ft.getItalic()) styleName = STYLES[3];
    _styleField.setSelectedItem(styleName);
  }


  public void setTargetFont() {
    if (_target == null) return;
    String fontStr = (String) _fontField.getSelectedItem();
    if (fontStr.length() == 0) return;
    _target.startTrans();
    ((FigText)_target).setFontFamily(fontStr);
    _target.endTrans();
  }


  public void setTargetSize() {
    if (_target == null) return;
    Integer size = (Integer) _sizeField.getSelectedItem();
    _target.startTrans();
    ((FigText)_target).setFontSize(size.intValue());
    _target.endTrans();
  }


  public void setTargetStyle() {
    if (_target == null) return;
    String styleStr = (String) _styleField.getSelectedItem();
    if (styleStr == null) return;
    boolean bold = (styleStr.indexOf("Bold") != -1);
    boolean italic = (styleStr.indexOf("Italic") != -1);
    _target.startTrans();
    ((FigText)_target).setBold(bold);
    ((FigText)_target).setItalic(italic);
    _target.endTrans();
  }

  public void setTargetTextFill() {
    if (_target == null) return;
    _target.startTrans();
    Object c =  _textFillField.getSelectedItem();
    if (c instanceof Color)
      ((FigText)_target).setTextFillColor((Color)c);
    ((FigText)_target).setTextFilled(c instanceof Color);
    _target.endTrans();
  }

  
  public void setTargetTextColor() {
    if (_target == null) return;
    _target.startTrans();
    Object c =  _textColorField.getSelectedItem();
    if (c instanceof Color)
      ((FigText)_target).setTextColor((Color)c);
    _target.endTrans();
  }

  
  ////////////////////////////////////////////////////////////////
  // event handling


  public void itemStateChanged(ItemEvent e) {
    Object src = e.getSource();
    if (src == _fontField) setTargetFont();
    else if (src == _sizeField) setTargetSize();
    else if (src == _styleField) setTargetStyle();
    else if (src == _textColorField) setTargetTextColor();
    else if (src == _textFillField) setTargetTextFill();
    else super.itemStateChanged(e);
  }

  
} /* end class StylePanelFigText */
