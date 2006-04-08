<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:output method="xml" indent="yes"/>
	<xsl:preserve-space elements="uml"/>

	
<!--
Remove any FigAssociationClassTee groups (issue 4136)
-->
	<xsl:template match='group[starts-with(./@description, "org.argouml.uml.diagram.ui.FigAssociationClassTee")]' />
	
<!--
Remove href attribute from any FigEdgeNote groups (issue 4021)
-->
	<xsl:template match='group[./@description="org.argouml.uml.diagram.static_structure.ui.FigEdgeNote"]'>
	
		<group name="{@name}" description="{@description}" stroke="{@stroke}" strokecolor="{@strokecolor}">
			<xsl:copy-of select="./node()"/>
		</group>
	</xsl:template>
	
	

<!-- 
Anything not touched by the fixes above must be copied over unchanged
-->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>
