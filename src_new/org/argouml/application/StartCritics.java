// $Id: StartCritics.java,v 1.19 2005/01/09 14:58:02 linus Exp $
// Copyright (c) 1996-2005 The Regents of the University of California. All
// Rights Reserved. Permission to use, copy, modify, and distribute this
// software and its documentation without fee, and without a written
// agreement is hereby granted, provided that the above copyright notice
// and this paragraph appear in all copies.  This software program and
// documentation are copyrighted by The Regents of the University of
// California. The software program and documentation are supplied "AS
// IS", without any accompanying services from The Regents. The Regents
// does not warrant that the operation of the program will be
// uninterrupted or error-free. The end-user understands that the program
// was developed for research purposes and is advised not to rely
// exclusively on the program for any reason.  IN NO EVENT SHALL THE
// UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
// SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE. THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT,
// UPDATES, ENHANCEMENTS, OR MODIFICATIONS.

package org.argouml.application;

import org.apache.log4j.Logger;
import org.argouml.application.api.Argo;
import org.argouml.application.api.Configuration;
import org.argouml.application.helpers.ResourceLoaderWrapper;
import org.argouml.cognitive.Designer;
import org.argouml.kernel.Project;
import org.argouml.kernel.ProjectManager;
import org.argouml.model.Model;
import org.argouml.uml.cognitive.critics.ChildGenUML;
import org.argouml.uml.cognitive.critics.CrUML;

/**
 * StartCritics is a thread which helps to start the critiquing thread.
 */
public class StartCritics implements Runnable {
    /**
     * Logger.
     */
    private static final Logger LOG = Logger.getLogger(StartCritics.class);

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        Designer dsgr = Designer.theDesigner();
        org.argouml.uml.cognitive.critics.Init.init();
        org.argouml.uml.cognitive.checklist.Init.init();
        Project p = ProjectManager.getManager().getCurrentProject();
        Designer.addListener(ProjectManager.getManager());
        // set the icon for this poster
        dsgr.setClarifier(ResourceLoaderWrapper.
            lookupIconResource("PostItD0"));
        dsgr.setDesignerName(Configuration.getString(Argo.KEY_USER_FULLNAME));
        Configuration.addListener(Argo.KEY_USER_FULLNAME, dsgr); //MVW
        dsgr.spawnCritiquer(p);
        dsgr.setChildGenerator(new ChildGenUML());
        java.util.Enumeration models = (p.getUserDefinedModels()).elements();
        while (models.hasMoreElements()) {
            Object o = models.nextElement();
            Model.getPump().addModelEventListener(dsgr, o);
        }
        LOG.info("spawned critiquing thread");

        // should be in logon wizard?
        dsgr.startConsidering(CrUML.DEC_INHERITANCE);
        dsgr.startConsidering(CrUML.DEC_CONTAINMENT);
        Designer.setUserWorking(true);
    }

} /* end class StartCritics */





