package org.tigris.scarab.actions.setup;


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
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.apache.tools.ant.BuildLogger;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.DemuxOutputStream;
import org.apache.tools.ant.Main;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.input.DefaultInputHandler;
import org.apache.tools.ant.input.InputHandler;

/**
 * This class is the bridge to ant.
 * Most of the code is derived form the
 * org.apache.ant.Main class, but simplyfied for
 * our purposes. i.e. you can only run one single
 * ant task per call. Also most of the configuration
 * properties available in the original class have
 * been dropped to keep the bridge small.
 * We could have used the ant Main class directly,
 * but i think, having control over what happens
 * here is better (i.e. where does out and err go,
 * how do we log, etc...)
 *
 * @author <a href="mailto:dabbous@saxess.com">Hussayn Dabbous</a>
 * @version $Id: AntRunner.java,v 1.1 2004/12/22 22:48:10 dabbous Exp $
 */
public class AntRunner
{

    /**
     * Cache of the Ant version information when it has been loaded.
     */
    private static String      antVersion      = null;

    /**
     * The Ant logger class. There may be only one logger. It will have
     * the right to use the 'out' PrintStream. The class must implements the
     * BuildLogger interface.
     */
    private String             loggerClassname = null;

    /** Our current message output status. Follows Project.MSG_XXX. */
    private int                msgOutputLevel  = Project.MSG_INFO;


    /** Stream to use for logging. */
    private static PrintStream out             = System.out;

    /** Stream that we are using for logging error messages. */
    private static PrintStream err             = System.err;


    /**
     * Execute the given target in the given buildFile.
     * If target is null, the default target is executred.
     * theBuildfile must NOT be null. and readable in the
     * current sessoin context.
     * Currently no exception handling. Every Exception is
     * rethrown.
     * @param theBuildFile
     * @param theTarget
     */
    public void execute(File theBuildFile, String theTarget, Map properties)
    {

        final Project project = new Project();
        project.setCoreLoader(null);

        Throwable error = null;

        try
        {
            addBuildListener(project);
            addInputHandler(project);

            PrintStream err = System.err;
            PrintStream out = System.out;

            // use a system manager that prevents from System.exit()
            SecurityManager oldsm = System.getSecurityManager();

            try
            {
                System.setOut(new PrintStream(new DemuxOutputStream(project, false)));
                System.setErr(new PrintStream(new DemuxOutputStream(project, true)));
                project.fireBuildStarted();

                project.init();
                project.setUserProperty("ant.version", Main.getAntVersion());
                project.setUserProperty("ant.file",    theBuildFile.getAbsolutePath());
                
                if(properties != null)
                {
                    Iterator iter = properties.keySet().iterator();
                    while(iter.hasNext())
                    {
                     String key = (String) iter.next();
                     String val = (String) properties.get(key);
                     project.setUserProperty(key,val);
                    }
                }
                
                ProjectHelper.configureProject(project, theBuildFile);

                if (theTarget == null)
                {
                    theTarget = project.getDefaultTarget();
                }

                project.executeTarget(theTarget);

            }
            finally
            {
                // put back the original security manager
                // actually, did it ever change ???
                // Need to look into the ant sources to answer this
                // question [HD]
                if (oldsm != null)
                {
                    System.setSecurityManager(oldsm);
                }

                System.setOut(out);
                System.setErr(err);
            }
        }
        catch (RuntimeException exc)
        {
            error = exc;
            throw exc;
        }
        catch (Error err)
        {
            error = err;
            throw err;
        }
        finally
        {
            project.fireBuildFinished(error);
        }

    }


    protected void addBuildListener(Project project)
    {
        // Add the default listener
        BuildLogger logger = createLogger();
        project.addBuildListener(logger);
    }


    private void addInputHandler(Project project)
    {
        InputHandler handler = new DefaultInputHandler();
        project.setInputHandler(handler);
    }


    private BuildLogger createLogger()
    {
        BuildLogger logger = null;
        if (loggerClassname != null)
        {
            try
            {
                logger = (BuildLogger) (Class.forName(loggerClassname)
                        .newInstance());
            }
            catch (ClassCastException e)
            {
                System.err
                        .println("The specified logger class " + loggerClassname
                                + " does not implement the BuildLogger interface");
                throw new RuntimeException();
            }
            catch (Exception e)
            {
                System.err
                        .println("Unable to instantiate specified logger " + "class "
                                + loggerClassname
                                + " : "
                                + e.getClass().getName());
                throw new RuntimeException();
            }
        }
        else
        {
            logger = new DefaultLogger();
        }

        logger.setMessageOutputLevel(msgOutputLevel);
        logger.setOutputPrintStream(out);
        logger.setErrorPrintStream(err);
        logger.setEmacsMode(false);

        return logger;
    }
    
    static public void main(String[] args)
    {
        String theBuildFileName = args[0];
        File theBuildFile = new File(theBuildFileName);
        String theTarget = args[1];
        Map properties = null;
        for(int index=2; index< args.length; index++)
        {
            if(properties == null)
            {
                properties = new Hashtable();
            }
            String pdef = args[index];
            int i = pdef.indexOf('=');
            String key;
            String val;
            if(i > 0)
            {
                key = pdef.substring(0,i);
                val = pdef.substring(i+1);
            }
            else
            {
                key = pdef;
                val = "";
            }
            properties.put(key,val);
        }
        
        AntRunner antRunner= new AntRunner();
        antRunner.execute(theBuildFile, theTarget, properties);
             
    }

}
