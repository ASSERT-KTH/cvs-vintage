/*
 *  Copyright 1999-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package calendar;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

public class Serialize {

    // Serialize a bean.
    // It must have a no-arg constructor.

    public static void main(String[] args) {

	if (args.length != 2) {
	    System.out.println(" java serialize output-file classname");
	    return;
	}

	try {

	    FileOutputStream fos = new FileOutputStream(args[0]);
	    ObjectOutputStream objout = new ObjectOutputStream(fos);
	
	    Class clazz = Class.forName(args[1]);
	    objout.writeObject(clazz.newInstance());
	    objout.flush();

	    System.out.println("Serialized bean is in file " + args[0]);

	} catch (Exception ex) {
	    System.out.println("Sorry the follwing exception occured");
	    ex.printStackTrace();
	}
    }
}
