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
package org.columba.mail.gui.composer.text;

import java.awt.Font;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.columba.core.main.MainInterface;
import org.columba.core.xml.XmlElement;
import org.columba.mail.gui.composer.AbstractEditorController;
import org.columba.mail.gui.composer.ComposerController;
import org.columba.mail.gui.composer.util.UndoDocument;


/**
 * Editor controller used when composing plain text mails.
 *
 * @author frd, Karl Peder Olesen (karlpeder)
 *
 */
public class TextEditorController extends AbstractEditorController
    implements DocumentListener, CaretListener, Observer {
    /*
     * *20030906, karlpeder* Changed to extend AbstractEditorController
     * to be able to fit into frame work supporting both plain text and
     * html composing.
     */

    //ComposerController controller;

    /** The editor view, i.e. the component used for editing text */
    private TextEditorView view;

    /** Document used in the editor view */
    private UndoDocument document;

    //	name of font
    private String name;

    // size of font
    private String size;

    // currently used font
    private Font font;

    // font configuration
    private XmlElement textFontElement;
    private XmlElement fonts;

    // overwrite look and feel font settings
    private boolean overwrite;

    public TextEditorController(ComposerController controller) {
        super(controller);

        //this.controller = controller;
        document = new UndoDocument();

        view = new TextEditorView(this, document);

        MainInterface.focusManager.registerComponent(this);

        view.addCaretListener(this);

        XmlElement options = MainInterface.config.get("options").getElement("/options");
        XmlElement guiElement = options.getElement("gui");
        fonts = guiElement.getElement("fonts");

        if (fonts == null) {
            fonts = guiElement.addSubElement("fonts");
        }

        overwrite = Boolean.valueOf(fonts.getAttribute("overwrite", "true"))
                           .booleanValue();

        // register for configuration changes
        fonts.addObserver(this);

        textFontElement = fonts.getElement("text");

        if (textFontElement == null) {
            textFontElement = fonts.addSubElement("text");
        }

        if (overwrite) {
            name = "Default";
            size = "12";

            font = new Font(name, Font.PLAIN, Integer.parseInt(size));
        } else {
            name = textFontElement.getAttribute("name", "Default");
            size = textFontElement.getAttribute("size", "12");

            font = new Font(name, Font.PLAIN, Integer.parseInt(size));
        }
    }

    /* *20030906, karlpeder* Removed, use misc. setView* / getView*
     *                       methods instead
    public TextEditorView getView() {
            return view;
    }
    */
    public void installListener() {
        view.installListener(this);
    }

    public void updateComponents(boolean b) {
        if (b) {
            if (this.getController().getModel().getBodyText() != null) {
                view.setText(controller.getModel().getBodyText());
            }
        } else {
            if (view.getText() != null) {
                this.getController().getModel().setBodyText(view.getText());
            }
        }
    }

    public void undo() {
        document.Undo();
    }

    public void redo() {
        document.Redo();
    }

    /************* DocumentListener implementation *******************/
    public void insertUpdate(DocumentEvent e) {
    }

    public void removeUpdate(DocumentEvent e) {
    }

    public void changedUpdate(DocumentEvent e) {
    }

    /************** FocusOwner implementation **************************/

    // the following lines add cut/copy/paste/undo/redo/selectall
    // actions support using the Columba action objects.
    // 
    // This means that we only have a single instance of these
    // specific actions, which is shared by all menuitems and
    // toolbar buttons.

    /* (non-Javadoc)
     * @see org.columba.core.gui.focus.FocusOwner#copy()
     */
    public void copy() {
        view.copy();
    }

    /* (non-Javadoc)
     * @see org.columba.core.gui.focus.FocusOwner#cut()
     */
    public void cut() {
        view.cut();
    }

    /* (non-Javadoc)
     * @see org.columba.core.gui.focus.FocusOwner#delete()
     */
    public void delete() {
        //view.cut(); // This would place text on clipboard
        view.replaceSelection("");
    }

    /* (non-Javadoc)
     * @see org.columba.core.gui.focus.FocusOwner#getComponent()
     */
    public JComponent getComponent() {
        return view;
    }

    /* (non-Javadoc)
     * @see org.columba.core.gui.focus.FocusOwner#isCopyActionEnabled()
     */
    public boolean isCopyActionEnabled() {
        if (view.getSelectedText() == null) {
            return false;
        }

        if (view.getSelectedText().length() > 0) {
            return true;
        }

        return false;
    }

    /* (non-Javadoc)
     * @see org.columba.core.gui.focus.FocusOwner#isCutActionEnabled()
     */
    public boolean isCutActionEnabled() {
        if (view.getSelectedText() == null) {
            return false;
        }

        if (view.getSelectedText().length() > 0) {
            return true;
        }

        return false;
    }

    /* (non-Javadoc)
     * @see org.columba.core.gui.focus.FocusOwner#isDeleteActionEnabled()
     */
    public boolean isDeleteActionEnabled() {
        if (view.getSelectedText() == null) {
            return false;
        }

        if (view.getSelectedText().length() > 0) {
            return true;
        }

        return false;
    }

    /* (non-Javadoc)
     * @see org.columba.core.gui.focus.FocusOwner#isPasteActionEnabled()
     */
    public boolean isPasteActionEnabled() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.columba.core.gui.focus.FocusOwner#isSelectAllActionEnabled()
     */
    public boolean isSelectAllActionEnabled() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.columba.core.gui.focus.FocusOwner#paste()
     */
    public void paste() {
        view.paste();
    }

    /* (non-Javadoc)
     * @see org.columba.core.gui.focus.FocusOwner#isRedoActionEnabled()
     */
    public boolean isRedoActionEnabled() {
        // TODO: use UndoableEditEvent to make this really work
        return true;
    }

    /* (non-Javadoc)
     * @see org.columba.core.gui.focus.FocusOwner#isUndoActionEnabled()
     */
    public boolean isUndoActionEnabled() {
        // TODO: use UndoableEditEvent to make this really work
        return true;
    }

    /* (non-Javadoc)
     * @see org.columba.core.gui.focus.FocusOwner#selectAll()
     */
    public void selectAll() {
        view.selectAll();
    }

    /************************** CaretUpdateListener interface *****************/

    /* (non-Javadoc)
     * @see javax.swing.event.CaretListener#caretUpdate(javax.swing.event.CaretEvent)
     */
    public void caretUpdate(CaretEvent arg0) {
        MainInterface.focusManager.updateActions();
    }

    /********************** Methods necessary to hide view from clients *******/

    /* (non-Javadoc)
     * @see org.columba.mail.gui.composer.AbstractEditorController#getViewUIComponent()
     */
    public JComponent getViewUIComponent() {
        // Returns the view encapsulated in a scroll pane. This means
        // that the caller shouldn't add the scroll pane him self
        return new JScrollPane(view);
    }

    /* (non-Javadoc)
     * @see org.columba.mail.gui.composer.AbstractEditorController#getViewFont()
     */
    public Font getViewFont() {
        return view.getFont();
    }

    /* (non-Javadoc)
     * @see org.columba.mail.gui.composer.AbstractEditorController#setViewFont(java.awt.Font)
     */
    public void setViewFont(Font f) {
        view.setFont(f);
    }

    /* (non-Javadoc)
     * @see org.columba.mail.gui.composer.AbstractEditorController#getViewText()
     */
    public String getViewText() {
        return view.getText();
    }

    /* (non-Javadoc)
     * @see org.columba.mail.gui.composer.AbstractEditorController#setViewText(java.lang.String)
     */
    public void setViewText(String text) {
        view.setText(text);
    }

    /* (non-Javadoc)
     * @see org.columba.mail.gui.composer.AbstractEditorController#setViewEnabled(boolean)
     */
    public void setViewEnabled(boolean enabled) {
        view.setEnabled(enabled);
    }

    /**
     * Gets fired when configuration changes occur.
     *
     * @see org.columba.core.gui.config.GeneralOptionsDialog
     *
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    public void update(Observable arg0, Object arg1) {
        // fonts
        overwrite = Boolean.valueOf(fonts.getAttribute("overwrite", "true"))
                           .booleanValue();

        if (overwrite == false) {
            // use default font settings
            name = "Default";
            size = "12";

            font = new Font(name, Font.PLAIN, Integer.parseInt(size));
        } else {
            // overwrite look and feel font settings
            name = textFontElement.getAttribute("name", "Default");
            size = textFontElement.getAttribute("size", "12");

            font = new Font(name, Font.PLAIN, Integer.parseInt(size));

            setViewFont(font);
        }
    }
}
