package org.columba.core.base;

/**
 * Text utilities.
 * 
 * @author Frederik Dietz
 */
public class TextUtil {

	/**
	 * Replace all occurences of <code>oldPattern</code> with <code>newPattern</code>
	 * 
	 * @param inputString		input string
	 * @param oldPattern		old pattern
	 * @param newPattern		new pattern
	 * @return					new string
	 */
	public static String replaceAll(final String inputString,
			final String oldPattern, final String newPattern) {
		
		if (oldPattern.equals("")) {
			throw new IllegalArgumentException("Old pattern must have content.");
		}

		final StringBuffer result = new StringBuffer();
		// startIdx and idxOld delimit various chunks of aInput; these
		// chunks always end where aOldPattern begins
		int startIdx = 0;
		int idxOld = 0;
		while ((idxOld = inputString.indexOf(oldPattern, startIdx)) >= 0) {
			// grab a part of aInput which does not include aOldPattern
			result.append(inputString.substring(startIdx, idxOld));
			// add aNewPattern to take place of aOldPattern
			result.append(newPattern);

			// reset the startIdx to just after the current match, to see
			// if there are any further matches
			startIdx = idxOld + oldPattern.length();
		}
		// the final chunk will go to the end of aInput
		result.append(inputString.substring(startIdx));
		return result.toString();
	}
}
