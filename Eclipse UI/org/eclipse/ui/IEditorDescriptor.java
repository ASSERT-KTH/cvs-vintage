package org.eclipse.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Description of an editor in the workbench editor registry. The
 * editor descriptor contains the information needed to create editor instances.
 * <p>
 * An editor descriptor typically represents one of three types of editors:
 * <ul>
 *   <li>a file editor extension for a specific file extension.</li>
 *   <li>a file editor added by the user (via the workbench preference page)</li>
 *   <li>a general editor extension which works on objects other than files.</li>
 * </ul>
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @see IEditorRegistry
 */
public interface IEditorDescriptor extends IWorkbenchPartDescriptor {
/**
 * Returns the editor id.
 * <p>
 * For internal editors, this is the extension id as defined in the workbench
 * registry; for external editors, it is path and file name of the external 
 * program.
 * </p>
 *
 * @return the id of the editor
 */
public String getId();
/**
 * Returns the descriptor of the image for this editor.
 *
 * @return the descriptor of the image to display next to this editor
 */
public ImageDescriptor getImageDescriptor();
/**
 * Returns the label to show for this editor.
 *
 * @return the editor label
 */
public String getLabel();
}
