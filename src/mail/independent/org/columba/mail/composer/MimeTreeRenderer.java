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

package org.columba.mail.composer;

import java.lang.reflect.Array;
import java.util.Hashtable;

import org.columba.core.command.WorkerStatusController;
import org.columba.mail.message.MimeHeader;
import org.columba.mail.message.MimePart;
import org.columba.mail.message.MimePartTree;

/**
 * @author timo
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class MimeTreeRenderer {
	
	private static final String rendererPath = "org.columba.mail.composer.mimepartrenderers.";
	
	private static final String[] renderers = {
		"MultipartRenderer", "MultipartSignedRenderer" };
	
	private static MimeTreeRenderer myInstance;
	
	private Hashtable rendererTable;
	private MimePartRenderer defaultRenderer;
	
	protected MimeTreeRenderer() {
		rendererTable = new Hashtable();
		loadAllRenderer();
		
		defaultRenderer = new DefaultMimePartRenderer();
	}
	
	public static MimeTreeRenderer getInstance() {
		
		if( myInstance == null )
			myInstance = new MimeTreeRenderer();
		
		return myInstance;	
	}
	
	public String render( MimePartTree tree, WorkerStatusController workerStatusController ) {
		return renderMimePart( tree.getRootMimeNode(), workerStatusController );
	}
	
	public String renderMimePart( MimePart part, WorkerStatusController workerStatusController ) {		
		MimePartRenderer renderer = getRenderer( part.getHeader() );
		
		return renderer.render(part, workerStatusController);	
	}
	
	private MimePartRenderer getRenderer( MimeHeader input ) {
		// If no ContentType specified return StandardParser
		if (input.contentType == null)
			return defaultRenderer;

		MimePartRenderer renderer;

		// First try to find renderer for "type/subtype"

		renderer =
			(MimePartRenderer) rendererTable.get(
				input.contentType + "/" + input.contentSubtype);
		if (renderer != null) {
			return renderer;
		}

		// Next try to find renderer for "type"

		renderer = (MimePartRenderer) rendererTable.get(input.contentType);
		if (renderer != null) {
			return renderer;
		}

		// Nothing found -> return Standardrenderer
		return defaultRenderer;
		
	}

	private void loadAllRenderer() {
		ClassLoader loader = ClassLoader.getSystemClassLoader();
		Class actClass = null;

		try {
			for (int i = 0; i < Array.getLength(renderers); i++) {
				actClass = loader.loadClass(rendererPath + renderers[i]);

				if (actClass
					.getSuperclass()
					.getName()
					.equals("org.columba.mail.composer.MimePartRenderer")) {

					MimePartRenderer renderer =
						(MimePartRenderer) actClass.newInstance();
							
					rendererTable.put( renderer.getRegisterString(), renderer);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
