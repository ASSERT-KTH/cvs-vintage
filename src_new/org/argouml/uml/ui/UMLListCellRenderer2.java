// $Id: UMLListCellRenderer2.java,v 1.16 2003/10/26 16:40:02 alexb Exp $
// Copyright (c) 1996-2002 The Regents of the University of California. All
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

// $Id: UMLListCellRenderer2.java,v 1.16 2003/10/26 16:40:02 alexb Exp $
package org.argouml.uml.ui;

import org.argouml.model.ModelFacade;
import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;

import org.apache.log4j.Logger;
import org.argouml.application.helpers.ResourceLoaderWrapper;

/**
 * The default cell renderer for uml model elements. Used by UMLList2 and its
 * children.
 * @author jaap.branderhorst@xs4all.nl	
 * @since Jan 2, 2003
 */
public class UMLListCellRenderer2 extends DefaultListCellRenderer {

    private Logger cat = Logger.getLogger(UMLListCellRenderer2.class);

    /**
     * True if the icon for the modelelement should be shown. The icon is, for
     * instance, a small class symbol for a class.
     */
    private boolean _showIcon;

    /**
     * Constructor for UMLListCellRenderer2.
     */
    public UMLListCellRenderer2(boolean showIcon) {
        super();
        _showIcon = showIcon;
    }

    /**
     * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
     */
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {       
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        if (org.argouml.model.ModelFacade.isABase(value)) {
            String text = makeText(value);            
            label.setText(text);
            if (_showIcon) {
                Icon icon = ResourceLoaderWrapper.getResourceLoaderWrapper().lookupIcon(value);
                // if (icon != null)
		label.setIcon(icon);
            } else {
                // hack to make sure that the right hight is applied when no icon is used.
                label = (JLabel) super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
            }

        } else
	    if (value == null || value.equals("")) {      
		label = new JLabel(" ");      
		label.setIcon(null);
	    }
        

        return label;
    }

    /**
     * Makes the text that must be placed on the label that is returned.
     * @param value
     * @return String
     */
    public String makeText(Object value) {
        String name = null;
        if (ModelFacade.isAModelElement(value)) {
            Object/*MModelElement*/ elem = value;
            name = ModelFacade.getName(elem);
            if (name == null || name.equals("")) {
                name = "(anon " + makeTypeName(elem) + ")";
            }
        } else if (ModelFacade.isAMultiplicity(value)) {
            name = value.toString();
        } else {                    
            name = makeTypeName(value);
        }
        return name;

    }

    private String makeTypeName(Object elem) {
        if (org.argouml.model.ModelFacade.isABase(elem)) {
            return ModelFacade.getUMLClassName(elem);
        }
        return null;
    }

}