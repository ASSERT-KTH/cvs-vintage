/**
 * This library is developed inside the ObjectWeb Consortium,
 * http://www.objectweb.org
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
 *
 * --------------------------------------------------------------------------
 * $Id: CarolTestTask.java,v 1.1 2005/02/08 18:13:11 el-vadimo Exp $
 * --------------------------------------------------------------------------
 */

package org.objectweb.carol.ant;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Ant;

public final class CarolTestTask extends Task {
    private final List configs;
    private String antfile;

    public CarolTestTask() {
        configs = new LinkedList();
    }

    public void setAntfile(String antfile) {
        this.antfile = antfile;
    }

    public CarolConfig createCarolConfig() {
        CarolConfig config = new CarolConfig();
        configs.add(config);
        return config;
    }

    public void execute() throws BuildException {
        if (configs.size() == 0) {
            throw new BuildException("no nested config elements found");
        }

        for (Iterator ii=configs.iterator(); ii.hasNext(); ) {
            CarolConfig config = (CarolConfig) ii.next();
            Ant ant = new Ant();
            ant.setProject(getProject());
            ant.setOwningTarget(getOwningTarget());
            ant.setAntfile(getLocation().getFileName());
            ant.setTaskName("antcall");
            ant.setTarget(config.getName());
            ant.execute();
        }
    }
}
