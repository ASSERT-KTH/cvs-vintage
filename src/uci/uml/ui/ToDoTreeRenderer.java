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

//import jargo.kernel.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import com.sun.java.swing.*;
import com.sun.java.swing.event.*;
import com.sun.java.swing.border.*;
import com.sun.java.swing.plaf.basic.*;
import com.sun.java.swing.plaf.metal.*;

import uci.argo.kernel.*;
import uci.uml.Foundation.Core.*;
import uci.uml.Model_Management.*;

public class ToDoTreeRenderer extends BasicTreeCellRenderer {
  ////////////////////////////////////////////////////////////////
  // class variables
  protected ImageIcon _PostIt0 = loadIconResource("PostIt0");
  protected ImageIcon _PostIt25 = loadIconResource("PostIt25");
  protected ImageIcon _PostIt50 = loadIconResource("PostIt50");
  protected ImageIcon _PostIt75 = loadIconResource("PostIt75");
  protected ImageIcon _PostIt99 = loadIconResource("PostIt99");
  protected ImageIcon _MultiPostIt = loadIconResource("MultiPostIt");
  

  protected UMLTreeCellRenderer _navRenderer = new UMLTreeCellRenderer();
  
  ////////////////////////////////////////////////////////////////
  // TreeCellRenderer implementation
  
  public Component getTreeCellRendererComponent(JTree tree, Object value,
						boolean sel,
						boolean expanded,
						boolean leaf, int row,
						boolean hasFocus) {

    Component r = super.getTreeCellRendererComponent(tree, value, sel,
						     expanded, leaf,
						     row, hasFocus);

    if (r instanceof JLabel) {
      JLabel lab = (JLabel) r;
      if (value instanceof ToDoItem) {
	ToDoItem item = (ToDoItem) value;
	if (item.getProgress() == 0) lab.setIcon(_PostIt0);
	else if (item.getProgress() <= 25) lab.setIcon(_PostIt25);
	else if (item.getProgress() <= 50) lab.setIcon(_PostIt50);
	else if (item.getProgress() <= 75) lab.setIcon(_PostIt75);
	else if (item.getProgress() <= 100) lab.setIcon(_PostIt99);
      }
      else if (value instanceof Decision) {
	lab.setIcon(MetalIconFactory.getTreeFolderIcon());
      }
      else if (value instanceof Goal) {
	lab.setIcon(MetalIconFactory.getTreeFolderIcon());
      }
      else if (value instanceof Poster) {
	lab.setIcon(MetalIconFactory.getTreeFolderIcon());
      }
      else if (value instanceof PriorityNode) {
	lab.setIcon(MetalIconFactory.getTreeFolderIcon());
      }
      else if (value instanceof KnowledgeTypeNode) {
	lab.setIcon(MetalIconFactory.getTreeFolderIcon());
      }
      else if (value instanceof ModelElement) {
	return _navRenderer.getTreeCellRendererComponent(tree, value, sel,
					      expanded, leaf, row, hasFocus);
      }
      

      lab.setToolTipText(value.toString());

      if (sel) {
	Color high = uci.gef.Globals.getPrefs().getHighlightColor();
	high = high.brighter().brighter();
	lab.setBackground(high);
      }
      lab.setOpaque(sel);

    }
    return r;
  }

  ////////////////////////////////////////////////////////////////
  // utility functions

  protected static ImageIcon loadIconResource(String name) {
    String imgName = imageName(name);
    ImageIcon res = null;
    try {
      java.net.URL imgURL = ToDoTreeRenderer.class.getResource(imgName);
      return new ImageIcon(imgURL);
    }
    catch (Exception ex) {
      return new ImageIcon(name);
    }
  }

  protected static String imageName(String name) {
    return "/uci/Images/" + stripJunk(name) + ".gif";
  }
  
  protected static String stripJunk(String s) {
    String res = "";
    int len = s.length();
    for (int i = 0; i < len; i++) {
      char c = s.charAt(i);
      if (Character.isJavaLetterOrDigit(c)) res += c;
    }
    return res;
  }
  

  
} /* end class ToDoTreeRenderer */
