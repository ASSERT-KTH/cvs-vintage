<?xml version="1.0" encoding="ISO-8859-1" ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output indent="yes"/>

<xsl:template match="/properties">
  <document>

  <properties>
    <title>Scarab Webapp Properties</title>
    <author email="epugh@opensourceconnections.com">Eric Pugh</author>
    <author email="p.ledbrook@cacoethes.co.uk">Peter Ledbrook</author>    
  </properties>
  
   <body>
    <section name="Scarab Properties">
      <xsl:apply-templates />
    </section>
  </body>
</document>
</xsl:template>

<xsl:template match="section">
  <subsection name="{@name}">
    <p>
      <pre><xsl:value-of select="details"/></pre>
    </p>
    <table>
      <tr>
        <th>Property name</th>
        <th>Type</th>
        <th>Comment</th>          
      </tr>
      <xsl:apply-templates />
    </table>
  </subsection>
</xsl:template>

<xsl:template match="group">
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="property">
  <tr>
  <td><xsl:value-of select="name" /></td>
  <td><xsl:value-of select="type" /></td>
  <td><xsl:value-of select="comment" /></td>
  </tr>
</xsl:template>

  <!-- By default, discard anything not matched yet -->
  <xsl:template match="@*|node()" priority="-1">
	<!--xsl:copy>
	  <xsl:apply-templates select="@*|node()"/>
	</xsl:copy-->
  </xsl:template>

</xsl:stylesheet>
