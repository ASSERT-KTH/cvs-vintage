/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.keys;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.eclipse.ui.internal.util.Util;

/**
 * <p>
 * A <code>KeyStroke</code> is defined as an optional set of modifier keys
 * followed optionally by a natural key. A <code>KeyStroke</code> is said to
 * be complete if it contains a natural key.
 * </p>
 * <p>
 * All <code>KeyStroke</code> objects have a formal string representation
 * available via the <code>toString()</code> method. There are a number of
 * methods to get instances of <code>KeyStroke</code> objects, including one
 * which can parse this formal string representation.
 * </p>
 * <p>
 * All <code>KeyStroke</code> objects, via the <code>format()</code>
 * method, provide a version of their formal string representation translated
 * by platform and locale, suitable for display to a user.
 * </p>
 * <p>
 * <code>KeyStroke</code> objects are immutable. Clients are not permitted to
 * extend this class.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 */
public final class KeyStroke implements Comparable {

	/**
	 * An internal constant used only in this object's hash code algorithm.
	 */
	private final static int HASH_FACTOR = 89;

	/**
	 * An internal constant used only in this object's hash code algorithm.
	 */
	private final static int HASH_INITIAL = KeyStroke.class.getName().hashCode();

	/**
	 * The set of delimiters for <code>Key</code> objects allowed during
	 * parsing of the formal string representation.
	 */
	public final static String KEY_DELIMITERS = KeyFormatter.KEY_DELIMITER;

	/**
	 * Gets an instance of <code>KeyStroke</code> given a single modifier key
	 * and a natural key.
	 * 
	 * @param modifierKey
	 *            a modifier key. Must not be <code>null</code>.
	 * @param naturalKey
	 *            the natural key. May be <code>null</code>.
	 * @return a key stroke. Guaranteed not to be <code>null</code>.
	 */
	public static KeyStroke getInstance(ModifierKey modifierKey, NaturalKey naturalKey) {
		if (modifierKey == null)
			throw new NullPointerException();

		return new KeyStroke(new TreeSet(Collections.singletonList(modifierKey)), naturalKey);
	}

	/**
	 * Gets an instance of <code>KeyStroke</code> given an array of modifier
	 * keys and a natural key.
	 * 
	 * @param modifierKeys
	 *            the array of modifier keys. This array may be empty, but it
	 *            must not be <code>null</code>. If this array is not empty,
	 *            it must not contain <code>null</code> elements.
	 * @param naturalKey
	 *            the natural key. May be <code>null</code>.
	 * @return a key stroke. Guaranteed not to be <code>null</code>.
	 */
	public static KeyStroke getInstance(ModifierKey[] modifierKeys, NaturalKey naturalKey) {
		Util.assertInstance(modifierKeys, ModifierKey.class);
		return new KeyStroke(new TreeSet(Arrays.asList(modifierKeys)), naturalKey);
	}

	/**
	 * Gets an instance of <code>KeyStroke</code> given a natural key.
	 * 
	 * @param naturalKey
	 *            the natural key. May be <code>null</code>.
	 * @return a key stroke. This key stroke will have no modifier keys.
	 *         Guaranteed not to be <code>null</code>.
	 */
	public static KeyStroke getInstance(NaturalKey naturalKey) {
		return new KeyStroke(Util.EMPTY_SORTED_SET, naturalKey);
	}

	/**
	 * Gets an instance of <code>KeyStroke</code> given a set of modifier
	 * keys and a natural key.
	 * 
	 * @param modifierKeys
	 *            the set of modifier keys. This set may be empty, but it must
	 *            not be <code>null</code>. If this set is not empty, it
	 *            must only contain instances of <code>ModifierKey</code>.
	 * @param naturalKey
	 *            the natural key. May be <code>null</code>.
	 * @return a key stroke. Guaranteed not to be <code>null</code>.
	 */
	public static KeyStroke getInstance(SortedSet modifierKeys, NaturalKey naturalKey) {
		return new KeyStroke(modifierKeys, naturalKey);
	}

	/**
	 * Gets an instance of <code>KeyStroke</code> by parsing a given a formal
	 * string representation.
	 * 
	 * @param string
	 *            the formal string representation to parse.
	 * @return a key stroke. Guaranteed not to be <code>null</code>.
	 * @throws ParseException
	 *             if the given formal string representation could not be
	 *             parsed to a valid key stroke.
	 */
	public static KeyStroke getInstance(String string) throws ParseException {
		if (string == null)
			throw new NullPointerException();

		SortedSet modifierKeys = new TreeSet();
		NaturalKey naturalKey = null;
		StringTokenizer stringTokenizer = new StringTokenizer(string, KEY_DELIMITERS, true);
		int i = 0;

		while (stringTokenizer.hasMoreTokens()) {
			String token = stringTokenizer.nextToken();

			if (i % 2 == 0) {
				if (stringTokenizer.hasMoreTokens()) {
					token = token.toUpperCase();
					ModifierKey modifierKey =
						(ModifierKey) ModifierKey.modifierKeysByName.get(token);

					if (modifierKey == null || !modifierKeys.add(modifierKey))
						throw new ParseException();
				} else if (token.length() == 1) {
					naturalKey = CharacterKey.getInstance(token.charAt(0));
					break;
				} else {
					token = token.toUpperCase();
					naturalKey = (NaturalKey) CharacterKey.characterKeysByName.get(token);

					if (naturalKey == null)
						naturalKey = (NaturalKey) SpecialKey.specialKeysByName.get(token);

					if (naturalKey == null)
						throw new ParseException();
				}
			}

			i++;
		}

		try {
			return new KeyStroke(modifierKeys, naturalKey);
		} catch (Throwable t) {
			throw new ParseException();
		}
	}

	/**
	 * The cached hash code for this object. Because <code>KeyStroke</code>
	 * objects are immutable, their hash codes need only to be computed once.
	 * After the first call to <code>hashCode()</code>, the computed value
	 * is cached here for all subsequent calls.
	 */
	private transient int hashCode;

	/**
	 * A flag to determine if the <code>hashCode</code> field has already
	 * been computed.
	 */
	private transient boolean hashCodeComputed;

	/**
	 * The set of modifier keys for this key stroke.
	 */
	private SortedSet modifierKeys;

	/**
	 * The set of modifier keys for this key stroke in the form of an array.
	 * Used internally by <code>int compareTo(Object)</code>.
	 */
	private transient ModifierKey[] modifierKeysAsArray;

	/**
	 * The natural key for this key stroke.
	 */
	private NaturalKey naturalKey;

	/**
	 * Constructs an instance of <code>KeyStroke</code> given a set of
	 * modifier keys and a natural key.
	 * 
	 * @param modifierKeys
	 *            the set of modifier keys. This set may be empty, but it must
	 *            not be <code>null</code>. If this set is not empty, it
	 *            must only contain instances of <code>ModifierKey</code>.
	 * @param naturalKey
	 *            the natural key. May be <code>null</code>.
	 */
	private KeyStroke(SortedSet modifierKeys, NaturalKey naturalKey) {
		this.modifierKeys = Util.safeCopy(modifierKeys, ModifierKey.class);
		this.naturalKey = naturalKey;
		this.modifierKeysAsArray =
			(ModifierKey[]) this.modifierKeys.toArray(new ModifierKey[this.modifierKeys.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object object) {
		KeyStroke castedObject = (KeyStroke) object;
		int compareTo = Util.compare(modifierKeysAsArray, castedObject.modifierKeysAsArray);

		if (compareTo == 0)
			compareTo = Util.compare(naturalKey, castedObject.naturalKey);

		return compareTo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object object) {
		if (!(object instanceof KeyStroke))
			return false;

		KeyStroke castedObject = (KeyStroke) object;
		boolean equals = true;
		equals &= modifierKeys.equals(castedObject.modifierKeys);
		equals &= Util.equals(naturalKey, castedObject.naturalKey);
		return equals;
	}

	/**
	 * Formats this key stroke into the native look.
	 * 
	 * @return A string representation for this key stroke using the native
	 *         look; never <code>null</code>.
	 */
	public String format() {
		return format(FormatManager.NATIVE);
	}

	/**
	 * Formats this key stroke into the given <code>format</code>.
	 * 
	 * @param format
	 *            The integer constant representing the format you want. This
	 *            value must be one of the constants defined in the <code>FormatManager</code>.
	 *            If it is not, then it the <code>FormalKeyFormatter</code>
	 *            is used.
	 * @return A string representation for this key stroke in the given format;
	 *         never <code>null</code>.
	 */
	public String format(int format) {
		return FormatManager.getFormatter(format).formatKeyStroke(this);
	}

	/**
	 * Returns the set of modifier keys for this key stroke.
	 * 
	 * @return the set of modifier keys. This set may be empty, but is
	 *         guaranteed not to be <code>null</code>. If this set is not
	 *         empty, it is guaranteed to only contain instances of <code>ModifierKey</code>.
	 */
	public Set getModifierKeys() {
		return Collections.unmodifiableSet(modifierKeys);
	}

	/**
	 * Returns the natural key for this key stroke.
	 * 
	 * @return the natural key. May be <code>null</code>.
	 */
	public NaturalKey getNaturalKey() {
		return naturalKey;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		if (!hashCodeComputed) {
			hashCode = HASH_INITIAL;
			hashCode = hashCode * HASH_FACTOR + modifierKeys.hashCode();
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(naturalKey);
			hashCodeComputed = true;
		}

		return hashCode;
	}

	/**
	 * Returns whether or not this key stroke is complete. Key strokes are
	 * complete iff they have a natural key which is not <code>null</code>.
	 * 
	 * @return <code>true</code>, iff the key stroke is complete.
	 */
	public boolean isComplete() {
		return naturalKey != null;
	}

	/**
	 * Returns the formal string representation for this key stroke.
	 * 
	 * @return The formal string representation for this key stroke. Guaranteed
	 *         not to be <code>null</code>.
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return format(FormatManager.FORMAL);
	}
}
