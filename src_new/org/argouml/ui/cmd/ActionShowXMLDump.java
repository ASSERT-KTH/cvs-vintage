// $Id: ActionShowXMLDump.java,v 1.6 2006/08/09 18:10:17 mvw Exp $
// Copyright (c) 2004-2006 The Regents of the University of California. All
// Rights Reserved. Permission to use, copy, modify, and distribute this
// software and its documentation without fee, and without a written
// agreement is hereby granted, provided that the above copyright notice
// and this paragraph appear in all copies. This software program and
// documentation are copyrighted by The Regents of the University of
// California. The software program and documentation are supplied "AS
// IS", without any accompanying services from The Regents. The Regents
// does not warrant that the operation of the program will be
// uninterrupted or error-free. The end-user understands that the program
// was developed for research purposes and is advised not to rely
// exclusively on the program for any reason. IN NO EVENT SHALL THE
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

package org.argouml.ui.cmd;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;

import org.argouml.i18n.Translator;
import org.argouml.kernel.Project;
import org.argouml.kernel.ProjectManager;
import org.argouml.persistence.PersistenceManager;
import org.argouml.ui.ProjectBrowser;

/**
 * Action that shows an XML dump of the current project contents.
 */
class ActionShowXMLDump extends AbstractAction {
    
    /**
     * The key for the escape action
     */
    private static final String ACTION_KEY_ESCAPE = "escapeAction";

    /**
     * Insets in pixels.
     */
    private static final int INSET_PX = 3;

    /**
     * Constructor.
     */
    public ActionShowXMLDump() {
        super(Translator.localize("action.show-saved"));
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
	ProjectBrowser pb = ProjectBrowser.getInstance();
	Project project = ProjectManager.getManager().getCurrentProject();

	String data =
	    PersistenceManager.getInstance().getQuickViewDump(project);

	JDialog pw = new JDialog(pb, Translator.localize("action.show-saved"),
            false);

	JTextArea a = new JTextArea(data, 50, 80);
	a.setEditable(false);
	a.setLineWrap(true);
	a.setWrapStyleWord(true);
	a.setMargin(new Insets(INSET_PX, INSET_PX, INSET_PX, INSET_PX));
	a.setCaretPosition(0);

	pw.getContentPane().add(new JScrollPane(a));

	pw.setSize(400, 500);

	pw.setLocationRelativeTo(pb);
        
        loadCommonKeyMap(pw);
        
	pw.setVisible(true);
    }

    /**
     * This method does load common key maps for 
     *
     */
    private void loadCommonKeyMap(final JDialog dialog) {
        dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), 
                ACTION_KEY_ESCAPE);
        // Add the action to the component
        dialog.getRootPane().getActionMap().put(ACTION_KEY_ESCAPE,
            new AbstractAction() {
                public void actionPerformed(ActionEvent evt) {
                    dialog.dispose();
                }
            });
    }    
}

