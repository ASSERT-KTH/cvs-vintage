<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 3.2//EN">
<HTML>

<HEAD>
   <TITLE>Java(TM) 2 SDK Documentation</TITLE>
</HEAD>


<BODY TEXT="#000000" BGCOLOR="#FFFFFF" LINK="#0000FF" VLINK="#000077" ALINK="#FF0000">


<TABLE BORDER="0" WIDTH="100%">
<TR>
<TD WIDTH=40>
   <IMG SRC="images/javalogo52x88.gif" ALT="Java" BORDER=0 WIDTH=52 HEIGHT=88>
</TD>


<TD ALIGN=CENTER>

         <font size="+2"><b>Java<sup><font size="-2">TM</font></sup> 2 SDK, Standard Edition</b>
                       <BR> <b>Documentation</b></font><p>
<b><font size=-1>Version 1.2.2</font><br>
<p>
</TD>


<TD ALIGN=RIGHT VALIGN=TOP>
   <FONT SIZE="-1">Contents</FONT> 


</TD>
</TR>

</TABLE>
<p>

<!-- ============================================================= -->

<table border=1 cellpadding=2 cellspacing=0 width="100%"><tr><td bgcolor="#EEEEFF" align="center">
<font style="font-family: Arial, Helvetica, sans-serif" size="-1">
        <a href="#notes">General Info</a>
 &nbsp; <a href="#api">API & Language</a>
 &nbsp; <a href="#guide">Guide to Features</a>
 &nbsp; <a href="#tools">Tool Docs</a>
 &nbsp; <a href="#jre">J2RE & Plug-in</a>
 &nbsp; <a href="#demos">Demos/Tutorials</a>
</font>
</td></tr></table>


<!-- ============================================================= -->

<blockquote>
<font size="-1">   
Your feedback is important to us. Please send us comments: 
<a href=relnotes/contacts.html>Contacting 
Java<sup><font size="-2">TM</font></sup> Software</a>.
</font>
<P>

<!-- ============================================================= -->

<A NAME="notes"></a>
<table border=0 width="83%" cellspacing=0 cellpadding=0>
<tr><td>

<font size="+1"><b>General Information</b></font><br>
<font size="-1">
General information about the Production Release for Windows <br>
and Reference Implementation for Solaris.
</font>
</td>

<td width="5%">
<font size="-1">
<a href="#location">Location</a>&nbsp;&nbsp;&nbsp;&nbsp;
</font>
</td>
</tr>
</table>

<blockquote>
<A NAME="java"></a>
<table border=0 width="75%" cellspacing=0 cellpadding=0>

<tr>
  <td colspan=2>
    <b>Readme, Overview, Changes</b><br>
  </td>
</tr>

<tr>
  <td>
    <A HREF="../README.html">README</A>
  </td>
            <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><i><font size="-1">software</font></i></td>
</tr>

<tr>
  <td>
    <A HREF="http://java.sun.com/products/jdk/1.2/changes.html">Changes and Release Notes for the Java 2 SDK, v1.2.2</A>
  </td>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><i><font size="-1">website</font></i></td>
</tr>

<tr>
  <td>
    <a href="http://java.sun.com/products/jdk/1.2/docchanges.html">
    Documentation Changes</a>
  </td>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><i><font size="-1">website</font></i></td>
</tr>

<tr>
  <td colspan=2>
    &nbsp; <!-- Blank link -->
  </td>
</tr>

<!-- =========== Compatibility ============== -->
<tr>
  <td colspan=2>
      <b>Compatibility</b>
  </td>
<tr>

<tr>
  <td>
    <A HREF="http://java.sun.com/products/jdk/1.2/compatibility.html">Version 
    Compatibility with Previous Releases</a> 
  </TD>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><I><font size="-1">website</font></I></TD>
</tr>

<tr>
  <td colspan=2>
    &nbsp; <!-- Blank link -->
  </td>
</tr>

<!-- ============ Bugs ============= -->
<tr>
  <td colspan=2>
      <b>Bugs</b>
  </td>
<tr>

<tr>
  <td>
    <A HREF="http://java.sun.com/products/jdk/1.2/bugs.html">
    Known and Fixed Bugs</a> 
  </TD>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><I><font size="-1">website</font></I></TD>
</tr>

<tr>
  <td>
    <A HREF="http://java.sun.com/cgi-bin/bugreport.cgi">Submitting a Bug Report</a> 
  </TD>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><I><font size="-1">website</font></I></TD> 
</tr>

<tr>
  <td colspan=2>
    &nbsp; <!-- Blank link -->
  </td>
</tr>

<!-- ============ Contacts =========== -->
<tr>
  <td colspan=2>
      <b>Contacts</b>
  </td>
<tr>

<tr>
  <td>
    <A HREF="relnotes/contacts.html">Contacting Java Software</A> 
  </TD>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><I><font size="-1">docs</font></I></TD>
</tr>

<tr>
  <td colspan=2>
    &nbsp; <!-- Blank link -->
  </td>
</tr>

<!-- ====== Releases and Download ====== -->
<tr>
  <td colspan=2>
      <b>Releases and Downloads</b>
  </td>
<tr>

<tr>
  <td>
    <A HREF="http://java.sun.com/products/jdk/1.2/index.html">
    Java 2 SDK Download Page</a> 
  </TD>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><I><font size="-1">website</font></I></TD>
</tr>

<tr>
  <td>
    <A HREF="http://java.sun.com/">Java Software Home Page</a> 
  </TD>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><I><font size="-1">website</font></I></TD>
</tr>

<tr>
  <td colspan=2>
    &nbsp; <!-- Blank link -->
  </td>
</tr>

<!-- ========= Legal Notices ======== -->
<tr>
  <td colspan=2>
      <b>Legal Notices</b>
  </td>
<tr>

<tr>
  <td>
     <A HREF="../COPYRIGHT">COPYRIGHT for Java 2 SDK</A>
  </td>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><i><font size="-1">software</font></i></td>
</tr>

<tr>
  <td>
    <A HREF="../LICENSE">LICENSE for Java 2 SDK</A>
  </td>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><i><font size="-1">software</font></i></td>
</tr>

<tr>
  <td>
    <A HREF="relnotes/SMICopyright.html">
    Copyright and License Terms for Documentation</A> 
  </TD>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><I><font size="-1">docs</font></I></TD>
</tr>


</table>
<p>

</blockquote>
<br>

<p>

<!-- ============================================================= -->
<A NAME="api"></a>

<font size="+1"><b>API & Language Documentation </b></font> <BR>
<font size="-1">
</font>

<blockquote>

<p>

<table border=0 width=75% cellspacing=0 cellpadding=0>
<tr>
  <td>
    <A HREF="api/index.html">Java 2 Platform API Specification</A> &nbsp; 
    <a href="api/overview-summary.html"><font size="-2">(NO FRAMES)</font></a><BR>
  </td>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><i><font size="-1">docs</font></i></td>
</tr>

<tr>
  <td>
     &nbsp; <!-- Blank line -->
  </td>
</tr>

<tr>
  <td>
    <A HREF="guide/2d/api-jpeg/overview-summary.html">Other API Bundled with the Java 2 SDK</A> &nbsp; 
<!--
    <a href="guide/2d/api-jpeg/overview-summary.html"><font size="-2">(NO FRAMES)</font></a><BR>
-->
  </td>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><i><font size="-1">docs</font></i></td>
</tr>

<tr>
  <td>
     &nbsp; <!-- Blank line -->
  </td>
</tr>

<tr>
  <td>
<a href="http://java.sun.com/products/jdk/faq/faq-sun-packages.html">Note About sun.* Packages</a>
  </td>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><i><font size="-1">website</font></i></TD>
</tr>

<tr>
  <td>
     &nbsp; <!-- Blank line -->
  </td>
</tr>

<tr>
  <td>
    <A HREF="http://java.sun.com/docs/books/jls/html/index.html">
    The Java Language Specification</A> &nbsp;
    <a href="http://java.sun.com/docs/books/jls/index.html"><font size="-2">(DOWNLOAD)</font></a><BR>
  </td>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><i><font size="-1">website</font></i></TD>
</tr>

<tr>
  <td>
     &nbsp; <!-- Blank line -->
  </td>
</tr>

<tr>
  <td>
    <A HREF="http://java.sun.com/docs/books/vmspec/html/VMSpecTOC.doc.html">
    The Java Virtual Machine Specification</A> &nbsp;
    <a href="http://java.sun.com/docs/books/vmspec/index.html"><font size="-2">(DOWNLOAD)</font></a><BR>
  </td>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><i><font size="-1">website</font></i></TD>
</tr>


</table>
</blockquote>

<p>
<br>

<!-- ============================================================= -->

<A NAME="guide"></a>

<font size="+1"><b>Guide to Features - Java Platform </b></font><br>
<font size="-1">
Design specs, functional specs, user guides, tutorials and demos. <br>
You can <a href="http://java.sun.com/products/jdk/1.2/download-pdf-ps.html">
Download PDF and PS</a> versions of some docs.
</font>
<p>

<blockquote>

<table border=0 width=75% cellspacing=0 cellpadding=0>
<tr>
  <td colspan=2>
    <A HREF="relnotes/features.html">Summary of New Features</A><br>
    <font size="-1">
    Features added since JDK 1.1 
    </font>
  </td>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><i><font size="-1">docs</font></i></td>
</tr>
</table>
<p>

<!--
   <font size="-1">This section was originally titled the <i>Guide to New
   Features</i> and covered only features introduced in the Java 1.2
   platform. We've expanded the scope of the <i>Guide</i> and added material
   originally written for the 1.1 platform.</font>
   <p>
-->

<table border=0 width=75% cellspacing=0 cellpadding=0>
<tr>
  <td colspan=2>
    <b>Basic Features</b>
  </td>
</tr>

<tr>
  <td>
	<img src="images/bullet-round-indented.gif">
            <a href="guide/security/index.html">Security and Signed Applets</a>
  </td>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><i><font size="-1">docs</font></i></td>
</tr>
<tr>
  <td>
        <img src="images/bullet-round-indented.gif">
            <a href="guide/collections/index.html">Collections
           Framework</a>
  </td>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><i><font size="-1">docs</font></i></td>
</tr>
<tr>
  <td>
        <img src="images/bullet-round-indented.gif">
            <a href="guide/beans/index.html">
            JavaBeans</a><sup><font size="-2">TM</font></sup>
  </td>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><i><font size="-1">docs</font></i></td>
</tr>
<tr>
  <td>
        <img src="images/bullet-round-indented.gif">
            <a href="guide/internat/index.html">Internationalization</a>
  </td>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><i><font size="-1">docs</font></i></td>
</tr>
<tr>
<tr>
  <td>
        <img src="images/bullet-round-indented.gif">
            <a href="guide/io/index.html">I/O</a>
  </td>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><i><font size="-1">docs</font></i></td>
</tr>
<tr>
  <td>
        <img src="images/bullet-round-indented.gif">
            <a href="guide/net/index.html">Networking</a>
  </td>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><i><font size="-1">docs</font></i></td>
</tr>
<tr>
  <td>
        <img src="images/bullet-round-indented.gif">
            <a href="guide/rmi/index.html">Remote Method Invocation (RMI)</a>
  </td>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><i><font size="-1">docs</font></i></td>
</tr>
<tr>
  <td>
        <img src="images/bullet-round-indented.gif">
            <a href="guide/math/index.html">Arbitrary-Precision Math</a>
  </td>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><i><font size="-1">docs</font></i></td>
</tr>
<tr>
  <td>
        <img src="images/bullet-round-indented.gif">
            <a href="guide/reflection/index.html">Reflection</a> <!-- class
            access.-->
  </td>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><i><font size="-1">docs</font></i></td>
</tr>
<tr>
  <td>
        <img src="images/bullet-round-indented.gif">
            <a href="guide/versioning/index.html">Package Version
            Identification</a> 
  </td>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><i><font size="-1">docs</font></i></td>
</tr>
<tr>
  <td>
        <img src="images/bullet-round-indented.gif">
            <a href="guide/sound/index.html">Sound</a>
  </td>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><i><font size="-1">docs</font></i></td>
</tr>
<tr>
  <td>
        <img src="images/bullet-round-indented.gif">
            <a href="guide/refobs/index.html">Reference Objects</a> 
  </td>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><i><font size="-1">docs</font></i></td>
</tr>
<tr>
  <td>
        <img src="images/bullet-round-indented.gif">
            <a href="guide/resources/index.html">Resources</a>
  </td>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><i><font size="-1">docs</font></i></td>
</tr>
<tr>
  <td>
        <img src="images/bullet-round-indented.gif">
            <a href="guide/serialization/index.html">Object Serialization</a> 
  </td>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><i><font size="-1">docs</font></i></td>
</tr>
<tr>
  <td>
        <img src="images/bullet-round-indented.gif">
            <a href="guide/extensions/index.html">Extension Mechanism</a>
  </td>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><i><font size="-1">docs</font></i></td>
</tr>
<tr>
  <td>
        <img src="images/bullet-round-indented.gif">
            <a href="guide/jar/index.html">Java Archive (JAR) Files</a> 
  </td>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><i><font size="-1">docs</font></i></td>
</tr>
<tr>
  <td>
        <img src="images/bullet-round-indented.gif">
            <a href="guide/jni/index.html">Java Native Interface (JNI)</a>
  </td>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><i><font size="-1">docs</font></i></td>
</tr>
<tr>
  <td>
        <img src="images/bullet-round-indented.gif">
            <a href="relnotes/features.html#performance">Performance Enhancements</a><br>
  </td>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><i><font size="-1">docs</font></i></td>
</tr>
<tr>
  <td>
        <img src="images/bullet-round-indented.gif">
            <a href="guide/misc/index.html">Miscellaneous Features</a><br>
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
            <font size="-1">
            (Applet tag, Deprecation, Resources)
            </font>
  </td>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><i><font size="-1">docs</font></i></td>
</tr>
<tr>
  <td colspan=2>
    <b>Java Foundation Classes (JFC)</b>
  </td>
</tr>
<tr>
  <td>
        <img src="images/bullet-round-indented.gif">
            <a href="guide/awt/index.html">Abstract Window Toolkit</a> (AWT) 
  </td>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><i><font size="-1">docs</font></i></td>
</tr>
<tr>
  <td>
        <img src="images/bullet-round-indented.gif">
            <a href="guide/swing/index.html">Swing Components</a>
  </td>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><i><font size="-1">docs</font></i></td>
</tr>
<tr>
  <td>
        <img src="images/bullet-round-indented.gif">
            <a href="guide/2d/index.html">2D Graphics and Imaging</a>
  </td>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><i><font size="-1">docs</font></i></td>
</tr>
<tr>
  <td>
        <img src="images/bullet-round-indented.gif">
            <a href="guide/intl/index.html">Input Method Framework</a> 
  </td>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><i><font size="-1">docs</font></i></td>
</tr>
<tr>
  <td>
        <img src="images/bullet-round-indented.gif">
            <a href="guide/access/index.html">Accessibility</a> 
  </td>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><i><font size="-1">docs</font></i></td>
</tr>
<tr>
  <td>
        <img src="images/bullet-round-indented.gif">
            <a href="guide/dragndrop/index.html">Drag-and-Drop data
            transfer</a>
  </td>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><i><font size="-1">docs</font></i></td>
</tr>
<tr>
  <td colspan=2>
    <b>Enterprise Features</b>
  </td>
</tr>
<tr>
  <td>
        <img src="images/bullet-round-indented.gif">
            <a href="guide/idl/index.html">Interface Definition Language (IDL)</a>
  </td>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><i><font size="-1">docs</font></i></td>
</tr>
<tr>
  <td>
        <img src="images/bullet-round-indented.gif">
            <a href="guide/jdbc/index.html">Java Database Connectivity (JDBC<sup><font size="-2">TM</font></sup>)</a>
  </td>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><i><font size="-1">docs</font></i></td>
</tr>
<tr>
  <td colspan=2>
    <b>Tool Support</b>
  </td>
</tr>
<tr>
  <td>
        <img src="images/bullet-round-indented.gif">
            <a href="guide/jvmdi/index.html">Java Virtual Machine Debugger 
            Interface (JVMDI)</a>
  </td>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><i><font size="-1">docs</font></i></td>
</tr>
<tr>
  <td>
        <img src="images/bullet-round-indented.gif">
            <a href="guide/jvmpi/index.html">Java Virtual Machine Profiler 
            Interface (JVMPI)</a>
  </td>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><i><font size="-1">docs</font></i></td>
</tr>

</table>

</blockquote>
<p>
<br>

<!-- ============================================================= -->

<A NAME="tools"></a>

<font size="+1"><b>SDK Tool Documentation</b></font><br>
<font size="-1">
Reference documentation for the SDK tools. 
</font>
<p>

<blockquote>

<table border=0 width=75% cellspacing=0 cellpadding=0>
<tr>
  <td>
   <A HREF="tooldocs/tools.html">Tool Documentation</A>
  </td>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><i><font size="-1">docs</font></i></TD>
</tr>

<tr>
  <td>
   <A HREF="tooldocs/javadoc/index.html">Javadoc Enhancements</a>
  </td>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><i><font size="-1">docs</font></i></TD>
</tr>
</table>

</blockquote>

<p>
<br>

<!-- ============================================================= -->
<a name="jre"></a>
<font size="+1"><b>Java Runtime Environment and Java Plug-in</b></font><br>
<font size="-1">
Documentation for the JRE and Plug-in.
</font>

<blockquote>

<table border=0 width=75% cellspacing=0 cellpadding=0>
<tr>
  <td>
<!--
 <!a href="http://java.sun.com/products/jdk/1.2/jre/index.html">
 JRE Download Page </a> <br>
-->
<a href="http://java.sun.com/products/jdk/1.2/jre/README">README</a><br>
  </td>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><i><font size="-1">website</font></i></TD>
</tr>

<tr>
  <td>
<a href="http://java.sun.com/products/jdk/1.2/jre/CHANGES">CHANGES </a> <br>
  </td>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><i><font size="-1">website</font></i></TD>
</tr>

<tr>
  <td>
<a href="http://java.sun.com/products/jdk/1.2/jre/COPYRIGHT">COPYRIGHT </a> <br>
  </td>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><i><font size="-1">website</font></i></TD>
</tr>

<tr>
  <td>
<a href="http://java.sun.com/products/jdk/1.2/jre/LICENSE">LICENSE </a> <br>
  </td>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><i><font size="-1">website</font></i></TD>
</tr>

<tr>
  <td>
<a href="http://java.sun.com/products/jdk/1.2/runtime.html">JRE Notes for Developers</a>
  </td>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><i><font size="-1">website</font></i></TD>
</tr>

<tr>
  <td>
<a href="http://java.sun.com/products/plugin">Java Plug-in Home Page and Documentation</a><br>
  </td>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><i><font size="-1">website</font></i></TD>
</tr>

</table>

<br>
<br>

</blockquote>

<!-- ============================================================= -->
<A NAME="demos"></a>
<font size="+1"><b>Demos, Tutorials, Training, and Reference</b></font><br>
<font size="-1">
Sample code for applets and applications, training, courses, and 
tutorials for the SDK.
</font>
<p>
<blockquote>

<table border=0 width=75% cellspacing=0 cellpadding=0>
<tr>
  <td>
    <A HREF="relnotes/demos.html">Demonstration Applets and Applications</A>
  </td>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP>
                 <I><font size="-1">software/docs</font></I></TD>
</tr>

<tr>
   <td colspan=2>
      &nbsp; <!-- Blank link -->
   </td>
</tr>


<tr>
  <td>
    <A HREF="http://java.sun.com/docs/books/tutorial/index.html">
    The Java Tutorial</a> <BR>
    &nbsp; &nbsp; &nbsp; &nbsp;<font size="-1">
    Object-Oriented Programming for the Internet.<br>
    &nbsp; &nbsp; &nbsp; &nbsp;
    A quick start to learning the Java programming language.
    </font>
  </td>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><i><font size="-1">website</font></i></TD>
</tr>

<tr>
   <td colspan=2>
      &nbsp; <!-- Blank link -->
   </td>
</tr>

<tr>
  <td>
    <a href="http://java.sun.com/aboutJava/training">
    Training for the Java programming language</a> <BR>
    &nbsp; &nbsp; &nbsp; &nbsp; <font size="-1">
    Directory of various training resources.
    </font>
  </td>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><i><font size="-1">website</font></i></TD>
</tr>

<tr>
   <td colspan=2>
      &nbsp; <!-- Blank link -->
   </td>
</tr>

<tr>
  <td>
    <a href="http://java.sun.com/jdc/onlineTraining">
    On-Line Courses for the Java Programming Language</a> <BR>
    &nbsp; &nbsp; &nbsp; &nbsp; <font size="-1">
    Courses from the Java Developer 
    Connection<font size="-2"><sup>SM</sup></font>.
    </font>
  </td>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><i><font size="-1">website</font></i></TD>
</tr>

<tr>
   <td colspan=2>
      &nbsp; <!-- Blank link -->
   </td>
</tr>

<tr>
  <td>
    <a href="http://java.sun.com/docs/books/chanlee">
    The Java Class Libraries</a> <BR>
    &nbsp; &nbsp; &nbsp; &nbsp; <font size="-1">
    Code examples for the Java 2 Platform API:<br>
<img src="images/bullet-round-indented.gif">
    <a href="http://java.sun.com/docs/books/chanlee/supplement/examples.html">
    1.2 Supplement</a><br>

<img src="images/bullet-round-indented.gif">
     <a href="http://java.sun.com/docs/books/chanlee/second_edition/vol1/examples.html">
    1.1 Packages: java.lang, java.net, java.text, java.util, java.math</a> <br>

<img src="images/bullet-round-indented.gif"> 
    <a href="http://java.sun.com/docs/books/chanlee/second_edition/examples.html">
    1.1 Packages: java.applet, java.awt, java.beans</a>
    </font>
  </td>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><i><font size="-1">website</font></i></TD>
</tr>

<tr>
   <td colspan=2>
      &nbsp; <!-- Blank link -->
   </td>
</tr>

<tr>
  <td>
    <A HREF="http://java.sun.com/products/jdk/faq.html">
    The Java and Java 2 SDK FAQ</A> <BR>
    &nbsp; &nbsp; &nbsp; &nbsp; <font size="-1">
    Frequently asked questions about Java and the SDK.
    </font>
  </td>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><i><font size="-1">website</font></i></TD>
</tr>

<tr>
   <td colspan=2>
      &nbsp; <!-- Blank link -->
   </td>
</tr>

<tr>
        <td> <a href="http://java.sun.com/docs/codeconv/index.html">Code
          Conventions for the Java Programming Language</a><BR>
          &nbsp; &nbsp; &nbsp; &nbsp; <font size="-1"> Standards and styles for           coding Java programs. </font> </td>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><i><font e</font></i></TD>
</tr>

<tr>
   <td colspan=2>
      &nbsp; <!-- Blank link -->
   </td>
</tr>

<tr>
        <td> <a href="http://java.sun.com/docs/windows_format.html">Java Documentation in WinHelp Format</a> <BR>
          &nbsp; &nbsp; &nbsp; &nbsp; <font size="-1"> For Windows users. </font> 
        </td>
                 <TD WIDTH="1%" ALIGN=RIGHT VALIGN=TOP><i><font size="-1">website</font></i></TD>
</tr>


</TABLE>

</blockquote>

<p>

<br>


</blockquote>


<!-- Body text ends here -->

<!-- ============================================================== -->

<HR SIZE=3 NOSHADE>

<TABLE BORDER="0" WIDTH="100%">
<TR VALIGN=TOP>

<TD>
   <FONT SIZE="-2">

   <A HREF="relnotes/SMICopyright.html">Copyright &#169;</A> 1995-99
   <A HREF="http://www.sun.com/">Sun Microsystems, Inc.</A>
   All Rights Reserved.

   </FONT>
   <P>

   <FONT SIZE="-1">

   Please <a href="relnotes/contacts.html">send us comments and suggestions</a><BR>

   <a href="http://java.sun.com/products/jdk/1.2/index.html">
   Download this SDK documentation</a><br>

   </FONT>
<p>
<a name="location"></a>
<font size="-1">
<b>Location:</b><br> 
<i>docs</i> = In documentation download bundle and on website<br>
<i>software</i> = In software download bundle and on website<br>
<i>website</i> = Only on Java Software website
</font>
</TD>

<TD ALIGN=RIGHT>
   <IMG SRC="images/sunlogo64x30.gif" ALT="Sun" BORDER=0 WIDTH=64 HEIGHT=30>
   <BR><BR>
   <FONT SIZE="+1">
   <i>Java Software</i>
   </FONT>
</TD>

</TR>
</TABLE>


<PRE>


























































</PRE>
<PRE>
                                   README

                           JavaTM Development Kit
                                 Version 1.2

           For a hypertext version of this file, see "README.html"

   * Introduction
   * Features
   * JDK Documentation
   * System Requirements
   * Installation
   * Demos
   * Sun Java Web Pages

   This further information is in README.html:
   * Changes
   * Compatibility
   * Bug Reports and Feedback
   * Contents of the Java Development Kit
   * Java Runtime Environment including Java Plug-in

Introduction

     Thank you for downloading this release of the Java(TM) Development
     Kit, version 1.2. The Java Development Kit (JDK(TM)) is the
     development environment for building applications, applets, and
     components that can be deployed on the Java platform. This README
     file covers both the JDK Production Release for Windows and the
     JDK Reference Implementation for Solaris.  A Japanese version of 
     this README is on the Java Software website at 
     http://java.sun.com/products/jdk/1.2/ja/README.


     JDK software includes tools useful for developing and testing
     programs written in the Java programming language and running on
     the Java 1.2 platform. These tools are designed to be used from
     the command line. Except for appletviewer, these tools do not
     provide a graphical user interface.

     On Windows, a separate Java Runtime Environment including
     Java Plug-in is also installed with the JDK.  On Solaris, you 
     can obtain this separately.  For more information,
     see Java Runtime Environment including Java Plug-in.

Features

     Version 1.2 of the Java Development Kit offers significant
     improvements in functionality, performance, security and global
     support. See:

        * Summary of New Features - Features added since JDK 1.1.
          http://java.sun.com/products/jdk/1.2/docs/relnotes/features.html

        * Guide to Features - Complete list of all features.
          http://java.sun.com/products/jdk/1.2/docs/index.html#guide

JDK Documentation

     The JDK Documentation contains release documentation, Java
     API specifications, developer guides, tool documentation, demos,
     and links to related documentation. It is available in a separate
     download bundle.

     If you have not already downloaded your own local copy of the JDK
     Docs, you can do so from the JDK 1.2 download page.

System Requirements

     The JDK 1.2 software is available on three platforms:

        * Win32 Release for Windows 95, Windows 98 and Windows NT on
          Intel hardware. For Windows NT, only version 4.0 is
          supported. A 486/DX or faster processor and at least 48
          megabytes of RAM are recommended.  
        * Solaris/SPARC Release. Only Solaris versions 2.5.1, 2.6
          and 7 (also known as 2.7) are supported. At least 
          48 megabytes of RAM is recommended.  Solaris patches
          may need to be installed (see installation instructions).
        * Solaris/Intel Release. Only Solaris versions 2.5.1, 2.6
          and 7 (also known as 2.7) are supported. A 486/DX or 
          faster processor and at least 48 megabytes of RAM are 
          recommended.  Solaris patches may need to be installed
          (see installation instructions).

     On Solaris, Sun releases both a Reference Implementation and 
     Production Release.  See: 
     http://java.sun.com/products/jdk/faq.html#prod|ref

     On all systems you should have 65 megabytes of free disk space
     before attempting to install the JDK software. If you also install
     the separate documentation download bundle, you need an additional
     85 megabytes of free disk space.

     JDK 1.2 is localized for Japanese.  For more information, see
     http://java.sun.com/products/jdk/1.2/japan-notes.html

Installation

     The complete JDK is composed of the JDK Software plus the JDK
     Documentation, each of which is separately downloadable.
     Installation instructions for each release are maintained on the
     Java Software web site:

        * For the Win32 Installation Instructions, go to
          http://java.sun.com/products/jdk/1.2/install-windows.html
        * For the Solaris Installation Instructions, go to
          http://java.sun.com/products/jdk/1.2/install-solaris.html

     Refer to the COPYRIGHT and LICENSE for legal terms of use.

Demos

     A demo directory is included in this software bundle with a
     variety of applets and Swing applications for you to try out. The
     demos come with complete source code.

Sun Java Web Pages

     For additional information, refer to these Sun Microsystems pages
     on the World Wide Web:

     http://java.sun.com/
          The Java Software web site, with the latest information on
          Java technology, product information, news, and features.
     http://java.sun.com/products/jdk/1.2/index.html
          JDK 1.2 Product and Download Page
     http://java.sun.com/docs
          Java Platform Documentation provides access to white papers,
          the Java Tutorial and other documents.
     http://developer.java.sun.com/
          The Java Developer Connection web site. (Free registration
          required.) Additional technical information, news, and
          features; user forums; support information, and much more.
     http://java.sun.com/products/
          Java Technology Products & API
     http://www.sun.com/solaris/java/
          Java Development Kit for Solaris - Production Release

  ------------------------------------------------------------------------
This Java Development Kit is a product of Java Software of Sun
Microsystems(tm), Inc.

Copyright © 1997, 1998, Sun Microsystems, Inc.
901 San Antonio Road, Palo Alto, California 94303-4900 USA.
All rights reserved.                                   README

                           JavaTM Development Kit
                                 Version 1.2

           For a hypertext version of this file, see "README.html"

   * Introduction
   * Features
   * JDK Documentation
   * System Requirements
   * Installation
   * Demos
   * Sun Java Web Pages

   This further information is in README.html:
   * Changes
   * Compatibility
   * Bug Reports and Feedback
   * Contents of the Java Development Kit
   * Java Runtime Environment including Java Plug-in

Introduction

     Thank you for downloading this release of the Java(TM) Development
     Kit, version 1.2. The Java Development Kit (JDK(TM)) is the
     development environment for building applications, applets, and
     components that can be deployed on the Java platform. This README
     file covers both the JDK Production Release for Windows and the
     JDK Reference Implementation for Solaris.  A Japanese version of 
     this README is on the Java Software website at 
     http://java.sun.com/products/jdk/1.2/ja/README.


     JDK software includes tools useful for developing and testing
     programs written in the Java programming language and running on
     the Java 1.2 platform. These tools are designed to be used from
     the command line. Except for appletviewer, these tools do not
     provide a graphical user interface.

     On Windows, a separate Java Runtime Environment including
     Java Plug-in is also installed with the JDK.  On Solaris, you 
     can obtain this separately.  For more information,
     see Java Runtime Environment including Java Plug-in.

Features

     Version 1.2 of the Java Development Kit offers significant
     improvements in functionality, performance, security and global
     support. See:

        * Summary of New Features - Features added since JDK 1.1.
          http://java.sun.com/products/jdk/1.2/docs/relnotes/features.html

        * Guide to Features - Complete list of all features.
          http://java.sun.com/products/jdk/1.2/docs/index.html#guide

JDK Documentation

     The JDK Documentation contains release documentation, Java
     API specifications, developer guides, tool documentation, demos,
     and links to related documentation. It is available in a separate
     download bundle.

     If you have not already downloaded your own local copy of the JDK
     Docs, you can do so from the JDK 1.2 download page.

System Requirements

     The JDK 1.2 software is available on three platforms:

        * Win32 Release for Windows 95, Windows 98 and Windows NT on
          Intel hardware. For Windows NT, only version 4.0 is
          supported. A 486/DX or faster processor and at least 48
          megabytes of RAM are recommended.  
        * Solaris/SPARC Release. Only Solaris versions 2.5.1, 2.6
          and 7 (also known as 2.7) are supported. At least 
          48 megabytes of RAM is recommended.  Solaris patches
          may need to be installed (see installation instructions).
        * Solaris/Intel Release. Only Solaris versions 2.5.1, 2.6
          and 7 (also known as 2.7) are supported. A 486/DX or 
          faster processor and at least 48 megabytes of RAM are 
          recommended.  Solaris patches may need to be installed
          (see installation instructions).

     On Solaris, Sun releases both a Reference Implementation and 
     Production Release.  See: 
     http://java.sun.com/products/jdk/faq.html#prod|ref

     On all systems you should have 65 megabytes of free disk space
     before attempting to install the JDK software. If you also install
     the separate documentation download bundle, you need an additional
     85 megabytes of free disk space.

     JDK 1.2 is localized for Japanese.  For more information, see
     http://java.sun.com/products/jdk/1.2/japan-notes.html

Installation

     The complete JDK is composed of the JDK Software plus the JDK
     Documentation, each of which is separately downloadable.
     Installation instructions for each release are maintained on the
     Java Software web site:

        * For the Win32 Installation Instructions, go to
          http://java.sun.com/products/jdk/1.2/install-windows.html
        * For the Solaris Installation Instructions, go to
          http://java.sun.com/products/jdk/1.2/install-solaris.html

     Refer to the COPYRIGHT and LICENSE for legal terms of use.

Demos

     A demo directory is included in this software bundle with a
     variety of applets and Swing applications for you to try out. The
     demos come with complete source code.

Sun Java Web Pages

     For additional information, refer to these Sun Microsystems pages
     on the World Wide Web:

     http://java.sun.com/
          The Java Software web site, with the latest information on
          Java technology, product information, news, and features.
     http://java.sun.com/products/jdk/1.2/index.html
          JDK 1.2 Product and Download Page
     http://java.sun.com/docs
          Java Platform Documentation provides access to white papers,
          the Java Tutorial and other documents.
     http://developer.java.sun.com/
          The Java Developer Connection web site. (Free registration
          required.) Additional technical information, news, and
          features; user forums; support information, and much more.
     http://java.sun.com/products/
          Java Technology Products & API
     http://www.sun.com/solaris/java/
          Java Development Kit for Solaris - Production Release

  ------------------------------------------------------------------------
This Java Development Kit is a product of Java Software of Sun
Microsystems(tm), Inc.

Copyright © 1997, 1998, Sun Microsystems, Inc.
901 San Antonio Road, Palo Alto, California 94303-4900 USA.
All rights reserved.                                   README

                           JavaTM Development Kit
                                 Version 1.2

           For a hypertext version of this file, see "README.html"

   * Introduction
   * Features
   * JDK Documentation
   * System Requirements
   * Installation
   * Demos
   * Sun Java Web Pages

   This further information is in README.html:
   * Changes
   * Compatibility
   * Bug Reports and Feedback
   * Contents of the Java Development Kit
   * Java Runtime Environment including Java Plug-in

Introduction

     Thank you for downloading this release of the Java(TM) Development
     Kit, version 1.2. The Java Development Kit (JDK(TM)) is the
     development environment for building applications, applets, and
     components that can be deployed on the Java platform. This README
     file covers both the JDK Production Release for Windows and the
     JDK Reference Implementation for Solaris.  A Japanese version of 
     this README is on the Java Software website at 
     http://java.sun.com/products/jdk/1.2/ja/README.


     JDK software includes tools useful for developing and testing
     programs written in the Java programming language and running on
     the Java 1.2 platform. These tools are designed to be used from
     the command line. Except for appletviewer, these tools do not
     provide a graphical user interface.

     On Windows, a separate Java Runtime Environment including
     Java Plug-in is also installed with the JDK.  On Solaris, you 
     can obtain this separately.  For more information,
     see Java Runtime Environment including Java Plug-in.

Features

     Version 1.2 of the Java Development Kit offers significant
     improvements in functionality, performance, security and global
     support. See:

        * Summary of New Features - Features added since JDK 1.1.
          http://java.sun.com/products/jdk/1.2/docs/relnotes/features.html

        * Guide to Features - Complete list of all features.
          http://java.sun.com/products/jdk/1.2/docs/index.html#guide

JDK Documentation

     The JDK Documentation contains release documentation, Java
     API specifications, developer guides, tool documentation, demos,
     and links to related documentation. It is available in a separate
     download bundle.

     If you have not already downloaded your own local copy of the JDK
     Docs, you can do so from the JDK 1.2 download page.

System Requirements

     The JDK 1.2 software is available on three platforms:

        * Win32 Release for Windows 95, Windows 98 and Windows NT on
          Intel hardware. For Windows NT, only version 4.0 is
          supported. A 486/DX or faster processor and at least 48
          megabytes of RAM are recommended.  
        * Solaris/SPARC Release. Only Solaris versions 2.5.1, 2.6
          and 7 (also known as 2.7) are supported. At least 
          48 megabytes of RAM is recommended.  Solaris patches
          may need to be installed (see installation instructions).
        * Solaris/Intel Release. Only Solaris versions 2.5.1, 2.6
          and 7 (also known as 2.7) are supported. A 486/DX or 
          faster processor and at least 48 megabytes of RAM are 
          recommended.  Solaris patches may need to be installed
          (see installation instructions).

     On Solaris, Sun releases both a Reference Implementation and 
     Production Release.  See: 
     http://java.sun.com/products/jdk/faq.html#prod|ref

     On all systems you should have 65 megabytes of free disk space
     before attempting to install the JDK software. If you also install
     the separate documentation download bundle, you need an additional
     85 megabytes of free disk space.

     JDK 1.2 is localized for Japanese.  For more information, see
     http://java.sun.com/products/jdk/1.2/japan-notes.html

Installation

     The complete JDK is composed of the JDK Software plus the JDK
     Documentation, each of which is separately downloadable.
     Installation instructions for each release are maintained on the
     Java Software web site:

        * For the Win32 Installation Instructions, go to
          http://java.sun.com/products/jdk/1.2/install-windows.html
        * For the Solaris Installation Instructions, go to
          http://java.sun.com/products/jdk/1.2/install-solaris.html

     Refer to the COPYRIGHT and LICENSE for legal terms of use.

Demos

     A demo directory is included in this software bundle with a
     variety of applets and Swing applications for you to try out. The
     demos come with complete source code.

Sun Java Web Pages

     For additional information, refer to these Sun Microsystems pages
     on the World Wide Web:

     http://java.sun.com/
          The Java Software web site, with the latest information on
          Java technology, product information, news, and features.
     http://java.sun.com/products/jdk/1.2/index.html
          JDK 1.2 Product and Download Page
     http://java.sun.com/docs
          Java Platform Documentation provides access to white papers,
          the Java Tutorial and other documents.
     http://developer.java.sun.com/
          The Java Developer Connection web site. (Free registration
          required.) Additional technical information, news, and
          features; user forums; support information, and much more.
     http://java.sun.com/products/
          Java Technology Products & API
     http://www.sun.com/solaris/java/
          Java Development Kit for Solaris - Production Release

  ------------------------------------------------------------------------
This Java Development Kit is a product of Java Software of Sun
Microsystems(tm), Inc.

Copyright © 1997, 1998, Sun Microsystems, Inc.
901 San Antonio Road, Palo Alto, California 94303-4900 USA.
All rights reserved.                                   README

                           JavaTM Development Kit
                                 Version 1.2

           For a hypertext version of this file, see "README.html"

   * Introduction
   * Features
   * JDK Documentation
   * System Requirements
   * Installation
   * Demos
   * Sun Java Web Pages

   This further information is in README.html:
   * Changes
   * Compatibility
   * Bug Reports and Feedback
   * Contents of the Java Development Kit
   * Java Runtime Environment including Java Plug-in

Introduction

     Thank you for downloading this release of the Java(TM) Development
     Kit, version 1.2. The Java Development Kit (JDK(TM)) is the
     development environment for building applications, applets, and
     components that can be deployed on the Java platform. This README
     file covers both the JDK Production Release for Windows and the
     JDK Reference Implementation for Solaris.  A Japanese version of 
     this README is on the Java Software website at 
     http://java.sun.com/products/jdk/1.2/ja/README.


     JDK software includes tools useful for developing and testing
     programs written in the Java programming language and running on
     the Java 1.2 platform. These tools are designed to be used from
     the command line. Except for appletviewer, these tools do not
     provide a graphical user interface.

     On Windows, a separate Java Runtime Environment including
     Java Plug-in is also installed with the JDK.  On Solaris, you 
     can obtain this separately.  For more information,
     see Java Runtime Environment including Java Plug-in.

Features

     Version 1.2 of the Java Development Kit offers significant
     improvements in functionality, performance, security and global
     support. See:

        * Summary of New Features - Features added since JDK 1.1.
          http://java.sun.com/products/jdk/1.2/docs/relnotes/features.html

        * Guide to Features - Complete list of all features.
          http://java.sun.com/products/jdk/1.2/docs/index.html#guide

JDK Documentation

     The JDK Documentation contains release documentation, Java
     API specifications, developer guides, tool documentation, demos,
     and links to related documentation. It is available in a separate
     download bundle.

     If you have not already downloaded your own local copy of the JDK
     Docs, you can do so from the JDK 1.2 download page.

System Requirements

     The JDK 1.2 software is available on three platforms:

        * Win32 Release for Windows 95, Windows 98 and Windows NT on
          Intel hardware. For Windows NT, only version 4.0 is
          supported. A 486/DX or faster processor and at least 48
          megabytes of RAM are recommended.  
        * Solaris/SPARC Release. Only Solaris versions 2.5.1, 2.6
          and 7 (also known as 2.7) are supported. At least 
          48 megabytes of RAM is recommended.  Solaris patches
          may need to be installed (see installation instructions).
        * Solaris/Intel Release. Only Solaris versions 2.5.1, 2.6
          and 7 (also known as 2.7) are supported. A 486/DX or 
          faster processor and at least 48 megabytes of RAM are 
          recommended.  Solaris patches may need to be installed
          (see installation instructions).

     On Solaris, Sun releases both a Reference Implementation and 
     Production Release.  See: 
     http://java.sun.com/products/jdk/faq.html#prod|ref

     On all systems you should have 65 megabytes of free disk space
     before attempting to install the JDK software. If you also install
     the separate documentation download bundle, you need an additional
     85 megabytes of free disk space.

     JDK 1.2 is localized for Japanese.  For more information, see
     http://java.sun.com/products/jdk/1.2/japan-notes.html

Installation

     The complete JDK is composed of the JDK Software plus the JDK
     Documentation, each of which is separately downloadable.
     Installation instructions for each release are maintained on the
     Java Software web site:

        * For the Win32 Installation Instructions, go to
          http://java.sun.com/products/jdk/1.2/install-windows.html
        * For the Solaris Installation Instructions, go to
          http://java.sun.com/products/jdk/1.2/install-solaris.html

     Refer to the COPYRIGHT and LICENSE for legal terms of use.

Demos

     A demo directory is included in this software bundle with a
     variety of applets and Swing applications for you to try out. The
     demos come with complete source code.

Sun Java Web Pages

     For additional information, refer to these Sun Microsystems pages
     on the World Wide Web:

     http://java.sun.com/
          The Java Software web site, with the latest information on
          Java technology, product information, news, and features.
     http://java.sun.com/products/jdk/1.2/index.html
          JDK 1.2 Product and Download Page
     http://java.sun.com/docs
          Java Platform Documentation provides access to white papers,
          the Java Tutorial and other documents.
     http://developer.java.sun.com/
          The Java Developer Connection web site. (Free registration
          required.) Additional technical information, news, and
          features; user forums; support information, and much more.
     http://java.sun.com/products/
          Java Technology Products & API
     http://www.sun.com/solaris/java/
          Java Development Kit for Solaris - Production Release

  ------------------------------------------------------------------------
This Java Development Kit is a product of Java Software of Sun
Microsystems(tm), Inc.

Copyright © 1997, 1998, Sun Microsystems, Inc.
901 San Antonio Road, Palo Alto, California 94303-4900 USA.
All rights reserved.                                   README

                           JavaTM Development Kit
                                 Version 1.2

           For a hypertext version of this file, see "README.html"

   * Introduction
   * Features
   * JDK Documentation
   * System Requirements
   * Installation
   * Demos
   * Sun Java Web Pages

   This further information is in README.html:
   * Changes
   * Compatibility
   * Bug Reports and Feedback
   * Contents of the Java Development Kit
   * Java Runtime Environment including Java Plug-in

Introduction

     Thank you for downloading this release of the Java(TM) Development
     Kit, version 1.2. The Java Development Kit (JDK(TM)) is the
     development environment for building applications, applets, and
     components that can be deployed on the Java platform. This README
     file covers both the JDK Production Release for Windows and the
     JDK Reference Implementation for Solaris.  A Japanese version of 
     this README is on the Java Software website at 
     http://java.sun.com/products/jdk/1.2/ja/README.


     JDK software includes tools useful for developing and testing
     programs written in the Java programming language and running on
     the Java 1.2 platform. These tools are designed to be used from
     the command line. Except for appletviewer, these tools do not
     provide a graphical user interface.

     On Windows, a separate Java Runtime Environment including
     Java Plug-in is also installed with the JDK.  On Solaris, you 
     can obtain this separately.  For more information,
     see Java Runtime Environment including Java Plug-in.

Features

     Version 1.2 of the Java Development Kit offers significant
     improvements in functionality, performance, security and global
     support. See:

        * Summary of New Features - Features added since JDK 1.1.
          http://java.sun.com/products/jdk/1.2/docs/relnotes/features.html

        * Guide to Features - Complete list of all features.
          http://java.sun.com/products/jdk/1.2/docs/index.html#guide

JDK Documentation

     The JDK Documentation contains release documentation, Java
     API specifications, developer guides, tool documentation, demos,
     and links to related documentation. It is available in a separate
     download bundle.

     If you have not already downloaded your own local copy of the JDK
     Docs, you can do so from the JDK 1.2 download page.

System Requirements

     The JDK 1.2 software is available on three platforms:

        * Win32 Release for Windows 95, Windows 98 and Windows NT on
          Intel hardware. For Windows NT, only version 4.0 is
          supported. A 486/DX or faster processor and at least 48
          megabytes of RAM are recommended.  
        * Solaris/SPARC Release. Only Solaris versions 2.5.1, 2.6
          and 7 (also known as 2.7) are supported. At least 
          48 megabytes of RAM is recommended.  Solaris patches
          may need to be installed (see installation instructions).
        * Solaris/Intel Release. Only Solaris versions 2.5.1, 2.6
          and 7 (also known as 2.7) are supported. A 486/DX or 
          faster processor and at least 48 megabytes of RAM are 
          recommended.  Solaris patches may need to be installed
          (see installation instructions).

     On Solaris, Sun releases both a Reference Implementation and 
     Production Release.  See: 
     http://java.sun.com/products/jdk/faq.html#prod|ref

     On all systems you should have 65 megabytes of free disk space
     before attempting to install the JDK software. If you also install
     the separate documentation download bundle, you need an additional
     85 megabytes of free disk space.

     JDK 1.2 is localized for Japanese.  For more information, see
     http://java.sun.com/products/jdk/1.2/japan-notes.html

Installation

     The complete JDK is composed of the JDK Software plus the JDK
     Documentation, each of which is separately downloadable.
     Installation instructions for each release are maintained on the
     Java Software web site:

        * For the Win32 Installation Instructions, go to
          http://java.sun.com/products/jdk/1.2/install-windows.html
        * For the Solaris Installation Instructions, go to
          http://java.sun.com/products/jdk/1.2/install-solaris.html

     Refer to the COPYRIGHT and LICENSE for legal terms of use.

Demos

     A demo directory is included in this software bundle with a
     variety of applets and Swing applications for you to try out. The
     demos come with complete source code.

Sun Java Web Pages

     For additional information, refer to these Sun Microsystems pages
     on the World Wide Web:

     http://java.sun.com/
          The Java Software web site, with the latest information on
          Java technology, product information, news, and features.
     http://java.sun.com/products/jdk/1.2/index.html
          JDK 1.2 Product and Download Page
     http://java.sun.com/docs
          Java Platform Documentation provides access to white papers,
          the Java Tutorial and other documents.
     http://developer.java.sun.com/
          The Java Developer Connection web site. (Free registration
          required.) Additional technical information, news, and
          features; user forums; support information, and much more.
     http://java.sun.com/products/
          Java Technology Products & API
     http://www.sun.com/solaris/java/
          Java Development Kit for Solaris - Production Release

  ------------------------------------------------------------------------
This Java Development Kit is a product of Java Software of Sun
Microsystems(tm), Inc.

Copyright © 1997, 1998, Sun Microsystems, Inc.
901 San Antonio Road, Palo Alto, California 94303-4900 USA.
All rights reserved.                                   README

                           JavaTM Development Kit
                                 Version 1.2

           For a hypertext version of this file, see "README.html"

   * Introduction
   * Features
   * JDK Documentation
   * System Requirements
   * Installation
   * Demos
   * Sun Java Web Pages

   This further information is in README.html:
   * Changes
   * Compatibility
   * Bug Reports and Feedback
   * Contents of the Java Development Kit
   * Java Runtime Environment including Java Plug-in

Introduction

     Thank you for downloading this release of the Java(TM) Development
     Kit, version 1.2. The Java Development Kit (JDK(TM)) is the
     development environment for building applications, applets, and
     components that can be deployed on the Java platform. This README
     file covers both the JDK Production Release for Windows and the
     JDK Reference Implementation for Solaris.  A Japanese version of 
     this README is on the Java Software website at 
     http://java.sun.com/products/jdk/1.2/ja/README.


     JDK software includes tools useful for developing and testing
     programs written in the Java programming language and running on
     the Java 1.2 platform. These tools are designed to be used from
     the command line. Except for appletviewer, these tools do not
     provide a graphical user interface.

     On Windows, a separate Java Runtime Environment including
     Java Plug-in is also installed with the JDK.  On Solaris, you 
     can obtain this separately.  For more information,
     see Java Runtime Environment including Java Plug-in.

Features

     Version 1.2 of the Java Development Kit offers significant
     improvements in functionality, performance, security and global
     support. See:

        * Summary of New Features - Features added since JDK 1.1.
          http://java.sun.com/products/jdk/1.2/docs/relnotes/features.html

        * Guide to Features - Complete list of all features.
          http://java.sun.com/products/jdk/1.2/docs/index.html#guide

JDK Documentation

     The JDK Documentation contains release documentation, Java
     API specifications, developer guides, tool documentation, demos,
     and links to related documentation. It is available in a separate
     download bundle.

     If you have not already downloaded your own local copy of the JDK
     Docs, you can do so from the JDK 1.2 download page.

System Requirements

     The JDK 1.2 software is available on three platforms:

        * Win32 Release for Windows 95, Windows 98 and Windows NT on
          Intel hardware. For Windows NT, only version 4.0 is
          supported. A 486/DX or faster processor and at least 48
          megabytes of RAM are recommended.  
        * Solaris/SPARC Release. Only Solaris versions 2.5.1, 2.6
          and 7 (also known as 2.7) are supported. At least 
          48 megabytes of RAM is recommended.  Solaris patches
          may need to be installed (see installation instructions).
        * Solaris/Intel Release. Only Solaris versions 2.5.1, 2.6
          and 7 (also known as 2.7) are supported. A 486/DX or 
          faster processor and at least 48 megabytes of RAM are 
          recommended.  Solaris patches may need to be installed
          (see installation instructions).

     On Solaris, Sun releases both a Reference Implementation and 
     Production Release.  See: 
     http://java.sun.com/products/jdk/faq.html#prod|ref

     On all systems you should have 65 megabytes of free disk space
     before attempting to install the JDK software. If you also install
     the separate documentation download bundle, you need an additional
     85 megabytes of free disk space.

     JDK 1.2 is localized for Japanese.  For more information, see
     http://java.sun.com/products/jdk/1.2/japan-notes.html

Installation

     The complete JDK is composed of the JDK Software plus the JDK
     Documentation, each of which is separately downloadable.
     Installation instructions for each release are maintained on the
     Java Software web site:

        * For the Win32 Installation Instructions, go to
          http://java.sun.com/products/jdk/1.2/install-windows.html
        * For the Solaris Installation Instructions, go to
          http://java.sun.com/products/jdk/1.2/install-solaris.html

     Refer to the COPYRIGHT and LICENSE for legal terms of use.

Demos

     A demo directory is included in this software bundle with a
     variety of applets and Swing applications for you to try out. The
     demos come with complete source code.

Sun Java Web Pages

     For additional information, refer to these Sun Microsystems pages
     on the World Wide Web:

     http://java.sun.com/
          The Java Software web site, with the latest information on
          Java technology, product information, news, and features.
     http://java.sun.com/products/jdk/1.2/index.html
          JDK 1.2 Product and Download Page
     http://java.sun.com/docs
          Java Platform Documentation provides access to white papers,
          the Java Tutorial and other documents.
     http://developer.java.sun.com/
          The Java Developer Connection web site. (Free registration
          required.) Additional technical information, news, and
          features; user forums; support information, and much more.
     http://java.sun.com/products/
          Java Technology Products & API
     http://www.sun.com/solaris/java/
          Java Development Kit for Solaris - Production Release

  ------------------------------------------------------------------------
This Java Development Kit is a product of Java Software of Sun
Microsystems(tm), Inc.

Copyright © 1997, 1998, Sun Microsystems, Inc.
901 San Antonio Road, Palo Alto, California 94303-4900 USA.
All rights reserved.                                   README

                           JavaTM Development Kit
                                 Version 1.2

           For a hypertext version of this file, see "README.html"

   * Introduction
   * Features
   * JDK Documentation
   * System Requirements
   * Installation
   * Demos
   * Sun Java Web Pages

   This further information is in README.html:
   * Changes
   * Compatibility
   * Bug Reports and Feedback
   * Contents of the Java Development Kit
   * Java Runtime Environment including Java Plug-in

Introduction

     Thank you for downloading this release of the Java(TM) Development
     Kit, version 1.2. The Java Development Kit (JDK(TM)) is the
     development environment for building applications, applets, and
     components that can be deployed on the Java platform. This README
     file covers both the JDK Production Release for Windows and the
     JDK Reference Implementation for Solaris.  A Japanese version of 
     this README is on the Java Software website at 
     http://java.sun.com/products/jdk/1.2/ja/README.


     JDK software includes tools useful for developing and testing
     programs written in the Java programming language and running on
     the Java 1.2 platform. These tools are designed to be used from
     the command line. Except for appletviewer, these tools do not
     provide a graphical user interface.

     On Windows, a separate Java Runtime Environment including
     Java Plug-in is also installed with the JDK.  On Solaris, you 
     can obtain this separately.  For more information,
     see Java Runtime Environment including Java Plug-in.

Features

     Version 1.2 of the Java Development Kit offers significant
     improvements in functionality, performance, security and global
     support. See:

        * Summary of New Features - Features added since JDK 1.1.
          http://java.sun.com/products/jdk/1.2/docs/relnotes/features.html

        * Guide to Features - Complete list of all features.
          http://java.sun.com/products/jdk/1.2/docs/index.html#guide

JDK Documentation

     The JDK Documentation contains release documentation, Java
     API specifications, developer guides, tool documentation, demos,
     and links to related documentation. It is available in a separate
     download bundle.

     If you have not already downloaded your own local copy of the JDK
     Docs, you can do so from the JDK 1.2 download page.

System Requirements

     The JDK 1.2 software is available on three platforms:

        * Win32 Release for Windows 95, Windows 98 and Windows NT on
          Intel hardware. For Windows NT, only version 4.0 is
          supported. A 486/DX or faster processor and at least 48
          megabytes of RAM are recommended.  
        * Solaris/SPARC Release. Only Solaris versions 2.5.1, 2.6
          and 7 (also known as 2.7) are supported. At least 
          48 megabytes of RAM is recommended.  Solaris patches
          may need to be installed (see installation instructions).
        * Solaris/Intel Release. Only Solaris versions 2.5.1, 2.6
          and 7 (also known as 2.7) are supported. A 486/DX or 
          faster processor and at least 48 megabytes of RAM are 
          recommended.  Solaris patches may need to be installed
          (see installation instructions).

     On Solaris, Sun releases both a Reference Implementation and 
     Production Release.  See: 
     http://java.sun.com/products/jdk/faq.html#prod|ref

     On all systems you should have 65 megabytes of free disk space
     before attempting to install the JDK software. If you also install
     the separate documentation download bundle, you need an additional
     85 megabytes of free disk space.

     JDK 1.2 is localized for Japanese.  For more information, see
     http://java.sun.com/products/jdk/1.2/japan-notes.html

Installation

     The complete JDK is composed of the JDK Software plus the JDK
     Documentation, each of which is separately downloadable.
     Installation instructions for each release are maintained on the
     Java Software web site:

        * For the Win32 Installation Instructions, go to
          http://java.sun.com/products/jdk/1.2/install-windows.html
        * For the Solaris Installation Instructions, go to
          http://java.sun.com/products/jdk/1.2/install-solaris.html

     Refer to the COPYRIGHT and LICENSE for legal terms of use.

Demos

     A demo directory is included in this software bundle with a
     variety of applets and Swing applications for you to try out. The
     demos come with complete source code.

Sun Java Web Pages

     For additional information, refer to these Sun Microsystems pages
     on the World Wide Web:

     http://java.sun.com/
          The Java Software web site, with the latest information on
          Java technology, product information, news, and features.
     http://java.sun.com/products/jdk/1.2/index.html
          JDK 1.2 Product and Download Page
     http://java.sun.com/docs
          Java Platform Documentation provides access to white papers,
          the Java Tutorial and other documents.
     http://developer.java.sun.com/
          The Java Developer Connection web site. (Free registration
          required.) Additional technical information, news, and
          features; user forums; support information, and much more.
     http://java.sun.com/products/
          Java Technology Products & API
     http://www.sun.com/solaris/java/
          Java Development Kit for Solaris - Production Release

  ------------------------------------------------------------------------
This Java Development Kit is a product of Java Software of Sun
Microsystems(tm), Inc.

Copyright © 1997, 1998, Sun Microsystems, Inc.
901 San Antonio Road, Palo Alto, California 94303-4900 USA.
All rights reserved.                                   README

                           JavaTM Development Kit
                                 Version 1.2

           For a hypertext version of this file, see "README.html"

   * Introduction
   * Features
   * JDK Documentation
   * System Requirements
   * Installation
   * Demos
   * Sun Java Web Pages

   This further information is in README.html:
   * Changes
   * Compatibility
   * Bug Reports and Feedback
   * Contents of the Java Development Kit
   * Java Runtime Environment including Java Plug-in

Introduction

     Thank you for downloading this release of the Java(TM) Development
     Kit, version 1.2. The Java Development Kit (JDK(TM)) is the
     development environment for building applications, applets, and
     components that can be deployed on the Java platform. This README
     file covers both the JDK Production Release for Windows and the
     JDK Reference Implementation for Solaris.  A Japanese version of 
     this README is on the Java Software website at 
     http://java.sun.com/products/jdk/1.2/ja/README.


     JDK software includes tools useful for developing and testing
     programs written in the Java programming language and running on
     the Java 1.2 platform. These tools are designed to be used from
     the command line. Except for appletviewer, these tools do not
     provide a graphical user interface.

     On Windows, a separate Java Runtime Environment including
     Java Plug-in is also installed with the JDK.  On Solaris, you 
     can obtain this separately.  For more information,
     see Java Runtime Environment including Java Plug-in.

Features

     Version 1.2 of the Java Development Kit offers significant
     improvements in functionality, performance, security and global
     support. See:

        * Summary of New Features - Features added since JDK 1.1.
          http://java.sun.com/products/jdk/1.2/docs/relnotes/features.html

        * Guide to Features - Complete list of all features.
          http://java.sun.com/products/jdk/1.2/docs/index.html#guide

JDK Documentation

     The JDK Documentation contains release documentation, Java
     API specifications, developer guides, tool documentation, demos,
     and links to related documentation. It is available in a separate
     download bundle.

     If you have not already downloaded your own local copy of the JDK
     Docs, you can do so from the JDK 1.2 download page.

System Requirements

     The JDK 1.2 software is available on three platforms:

        * Win32 Release for Windows 95, Windows 98 and Windows NT on
          Intel hardware. For Windows NT, only version 4.0 is
          supported. A 486/DX or faster processor and at least 48
          megabytes of RAM are recommended.  
        * Solaris/SPARC Release. Only Solaris versions 2.5.1, 2.6
          and 7 (also known as 2.7) are supported. At least 
          48 megabytes of RAM is recommended.  Solaris patches
          may need to be installed (see installation instructions).
        * Solaris/Intel Release. Only Solaris versions 2.5.1, 2.6
          and 7 (also known as 2.7) are supported. A 486/DX or 
          faster processor and at least 48 megabytes of RAM are 
          recommended.  Solaris patches may need to be installed
          (see installation instructions).

     On Solaris, Sun releases both a Reference Implementation and 
     Production Release.  See: 
     http://java.sun.com/products/jdk/faq.html#prod|ref

     On all systems you should have 65 megabytes of free disk space
     before attempting to install the JDK software. If you also install
     the separate documentation download bundle, you need an additional
     85 megabytes of free disk space.

     JDK 1.2 is localized for Japanese.  For more information, see
     http://java.sun.com/products/jdk/1.2/japan-notes.html

Installation

     The complete JDK is composed of the JDK Software plus the JDK
     Documentation, each of which is separately downloadable.
     Installation instructions for each release are maintained on the
     Java Software web site:

        * For the Win32 Installation Instructions, go to
          http://java.sun.com/products/jdk/1.2/install-windows.html
        * For the Solaris Installation Instructions, go to
          http://java.sun.com/products/jdk/1.2/install-solaris.html

     Refer to the COPYRIGHT and LICENSE for legal terms of use.

Demos

     A demo directory is included in this software bundle with a
     variety of applets and Swing applications for you to try out. The
     demos come with complete source code.

Sun Java Web Pages

     For additional information, refer to these Sun Microsystems pages
     on the World Wide Web:

     http://java.sun.com/
          The Java Software web site, with the latest information on
          Java technology, product information, news, and features.
     http://java.sun.com/products/jdk/1.2/index.html
          JDK 1.2 Product and Download Page
     http://java.sun.com/docs
          Java Platform Documentation provides access to white papers,
          the Java Tutorial and other documents.
     http://developer.java.sun.com/
          The Java Developer Connection web site. (Free registration
          required.) Additional technical information, news, and
          features; user forums; support information, and much more.
     http://java.sun.com/products/
          Java Technology Products & API
     http://www.sun.com/solaris/java/
          Java Development Kit for Solaris - Production Release

  ------------------------------------------------------------------------
This Java Development Kit is a product of Java Software of Sun
Microsystems(tm), Inc.

Copyright © 1997, 1998, Sun Microsystems, Inc.
901 San Antonio Road, Palo Alto, California 94303-4900 USA.
All rights reserved.                                   README

                           JavaTM Development Kit
                                 Version 1.2

           For a hypertext version of this file, see "README.html"

   * Introduction
   * Features
   * JDK Documentation
   * System Requirements
   * Installation
   * Demos
   * Sun Java Web Pages

   This further information is in README.html:
   * Changes
   * Compatibility
   * Bug Reports and Feedback
   * Contents of the Java Development Kit
   * Java Runtime Environment including Java Plug-in

Introduction

     Thank you for downloading this release of the Java(TM) Development
     Kit, version 1.2. The Java Development Kit (JDK(TM)) is the
     development environment for building applications, applets, and
     components that can be deployed on the Java platform. This README
     file covers both the JDK Production Release for Windows and the
     JDK Reference Implementation for Solaris.  A Japanese version of 
     this README is on the Java Software website at 
     http://java.sun.com/products/jdk/1.2/ja/README.


     JDK software includes tools useful for developing and testing
     programs written in the Java programming language and running on
     the Java 1.2 platform. These tools are designed to be used from
     the command line. Except for appletviewer, these tools do not
     provide a graphical user interface.

     On Windows, a separate Java Runtime Environment including
     Java Plug-in is also installed with the JDK.  On Solaris, you 
     can obtain this separately.  For more information,
     see Java Runtime Environment including Java Plug-in.

Features

     Version 1.2 of the Java Development Kit offers significant
     improvements in functionality, performance, security and global
     support. See:

        * Summary of New Features - Features added since JDK 1.1.
          http://java.sun.com/products/jdk/1.2/docs/relnotes/features.html

        * Guide to Features - Complete list of all features.
          http://java.sun.com/products/jdk/1.2/docs/index.html#guide

JDK Documentation

     The JDK Documentation contains release documentation, Java
     API specifications, developer guides, tool documentation, demos,
     and links to related documentation. It is available in a separate
     download bundle.

     If you have not already downloaded your own local copy of the JDK
     Docs, you can do so from the JDK 1.2 download page.

System Requirements

     The JDK 1.2 software is available on three platforms:

        * Win32 Release for Windows 95, Windows 98 and Windows NT on
          Intel hardware. For Windows NT, only version 4.0 is
          supported. A 486/DX or faster processor and at least 48
          megabytes of RAM are recommended.  
        * Solaris/SPARC Release. Only Solaris versions 2.5.1, 2.6
          and 7 (also known as 2.7) are supported. At least 
          48 megabytes of RAM is recommended.  Solaris patches
          may need to be installed (see installation instructions).
        * Solaris/Intel Release. Only Solaris versions 2.5.1, 2.6
          and 7 (also known as 2.7) are supported. A 486/DX or 
          faster processor and at least 48 megabytes of RAM are 
          recommended.  Solaris patches may need to be installed
          (see installation instructions).

     On Solaris, Sun releases both a Reference Implementation and 
     Production Release.  See: 
     http://java.sun.com/products/jdk/faq.html#prod|ref

     On all systems you should have 65 megabytes of free disk space
     before attempting to install the JDK software. If you also install
     the separate documentation download bundle, you need an additional
     85 megabytes of free disk space.

     JDK 1.2 is localized for Japanese.  For more information, see
     http://java.sun.com/products/jdk/1.2/japan-notes.html

Installation

     The complete JDK is composed of the JDK Software plus the JDK
     Documentation, each of which is separately downloadable.
     Installation instructions for each release are maintained on the
     Java Software web site:

        * For the Win32 Installation Instructions, go to
          http://java.sun.com/products/jdk/1.2/install-windows.html
        * For the Solaris Installation Instructions, go to
          http://java.sun.com/products/jdk/1.2/install-solaris.html

     Refer to the COPYRIGHT and LICENSE for legal terms of use.

Demos

     A demo directory is included in this software bundle with a
     variety of applets and Swing applications for you to try out. The
     demos come with complete source code.

Sun Java Web Pages

     For additional information, refer to these Sun Microsystems pages
     on the World Wide Web:

     http://java.sun.com/
          The Java Software web site, with the latest information on
          Java technology, product information, news, and features.
     http://java.sun.com/products/jdk/1.2/index.html
          JDK 1.2 Product and Download Page
     http://java.sun.com/docs
          Java Platform Documentation provides access to white papers,
          the Java Tutorial and other documents.
     http://developer.java.sun.com/
          The Java Developer Connection web site. (Free registration
          required.) Additional technical information, news, and
          features; user forums; support information, and much more.
     http://java.sun.com/products/
          Java Technology Products & API
     http://www.sun.com/solaris/java/
          Java Development Kit for Solaris - Production Release

  ------------------------------------------------------------------------
This Java Development Kit is a product of Java Software of Sun
Microsystems(tm), Inc.

Copyright © 1997, 1998, Sun Microsystems, Inc.
901 San Antonio Road, Palo Alto, California 94303-4900 USA.
All rights reserved.                                   README

                           JavaTM Development Kit
                                 Version 1.2

           For a hypertext version of this file, see "README.html"

   * Introduction
   * Features
   * JDK Documentation
   * System Requirements
   * Installation
   * Demos
   * Sun Java Web Pages

   This further information is in README.html:
   * Changes
   * Compatibility
   * Bug Reports and Feedback
   * Contents of the Java Development Kit
   * Java Runtime Environment including Java Plug-in

Introduction

     Thank you for downloading this release of the Java(TM) Development
     Kit, version 1.2. The Java Development Kit (JDK(TM)) is the
     development environment for building applications, applets, and
     components that can be deployed on the Java platform. This README
     file covers both the JDK Production Release for Windows and the
     JDK Reference Implementation for Solaris.  A Japanese version of 
     this README is on the Java Software website at 
     http://java.sun.com/products/jdk/1.2/ja/README.


     JDK software includes tools useful for developing and testing
     programs written in the Java programming language and running on
     the Java 1.2 platform. These tools are designed to be used from
     the command line. Except for appletviewer, these tools do not
     provide a graphical user interface.

     On Windows, a separate Java Runtime Environment including
     Java Plug-in is also installed with the JDK.  On Solaris, you 
     can obtain this separately.  For more information,
     see Java Runtime Environment including Java Plug-in.

Features

     Version 1.2 of the Java Development Kit offers significant
     improvements in functionality, performance, security and global
     support. See:

        * Summary of New Features - Features added since JDK 1.1.
          http://java.sun.com/products/jdk/1.2/docs/relnotes/features.html

        * Guide to Features - Complete list of all features.
          http://java.sun.com/products/jdk/1.2/docs/index.html#guide

JDK Documentation

     The JDK Documentation contains release documentation, Java
     API specifications, developer guides, tool documentation, demos,
     and links to related documentation. It is available in a separate
     download bundle.

     If you have not already downloaded your own local copy of the JDK
     Docs, you can do so from the JDK 1.2 download page.

System Requirements

     The JDK 1.2 software is available on three platforms:

        * Win32 Release for Windows 95, Windows 98 and Windows NT on
          Intel hardware. For Windows NT, only version 4.0 is
          supported. A 486/DX or faster processor and at least 48
          megabytes of RAM are recommended.  
        * Solaris/SPARC Release. Only Solaris versions 2.5.1, 2.6
          and 7 (also known as 2.7) are supported. At least 
          48 megabytes of RAM is recommended.  Solaris patches
          may need to be installed (see installation instructions).
        * Solaris/Intel Release. Only Solaris versions 2.5.1, 2.6
          and 7 (also known as 2.7) are supported. A 486/DX or 
          faster processor and at least 48 megabytes of RAM are 
          recommended.  Solaris patches may need to be installed
          (see installation instructions).

     On Solaris, Sun releases both a Reference Implementation and 
     Production Release.  See: 
     http://java.sun.com/products/jdk/faq.html#prod|ref

     On all systems you should have 65 megabytes of free disk space
     before attempting to install the JDK software. If you also install
     the separate documentation download bundle, you need an additional
     85 megabytes of free disk space.

     JDK 1.2 is localized for Japanese.  For more information, see
     http://java.sun.com/products/jdk/1.2/japan-notes.html

Installation

     The complete JDK is composed of the JDK Software plus the JDK
     Documentation, each of which is separately downloadable.
     Installation instructions for each release are maintained on the
     Java Software web site:

        * For the Win32 Installation Instructions, go to
          http://java.sun.com/products/jdk/1.2/install-windows.html
        * For the Solaris Installation Instructions, go to
          http://java.sun.com/products/jdk/1.2/install-solaris.html

     Refer to the COPYRIGHT and LICENSE for legal terms of use.

Demos

     A demo directory is included in this software bundle with a
     variety of applets and Swing applications for you to try out. The
     demos come with complete source code.

Sun Java Web Pages

     For additional information, refer to these Sun Microsystems pages
     on the World Wide Web:

     http://java.sun.com/
          The Java Software web site, with the latest information on
          Java technology, product information, news, and features.
     http://java.sun.com/products/jdk/1.2/index.html
          JDK 1.2 Product and Download Page
     http://java.sun.com/docs
          Java Platform Documentation provides access to white papers,
          the Java Tutorial and other documents.
     http://developer.java.sun.com/
          The Java Developer Connection web site. (Free registration
          required.) Additional technical information, news, and
          features; user forums; support information, and much more.
     http://java.sun.com/products/
          Java Technology Products & API
     http://www.sun.com/solaris/java/
          Java Development Kit for Solaris - Production Release

  ------------------------------------------------------------------------
This Java Development Kit is a product of Java Software of Sun
Microsystems(tm), Inc.

Copyright © 1997, 1998, Sun Microsystems, Inc.
901 San Antonio Road, Palo Alto, California 94303-4900 USA.
All rights reserved.
</PRE>

</BODY>
</HTML>
