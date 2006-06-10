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

import java.util.List;

import junit.framework.TestCase;


/**
 * Boundary testcases.
 *
 * @author fdietz
 */
public class MessageSetTokenizerTest extends TestCase {
    public void test() {
        int max = 1000;

        Object[] uids = new Object[max];

        for (int i = 0; i < max; i++)
            uids[i] = new Integer(i);

        int stepSize = 100;
        MessageSetTokenizer tok = new MessageSetTokenizer(uids, stepSize);

        while (tok.hasNext()) {
            List sublist = (List) tok.next();

            System.out.print("start=" + sublist.get(0));
            System.out.println(" end=" + sublist.get(sublist.size() - 1));

            // if this is the last token, check if the last element
            // is the same than in the array
            if (tok.hasNext() == false) {
                assertEquals(sublist.get(sublist.size() - 1),
                    uids[uids.length - 1]);
            }
        }
    }

    public void test2() {
        int max = 1001;

        Object[] uids = new Object[max];

        for (int i = 0; i < max; i++)
            uids[i] = new Integer(i);

        int stepSize = 100;
        MessageSetTokenizer tok = new MessageSetTokenizer(uids, stepSize);

        while (tok.hasNext()) {
            List sublist = (List) tok.next();

            System.out.print("start=" + sublist.get(0));
            System.out.println(" end=" + sublist.get(sublist.size() - 1));

            // if this is the last token, check if the last element
            // is the same than in the array
            if (tok.hasNext() == false) {
                assertEquals(sublist.get(sublist.size() - 1),
                    uids[uids.length - 1]);
            }
        }
    }

    public void test3() {
        int max = 999;

        Object[] uids = new Object[max];

        for (int i = 0; i < max; i++)
            uids[i] = new Integer(i);

        int stepSize = 100;
        MessageSetTokenizer tok = new MessageSetTokenizer(uids, stepSize);

        while (tok.hasNext()) {
            List sublist = (List) tok.next();

            System.out.print("start=" + sublist.get(0));
            System.out.println(" end=" + sublist.get(sublist.size() - 1));

            // if this is the last token, check if the last element
            // is the same than in the array
            if (tok.hasNext() == false) {
                assertEquals(sublist.get(sublist.size() - 1),
                    uids[uids.length - 1]);
            }
        }
    }

    public void test4() {
        int max = 99;

        Object[] uids = new Object[max];

        for (int i = 0; i < max; i++)
            uids[i] = new Integer(i);

        int stepSize = 100;
        MessageSetTokenizer tok = new MessageSetTokenizer(uids, stepSize);

        while (tok.hasNext()) {
            List sublist = (List) tok.next();

            System.out.print("start=" + sublist.get(0));
            System.out.println(" end=" + sublist.get(sublist.size() - 1));

            // if this is the last token, check if the last element
            // is the same than in the array
            if (tok.hasNext() == false) {
                assertEquals(sublist.get(sublist.size() - 1),
                    uids[uids.length - 1]);
            }
        }
    }

    public void test5() {
        int max = 1;

        Object[] uids = new Object[max];

        for (int i = 0; i < max; i++)
            uids[i] = new Integer(i);

        int stepSize = 100;
        MessageSetTokenizer tok = new MessageSetTokenizer(uids, stepSize);

        while (tok.hasNext()) {
            List sublist = (List) tok.next();

            System.out.print("start=" + sublist.get(0));
            System.out.println(" end=" + sublist.get(sublist.size() - 1));

            // if this is the last token, check if the last element
            // is the same than in the array
            if (tok.hasNext() == false) {
                assertEquals(sublist.get(sublist.size() - 1),
                    uids[uids.length - 1]);
            }
        }
    }
}
