package org.columba.mail.filter.plugins;

import org.columba.mail.filter.FilterCriteria;
import org.columba.mail.folder.Folder;

import org.columba.ristretto.message.Header;


/**
 *
 *
 * This FilterPlugin searches every To and Cc headerfield
 * of an occurence of a search string and combines the result
 * with an logical OR operation
 *
 * @author fdietz
 */
public class ToOrCcFilter extends HeaderfieldFilter {
    private String criteria;
    private String pattern;

    /**
     * Constructor for ToOrCcFilter.
     */
    public ToOrCcFilter() {
        super();
    }

    /**
     * @see org.columba.mail.filter.plugins.AbstractFilter#process(java.lang.Object, org.columba.mail.folder.Folder, java.lang.Object, org.columba.core.command.WorkerStatusController)
     */
    public boolean process(Object[] args, Folder folder, Object uid)
        throws Exception {
        // get the header of the message
        Header header = folder.getHeaderFields(uid, new String[] { "To", "Cc" });

        if (header == null) {
            return false;
        }

        // convert the condition string to an int which is easier to handle
        int condition = FilterCriteria.getCriteria(criteria);

        // get the "To" headerfield from the header
        String to = (String) header.get("To");

        // get the "Cc" headerfield from the header
        String cc = (String) header.get("Cc");

        // test if our To headerfield contains or contains not the search string	
        boolean result = match(to, condition, pattern);

        // do the same for the Cc headerfield and OR the results
        result |= match(cc, condition, pattern);

        // return the result as boolean value true or false
        return result;
    }

    /**
     * @see org.columba.mail.filter.plugins.AbstractFilter#setUp(org.columba.mail.filter.FilterCriteria)
     */
    public void setUp(FilterCriteria f) {
        //  before/after
        criteria = f.get("criteria");

        // string to search
        pattern = f.get("pattern");
    }
}
