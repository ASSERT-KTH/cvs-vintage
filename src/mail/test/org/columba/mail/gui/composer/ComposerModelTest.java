// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.mail.gui.composer;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.columba.ristretto.message.Address;

/**
 * @author fdietz
 *  
 */
public class ComposerModelTest extends TestCase {

	/**
	 * Test using String method.
	 *
	 */
	public void testGetInvalidRecipients() {
		ComposerModel model = new ComposerModel();
		model.setTo("test@mail.de");

		String invalidEmail = model.getInvalidRecipients();
		System.out.println(invalidEmail);
		assertNull(invalidEmail);
	}
	
	/**
	 * Test using String method.
	 *
	 */
	public void testGetInvalidRecipientsString2() {
		ComposerModel model = new ComposerModel();
		model.setTo("test@top.test.de");

		String invalidEmail = model.getInvalidRecipients();
		System.out.println(invalidEmail);
		assertNull(invalidEmail);
	}

	/**
	 * Test using two recipients as String
	 *
	 */
	public void testGetInvalidRecipients2() {
		ComposerModel model = new ComposerModel();
		model.setTo("test@mail.de, t@t.de");

		String invalidEmail = model.getInvalidRecipients();
		System.out.println(invalidEmail);
		assertNull(invalidEmail);
	}

	/**
	 * Test using Address[] array method.
	 *
	 */
	public void testGetInvalidRecipients3() {
		ComposerModel model = new ComposerModel();
		model.setTo(new Address[] { new Address("test@mail.de"),
				new Address("t@t.de") });

		String invalidEmail = model.getInvalidRecipients();
		System.out.println(invalidEmail);
		assertNull(invalidEmail);
	}

	/**
	 * Test using List method.
	 *
	 */
	public void testGetInvalidRecipients4() {
		ComposerModel model = new ComposerModel();
		ArrayList l = new ArrayList();
		l.add("test@mail.de");
		l.add("t@t.de");
		model.setToList(l);

		String invalidEmail = model.getInvalidRecipients();
		System.out.println(invalidEmail);
		assertNull(invalidEmail);
	}

}