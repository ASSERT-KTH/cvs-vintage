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

package org.columba.mail.gui.composer;

import javax.swing.JFrame;

import org.columba.addressbook.gui.AddressbookPanel;
import org.columba.core.command.TaskManager;
import org.columba.core.config.Config;
import org.columba.core.config.ViewItem;
import org.columba.core.util.CharsetManager;
import org.columba.mail.composer.MessageComposer;
import org.columba.mail.folder.Folder;
import org.columba.mail.gui.composer.action.ComposerActionListener;
import org.columba.mail.gui.composer.util.IdentityInfoPanel;
import org.columba.mail.gui.composer.util.UndoDocument;
import org.columba.mail.gui.tree.TreeView;
import org.columba.mail.pgp.PGPController;
import org.columba.core.main.MainInterface;

public class ComposerInterface
{
  
    public ComposerSpellCheck composerSpellCheck;
    
    public JFrame composerFrame;
   
    public ComposerActionListener composerActionListener;
    public TreeView treeViewer;
    public MessageComposer messageComposer;
   
    public TaskManager taskManager;
    public IdentityInfoPanel identityInfoPanel;
    public Config config;
    public UndoDocument message;
    public PGPController pgpController;
    public Folder composerFolder;
    public MainInterface mainInterface;
    
    
    public AddressbookPanel addressbookPanel;


	public AttachmentController attachmentController;
	public SubjectController subjectController;
	public PriorityController priorityController;
	public AccountController accountController;
	public EditorController editorController;
	public HeaderController headerController;
	
	public ComposerController composerController;
	
	public CharsetManager charsetManager;
	
	//public  WindowItem windowItem;
	public ViewItem viewItem;
	
	public JFrame addressbookFrame;
	
	
		
    public ComposerInterface( Config conf  )
        {
	    config = conf;
        }
        
    public ComposerInterface() {};

}





