package org.tigris.scarab.util.xmlissues;

/* ================================================================
 * Copyright (c) 2000-2002 CollabNet.  All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if
 * any, must include the following acknowlegement: "This product includes
 * software developed by Collab.Net <http://www.Collab.Net/>."
 * Alternately, this acknowlegement may appear in the software itself, if
 * and wherever such third-party acknowlegements normally appear.
 * 
 * 4. The hosted project names must not be used to endorse or promote
 * products derived from this software without prior written
 * permission. For written permission, please contact info@collab.net.
 * 
 * 5. Products derived from this software may not use the "Tigris" or 
 * "Scarab" names nor may "Tigris" or "Scarab" appear in their names without 
 * prior written permission of Collab.Net.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL COLLAB.NET OR ITS CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 * 
 * This software consists of voluntary contributions made by many
 * individuals on behalf of Collab.Net.
 */ 

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Collection;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.DefaultFileItem;
import org.apache.commons.lang.exception.NestableRuntimeException;

import org.apache.torque.util.Criteria;

import org.tigris.scarab.test.BaseScarabOMTestCase;
//import org.tigris.scarab.om.Module;
//import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.ActivityPeer;
//import org.tigris.scarab.om.AttributeOptionManager;
//import org.tigris.scarab.om.AttributeOption;

/**
 * A Testing Suite for the ImportIssues class.
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id: ImportIssuesTest.java,v 1.5 2004/04/07 20:12:22 dep4b Exp $
 */
public class ImportIssuesTest extends BaseScarabOMTestCase
{
    private static final File INPUT_FILE = new File("src/test/org/tigris/scarab/util/xmlissues/test-issues.xml").getAbsoluteFile();
    private static final String INPUT_FILENAME = INPUT_FILE.toString();

    public void testImportIssuesViaXMLFile()
        throws Exception
    {
        
        assertTrue("Make sure input file exists at:" + INPUT_FILE,INPUT_FILE.exists());
        
        // this is quite a hack, need to modify ImportIssues to work with an
        // InputStream
        FileItem issuesToImport = new DefaultFileItem()
            {
                public InputStream getInputStream() throws RuntimeException
                {
                    try {
                    return new FileInputStream(INPUT_FILENAME);
                    }
                    catch(Exception e){
                        throw new NestableRuntimeException("Problem reading in file " + INPUT_FILENAME,e);
                    }
                }
                
                public String getName()
                {
                    return INPUT_FILE.getName();
                }
            };
        assertTrue("Could not locate input file", 
                   issuesToImport.getInputStream() != null);

        ImportIssues importIssues = new ImportIssues();
        Collection importErrors = importIssues.runImport(issuesToImport);
        //ScarabIssues si = importIssues.getScarabIssuesBeanReader();
        // looking at current code importErrors will never be null, so
        // putting in a test to confirm it
        assertTrue("importErrors should never be null", importErrors != null);
        assertTrue(importErrors.toString(), importErrors.isEmpty());
    }


    /**
     * Earlier versions of xmlimport would combine Activity's from
     * the xml file with those already in the db, if they shared an
     * ActivitySet id.
     * The sample data adds two Activities to PACS1 within
     * ActivitySet PK=1. The xml also references ActivitySet id=1
     * Make sure the Activities with ActivitySet = 1 are still only two.
     *
     * @exception Exception if an error occurs
     */
    public void testActivitySetCorruption()
        throws Exception
    {
        Criteria crit = new Criteria();
        crit.add(ActivityPeer.TRANSACTION_ID, 1);
        List activities = ActivityPeer.doSelect(crit);
        assertTrue("Activites size should be two, it was " + activities.size(),
                   activities.size() == 2);
    }
}

