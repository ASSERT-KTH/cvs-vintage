// $Id: FigEdgeModelElement.java,v 1.66 2004/10/19 05:40:05 linus Exp $
// Copyright (c) 1996-2004 The Regents of the University of California. All
// Rights Reserved. Permission to use, copy, modify, and distribute this
// software and its documentation without fee, and without a written
// agreement is hereby granted, provided that the above copyright notice
// and this paragraph appear in all copies.  This software program and
// documentation are copyrighted by The Regents of the University of
// California. The software program and documentation are supplied "AS
// IS", without any accompanying services from The Regents. The Regents
// does not warrant that the operation of the program will be
// uninterrupted or error-free. The end-user understands that the program
// was developed for research purposes and is advised not to rely
// exclusively on the program for any reason.  IN NO EVENT SHALL THE
// UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
// SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE. THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT,
// UPDATES, ENHANCEMENTS, OR MODIFICATIONS.

package org.argouml.uml.diagram.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.argouml.application.api.ArgoEventListener;
import org.argouml.application.api.Notation;
import org.argouml.application.api.NotationContext;
import org.argouml.application.api.NotationName;
import org.argouml.application.events.ArgoEvent;
import org.argouml.application.events.ArgoEventPump;
import org.argouml.application.events.ArgoNotationEvent;
import org.argouml.application.events.ArgoNotationEventListener;
import org.argouml.cognitive.Designer;
import org.argouml.cognitive.ItemUID;
import org.argouml.cognitive.ToDoItem;
import org.argouml.cognitive.ToDoList;
import org.argouml.kernel.DelayedChangeNotify;
import org.argouml.kernel.DelayedVChangeListener;
import org.argouml.kernel.Project;
import org.argouml.kernel.ProjectManager;
import org.argouml.model.ModelFacade;
import org.argouml.model.uml.UmlFactory;
import org.argouml.model.uml.UmlHelper;
import org.argouml.model.uml.UmlModelEventPump;
import org.argouml.model.uml.foundation.core.CoreHelper;
import org.argouml.ui.ActionAutoResize;
import org.argouml.ui.ActionGoToCritique;
import org.argouml.ui.ArgoDiagram;
import org.argouml.ui.ArgoJMenu;
import org.argouml.ui.Clarifier;
import org.argouml.ui.cmd.CmdSetPreferredSize;
import org.argouml.uml.UUIDManager;
import org.argouml.util.Trash;
import org.tigris.gef.base.Globals;
import org.tigris.gef.base.Selection;
import org.tigris.gef.presentation.Fig;
import org.tigris.gef.presentation.FigEdgePoly;
import org.tigris.gef.presentation.FigNode;
import org.tigris.gef.presentation.FigPoly;
import org.tigris.gef.presentation.FigText;
import org.tigris.gef.presentation.Handle;

import ru.novosoft.uml.MElementEvent;
import ru.novosoft.uml.MElementListener;

/**
 * Abstract class to display diagram arcs for UML ModelElements that
 * look like arcs and that have editiable names.
 */
public abstract class FigEdgeModelElement
    extends FigEdgePoly
    implements
        VetoableChangeListener,
        DelayedVChangeListener,
        MouseListener,
        KeyListener,
        PropertyChangeListener,
        MElementListener,
        NotationContext,
        ArgoNotationEventListener {

    private static final Logger LOG =
        Logger.getLogger(FigEdgeModelElement.class);
        
    /**
     * <code>BUNDLE</code>
     */
    protected static final String BUNDLE = "UMLMenu";
        
    ////////////////////////////////////////////////////////////////
    // constants

    private static final Font LABEL_FONT;
    private static final Font ITALIC_LABEL_FONT;

    static {
        LABEL_FONT = new Font("Dialog", Font.PLAIN, 10);
        ITALIC_LABEL_FONT =
            new Font(LABEL_FONT.getFamily(), Font.ITALIC, LABEL_FONT.getSize());
    }

    //public static final int MARGIN = 2;

    /**
     * Offset from the end of the set of popup actions at which new items
     * should be inserted by concrete figures.
    **/
    protected static final int POPUP_ADD_OFFSET = 3;

    ////////////////////////////////////////////////////////////////
    // instance variables

    /**
     * The Fig that displays the name of this model element.
     * Use getNameFig(), no setter should be required.
     */
    private FigText name;
    /**
     * The Fig that displays the stereotype of this model element.
     * Use getStereotypeFig(), no setter should be required.
     */
    private FigText stereo = new FigText(10, 30, 90, 20);
    
    private ItemUID id;
    
    /**
     * The current notation for this fig. The notation is for example
     * UML 1.3 or Java
     */
    private NotationName currentNotationName;

    ////////////////////////////////////////////////////////////////
    // constructors

    /** Partially construct a new FigNode.  This method creates the
     *  _name element that holds the name of the model element and adds
     *  itself as a listener. */
    public FigEdgeModelElement() {
        name = new FigText(10, 30, 90, 20);
        name.setFont(LABEL_FONT);
        name.setTextColor(Color.black);
        name.setTextFilled(false);
        name.setFilled(false);
        name.setLineWidth(0);
        name.setExpandOnly(false);
        name.setMultiLine(false);
        name.setAllowsTab(false);

        stereo.setFont(LABEL_FONT);
        stereo.setTextColor(Color.black);
        stereo.setTextFilled(false);
        stereo.setFilled(false);
        stereo.setLineWidth(0);
        stereo.setExpandOnly(false);
        stereo.setMultiLine(false);
        stereo.setAllowsTab(false);

        setBetweenNearestPoints(true);
        ((FigPoly) _fig).setRectilinear(false);
        ArgoEventPump.addListener(ArgoEvent.ANY_NOTATION_EVENT, this);
    }

    /**
     * the constructor that hooks the Fig into the UML model element
     * 
     * @param edge the UML element 
     */
    public FigEdgeModelElement(Object edge) {
        this();
        setOwner(edge);
        ArgoEventPump.addListener(ArgoEvent.ANY_NOTATION_EVENT, this);
    }

    /**
     * @see java.lang.Object#finalize()
     */
    public void finalize() {
        ArgoEventPump.removeListener(ArgoEvent.ANY_NOTATION_EVENT, this);
    }

    ////////////////////////////////////////////////////////////////
    // accessors

    /**
     * Setter for the UID
     * @param newId the new UID
     */
    public void setItemUID(ItemUID newId) {
        id = newId;
    }

    /**
     * Getter for the UID
     * @return the UID
     */
    public ItemUID getItemUID() {
        return id;
    }

    /**
     * @see org.tigris.gef.presentation.Fig#getTipString(java.awt.event.MouseEvent)
     */
    public String getTipString(MouseEvent me) {
        ToDoItem item = hitClarifier(me.getX(), me.getY());
        String tip = "";
        if (item != null 
            && Globals.curEditor().getSelectionManager().containsFig(this))
            tip = item.getHeadline();
        else if (getOwner() != null)
            tip = getOwner().toString();
        else
            tip = toString();
        if (tip != null && tip.length() > 0 && !tip.endsWith(" "))
            tip += " ";
        return tip;
    }

    /**
     * @see org.tigris.gef.ui.PopupGenerator#getPopUpActions(java.awt.event.MouseEvent)
     */
    public Vector getPopUpActions(MouseEvent me) {
        Vector popUpActions = super.getPopUpActions(me);
        ToDoList list = Designer.theDesigner().getToDoList();
        Vector items = (Vector) list.elementsForOffender(getOwner()).clone();
        if (items != null && items.size() > 0) {
            ArgoJMenu critiques = new ArgoJMenu("menu.popup.critiques");
            ToDoItem itemUnderMouse = hitClarifier(me.getX(), me.getY());
            if (itemUnderMouse != null) {
                critiques.add(new ActionGoToCritique(itemUnderMouse));
                critiques.addSeparator();
            }
            int size = items.size();
            for (int i = 0; i < size; i++) {
                ToDoItem item = (ToDoItem) items.elementAt(i);
                if (item == itemUnderMouse)
                    continue;
                critiques.add(new ActionGoToCritique(item));
            }
            popUpActions.insertElementAt(new JSeparator(), 0);
            popUpActions.insertElementAt(critiques, 0);
        }
        // POPUP_ADD_OFFSET should be equal to the number of items added here:
        popUpActions.addElement(new JSeparator());
        popUpActions.addElement(ActionProperties.getSingleton());
        popUpActions.addElement(ActionDeleteFromDiagram.getSingleton());
        return popUpActions;
    }

    
    /**
     * distance formula: (x-h)^2 + (y-k)^2 = distance^2
     * 
     * @param p1 point
     * @param p2 point
     * @return the square of the distance
     */
    public int getSquaredDistance(Point p1, Point p2) {
        int xSquared = p2.x - p1.x;
        xSquared *= xSquared;
        int ySquared = p2.y - p1.y;
        ySquared *= ySquared;
        return xSquared + ySquared;
    }

    /**
     * @param g the <code>Graphics</code> object
     */
    public void paintClarifiers(Graphics g) {
        int iconPos = 25, gap = 1, xOff = -4, yOff = -4;
        Point p = new Point();
        ToDoList list = Designer.theDesigner().getToDoList();
        Vector items = list.elementsForOffender(getOwner());
        int size = items.size();
        for (int i = 0; i < size; i++) {
            ToDoItem item = (ToDoItem) items.elementAt(i);
            Icon icon = item.getClarifier();
            if (icon instanceof Clarifier) {
                ((Clarifier) icon).setFig(this);
                ((Clarifier) icon).setToDoItem(item);
            }
            if (icon != null) {
                stuffPointAlongPerimeter(iconPos, p);
                icon.paintIcon(null, g, p.x + xOff, p.y + yOff);
                iconPos += icon.getIconWidth() + gap;
            }
        }
        items = list.elementsForOffender(this);
        size = items.size();
        for (int i = 0; i < size; i++) {
            ToDoItem item = (ToDoItem) items.elementAt(i);
            Icon icon = item.getClarifier();
            if (icon instanceof Clarifier) {
                ((Clarifier) icon).setFig(this);
                ((Clarifier) icon).setToDoItem(item);
            }
            if (icon != null) {
                stuffPointAlongPerimeter(iconPos, p);
                icon.paintIcon(null, g, p.x + xOff, p.y + yOff);
                iconPos += icon.getIconWidth() + gap;
            }
        }
    }

    /**
     * The user clicked on the clarifier.
     * 
     * @param x the x of the point clicked
     * @param y the y of the point clicked
     * @return the todo item clicked
     */
    public ToDoItem hitClarifier(int x, int y) {
        int iconPos = 25, xOff = -4, yOff = -4;
        Point p = new Point();
        ToDoList list = Designer.theDesigner().getToDoList();
        Vector items = list.elementsForOffender(getOwner());
        int size = items.size();
        for (int i = 0; i < size; i++) {
            ToDoItem item = (ToDoItem) items.elementAt(i);
            Icon icon = item.getClarifier();
            stuffPointAlongPerimeter(iconPos, p);
            int width = icon.getIconWidth();
            int height = icon.getIconHeight();
            if (y >= p.y + yOff
                && y <= p.y + height + yOff
                && x >= p.x + xOff
                && x <= p.x + width + xOff)
                return item;
            iconPos += width;
        }
        for (int i = 0; i < size; i++) {
            ToDoItem item = (ToDoItem) items.elementAt(i);
            Icon icon = item.getClarifier();
            if (icon instanceof Clarifier) {
                ((Clarifier) icon).setFig(this);
                ((Clarifier) icon).setToDoItem(item);
                if (((Clarifier) icon).hit(x, y))
                    return item;
            }
        }
        items = list.elementsForOffender(this);
        size = items.size();
        for (int i = 0; i < size; i++) {
            ToDoItem item = (ToDoItem) items.elementAt(i);
            Icon icon = item.getClarifier();
            stuffPointAlongPerimeter(iconPos, p);
            int width = icon.getIconWidth();
            int height = icon.getIconHeight();
            if (y >= p.y + yOff
                && y <= p.y + height + yOff
                && x >= p.x + xOff
                && x <= p.x + width + xOff)
                return item;
            iconPos += width;
        }
        for (int i = 0; i < size; i++) {
            ToDoItem item = (ToDoItem) items.elementAt(i);
            Icon icon = item.getClarifier();
            if (icon instanceof Clarifier) {
                ((Clarifier) icon).setFig(this);
                ((Clarifier) icon).setToDoItem(item);
                if (((Clarifier) icon).hit(x, y))
                    return item;
            }
        }
        return null;
    }

    /**
     * Returns a {@link SelectionRerouteEdge} object that manages selection
     * and rerouting of the edge.
     *
     * @return the SelectionRerouteEdge.
     */
    public Selection makeSelection() {
        return new SelectionRerouteEdge(this);
    }

    /**
     * Getter for name, the name Fig
     * @return the nameFig
     */
    public FigText getNameFig() {
        return name;
    }
    
    /**
     * Getter for stereo, the stereotype Fig
     * @return the stereo Fig
     */
    public FigText getStereotypeFig() {
        return stereo;
    }

    /**
     * @see java.beans.VetoableChangeListener#vetoableChange(java.beans.PropertyChangeEvent)
     */
    public void vetoableChange(PropertyChangeEvent pce) {
        Object src = pce.getSource();
        if (src == getOwner()) {
            DelayedChangeNotify delayedNotify =
                new DelayedChangeNotify(this, pce);
            SwingUtilities.invokeLater(delayedNotify);
        }
    }

    /**
     * @see org.argouml.kernel.DelayedVChangeListener#delayedVetoableChange(java.beans.PropertyChangeEvent)
     */
    public void delayedVetoableChange(PropertyChangeEvent pce) {
        // update any text, colors, fonts, etc.
        modelChanged(null);
        // update the relative sizes and positions of internel Figs
        Rectangle bbox = getBounds();
        setBounds(bbox.x, bbox.y, bbox.width, bbox.height);
        endTrans();
    }

    /**
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent pve) {
        Object src = pve.getSource();
        String pName = pve.getPropertyName();
        if (pName.equals("editing")
            && Boolean.FALSE.equals(pve.getNewValue())) {
            LOG.debug("finished editing");
            textEdited((FigText) src);
            calcBounds();
            endTrans();
        } else
            super.propertyChange(pve);
    }

    /**
     * This method is called after the user finishes editing a text
     * field that is in the FigEdgeModelElement.  Determine which field
     * and update the model.  This class handles the name, subclasses
     * should override to handle other text elements.
     *
     * @param ft the text Fig that has been edited
     */
    protected void textEdited(FigText ft) {
        if (ft == name) {
            if (getOwner() == null)
                return;
            ModelFacade.setName(getOwner(), ft.getText());
        }
    }

    /**
     * @param f the Fig
     * @return true if editable
     */
    protected boolean canEdit(Fig f) {
        return true;
    }

    ////////////////////////////////////////////////////////////////
    // event handlers - MouseListener implementation

    /**
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    public void mousePressed(MouseEvent me) {
    }

    /**
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    public void mouseReleased(MouseEvent me) {
    }

    /**
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    public void mouseEntered(MouseEvent me) {
    }

    /**
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    public void mouseExited(MouseEvent me) {
    }

    /**
     * If the user double clicks on anu part of this FigNode, pass it
     * down to one of the internal Figs.  This allows the user to
     * initiate direct text editing.
     *
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    public void mouseClicked(MouseEvent me) {
        if (me.isConsumed())
            return;
        if (me.getClickCount() >= 2) {
            Fig f = hitFig(new Rectangle(me.getX() - 2, me.getY() - 2, 4, 4));
            if (f instanceof MouseListener && canEdit(f))
		((MouseListener) f).mouseClicked(me);
        }
        me.consume();
    }

    /**
     * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
     */
    public void keyPressed(KeyEvent ke) {
        if (ke.isConsumed())
            return;
        if (name != null && canEdit(name))
            name.keyPressed(ke);
    }

    /**
     * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
     *
     * Not used, do nothing.
     */
    public void keyReleased(KeyEvent ke) {
    }

    /**
     * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
     */
    public void keyTyped(KeyEvent ke) {
    }

    ////////////////////////////////////////////////////////////////
    // internal methods

    /**
     * This is called after any part of the UML MModelElement has
     * changed. This method automatically updates the name FigText.
     * Subclasses should override and update other parts.<p>
     *
     * @param e the event
     */
    protected void modelChanged(MElementEvent e) {
        if (e == null
            || (e.getSource() == getOwner() && "name".equals(e.getName())))
            updateNameText();
        updateStereotypeText();

        if (ActionAutoResize.isAutoResizable()) {
            CmdSetPreferredSize cmdSPS =
                new CmdSetPreferredSize(CmdSetPreferredSize.MINIMUM_SIZE);
            cmdSPS.setFigToResize(this);
            cmdSPS.doIt();
        }
        if (!updateClassifiers())
            return;
    }

    /**
     * generate the notation for the modelelement and stuff it into the text Fig
     */
    protected void updateNameText() {
        
        if (getOwner() == null)
            return;
        String nameStr =
	    Notation.generate(this, ModelFacade.getName(getOwner()));
        name.setText(nameStr);
        calcBounds();
        setBounds(getBounds());
    }

    /**
     * generate the notation for the stereotype and stuff it into the text Fig
     */
    protected void updateStereotypeText() {
        if (getOwner() == null) {
            return;
        }
        Object stereotype = null;
        if (ModelFacade.getStereotypes(getOwner()).size() > 0) {
            stereotype =
		ModelFacade.getStereotypes(getOwner()).iterator().next();
        }
        if (stereotype == null) {
            stereo.setText("");
            return;
        }
        String stereoStr = ModelFacade.getName(stereotype);
        if (stereoStr.length() == 0)
            stereo.setText("");
        else {
            stereo.setText(Notation.generateStereotype(this, stereotype));
        }
    }

    /**
     * @see org.tigris.gef.presentation.Fig#setOwner(java.lang.Object)
     */
    public void setOwner(Object newOwner) {
        super.setOwner(newOwner);
        if (newOwner != null) {
            Object oldOwner = getOwner();

            if (org.argouml.model.ModelFacade.isAModelElement(oldOwner))
                UmlModelEventPump.getPump().removeModelEventListener(this,
								     oldOwner);
            if (org.argouml.model.ModelFacade.isAModelElement(newOwner)) {
                UmlModelEventPump.getPump().addModelEventListener(this,
								  newOwner);
                if (ModelFacade.getUUID(newOwner) == null) {
                    ModelFacade.setUUID(newOwner,
					UUIDManager.getInstance().getNewUUID());
		}
            }
            modelChanged(null);
        }

    }

    /**
     * @see ru.novosoft.uml.MElementListener#propertySet(ru.novosoft.uml.MElementEvent)
     */
    public void propertySet(MElementEvent mee) {
        modelChanged(mee);
        damage();
    }

    /**
     * @see ru.novosoft.uml.MElementListener#listRoleItemSet(ru.novosoft.uml.MElementEvent)
     */
    public void listRoleItemSet(MElementEvent mee) {
        
        modelChanged(mee);
        damage();
    }

    /**
     * @see ru.novosoft.uml.MElementListener#recovered(ru.novosoft.uml.MElementEvent)
     */
    public void recovered(MElementEvent mee) {
        
        modelChanged(mee);
        damage();
    }

    /**
     * @see ru.novosoft.uml.MElementListener#removed(ru.novosoft.uml.MElementEvent)
     */
    public void removed(MElementEvent mee) {
        
        LOG.debug("deleting: " + this + mee);
        if (mee.getSource() == getOwner())
            this.removeFromDiagram();
        else {
            modelChanged(mee);
            damage();
        }

    }

    /**
     * @see ru.novosoft.uml.MElementListener#roleAdded(ru.novosoft.uml.MElementEvent)
     */
    public void roleAdded(MElementEvent mee) {
        
        modelChanged(mee);
        damage();
    }

    /**
     * @see ru.novosoft.uml.MElementListener#roleRemoved(ru.novosoft.uml.MElementEvent)
     */
    public void roleRemoved(MElementEvent mee) {
        
        modelChanged(mee);
        damage();
    }

    /**
     * @see org.tigris.gef.presentation.Fig#deleteFromModel()
     */
    public void deleteFromModel() {
        Object own = getOwner();
        if (own != null) {
            Trash.SINGLETON.addItemFrom(getOwner(), null);
            if (org.argouml.model.ModelFacade.isAModelElement(own)) {
                UmlFactory.getFactory().delete(own);
            }
        }
        Iterator it = getPathItemFigs().iterator();
        while (it.hasNext()) {
            ((Fig) it.next()).deleteFromModel();
        }
        super.deleteFromModel();
    }

    /**
     * This default implementation simply requests the default notation.
     *
     * @see org.argouml.application.api.NotationContext#getContextNotation()
     */
    public NotationName getContextNotation() {
        return currentNotationName;
    }

    /**
     * @see org.argouml.application.events.ArgoNotationEventListener#notationChanged(org.argouml.application.events.ArgoNotationEvent)
     */
    public void notationChanged(ArgoNotationEvent event) {
        PropertyChangeEvent changeEvent =
	    (PropertyChangeEvent) event.getSource();
	currentNotationName =
	    Notation.findNotation((String) changeEvent.getNewValue());
        renderingChanged();
        damage();
    }

    /**
     * @see org.argouml.application.events.ArgoNotationEventListener#notationAdded(org.argouml.application.events.ArgoNotationEvent)
     */
    public void notationAdded(ArgoNotationEvent event) {
    }

    /**
     * @see org.argouml.application.events.ArgoNotationEventListener#notationRemoved(org.argouml.application.events.ArgoNotationEvent)
     */
    public void notationRemoved(ArgoNotationEvent event) {
    }

    /**
     * @see org.argouml.application.events.ArgoNotationEventListener#notationProviderAdded(org.argouml.application.events.ArgoNotationEvent)
     */
    public void notationProviderAdded(ArgoNotationEvent event) {
    }

    /**
     * @see org.argouml.application.events.ArgoNotationEventListener#notationProviderRemoved(org.argouml.application.events.ArgoNotationEvent)
     */
    public void notationProviderRemoved(ArgoNotationEvent event) {
    }

    /**
     * Rerenders the fig if needed. This functionality was originally
     * the functionality of modelChanged but modelChanged takes the
     * event now into account.
     */
    public void renderingChanged() {
        // updateAnnotationPositions();
        updateClassifiers();
        updateNameText();
        updateStereotypeText();
        damage();
    }

    /**
     * @see org.tigris.gef.presentation.Fig#hit(java.awt.Rectangle)
     */
    public boolean hit(Rectangle r) {
	// Check if labels etc have been hit
	// Apparently GEF does require PathItems to be "annotations"
	// which ours aren't, so until that is resolved...
	Iterator it = getPathItemFigs().iterator();
	while (it.hasNext()) {
	    Fig f = (Fig) it.next();
	    if (f.hit(r))
		return true;
	}

	return super.hit(r);
    }


    /**
     * @see org.tigris.gef.presentation.Fig#removeFromDiagram()
     */
    public void removeFromDiagram() {
        Object o = getOwner();
        if (ModelFacade.isABase(o)) {
            UmlModelEventPump.getPump().removeModelEventListener(this, o);
        }
        if (this instanceof ArgoEventListener) {
            ArgoEventPump.removeListener(this);
        }

        Iterator it = getPathItemFigs().iterator();
        while (it.hasNext()) {
            Fig fig = (Fig) it.next();
            fig.removeFromDiagram();
        }

        // GEF does not take into account the multiple diagrams we have
        // therefore we loop through our diagrams and delete each and every 
        // occurence on our own
        it =
	    ProjectManager.getManager().getCurrentProject()
	        .getDiagrams().iterator();
        while (it.hasNext()) {
            ArgoDiagram diagram = (ArgoDiagram) it.next();
            diagram.damage();
        }
        super.removeFromDiagram();
    }

    /**
     * @see org.tigris.gef.presentation.Fig#damage()
     */
    public void damage() {
        super.damage();
        _fig.damage();
    }

    /**
     * <p>Updates the classifiers the edge is attached to.  <p>Calls a
     * helper method (layoutThisToSelf) to avoid this edge
     * disappearing if the new source and dest are the same node.
     *
     * @return boolean whether or not the update was sucessful
     */
    protected boolean updateClassifiers() {
        Object owner = getOwner();
        if (owner == null || getLayer() == null)
            return false;

        Object newSource = getSource();
        Object newDest = getDestination();

        Fig currentSourceFig = getSourceFigNode();
        Fig currentDestFig = getDestFigNode();
        Object currentSource = null;
        Object currentDestination = null;
        if (currentSourceFig != null && currentDestFig != null) {
            currentSource = currentSourceFig.getOwner();
            currentDestination = currentDestFig.getOwner();
        }
        if (newSource != currentSource || newDest != currentDestination) {
            Fig newSourceFig = null;
            if (newSource != null)
                newSourceFig = getLayer().presentationFor(newSource);
            Fig newDestFig = null;
            if (newDest != null)
                newDestFig = getLayer().presentationFor(newDest);
            if (newSourceFig == null || newDestFig == null) {
                removeFromDiagram();
                return false;
            }
            if (newSourceFig != null && newSourceFig != currentSourceFig) {
                setSourceFigNode((FigNode) newSourceFig);
                setSourcePortFig(newSourceFig);

            }
            if (newDestFig != null && newDestFig != currentDestFig) {
                setDestFigNode((FigNode) newDestFig);
                setDestPortFig(newDestFig);
            }
            if (newDestFig != null && newSourceFig != null) {
                ((FigNode) newSourceFig).updateEdges();
            }
            if (newSourceFig != null && newDestFig != null) {
                ((FigNode) newDestFig).updateEdges();
            }
            calcBounds();

            // adapted from SelectionWButtons from line 280
            // calls a helper method to avoid this edge disappearing
            // if the new source and dest are the same node.
            if (newSourceFig == newDestFig) {

                layoutThisToSelf();
            }

        }

        return true;
    }

    /**
     * helper method for updateClassifiers() in order to automatically
     * layout an edge that is now from and to the same node type.
     * <p>adapted from SelectionWButtons from line 280
     */
    private void layoutThisToSelf() {

        FigPoly edgeShape = new FigPoly();
        //newFC = _content;
        Point fcCenter = getSourceFigNode().center();
        Point centerRight =
            new Point(
		      (int) (fcCenter.x
			     + getSourceFigNode().getSize().getWidth() / 2),
		      fcCenter.y);

        int yoffset = (int) ((getSourceFigNode().getSize().getHeight() / 2));
        edgeShape.addPoint(fcCenter.x, fcCenter.y);
        edgeShape.addPoint(centerRight.x, centerRight.y);
        edgeShape.addPoint(centerRight.x + 30, centerRight.y);
        edgeShape.addPoint(centerRight.x + 30, centerRight.y + yoffset);
        edgeShape.addPoint(centerRight.x, centerRight.y + yoffset);

        // place the edge on the layer and update the diagram
        this.setBetweenNearestPoints(true);
        edgeShape.setLineColor(Color.black);
        edgeShape.setFilled(false);
        edgeShape._isComplete = true;
        this.setFig(edgeShape);
    }

    /**
     * Returns the source of the edge. The source is the owner of the
     * node the edge travels from in a binary relationship. For
     * instance: for a classifierrole, this is the sender.
     * @return MModelElement
     */
    protected Object getSource() {
        if (getOwner() != null) {
            return UmlHelper.getHelper().getSource(getOwner());
        }
        return null;
    }
    /**
     * Returns the destination of the edge. The destination is the
     * owner of the node the edge travels to in a binary
     * relationship. For instance: for a classifierrole, this is the
     * receiver. Since we don't support n-array associations but only
     * binary relations, source/destination works for all edges.
     * @return Object
     */
    protected Object getDestination() {
        if (getOwner() != null) {
            return CoreHelper.getHelper().getDestination(getOwner());
        }
        return null;
    }

    /**
     * @see org.tigris.gef.presentation.Fig#postLoad()
     */
    public void postLoad() {
        super.postLoad();        
        if (this instanceof ArgoEventListener) {
            ArgoEventPump.removeListener(this);
            ArgoEventPump.addListener(this);
        }
    }

    /**
     * Overridden to notify project that save is needed when edge is moved.
     * @see org.tigris.gef.presentation.FigEdgePoly#setPoint(Handle, int, int)
     */
    public void setPoint(Handle h, int x, int y) {
        super.setPoint(h, x, y);
        Project p = ProjectManager.getManager().getCurrentProject();
        if (p != null) {      
            p.setNeedsSave(true);
        }
    }

    /**
     * @return Returns the lABEL_FONT.
     */
    public static Font getLabelFont() {
        return LABEL_FONT;
    }

    /**
     * @return Returns the iTALIC_LABEL_FONT.
     */
    public static Font getItalicLabelFont() {
        return ITALIC_LABEL_FONT;
    }

} /* end class FigEdgeModelElement */
