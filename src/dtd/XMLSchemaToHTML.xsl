<?xml version="1.0"?>
<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- $Id: XMLSchemaToHTML.xsl,v 1.1 2002/03/25 20:02:52 jmcnally Exp $ -->

<!-- 

  Synopsis: 

    Produce HTML documentation from an XML based database schema definition. 

  Input: 

    A database schema XML file that conforms to the torque 'database.dtd'.

  Output: 

    A single HTML file that documents the databse. The HTML consists 
    of a summary of all the tables and their descriptions. The summary 
    contains links that jump to more detailled descriptions of each table.

    The detailled table descriptions contain:

    - A list of all the fields, including the field name, data type and 
      field description. A '*' appears after field names if they are part of
      the primary key.
    - A list of foreign keys.
    - A list of indexes.

  Example: Using the Xalan XSLT processor:

    java org.apache.xalan.xslt.Process \
       -XSL XMLSchemaToHTML.xsl \
       -IN  scarab-schema.xml \
       -OUT out.html

  Author:

    Alan Hodgkinson, March 2002. mailto:alan.hodgkinson@alum.mit.edu

-->


<!-- Top level template -->

<xsl:template match="/">
  <html>
  <head>
    <title>Database Schema Documentation for 
      <xsl:value-of select="database/@name"/>
    </title>
  </head>
  <body bgcolor="FFFFFF">
  <xsl:apply-templates select="database"/>
  </body>
  </html>
</xsl:template>


<xsl:template match="database">
  <h1>Database: 
    <xsl:value-of select="@name"/>
  </h1>
  <xsl:call-template name="table-summary"/>
  <xsl:call-template name="table-detail"/>
</xsl:template> 


<!-- Table Summary -->

<xsl:template name="table-summary">
  <h2>Tables</h2>
  <table border="1">
    <tr>
      <th>Table Name</th>
      <th>Description</th>
    </tr>
    <xsl:for-each select="table">
      <xsl:sort select="@name"/>
      <xsl:call-template name="table-summary-row"/>
    </xsl:for-each>
  </table>
</xsl:template>

<xsl:template name="table-summary-row">
  <tr>
    <td>
      <a href="#{@name}">
        <xsl:value-of select="@name"/>
      </a>
    </td>
    <td>
      <xsl:if test="string-length(@description) > 0">
        <xsl:value-of select="@description"/>
      </xsl:if>
      <xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
    </td>
  </tr>
</xsl:template>


<!-- Table detail -->

<xsl:template name="table-detail">
  <xsl:for-each select="table">
    <xsl:sort select="@name"/>
    <xsl:call-template name="table-detail-table"/>
  </xsl:for-each>
</xsl:template>

<xsl:template name="table-detail-table">
  <br></br>
  <br></br>
  <hr></hr>
  <a name="{@name}"> <!-- Link to detail information -->
    <h2>
      <xsl:value-of select="@name"/>
    </h2>
  </a>
  <h3>
    Description:
  </h3>
  <xsl:value-of select="@description"/>

  <xsl:call-template name="columns"/>       <!-- Columns      -->
  <xsl:call-template name="foreign-keys"/>  <!-- Foreign Keys -->
  <xsl:call-template name="indexes"/>       <!-- Indexes      -->
</xsl:template>


<!-- Columns -->

<xsl:template name="columns">
  <h3>
    Columns:
  </h3>
  <table border="1">
    <tr>
      <th>Column Name</th>
      <th>Type</th>
      <th>Description</th>
    </tr>
    <xsl:for-each select="column">
      <xsl:sort select="@name"/>
      <tr>
        <td>
          <xsl:value-of select="@name"/>
          <!-- Display '*' after column, if it's part of primary key -->
          <xsl:if test="@primaryKey = 'true'">
           *
          </xsl:if>
        </td>
        <td>
          <xsl:call-template name="column-type-info"/>
        </td>
        <td>
          <xsl:if test="string-length(@description) > 0">
            <xsl:value-of select="@description"/>
          </xsl:if>
          <xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
        </td>
      </tr>
    </xsl:for-each>
  </table>
</xsl:template>

<xsl:template name="column-type-info">
  <!-- Displays: "@type (@size) {not} null" -->
  <xsl:value-of select="@type"/>
  <xsl:if test="@size > 0">
    (<xsl:value-of select="@size"/>)
  </xsl:if>
  <xsl:if test="@required = 'true'">
    not
  </xsl:if>
  null
</xsl:template>


<!-- Foreign Keys -->

<xsl:template name="foreign-keys">
  <!-- Only output if foreign keys defined -->
  <xsl:if test="count(foreign-key) != 0">
    <h3>
      Foreign Keys:
    </h3>
    <table border="1">
      <tr>
        <th>Foreign Table</th>
        <th>Local Field</th>
        <th>Foreign Field</th>
      </tr>
      <xsl:for-each select="foreign-key">
        <xsl:sort select="@foreignTable"/>
        <tr>
          <td>
            <a href="#{@foreignTable}">
              <xsl:value-of select="@foreignTable"/>
            </a>
          </td>
          <xsl:call-template name="foreign-key-detail"/>
        </tr>
      </xsl:for-each>
    </table>
  </xsl:if>
</xsl:template>

<xsl:template name="foreign-key-detail">
  <td>
    <xsl:for-each select="reference">
      <xsl:sort select="@local"/>
      <xsl:value-of select="@local"/>
      <br></br>
    </xsl:for-each>
  </td>
  <td>
    <xsl:for-each select="reference">
      <xsl:sort select="@local"/>
      <xsl:value-of select="@foreign"/>
      <br></br>
    </xsl:for-each>
  </td>

</xsl:template>


<!-- Indexes -->

<xsl:template name="indexes">
  <!-- Only output if foreign keys defined -->
  <xsl:if test="count(index) != 0">
    <h3>
      Indexes:
    </h3>
    <table border="1">
      <tr>
        <th>Index Name</th>
        <th>Fields</th>
      </tr>
      <xsl:for-each select="index">
        <xsl:sort select="@name"/>
        <tr>
          <td>
            <xsl:value-of select="@name"/>
          </td>
          <xsl:call-template name="index-detail"/>
        </tr>
      </xsl:for-each>
    </table>
  </xsl:if>
</xsl:template>

<xsl:template name="index-detail">
  <td>
    <xsl:for-each select="index-column">
      <xsl:sort select="@name"/>
      <xsl:value-of select="@name"/>
      <br></br>
    </xsl:for-each>
  </td>
</xsl:template>

</xsl:stylesheet>