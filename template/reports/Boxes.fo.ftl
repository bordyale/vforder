<#--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->

<#escape x as x?xml>
<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
    <fo:layout-master-set>
      <fo:simple-page-master master-name="main" page-height="8.5in" page-width="11in"
        margin-top="0.5in" margin-bottom="1in" margin-left=".5in" margin-right="1in">
          <fo:region-body margin-top="1in"/>
          <fo:region-before extent="1in"/>
          <fo:region-after extent="1in"/>
      </fo:simple-page-master>
    </fo:layout-master-set>

<#assign keys = boxes?keys>
<#list boxesListKey as key>
  <#assign box = boxesMap.get(key)>
  <#assign products = box.get("products")>
  <fo:page-sequence master-reference="main">
  	<fo:static-content flow-name="xsl-region-after">
			<fo:block font-size="60pt" font-weight="bold" text-align="center">
				Brutto - ${box.boxWeight?round}
			</fo:block>
	</fo:static-content>
    <fo:flow flow-name="xsl-region-body" font-family="Helvetica">
      
      <#assign productSize = products.size()>
      <#list products as product>
      
      	<#if productSize gt 1>
     		
     			<fo:block text-align="left" font-weight="bold" font-size="60pt">
        			${product.productName}
      			</fo:block>
      			<fo:block text-align="right" font-weight="bold" font-size="60pt">
        			St - ${product.quantity}
      			</fo:block>
     		
     	<#else>
     		
     			<fo:block text-align="left" font-weight="bold" font-size="90pt">
        			${product.productName}
      			</fo:block>
      			<fo:block text-align="right" font-weight="bold" font-size="90pt">
        			St - ${product.quantity}
      			</fo:block>

   		</#if>

      
      </#list>
      
      
    </fo:flow>
    
  </fo:page-sequence>
</#list>


</fo:root>
</#escape>
