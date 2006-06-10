//The contents of this file are subject to the Mozilla Public License Version 1.1
//(the "License"); you may not use this file except in compliance with the 
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License 
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.
package org.columba.core.gui.externaltools;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import net.javaprog.ui.wizard.AbstractStep;
import net.javaprog.ui.wizard.DataLookup;
import net.javaprog.ui.wizard.DataModel;

import org.columba.core.gui.base.ButtonWithMnemonic;
import org.columba.core.gui.base.LabelWithMnemonic;
import org.columba.core.gui.base.MultiLineLabel;
import org.columba.core.gui.base.WizardTextField;
import org.columba.core.resourceloader.GlobalResourceLoader;


/**
 * Asks the user about the location of an executable file.
 * <p>
 * Usually this should should include a short explanation about
 * what the tool does, where to download, etc.
 *
 * @author fdietz
 */
class LocationStep extends AbstractStep implements ActionListener {
    private static final String RESOURCE_PATH = "org.columba.core.i18n.dialog";
    protected DataModel data;
    protected JButton sourceButton;
    protected File sourceFile;

    /**
 * @param arg0
 * @param arg1
 */
    public LocationStep(DataModel data) {
        super(GlobalResourceLoader.getString(RESOURCE_PATH, "externaltools",
                "LocationStep.title"),
            GlobalResourceLoader.getString(RESOURCE_PATH, "externaltools",
                "LocationStep.description"));

        this.data = data;

        data.registerDataLookup("Location.source",
            new DataLookup() {
                public Object lookupData() {
                    return sourceFile;
                }
            });

        setCanGoNext(false);
    }

    /* (non-Javadoc)
 * @see net.javaprog.ui.wizard.AbstractStep#createComponent()
 */
    protected JComponent createComponent() {
        JPanel panel = new JPanel(new BorderLayout());

        AbstractExternalToolsPlugin plugin = (AbstractExternalToolsPlugin) data.getData(
                "Plugin");

        sourceFile = plugin.locate();

        if (sourceFile == null) {
            MultiLineLabel label = new MultiLineLabel(GlobalResourceLoader.getString(
                        RESOURCE_PATH, "externaltools", "LocationStep.noauto"));
            panel.add(label, BorderLayout.NORTH);

            WizardTextField middlePanel = new WizardTextField();
            LabelWithMnemonic sourceLabel = new LabelWithMnemonic(GlobalResourceLoader.getString(
                        RESOURCE_PATH, "externaltools", "LocationStep.location"));
            middlePanel.add(sourceLabel);

            sourceButton = new ButtonWithMnemonic(GlobalResourceLoader.getString(
                        RESOURCE_PATH, "externaltools", "LocationStep.browse"));
            sourceLabel.setLabelFor(sourceButton);
            sourceButton.addActionListener(this);
            middlePanel.add(sourceButton);

            panel.add(middlePanel, BorderLayout.CENTER);
        } else {
            JPanel northPanel = new JPanel(new GridLayout(2, 1, 0, 15));
            MultiLineLabel label = new MultiLineLabel(GlobalResourceLoader.getString(
                        RESOURCE_PATH, "externaltools", "LocationStep.auto"));
            northPanel.add(label);

            JPanel sourceFilePanel = new JPanel(new FlowLayout(
                        FlowLayout.LEFT, 20, 0));
            JLabel label2 = new JLabel(sourceFile.getPath());
            Font font = (Font) UIManager.getFont("Label.font");
            font = font.deriveFont(Font.BOLD);
            label2.setFont(font);
            sourceFilePanel.add(label2);

            sourceButton = new ButtonWithMnemonic(GlobalResourceLoader.getString(
                        RESOURCE_PATH, "externaltools", "LocationStep.change"));
            sourceButton.addActionListener(this);
            sourceFilePanel.add(sourceButton);

            northPanel.add(sourceFilePanel);
            panel.add(northPanel, BorderLayout.NORTH);
        }

        return panel;
    }

    /* (non-Javadoc)
 * @see net.javaprog.ui.wizard.Step#prepareRendering()
 */
    public void prepareRendering() {
        // init component before querying for sourceFile
        getComponent();
        updateCanFinish();
    }

    protected void updateCanFinish() {
        setCanFinish(sourceFile != null);
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == sourceButton) {
            JFileChooser fc = new JFileChooser();
            fc.setMultiSelectionEnabled(true);
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fc.setFileHidingEnabled(false);

            if (fc.showOpenDialog(getComponent()) == JFileChooser.APPROVE_OPTION) {
                sourceFile = fc.getSelectedFile();

                sourceButton.setText(sourceFile.getPath());
            }

            updateCanFinish();
        }
    }
}
