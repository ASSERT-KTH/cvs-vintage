/*
 * Copyright (C) 2002-2003, Simon Nieuviarts
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 */
package org.objectweb.carol.cmi.compiler;

import java.io.File;
import java.io.FileWriter;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogSystem;

public class TemplateCompiler implements LogSystem {
    private ClassConf ccc;
    private VelocityEngine ve;
    private String clFullName;
    private String clName;
    private String genDirName;
    private String pkgName;
    private Compiler c;

    public TemplateCompiler(Compiler c, ClassConf ccc) throws CompilerException {
        this.c = c;
        this.ccc = ccc;
        ve = new VelocityEngine();
        ve.setProperty( RuntimeConstants.RUNTIME_LOG_LOGSYSTEM, this);
        ve.setProperty(RuntimeConstants.VM_LIBRARY, "");
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "class");
        ve.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        try {
            ve.init();
        } catch (Exception e1) {
            throw new CompilerException("Velocity engine error", e1);
        }
        clFullName = ccc.getClassName();
        int n = clFullName.lastIndexOf(".");
        if (n < 0) {
            clName = ccc.getClassName();
            genDirName = c.getSrcDir();
            pkgName = "";
        } else {
            pkgName = ccc.getClassName().substring(0, n);
            clName = ccc.getClassName().substring(n + 1);
            genDirName = c.getSrcDir() + File.separator + pkgName.replace('.', File.separatorChar);
        }
    }

    public String genConfig() throws CompilerException {
        Template tmpl;
        try {
            tmpl = ve.getTemplate("org/objectweb/carol/cmi/compiler/ClusterConfigTemplate.vm");
        } catch (Exception e1) {
            throw new CompilerException("Velocity engine error", e1);
        }
        VelocityContext vc = createVelocityContext();
        String fileName = clName + "_ClusterConfig.java";
        return generate(tmpl, vc, fileName);
    }

    public String genStub() throws CompilerException {
        Template tmpl;
        try {
            tmpl = ve.getTemplate("org/objectweb/carol/cmi/compiler/ClusterStubTemplate.vm");
        } catch (Exception e1) {
            throw new CompilerException("Velocity engine error", e1);
        }
        VelocityContext vc = createVelocityContext();
        String fileName = clName + "_Cluster.java";
        return generate(tmpl, vc, fileName);
    }

    private String generate(Template tmpl, VelocityContext vc, String fileName) throws CompilerException {
        FileWriter fw;
        File file;
        String fullFileName = genDirName + File.separator + fileName;
        try {
            File dir = new File(genDirName);
            dir.mkdirs();
        } catch (Exception e) {
            throw new CompilerException("unable to create directory " + genDirName, e);
        }
        try {
            file = new File(genDirName, fileName);
            fw = new FileWriter(file);
        } catch (Exception e) {
            throw new CompilerException("unable to create file " + fileName, e);
        }

        try {
            tmpl.merge(vc, fw);
            fw.flush();
            fw.close();
        } catch (CompilerException ce) {
            throw ce;
        } catch (Exception e2) {
            throw new CompilerException(fullFileName, e2);
        }
        return fullFileName;
    }

    private VelocityContext createVelocityContext() throws CompilerException {
        VelocityContext vc = new VelocityContext();
        vc.put("className", clName);
        vc.put("pkgName", pkgName);
        vc.put("classFullName", clFullName);
        vc.put("classConf", ccc);
        return vc;
    }

    public void init(RuntimeServices arg0) throws Exception {
    }

    public void logVelocityMessage(int arg0, String arg1) {
    }
}
