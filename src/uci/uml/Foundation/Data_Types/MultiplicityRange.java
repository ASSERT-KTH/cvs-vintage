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




// Source file: f:/jr/projects/uml/Foundation/Data_Types/MultiplicityRange.java

package uci.uml.Foundation.Data_Types;

import java.util.*;

public class MultiplicityRange implements java.io.Serializable {
  public Integer _lower;
  public Integer _upper;
  //-public Multiplicity m_Multiplicity;

  public MultiplicityRange() { }
  public MultiplicityRange(int lower, int upper) {
    this(new Integer(lower), new Integer(upper));
  }
  public MultiplicityRange(Integer lower, Integer upper) {
    setLower(lower);
    setUpper(upper);
  }

  public Integer getLower() { return _lower; }
  public void setLower(Integer x) {
    _lower = x;
  }

  public Integer getUpper() { return _upper; }
  public void setUpper(Integer x) {
    _upper = x;
  }


  public boolean equals(Object obj) {
    if (!(obj instanceof MultiplicityRange)) return false;
    MultiplicityRange mr = (MultiplicityRange) obj;
    Integer mrLow = mr.getLower();
    Integer mrHigh = mr.getUpper();
    boolean low = (_lower == null && mrLow == null) ||
      (_lower != null && _lower.equals(mrLow));
    boolean high = (_upper == null && mrHigh == null) ||
      (_upper != null && _upper.equals(mrHigh));
    return low && high;
  }

  //-public Multiplicity m_getMultiplicity() { return m_Multiplicity; }
  //-public void setM_Multiplicity(Multiplicity x) {
  //-  Multiplicity Multiplicity = x;
  //-}

}
