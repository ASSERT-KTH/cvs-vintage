/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.keys;

import java.util.Comparator;
import java.util.HashMap;
import java.util.ResourceBundle;

import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.keys.CharacterKey;
import org.eclipse.ui.keys.Key;
import org.eclipse.ui.keys.ModifierKey;
import org.eclipse.ui.keys.SpecialKey;

public final class MacKeyFormatter extends AbstractKeyFormatter {

	private final static class MacModifierKeyComparator
		extends AbstractModifierKeyComparator {

		protected int rank(ModifierKey modifierKey) {
			if (ModifierKey.SHIFT.equals(modifierKey)) {
				return 0;
			}

			if (ModifierKey.CTRL.equals(modifierKey)) {
				return 1;
			}

			if (ModifierKey.ALT.equals(modifierKey)) {
				return 2;
			}

			if (ModifierKey.COMMAND.equals(modifierKey)) {
				return 3;
			}

			return Integer.MAX_VALUE;
		}
	}

	private final static HashMap KEY_LOOKUP = new HashMap();
	private final static Comparator MODIFIER_KEY_COMPARATOR =
		new MacModifierKeyComparator();
	private final static ResourceBundle RESOURCE_BUNDLE =
		ResourceBundle.getBundle(MacKeyFormatter.class.getName());

	static {
		KEY_LOOKUP.put(CharacterKey.BS_NAME, Character.toString('\u232B'));
		KEY_LOOKUP.put(CharacterKey.CR_NAME, Character.toString('\u21A9'));
		KEY_LOOKUP.put(CharacterKey.DEL_NAME, Character.toString('\u2326'));
		KEY_LOOKUP.put(CharacterKey.SPACE_NAME, Character.toString('\u2423'));
		KEY_LOOKUP.put(ModifierKey.ALT_NAME, Character.toString('\u2325'));
		KEY_LOOKUP.put(ModifierKey.COMMAND_NAME, Character.toString('\u2318'));
		KEY_LOOKUP.put(ModifierKey.CTRL_NAME, Character.toString('\u2303'));
		KEY_LOOKUP.put(ModifierKey.SHIFT_NAME, Character.toString('\u21E7'));
		KEY_LOOKUP.put(
			SpecialKey.ARROW_DOWN_NAME,
			Character.toString('\u2193'));
		KEY_LOOKUP.put(
			SpecialKey.ARROW_LEFT_NAME,
			Character.toString('\u2190'));
		KEY_LOOKUP.put(
			SpecialKey.ARROW_RIGHT_NAME,
			Character.toString('\u2192'));
		KEY_LOOKUP.put(SpecialKey.ARROW_UP_NAME, Character.toString('\u2191'));
		KEY_LOOKUP.put(SpecialKey.END_NAME, Character.toString('\u2198'));
		KEY_LOOKUP.put(
			SpecialKey.NUMPAD_ENTER_NAME,
			Character.toString('\u2324'));
		KEY_LOOKUP.put(SpecialKey.HOME_NAME, Character.toString('\u2196'));
		KEY_LOOKUP.put(SpecialKey.PAGE_DOWN_NAME, Character.toString('\u21DF'));
		KEY_LOOKUP.put(SpecialKey.PAGE_UP_NAME, Character.toString('\u21DE'));
	}

	public String format(Key key) {
		String string = (String) KEY_LOOKUP.get(key.toString());
		return string != null ? string : super.format(key);
	}

	protected String getKeyDelimiter() {
		return Util.ZERO_LENGTH_STRING;
	}

	protected String getKeyStrokeDelimiter() {
		return Util.translateString(
			RESOURCE_BUNDLE,
			KEY_STROKE_DELIMITER_KEY,
			KEY_STROKE_DELIMITER,
			false,
			false);
	}

	protected Comparator getModifierKeyComparator() {
		return MODIFIER_KEY_COMPARATOR;
	}
}
