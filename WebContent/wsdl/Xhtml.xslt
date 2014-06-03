<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="file:///C:/TEMP/bmecat_new_catalog_1_2_simple_without_NS.xsd">
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	<xsl:template match="/BMECAT">
		<html>
			<head>
				<xsl:apply-templates select="T_NEW_CATALOG/Header"/>
			</head>
			<body>
				<h1>
        Productcatalog
        </h1>
				<table>
			<tr>
			<th>Order No.</th>
			<th>Title</th>
			<th>Description</th>
			<th>Price net</th>
			<th>Price gross</th>
			<th>Currency</th>
		</tr>
		<tr>		
					<xsl:apply-templates select="T_NEW_CATALOG/ARTICLE"/>
		</tr>
				</table>
			</body>
		</html>
	</xsl:template>
	<xsl:template match="Header">
		<title>Firmenname</title>
	</xsl:template>
	<xsl:template match="Article">
		
		
			<td>
				<xsl:value-of select="SupplierAID"/>
			</td>
			<td>
				<xsl:apply-templates select="ShortDescr"/>
			</td>
			<td>
				<xsl:apply-templates select="LongDescr"/>
				<xsl:apply-templates select="PriceCurrency"/>
			</td>
			<td>
				<xsl:apply-templates select="PriceNet"/>
				<xsl:apply-templates select="PriceCurrency"/>
			</td>
			<td>
				<xsl:apply-templates select="PriceGros"/>
			</td>
		
	</xsl:template>
	<xsl:template match="SupplerAID">
		<xsl:value-of select="SUPPLIER_AID"/>
	</xsl:template>
	<xsl:template match="ShortDescr">
		<xsl:value-of select="ARTICLE_DETAILS/DESCRIPTION_SHORT"/>
	</xsl:template>
	<xsl:template match="LongDescr">
		<xsl:value-of select="ARTICLE_DETAILS/DESCRIPTION_LONG"/>
	</xsl:template>
	<!-- Problem in XML nur net_list Preise gelistet   -->
	<xsl:template match="PriceNet">
		<xsl:value-of select="ARTICLE_PRICE_DETAILS/ARTICLE_PRICE/PRICE_AMOUNT"/>
	</xsl:template>
	<xsl:template match="PriceGros">
		<xsl:value-of select="format-number((ARTICLE_PRICE_DETAILS/ARTICLE_PRICE/PRICE_AMOUNT) div (1 + number(RTICLE_PRICE_DETAILS/ARTICLE_PRICE/TAX)), '#.00')" />
		
	</xsl:template>
	<xsl:template match="PriceCurrency">
		<xsl:value-of select="ARTICLE_PRICE_DETAILS/ARTICLE_PRICE/PRICE_CURRENCY"/>
	</xsl:template>
</xsl:stylesheet>
