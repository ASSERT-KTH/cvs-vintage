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
package org.columba.mail.imap;

import org.columba.mail.filter.FilterCriteria;
import org.columba.mail.filter.FilterRule;

import org.columba.ristretto.imap.protocol.Arguments;
import org.columba.ristretto.imap.protocol.Atom;
import org.columba.ristretto.message.RFC822Date;

import java.io.UnsupportedEncodingException;

import java.util.Date;
import java.util.List;
import java.util.Vector;


/**
 * Builds IMAP search request strings, from {@link FilterList}
 * instances.
 * <p>
 * Note, that we combine every {@link Filter} to one big search
 * request.
 *
 * @author fdietz
 */
public class SearchRequestBuilder {
    protected String charset;
    protected boolean usesCharset = false;

    public SearchRequestBuilder() {
    }

    protected Arguments createBccArguments(FilterCriteria criteria)
        throws UnsupportedEncodingException {
        Arguments args = new Arguments();

        // we need to append "NOT"
        if (criteria.getCriteria() == FilterCriteria.CONTAINS_NOT) {
            args.add(new Atom("NOT"));
        }

        args.add(new Atom("BCC"));

        String pattern = criteria.getPattern();

        if (isAscii(pattern)) {
            args.add(pattern);
        } else {
            args.add(pattern, charset);
        }

        return args;
    }

    protected Arguments createBodyArguments(FilterCriteria criteria)
        throws UnsupportedEncodingException {
        Arguments args = new Arguments();

        // we need to append "NOT"
        if (criteria.getCriteria() == FilterCriteria.CONTAINS_NOT) {
            args.add(new Atom("NOT"));
        }

        args.add(new Atom("BODY"));

        String pattern = criteria.getPattern();

        if (isAscii(pattern)) {
            args.add(pattern);
        } else {
            args.add(pattern, charset);
        }

        return args;
    }

    protected Arguments createCcArguments(FilterCriteria criteria)
        throws UnsupportedEncodingException {
        Arguments args = new Arguments();

        // we need to append "NOT"
        if (criteria.getCriteria() == FilterCriteria.CONTAINS_NOT) {
            args.add(new Atom("NOT"));
        }

        args.add(new Atom("CC"));

        String pattern = criteria.getPattern();

        if (isAscii(pattern)) {
            args.add(pattern);
        } else {
            args.add(pattern, charset);
        }

        return args;
    }

    private Date transformDate(String pattern) {
        java.text.DateFormat df = java.text.DateFormat.getDateInstance();
        Date searchPattern = null;

        try {
            searchPattern = df.parse(pattern);
        } catch (java.text.ParseException ex) {
            System.out.println("exception: " + ex.getMessage());
            ex.printStackTrace();
        }

        return searchPattern;
    }

    protected Arguments createDateArguments(FilterCriteria criteria)
        throws UnsupportedEncodingException {
        Arguments args = new Arguments();

        args.add(new Atom("DATE"));

        if (criteria.getCriteria() == FilterCriteria.DATE_BEFORE) {
            args.add(new Atom("SENTBEFORE"));
        } else {
            args.add(new Atom("SENTAFTER"));
        }

        // transform text to Date representation
        Date date = transformDate(criteria.getPattern());

        // transform Date-object to RFC822-Date format
        args.add(RFC822Date.toString(date));

        return args;
    }

    protected Arguments createFlagsArguments(FilterCriteria criteria)
        throws UnsupportedEncodingException {
        Arguments args = new Arguments();

        //			we need to append "NOT"
        if (criteria.getCriteria() == FilterCriteria.CONTAINS_NOT) {
            args.add(new Atom("NOT"));
        }

        String headerField = criteria.getPattern();

        if (headerField.equalsIgnoreCase("Answered")) {
            args.add(new Atom("ANSWERED"));
        } else if (headerField.equalsIgnoreCase("Deleted")) {
            args.add(new Atom("DELETED"));
        } else if (headerField.equalsIgnoreCase("Flagged")) {
            args.add(new Atom("FLAGGED"));
        } else if (headerField.equalsIgnoreCase("Recent")) {
            args.add(new Atom("NEW"));
        } else if (headerField.equalsIgnoreCase("Draft")) {
            args.add(new Atom("DRAFT"));
        } else if (headerField.equalsIgnoreCase("Seen")) {
            args.add(new Atom("SEEN"));
        } 

        return args;
    }

    protected Arguments createFromArguments(FilterCriteria criteria)
        throws UnsupportedEncodingException {
        Arguments args = new Arguments();

        // we need to append "NOT"
        if (criteria.getCriteria() == FilterCriteria.CONTAINS_NOT) {
            args.add(new Atom("NOT"));
        }

        args.add(new Atom("FROM"));

        String pattern = criteria.getPattern();

        if (isAscii(pattern)) {
            args.add(pattern);
        } else {
            args.add(pattern, charset);
        }

        return args;
    }

    protected Arguments createPriorityArguments(FilterCriteria criteria)
        throws UnsupportedEncodingException {
        Arguments args = new Arguments();

        args.add(new Atom("HEADER"));

        args.add(new Atom("X-Priority"));

        Integer searchPattern = null;
        String pattern = criteria.getPattern();

        if (pattern.equalsIgnoreCase("Highest")) {
            searchPattern = new Integer(1);
        } else if (pattern.equalsIgnoreCase("High")) {
            searchPattern = new Integer(2);
        } else if (pattern.equalsIgnoreCase("Normal")) {
            searchPattern = new Integer(3);
        } else if (pattern.equalsIgnoreCase("Low")) {
            searchPattern = new Integer(4);
        } else if (pattern.equalsIgnoreCase("Lowest")) {
            searchPattern = new Integer(5);
        }

        args.add(searchPattern.toString());

        return args;
    }

    /*
protected Arguments createSizeArguments(FilterCriteria criteria)
    throws UnsupportedEncodingException {
    Arguments args = new Arguments();

    if (criteria.getCriteria() == FilterCriteria.SIZE_BIGGER) {
        args.add(new Atom("LARGER"));
    } else {
        args.add(new Atom("SMALLER"));
    }

    // size in KB
    String stringSizeInKB = criteria.getPattern();
    int sizeInKB = Integer.parseInt(stringSizeInKB);
    // transform to octets
    int sizeInOctets = sizeInKB * 1024;
    args.add(Integer.toString(sizeInOctets));

    return args;
}
*/
    protected Arguments createSubjectArguments(FilterCriteria criteria)
        throws UnsupportedEncodingException {
        Arguments args = new Arguments();

        // we need to append "NOT"
        if (criteria.getCriteria() == FilterCriteria.CONTAINS_NOT) {
            args.add(new Atom("NOT"));
        }

        args.add(new Atom("SUBJECT"));

        String pattern = criteria.getPattern();

        if (isAscii(pattern)) {
            args.add(pattern);
        } else {
            args.add(pattern, charset);
        }

        return args;
    }

    protected Arguments createToArguments(FilterCriteria criteria)
        throws UnsupportedEncodingException {
        Arguments args = new Arguments();

        // we need to append "NOT"
        if (criteria.getCriteria() == FilterCriteria.CONTAINS_NOT) {
            args.add(new Atom("NOT"));
        }

        args.add(new Atom("TO"));

        String pattern = criteria.getPattern();

        if (isAscii(pattern)) {
            args.add(pattern);
        } else {
            args.add(pattern, charset);
        }

        return args;
    }

    protected Arguments createCustomHeaderfieldsArguments(
        FilterCriteria criteria) throws UnsupportedEncodingException {
        Arguments args = new Arguments();

        // we need to append "NOT"
        if (criteria.getCriteria() == FilterCriteria.CONTAINS_NOT) {
            args.add(new Atom("NOT"));
        }

        args.add(new Atom("HEADER"));

        String headerfield = criteria.get("headerfield");
        args.add(new Atom(headerfield));

        String pattern = criteria.getPattern();

        if (isAscii(pattern)) {
            args.add(pattern);
        } else {
            args.add(pattern, charset);
        }

        return args;
    }

    public List generateSearchArguments(FilterRule rule)
        throws UnsupportedEncodingException {
        List ruleStringList = new Vector();

        for (int i = 0; i < rule.count(); i++) {
            FilterCriteria criteria = rule.get(i);

            //StringBuffer searchString = new StringBuffer();
            Arguments args = null;

            switch (criteria.getTypeItem()) {
            case FilterCriteria.SUBJECT: {
                args = createSubjectArguments(criteria);

                break;
            }

            case FilterCriteria.TO: {
                args = createToArguments(criteria);

                break;
            }

            case FilterCriteria.FROM: {
                args = createFromArguments(criteria);

                break;
            }

            case FilterCriteria.CC: {
                args = createCcArguments(criteria);

                break;
            }

            case FilterCriteria.BCC: {
                args = createBccArguments(criteria);

                break;
            }

            case FilterCriteria.BODY: {
                args = createBodyArguments(criteria);

                break;
            }

            /*
case FilterCriteria.SIZE :
    {
        args = createSizeArguments(criteria);

        break;
    }
*/
            case FilterCriteria.DATE: {
                args = createDateArguments(criteria);

                break;
            }

            case FilterCriteria.FLAGS: {
                args = createFlagsArguments(criteria);

                break;
            }

            case FilterCriteria.PRIORITY: {
                args = createPriorityArguments(criteria);

                break;
            }

            case FilterCriteria.CUSTOM_HEADERFIELD: {
                args = createCustomHeaderfieldsArguments(criteria);

                break;
            }
            }

            ruleStringList.add(args);
        }

        return ruleStringList;
    }

    public Arguments generateSearchArguments(FilterRule rule,
        List ruleStringList) {
        Arguments args = new Arguments();

        if (rule.count() > 1) {
            int condition = rule.getConditionInt();
            String conditionString;

            if (condition == FilterRule.MATCH_ALL) {
                // match all
                conditionString = "AND";
            } else {
                // match any
                conditionString = "OR";
            }

            // concatenate all criteria together
            //  -> create one search-request string
            for (int i = 0; i < rule.count(); i++) {
                if ((i != (rule.count() - 1)) &&
                        (conditionString.equals("OR"))) {
                    args.add(new Atom(conditionString));
                }

                args.add((Arguments) ruleStringList.get(i));
            }
        } else {
            args.add((Arguments) ruleStringList.get(0));
        }

        return args;
    }

    protected static boolean isAscii(String s) {
        int l = s.length();

        for (int i = 0; i < l; i++) {
            if ((int) s.charAt(i) > 0177) { // non-ascii

                return false;
            }
        }

        return true;
    }

    /**
 * @return
 */
    public String getCharset() {
        return charset;
    }

    /**
 * @param string
 */
    public void setCharset(String string) {
        charset = string;
    }
}
