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

package org.argouml.uml.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import org.argouml.application.api.Argo;
import org.argouml.kernel.Project;
import org.argouml.kernel.ProjectManager;
import org.argouml.model.ModelFacade;
import org.argouml.model.uml.modelmanagement.ModelManagementHelper;
import org.argouml.ui.ProjectBrowser;
import org.argouml.uml.generator.Generator;

/**
 * Provides support for setting a "src_path" tagged value used in Java 
 * round trip engineering.
 */
public class SourcePathDialog extends JDialog implements ActionListener {

    ////////////////////////////////////////////////////////////////
    // instance variables
    private SrcPathTableModel _srcPathTableModel = new SrcPathTableModel();

    protected JTable _srcPathTable;
    protected JButton _cancelButton;
    protected JButton _okButton;
    protected JScrollPane _srcPathScrollPane;

    ////////////////////////////////////////////////////////////////
    // constructors

    public SourcePathDialog() {
        super(
            ProjectBrowser.getInstance(),
            Argo.localize("CoreMenu", "action.generate-code-for-project"));

        GridBagConstraints gridBagConstraints;

        _cancelButton = new JButton();
        _okButton = new JButton();
        _srcPathScrollPane = new JScrollPane();
        _srcPathTable = new JTable();

        getContentPane().setLayout(new GridBagLayout());

        _cancelButton.setText("Cancel");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        getContentPane().add(_cancelButton, gridBagConstraints);

        _okButton.setText("Ok");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        getContentPane().add(_okButton, gridBagConstraints);

        _srcPathTable.setModel(_srcPathTableModel);
        _srcPathTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        _srcPathTable.setShowVerticalLines(false);
        _srcPathTable.setIntercellSpacing(new Dimension(0, 1));
        TableColumn elemCol = _srcPathTable.getColumnModel().getColumn(0);
        elemCol.setMinWidth(0);
        elemCol.setMaxWidth(0);
        elemCol = null;
        _srcPathScrollPane.setViewportView(_srcPathTable);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        gridBagConstraints.weighty = 2.0;
        getContentPane().add(_srcPathScrollPane, gridBagConstraints);

        pack();

        // Center Dialog on Screen -- todo: this should be a support function
        ProjectBrowser pb = ProjectBrowser.getInstance();
        Rectangle pbBox = pb.getBounds();
        setLocation(
            pbBox.x + (pbBox.width - this.getWidth()) / 2,
            pbBox.y + (pbBox.height - this.getHeight()) / 2);

        getRootPane().setDefaultButton(_okButton);
        _okButton.addActionListener(this);
        _cancelButton.addActionListener(this);
    }

    ////////////////////////////////////////////////////////////////
    // event handlers

    public void actionPerformed(ActionEvent e) {
        // Cancel Button ------------------------------------------
        if (e.getSource() == _cancelButton) {
            buttonCancelActionPerformed();
        }
        // Ok Button ------------------------------------------
        if (e.getSource() == _okButton) {
            buttonOkActionPerformed();
        }
    }

    public void buttonCancelActionPerformed() {
        setVisible(false);
        dispose();
    }

    public void buttonOkActionPerformed() {
        for (int i = 0; i < _srcPathTableModel.getRowCount(); i++) {
            Object elem = _srcPathTableModel.getValueAt(i, 0);
            String path = (String)_srcPathTableModel.getValueAt(i, 3);
            if (elem != null
                && path != null
                && !path.equals(ModelFacade.getTaggedValue(elem, "src_path"))) {
                ModelFacade.setTaggedValue(elem, "src_path", path);
            }
        }
        buttonCancelActionPerformed();
    }
} /* end class SourcePathDialog */

/**
 * Provides support for setting a "src_path" tagged value used in Java 
 * round trip engineering.
 */
class SrcPathTableModel extends DefaultTableModel {

    /** Creates a new instance of SrcPathTableModel */
    public SrcPathTableModel() {
        super(new Object[][] {
        }, new String[] { "", "Name", "Type", "Source path" });       
        Project p = ProjectManager.getManager().getCurrentProject();
        Collection elems =
            ModelManagementHelper.getHelper().getAllModelElementsOfKind(
                (Class)ModelFacade.MODELELEMENT);
        elems.add(p.getRoot());
        Iterator iter = elems.iterator();
        while (iter.hasNext()) {
            Object me = iter.next();
            String path = Generator.getCodePath(me);
            if (path != null) {
                String type = "";
                String name = ModelFacade.getName(me);
                if (ModelFacade.isAModel(me)) {
                    type = "Model";
                } else if (ModelFacade.isAPackage(me)) {
                    type = "Package";
                    Object parent = ModelFacade.getNamespace(me);
                    while (parent != null) {
                        // ommit root package name; it's the model's root
                        if (ModelFacade.getNamespace(parent) != null)
                            name = ModelFacade.getName(parent) + "." + name;
                        parent = ModelFacade.getNamespace(parent);
                    }
                } else if (ModelFacade.isAClass(me)) {
                    type = "Class";
                } else if (ModelFacade.isAInterface(me)) {
                    type = "Interface";
                }
                addRow(new Object[] { me, name, type, path });
            }
        }
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 3;
    }
}
