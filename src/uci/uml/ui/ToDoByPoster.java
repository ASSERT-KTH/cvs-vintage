// Copyright (c) 1996-98 The Regents of the University of California. All
// Rights Reserved. Permission to use, copy, modify, and distribute this
// software and its documentation for educational, research and non-profit
// purposes, without fee, and without a written agreement is hereby granted,
// provided that the above copyright notice and this paragraph appear in all
// copies. Permission to incorporate this software into commercial products
// must be negotiated with University of California. This software program and
// documentation are copyrighted by The Regents of the University of
// California. The software program and documentation are supplied "as is",
// without any accompanying services from The Regents. The Regents do not
// warrant that the operation of the program will be uninterrupted or
// error-free. The end-user understands that the program was developed for
// research purposes and is advised not to rely exclusively on the program for
// any reason. IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY
// PARTY FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES,
// INCLUDING LOST PROFITS, ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS
// DOCUMENTATION, EVEN IF THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGE. THE UNIVERSITY OF CALIFORNIA SPECIFICALLY
// DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
// WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE
// SOFTWARE PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
// ENHANCEMENTS, OR MODIFICATIONS.




package uci.uml.ui;

import java.util.*;

import uci.uml.Model_Management.*;
import uci.uml.Foundation.Core.*;
import uci.util.*;
import uci.argo.kernel.*;


public class ToDoByPoster extends ToDoPerspective
implements ToDoListListener {

  public ToDoByPoster() {
    super("By Poster");
    addSubTreeModel(new GoListToPosterToItem());
  }
  
  ////////////////////////////////////////////////////////////////
  // ToDoListListener implementation

  public void toDoItemsAdded(ToDoListEvent tde) {
    //System.out.println("toDoItemAdded");
    Vector items = tde.getToDoItems();
    int nItems = items.size();
    Object path[] = new Object[2];
    path[0] = Designer.TheDesigner.getToDoList();

    Set posters = Designer.theDesigner().getToDoList().getPosters();
    java.util.Enumeration enum = posters.elements();
    while (enum.hasMoreElements()) {
      Poster p = (Poster) enum.nextElement();
      path[1] = p;
      int nMatchingItems = 0;
      for (int i = 0; i < nItems; i++) {
	ToDoItem item = (ToDoItem) items.elementAt(i);
	Poster post = item.getPoster();
	if (post != p) continue;
	nMatchingItems++;
      }
      if (nMatchingItems == 0) continue;
      int childIndices[] = new int[nMatchingItems];
      Object children[] = new Object[nMatchingItems];
      nMatchingItems = 0;
      for (int i = 0; i < nItems; i++) {
	ToDoItem item = (ToDoItem) items.elementAt(i);
	Poster post = item.getPoster();
	if (post != p) continue;
	childIndices[nMatchingItems] = getIndexOfChild(p, item);
	children[nMatchingItems] = item;
	nMatchingItems++;
      }
      fireTreeNodesInserted(this, path, childIndices, children);
    }
  }

  public void toDoItemsRemoved(ToDoListEvent tde) {
    //System.out.println("toDoItemRemoved");
    ToDoList list = Designer.TheDesigner.getToDoList(); //source?
    Vector items = tde.getToDoItems();
    int nItems = items.size();
    Object path[] = new Object[2];
    path[0] = Designer.TheDesigner.getToDoList();


    java.util.Enumeration enum = list.getPosters().elements();
    while (enum.hasMoreElements()) {
      Poster p = (Poster) enum.nextElement();
//       boolean anyInPoster = false;
//       for (int i = 0; i < nItems; i++) {
// 	ToDoItem item = (ToDoItem) items.elementAt(i);
// 	Poster post = item.getPoster();
// 	if (post == p) anyInPoster = true;
//       }
//       if (!anyInPoster) continue;
      path[1] = p;
      fireTreeStructureChanged(path);
    }
  }

  public void toDoListChanged(ToDoListEvent tde) { }
  

} /* end class ToDoByPoster */


