/*
 * $Revision: 1.2 $
 * $Date: 2004/08/22 15:55:07 $
 * $Author: fdietz $
 *
 * Copyright (C) 2001 C. Scott Willy
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.columba.mail.spellcheck.cswilly;


//import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


/**
 * A validator of a spell check results
 *<p>
 * After a spell check engine runs, its results must be validated (normally by
 * a user). The {@link Validator} class provides this service.
 */
public class Validator {
    private final HashMap _changeAllMap = new HashMap();
    private final HashSet _ignoreAllSet = new HashSet();

    /**
 * Validate a line of words that have the <code>results</code> of a spell
 * check.
 *<p>
 * @param line String with a line of words that are to be corrected
 * @param results List of {@link Result} of a spell check
 * @return new line with all corrected words validated
 */
    public String validate(String line, List results) {
        String checkedLine = line;

        for (int ii = results.size() - 1; ii >= 0; ii--) {
            Result result = (Result) results.get(ii);

            if (result.getType() != Result.OK) {
                String replacementWord;

                if (_changeAllMap.containsKey(result.getOriginalWord())) {
                    replacementWord = (String) _changeAllMap.get(result.getOriginalWord());
                } else if (_ignoreAllSet.contains(result.getOriginalWord())) {
                    replacementWord = result.getOriginalWord();
                } else {
                    replacementWord = validate(result);

                    if (replacementWord == null) {
                        checkedLine = null;

                        break;
                    }
                }

                if (replacementWord != null) {
                    checkedLine = replaceWord(checkedLine,
                            result.getOriginalWord(), result.getOffset(),
                            replacementWord);
                }
            }
        }

        return checkedLine;
    }

    /**
 * Validates a single correction
 *<p>
 *
 *<p>
 * @param result A {@link Result} of spell checking one word
 * @return validated correction (this is the replacement word). <i>null</i>
 *         is returned if the operation is cancelled. The replacement word
 *         maybe the same or different from the original word in
 *         <code>result</code>.
 */
    public String validate(Result result) {
        String replacementWord = null;

        ValidationDialog validationDialog;
        validationDialog = new ValidationDialog(result.getOriginalWord(),
                result.getSuggestions());
        validationDialog.show();

        ValidationDialog.UserAction userAction = validationDialog.getUserAction();

        if (userAction == ValidationDialog.CANCEL) {
            replacementWord = null;
        } else if (userAction == ValidationDialog.CHANGE_ALL) {
            if (_changeAllMap.containsKey(result.getOriginalWord())) {
                System.err.println(
                    "Validator error: Change  all twice same word: " +
                    result.getOriginalWord());
            }

            _changeAllMap.put(result.getOriginalWord(),
                validationDialog.getSelectedWord());
            replacementWord = validationDialog.getSelectedWord();
        } else if (userAction == ValidationDialog.CHANGE) {
            replacementWord = validationDialog.getSelectedWord();
        } else if (userAction == ValidationDialog.IGNORE_ALL) {
            if (_ignoreAllSet.contains(result.getOriginalWord())) {
                System.err.println(
                    "Validator error: Ignore all twice same word: " +
                    result.getOriginalWord());
            }

            _ignoreAllSet.add(result.getOriginalWord());
            replacementWord = result.getOriginalWord();
        } else if (userAction == ValidationDialog.IGNORE) {
            replacementWord = result.getOriginalWord();
        }

        return replacementWord;
    }

    /**
 * Helper method to replace the original word with the correction in the line
 */
    protected String replaceWord(String originalLine, String originalWord,
        int originalIndex, String replacementWord) {
        String leftText = originalLine.substring(0, originalIndex - 1);
        String rightText = originalLine.substring((originalIndex +
                originalWord.length()) - 1);

        StringBuffer buf = new StringBuffer();
        buf.append(leftText);
        buf.append(replacementWord);
        buf.append(rightText);

        return buf.toString();
    }
}
