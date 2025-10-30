<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema">

    <xsl:output method="xml" encoding="UTF-8" indent="yes" omit-xml-declaration="no"/>

    <!-- Strip whitespace from all elements -->
    <xsl:strip-space elements="*"/>

    <!-- Identity transform -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <!-- Add editor-map attribute only to rendertheme element -->
    <xsl:template match="xs:complexType[@name='rendertheme']">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:apply-templates select="*[self::xs:choice or self::xs:sequence or self::xs:all]"/>
            <!-- Add the editor-map attribute to rendertheme -->
            <xs:attribute name="editor-map" type="xs:string" use="optional" default=""/>
            <xsl:apply-templates select="*[not(self::xs:choice or self::xs:sequence or self::xs:all)]"/>
        </xsl:copy>
    </xsl:template>

    <!-- Add editor-enabled attribute to selected complexType elements -->
    <xsl:template match="xs:complexType[
        @name='rule' or
        @name='area' or
        @name='line' or
        @name='caption' or
        @name='symbol' or
        @name='circle' or
        @name='lineSymbol' or
        @name='pathText'
    ]">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:apply-templates select="*[self::xs:choice or self::xs:sequence or self::xs:all]"/>
            <!-- Add the new attribute after the content model but before other elements -->
            <xs:attribute name="editor-enabled" type="xs:boolean" use="optional" default="true"/>
            <xs:attribute name="editor-comment" type="xs:string" use="optional" default=""/>
            <xsl:apply-templates select="*[not(self::xs:choice or self::xs:sequence or self::xs:all)]"/>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>