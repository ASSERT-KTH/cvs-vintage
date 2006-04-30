// $Id: FigOperationsCompartment.java,v 1.20 2006/04/30 08:27:55 mvw Exp $
// Copyright (c) 1996-2006 The Regents of the University of California. All
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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.argouml.kernel.Project;
import org.argouml.kernel.ProjectManager;
import org.argouml.model.Model;
import org.argouml.notation.NotationProviderFactory2;
import org.argouml.ui.targetmanager.TargetManager;

/**
 * @author Bob Tarling
 */
public class FigOperationsCompartment extends FigFeaturesCompartment {
    /**
     * Serial version generated by Eclipse for rev 1.17
     */
    private static final long serialVersionUID = -2605582251722944961L;

    /**
     * The constructor.
     *
     * @param x x
     * @param y y
     * @param w width
     * @param h height
     */
    public FigOperationsCompartment(int x, int y, int w, int h) {
        super(x, y, w, h);
    }

    /**
     * @see org.argouml.uml.diagram.ui.FigFeaturesCompartment#getUmlCollection()
     */
    protected Collection getUmlCollection() {
        Object classifier = getGroup().getOwner();
        return Model.getFacade().getOperations(classifier);
    }

    /**
     * @see org.argouml.uml.diagram.ui.FigFeaturesCompartment#getNotationType()
     */
    protected int getNotationType() {
        return NotationProviderFactory2.TYPE_OPERATION;
    }

    /**
     * @see org.argouml.uml.diagram.ui.FigFeaturesCompartment#addExtraVisualisations(java.lang.Object, org.argouml.uml.diagram.ui.CompartmentFigText)
     */
    protected void addExtraVisualisations(Object umlObject, 
            CompartmentFigText comp) {
        // underline, if static
        comp.setUnderline(
                Model.getScopeKind().
                getClassifier().equals(Model.getFacade().
                        getOwnerScope(umlObject)));
    }

    /**
     * Add handling abstract operations; they
     * are shown in italics.
     * 
     * @see org.argouml.uml.diagram.ui.FigFeaturesCompartment#populate()
     */
    public void populate() {
        super.populate();

        if (!isVisible()) {
            return;
        }

        List figs = getFigs();
        Iterator i = figs.iterator();
        while (i.hasNext()) {
            Object candidate = i.next();
            if (candidate instanceof CompartmentFigText) {
                CompartmentFigText f = (CompartmentFigText) candidate;
                Object owner = f.getOwner();
                
                if (Model.getFacade().isAbstract(owner)) {
                    f.setFont(FigNodeModelElement.getItalicLabelFont());
                } else {
                    f.setFont(FigNodeModelElement.getLabelFont());
                }
            }
        }
    }

    /**
     * @see org.argouml.uml.diagram.ui.FigFeaturesCompartment#createFeature()
     */
    public void createFeature() {
        Object classifier = getGroup().getOwner();
        Project project = ProjectManager.getManager().getCurrentProject();

        Object model = project.getModel();
        Object voidType = project.findType("void");
        Object oper = Model.getCoreFactory().buildOperation(classifier, model,
                voidType);
        populate();
        TargetManager.getInstance().setTarget(oper);

    }
}
