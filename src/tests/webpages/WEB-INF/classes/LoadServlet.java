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

import javax.servlet.*;
import javax.servlet.http.*;

public class LoadServlet extends HttpServlet {
  public LoadServlet() {};

  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, java.io.IOException {
    int num = (int) (java.lang.Math.random()*10000);
    int stop = (int) (java.lang.Math.random()*10000);

    java.io.PrintWriter pw = response.getWriter();
    pw.println("Lines = " + num + " " + stop);
    for (int i = 1;  i < num;  i++) {
      if (i == stop) {
        throw new ServletException("num = " + num + ", stop = " + stop);
      }
      pw.println("  Line " + i);
    }
  } 

  private class Loader implements Runnable {
    int num;
    public Loader(int pNum) { num = pNum; }
    public void run() {
      java.net.URL url;
      try {
        url = new java.net.URL("http://127.0.0.1:8080/test/servlet/LoadServlet?num=" + num);
      } catch (java.net.MalformedURLException e) {
        throw new NullPointerException();
      }
      int ok = 0;
      int ownErrors = 0;
      for (int i = 0;  ;  i++) {
        try {
          if (i % 100 == 100) {
            System.out.println("Thread " + num + " = " + i + ", ownErrors = " + ownErrors + ", ok = " + ok);
          }
          int stop = (int) (java.lang.Math.random()*40000);
          java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(url.openStream()));
          for (int j = 0;  ; j++) {
            String line = br.readLine();
            if (line == null) {
              break;
            }
            if (j == stop) {
              ++ownErrors;
              throw new NullPointerException();
            }
          }
          ++ok;
        } catch (Exception e) {
        }
      }
    }
  }

  public void run() {
    for (int i = 0;  i < 10;  i++) {
      Thread t = new Thread(new Loader(i));
      t.start();
    }
  }

  public static void main(String[] args) throws Exception {
    (new LoadServlet()).run();
  }
}
