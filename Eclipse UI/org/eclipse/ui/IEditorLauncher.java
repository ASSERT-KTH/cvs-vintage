package org.eclipse.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.IFile;

/**
 * An editor launcher is used to launch external editors on an
 * <code>IFile</code>input.
 * <p>
 * Clients should implement this interface to define a new type of editor
 * launcher.  Each new launcher must be registered as an editor in the 
 * workbench's editor extension point 
 * (named <code>"org.eclipse.ui.exportWizards"</code>).
 *
 * For example, the plug-in's XML markup might contain:
 * <pre>
 * &LT;extension point = "org.eclipse.ui.editors"&GT;
 *   &LT;editor
 *       id="org.eclipse.ui.SwingEditorLauncher"
 *       name="Swing Editor"
 *       extensions="xml"
 *       launcher="org.eclipse.ui.examples.swingeditor.SwingEditorLauncher"
 *       icon="icons/xml.gif"&GT;
 *   &LT;/editor&GT;
 * &LT;/extension&GT;
 * </pre>
 * </p><p>
 * In this example a launcher has been registered for use with <code>xml</code>
 * files.  Once registered, the launcher will appear in the <code>Open With</code>
 * menu for an <code>xml</code> file.  If the item is invoked the workbench will
 * create an instance of the launcher class and call <code>open</code> on it,
 * passing the input file.
 * </p>
 */
public interface IEditorLauncher{
/**
 * Launches this external editor to edit the given file.
 *
 * @param file the file to edit
 */
public void open(IFile file);
}
