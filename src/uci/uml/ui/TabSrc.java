package uci.uml.ui;

//import jargo.kernel.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import com.sun.java.swing.*;
import com.sun.java.swing.event.*;
import com.sun.java.swing.tree.*;
import com.sun.java.swing.text.*;

import uci.util.*;
import uci.gef.*;
import uci.uml.generate.*;


public class TabSrc extends TabText {
  ////////////////////////////////////////////////////////////////
  // constructor
  public TabSrc() {
    setTitle("Source");
    System.out.println("making TabSrc");
  }

  ////////////////////////////////////////////////////////////////
  // accessors

  protected String genText() {
    System.out.println("TabSrc getting src for " + _target);
    Object modelObject = null;
    if (_target instanceof FigNode)
      modelObject = ((FigNode)_target).getOwner();
    if (_target instanceof FigEdge)
      modelObject = ((FigEdge)_target).getOwner();
    if (modelObject == null) return null;
    System.out.println("TabSrc getting src for " + modelObject);
    return GeneratorDisplay.Generate(modelObject);
  }

  protected void parseText(String s) {
    System.out.println("TabSrc   setting src for "+ _target);
    Object modelObject = null;
    if (_target instanceof FigNode)
      modelObject = ((FigNode)_target).getOwner();
    if (_target instanceof FigEdge)
      modelObject = ((FigEdge)_target).getOwner();
    if (modelObject == null) return;
    System.out.println("TabSrc   setting src for " + modelObject);
    //Parser.ParseAndUpdate(modelObject, s);
  }

  
} /* end class TabSrc */
