package org.tigris.scarab.pipeline;

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
import java.io.FileReader;
import java.io.Reader;

import junit.framework.TestCase;

import org.apache.turbine.Pipeline;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * Tests TurbinePipeline starts up.  Therw was an issue with the FreshenUserValve
 * where it wasn't calling the default constructor.  This is to make sure
 * the pipeline is created properly.
 *
 * @author <a href="mailto:epugh@opensourceconnections.com">Eric Pugh</a>
 * @version $Id: PipelineCreationTest.java,v 1.1 2004/11/14 21:07:04 dep4b Exp $
 */
public class PipelineCreationTest extends TestCase
{
    private Pipeline pipeline;
    /**
     * Constructor
     */
    public PipelineCreationTest(String testName)
    {
        super(testName);
    }


    public void testReadingPipelineWXstream() throws Exception{
        File file = new File("./src/conf/conf/scarab-pipeline.xml").getAbsoluteFile();
        Reader reader = new FileReader(file);
        XStream xstream = new XStream(new DomDriver()); // does not require XPP3 library
        Object o = xstream.fromXML(reader);
        Pipeline pipeline = (Pipeline)o;
        assertEquals(18,pipeline.getValves().length);
        assertTrue(pipeline.getValves()[9] instanceof FreshenUserValve);
        FreshenUserValve valve = (FreshenUserValve)pipeline.getValves()[9];
        valve.initialize();
        assertTrue(FreshenUserValve.XMIT_SCREENS.size()>0);
        
        
    }

}
