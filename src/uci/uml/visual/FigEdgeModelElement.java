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



// File: FigEdgeModelElement.java
// Classes: FigEdgeModelElement
// Original Author: abonner
// $Id: FigEdgeModelElement.java,v 1.6 1998/10/08 00:06:41 jrobbins Exp $

package uci.uml.visual;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.beans.*;
import com.sun.java.swing.*;
import com.sun.java.swing.plaf.metal.MetalLookAndFeel;

import uci.gef.*;
import uci.graph.*;
import uci.uml.ui.*;
import uci.uml.generate.*;
import uci.uml.Foundation.Core.*;
import uci.uml.Foundation.Data_Types.*;
import uci.uml.Foundation.Extension_Mechanisms.*;

/** Abstract class to display diagram arcs for UML ModelElements that
 *  look like arcs and that have editiable names. */

public abstract class FigEdgeModelElement extends FigEdgePoly
implements VetoableChangeListener, DelayedVetoableChangeListener, MouseListener, KeyListener, PropertyChangeListener  { 

  public static Font LABEL_FONT;
  public static Font ITALIC_LABEL_FONT;

  static {
    LABEL_FONT = MetalLookAndFeel.getSubTextFont();
    ITALIC_LABEL_FONT = new Font(LABEL_FONT.getFamily(),
				 Font.ITALIC, LABEL_FONT.getSize());
  }

  public final int MARGIN = 2;

  protected FigText _name;
  protected FigText _stereo = new FigText(10, 30, 90, 20);
  

  /** Partially construct a new FigNode.  This method creates the
   *  _name element that holds the name of the model element and adds
   *  itself as a listener. */
  public FigEdgeModelElement(Object edge) {
    super();
    setOwner(edge);
    ModelElement me = (ModelElement) edge;
    //me.addVetoableChangeListener(this);

    _name = new FigText(10, 30, 90, 20);
    _name.setFont(LABEL_FONT);
    _name.setTextColor(Color.black);
    _name.setTextFilled(false);
    _name.setFilled(false);
    _name.setLineWidth(0);
    _name.setExpandOnly(false);
    _name.setMultiLine(false);
    _name.setAllowsTab(false);

    _stereo.setFont(LABEL_FONT);
    _stereo.setTextColor(Color.black);
    _stereo.setTextFilled(false);
    _stereo.setFilled(false);
    _stereo.setLineWidth(0);
    _stereo.setExpandOnly(false);
    _stereo.setMultiLine(false);
    _stereo.setAllowsTab(false);

    setBetweenNearestPoints(true);
    ((FigPoly)_fig).setRectilinear(false);
  }


  public void vetoableChange(PropertyChangeEvent pce) {
    Object src = pce.getSource();
    if (src == getOwner()) {
      DelayedChangeNotify delayedNotify = new DelayedChangeNotify(this, pce);
      SwingUtilities.invokeLater(delayedNotify);
    }
  }

  public void delayedVetoableChange(PropertyChangeEvent pce) {
    Object src = pce.getSource();
    startTrans();
    // update any text, colors, fonts, etc.
    modelChanged();
    // update the relative sizes and positions of internel Figs
    Rectangle bbox = getBounds();
    setBounds(bbox.x, bbox.y, bbox.width, bbox.height);
    endTrans();
  }

  public void propertyChange(PropertyChangeEvent pve) {
    Object src = pve.getSource();
    String pName = pve.getPropertyName();
    if (pName.equals("editing") && Boolean.FALSE.equals(pve.getNewValue())) {
      //System.out.println("finished editing");
      try {
	startTrans();
	textEdited((FigText) src);
	calcBounds();
	endTrans();
      }
      catch (PropertyVetoException ex) {
	System.out.println("could not parse and use the text you entered");
      }
    }
    else super.propertyChange(pve);
  }

  /** This method is called after the user finishes editing a text
   *  field that is in the FigEdgeModelElement.  Determine which field
   *  and update the model.  This class handles the name, subclasses
   *  should override to handle other text elements. */
  protected void textEdited(FigText ft) throws PropertyVetoException {
    if (ft == _name)
      ((ModelElement)getOwner()).setName(new Name(ft.getText()));
  }

  protected boolean canEdit(Fig f) { return true; }

  ////////////////////////////////////////////////////////////////
  // event handlers - MouseListener implementation

   public void mousePressed(MouseEvent me) { }
   public void mouseReleased(MouseEvent me) { }
   public void mouseEntered(MouseEvent me) { }
   public void mouseExited(MouseEvent me) { }


  /** If the user double clicks on anu part of this FigNode, pass it
   *  down to one of the internal Figs.  This allows the user to
   *  initiate direct text editing. */
  public void mouseClicked(MouseEvent me) {
    if (me.isConsumed()) return;
    if (me.getClickCount() >= 2) {
      Fig f = hitFig(new Rectangle(me.getX() - 2, me.getY() - 2, 4, 4));
      if (f instanceof MouseListener && canEdit(f))
	((MouseListener)f).mouseClicked(me);
    }
    me.consume();
  }

  public void keyPressed(KeyEvent ke) {
    if (ke.isConsumed()) return;
    if (_name != null && canEdit(_name)) _name.keyPressed(ke);
    //ke.consume();
//     ModelElement me = (ModelElement) getOwner();
//     try { me.setName(new Name(_name.getText())); }
//     catch (PropertyVetoException pve) { }
  }

  /** not used, do nothing. */
  public void keyReleased(KeyEvent ke) { }

  public void keyTyped(KeyEvent ke) {
//     if (ke.isConsumed()) return;
//     System.out.println("hjasdjsg222");
//     _name.keyTyped(ke);
//     //ke.consume();
//     ModelElement me = (ModelElement) getOwner();
//     try { me.setName(new Name(_name.getText())); }
//     catch (PropertyVetoException pve) { }
  }

  ////////////////////////////////////////////////////////////////
  // internal methods

  /** This is called aftern any part of the UML ModelElement has
   *  changed. This method automatically updates the name FigText.
   *  Subclasses should override and update other parts. */
  protected void modelChanged() {
    updateNameText();
    updateStereotypeText();
  }


  public void updateNameText() {
    ModelElement me = (ModelElement) getOwner();
    if (me == null) return;
    String nameStr = GeneratorDisplay.Generate(me.getName());
    _name.setText(nameStr);
  }

  public void updateStereotypeText() {
    ModelElement me = (ModelElement) getOwner();
    if (me == null) return;
    Vector stereos = me.getStereotype();
    if (stereos == null || stereos.size() == 0) return;
    String stereoStr = ((Stereotype) stereos.elementAt(0)).getName().getBody();
    _stereo.setText("<<" + stereoStr + ">>");
  }

  /** needs-more-work: When the user deletes a ModelElement, it is
   *  moved to a special trash container. */
  public void dispose() {
    //System.out.println("disposing FigEdgeModelElement");
    ModelElement me = (ModelElement) getOwner();
    if (me == null) return;
    Project p = ProjectBrowser.TheInstance.getProject();
    p.moveToTrash(me);
    super.dispose();
  }

  public void setOwner(Object own) {
    Object oldOwner = getOwner();
    super.setOwner(own);
    if (oldOwner instanceof ModelElement)
      ((ModelElement)oldOwner).removeVetoableChangeListener(this);
    if (own instanceof ModelElement)
      ((ModelElement)own).addVetoableChangeListener(this);
  }


} /* end class FigEdgeModelElement */
