// $Id: UMLCollaborationDiagram.java,v 1.57 2004/11/14 10:06:01 linus Exp $
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

package org.argouml.uml.diagram.collaboration.ui;

import java.beans.PropertyVetoException;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.Action;

import org.apache.log4j.Logger;
import org.argouml.i18n.Translator;
import org.argouml.kernel.ProjectManager;
import org.argouml.model.ModelFacade;
import org.argouml.ui.CmdCreateNode;
import org.argouml.ui.CmdSetMode;
import org.argouml.uml.diagram.collaboration.CollabDiagramGraphModel;
import org.argouml.uml.diagram.ui.ActionAddAssociationRole;
import org.argouml.uml.diagram.ui.FigMessage;
import org.argouml.uml.diagram.ui.RadioAction;
import org.argouml.uml.diagram.ui.UMLDiagram;
import org.argouml.uml.diagram.ui.ActionAddMessage;
import org.tigris.gef.base.Layer;
import org.tigris.gef.base.LayerPerspective;
import org.tigris.gef.base.LayerPerspectiveMutable;
import org.tigris.gef.base.ModeCreatePolyEdge;
import org.tigris.gef.presentation.Fig;

/**
 * The base class of the collaboration diagram.<p>
 *
 * Defines the toolbar, provides for its initialization and provides
 * constructors for a top level diagram and one within a defined
 * namespace.<p>
 *
 * @author agauthie@ics.uci.edu
 */
public class UMLCollaborationDiagram extends UMLDiagram {

    /**
     * Logging.
     */
    private static final Logger LOG =
        Logger.getLogger(UMLCollaborationDiagram.class);

    ////////////////////////
    // actions for toolbar

    private Action actionMessage;
    private Action actionClassifierRole;
    private Action actionGeneralize;

    private Action actionAssociation;
    private Action actionAggregation;
    private Action actionComposition;
    private Action actionUniAssociation;
    private Action actionUniAggregation;
    private Action actionUniComposition;

    private Action actionDepend;
    private Action actionMesasage;

    ////////////////////////////////////////////////////////////////
    // contructors

    /**
     * Constructor.
     */
    public UMLCollaborationDiagram() {

        try {
            setName(getNewDiagramName());
        } catch (PropertyVetoException pve) { }
    }

    /**
     * @param namespace the namespace for the diagram
     */
    public UMLCollaborationDiagram(Object namespace) {
        this();
        setNamespace(namespace);
    }

    /**
     * @return the number of UML messages in the diagram
     */
    public int getNumMessages() {
        Layer lay = getLayer();
        Collection figs = lay.getContents(null);
        int res = 0;
        Iterator it = figs.iterator();
        while (it.hasNext()) {
            Fig f = (Fig) it.next();
            if (ModelFacade.isAMessage(f.getOwner())) {
                res++;
            }
        }
        return res;
    }

    /**
     * Method to perform a number of important initializations of a
     * <I>CollaborationDiagram</I>.<p>
     *
     * Each diagram type has a similar <I>UMLxxxDiagram</I> class.<p>
     *
     * Changed <I>lay</I> from <I>LayerPerspective</I> to
     * <I>LayerPerspectiveMutable</I>.  This class is a child of
     * <I>LayerPerspective</I> and was implemented to correct some
     * difficulties in changing the model.  <I>Lay</I> is used mainly
     * in <I>LayerManager</I>(GEF) to control the adding, changing and
     * deleting layers on the diagram...
     *
     * @param handle  MNamespace from the model in NSUML...
     * @author psager@tigris.org Jan. 24, 2002
     */
    public void setNamespace(Object handle) {
        if (!ModelFacade.isANamespace(handle)) {
            LOG.error(
                "Illegal argument. Object " + handle + " is not a namespace");
            throw new IllegalArgumentException(
                "Illegal argument. Object " + handle + " is not a namespace");
        }
        Object m = /*(MNamespace)*/ handle;
        super.setNamespace(m);
        CollabDiagramGraphModel gm = new CollabDiagramGraphModel();
        gm.setNamespace(m);
        LayerPerspective lay =
            new LayerPerspectiveMutable(ModelFacade.getName(m), gm);
        CollabDiagramRenderer rend = new CollabDiagramRenderer(); // singleton
        lay.setGraphNodeRenderer(rend);
        lay.setGraphEdgeRenderer(rend);
        setLayer(lay);

    }

    /**
     * Get the actions from which to create a toolbar or equivalent
     * graphic triggers.
     *
     * @see org.argouml.uml.diagram.ui.UMLDiagram#getUmlActions()
     */
    protected Object[] getUmlActions() {
        Object[] actions = {
	    getActionClassifierRole(),
	    null,
	    getAssociationActions(),
	    getActionGeneralize(),
	    getActionDepend(),
            null,
            getActionMesage(), //this one behaves differently, hence seperated!
        };
        return actions;
    }

    private Object[] getAssociationActions() {
        Object[][] actions = {
	    {getActionAssociation(), getActionUniAssociation() },
	    {getActionAggregation(), getActionUniAggregation() },
	    {getActionComposition(), getActionUniComposition() },
        };

        return actions;
    }

    /**
     * After loading the diagram it?s necessary to connect
     * every FigMessage to its FigAssociationRole.
     * This is done by adding the FigMessage
     * to the PathItems of its FigAssociationRole.
     */
    public void postLoad() {

        super.postLoad();

        Collection messages;
        Iterator msgIterator;
        if (getNamespace() == null) {
            LOG.error("Collaboration Diagram does not belong to a namespace");
            return;
        }
        Collection ownedElements = ModelFacade.getOwnedElements(getNamespace());
        Iterator oeIterator = ownedElements.iterator();
        Layer lay = getLayer();
        while (oeIterator.hasNext()) {
            Object me = /*(MModelElement)*/
		oeIterator.next();
            if (org.argouml.model.ModelFacade.isAAssociationRole(me)) {
                messages = ModelFacade.getMessages(me);
                msgIterator = messages.iterator();
                while (msgIterator.hasNext()) {
                    Object message = /*(MMessage)*/
			msgIterator.next();
                    FigMessage figMessage =
                        (FigMessage) lay.presentationFor(message);
                    if (figMessage != null) {
                        figMessage.addPathItemToFigAssociationRole(lay);
                    }
                }
            }
        }
    }

    /**
     * Creates a new diagramname.
     * @return String
     */
    protected String getNewDiagramName() {
        String name = "Collaboration Diagram " + getNextDiagramSerial();
        if (!ProjectManager.getManager().getCurrentProject()
	        .isValidDiagramName(name)) {
            name = getNewDiagramName();
        }
        return name;
    }

    /**
     * @see org.argouml.uml.diagram.ui.UMLDiagram#getLabelName()
     */
    public String getLabelName() {
        return Translator.localize("label.colaboration-diagram");
    }

    /**
     * @return Returns the actionClassifierRole.
     */
    private Action getActionClassifierRole() {
        if (actionClassifierRole == null) {
            actionClassifierRole = new RadioAction(new CmdCreateNode(
                    ModelFacade.CLASSIFIER_ROLE, "ClassifierRole"));
        }
        return actionClassifierRole;
    }

    /**
     * @return Returns the actionAssociation.
     */
    protected Action getActionAssociation() {
        if (actionAssociation == null) {
            actionAssociation = new RadioAction(
                    new ActionAddAssociationRole(
                        ModelFacade.NONE_AGGREGATIONKIND,
                        false,
                        "Association"));
        }
        return actionAssociation;
    }
    /**
     * @return Returns the actionComposition.
     */
    protected Action getActionComposition() {
        if (actionComposition == null) {
            actionComposition = new RadioAction(
                    new ActionAddAssociationRole(
                        ModelFacade.COMPOSITE_AGGREGATIONKIND,
                        false,
                        "Composition"));
        }
        return actionComposition;
    }
    /**
     * @return Returns the actionDepend.
     */
    protected Action getActionDepend() {
        if (actionDepend == null) {
            actionDepend = new RadioAction(
                    new CmdSetMode(
                        ModeCreatePolyEdge.class,
                        "edgeClass",
                        ModelFacade.DEPENDENCY,
                        "Dependency"));
        }
        return actionDepend;
    }
    /**
     * @return Returns the actionGeneralize.
     */
    protected Action getActionGeneralize() {
        if (actionGeneralize == null) {
            actionGeneralize = new RadioAction(
                    new CmdSetMode(
                        ModeCreatePolyEdge.class,
                        "edgeClass",
                        ModelFacade.GENERALIZATION,
                        "Generalization"));
        }
        return actionGeneralize;
    }
    /**
     * @return Returns the actionMessage.
     */
    protected Action getActionMessage() {
        return actionMessage;
    }
    /**
     * @return Returns the actionUniAggregation.
     */
    protected Action getActionUniAggregation() {
        if (actionUniAggregation == null) {
            actionUniAggregation = new RadioAction(
                    new ActionAddAssociationRole(
                        ModelFacade.AGGREGATE_AGGREGATIONKIND,
                        true,
                        "UniAggregation"));
        }
        return actionUniAggregation;
    }
    /**
     * @return Returns the actionUniAssociation.
     */
    protected Action getActionUniAssociation() {
        if (actionUniAssociation  == null) {
            actionUniAssociation = new RadioAction(
                    new ActionAddAssociationRole(
                        ModelFacade.NONE_AGGREGATIONKIND,
                        true,
                        "UniAssociation"));
        }
        return actionUniAssociation;
    }
    /**
     * @return Returns the actionUniComposition.
     */
    protected Action getActionUniComposition() {
        if (actionUniComposition == null) {
            actionUniComposition = new RadioAction(
                    new ActionAddAssociationRole(
                        ModelFacade.COMPOSITE_AGGREGATIONKIND,
                        true,
                        "UniComposition"));
        }
        return actionUniComposition;
    }

    /**
     * @return Returns the actionAggregation.
     */
    private Action getActionAggregation() {
        if (actionAggregation == null) {
            actionAggregation = new RadioAction(
                    new ActionAddAssociationRole(
                        ModelFacade.AGGREGATE_AGGREGATIONKIND,
                        false,
                        "Aggregation"));
        }
        return actionAggregation;
    }

    /**
     * @return Returns the actionMesasage.
     */
    private Action getActionMesage() {
        if (actionMesasage == null) {
            actionMesasage = ActionAddMessage.getSingleton();
        }
        return actionMesasage;
    }
} /* end class UMLCollaborationDiagram */
