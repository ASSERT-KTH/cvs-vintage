<?xml version="1.0" encoding="ISO-8859-1" ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">


<xsl:template match="/">
  <document>

  <properties>
    <title>Scarab Webapp Properties</title>
    <author email="epugh@opensourceconnections.com">Eric Pugh</author>
    <author email="p.ledbrook@cacoethes.co.uk">Peter Ledbrook</author>    
  </properties>
  
   <body>
    <section name="Scarab Properties">
      <table>
        <tr>
          <th>Property name</th>
          <th>Type</th>
          <th>File</th>
          <th>Comment</th>          
        </tr>
  <xsl:apply-templates />
      </table>
    </section>
  </body>
</document>
</xsl:template>

<xsl:template match="property">
  <tr>
  <td><xsl:value-of select="name" /></td>
  <td><xsl:value-of select="type" /></td>
  <td><xsl:value-of select="file" /></td>
  <td><xsl:value-of select="comment" /></td>
  </tr>
</xsl:template>

</xsl:stylesheet>
