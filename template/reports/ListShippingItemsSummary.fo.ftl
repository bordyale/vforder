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
      <fo:simple-page-master master-name="main" page-height="11in" page-width="8.5in"
        margin-top="0.5in" margin-bottom="1in" margin-left="1.5in" margin-right=".5in">
          <fo:region-body margin-top="1in"/>
          <fo:region-before extent="1in"/>
          <fo:region-after extent="1in"/>
      </fo:simple-page-master>
    </fo:layout-master-set>


  
  <fo:page-sequence master-reference="main">
  	<fo:static-content flow-name="xsl-region-before">
			<fo:block font-size="26pt" font-weight="bold" text-align="center">
				${uiLabelMap.ShippingDate} - ${shipment.estimatedShipDate?if_exists?string("yyyy.MM.dd")}
			</fo:block>
	</fo:static-content>
	<fo:flow flow-name="xsl-region-body" font-family="Helvetica">
            <fo:block>
            	<#if vfi gt 0>
            		<#if justSupplier != "YES">
		              <fo:table width="100%" table-layout="fixed">
		              	<fo:table-column column-width="40%" border-width="1px" border-style="solid" />
	              		<fo:table-column column-width="30%" border-width="1px" border-style="solid" />
						<fo:table-column column-width="30%" border-width="1px" border-style="solid" />             
		                <fo:table-body>
		                <#list listIt2 as item>
		                	<#if !item.supplier?has_content> 
			                  <fo:table-row>
			                    <fo:table-cell border-style="solid" border-width="0.3mm">
			                      <fo:block font-size="26pt">
			                        ${item.productName}
			                      </fo:block>
			                    </fo:table-cell>
			                    <fo:table-cell border-style="solid" border-width="0.3mm">
			                      <fo:block font-size="26pt">
			                        ${item.comments}
			                      </fo:block>
			                    </fo:table-cell>
			                    <fo:table-cell border-style="solid" border-width="0.3mm">
			                      <fo:block font-size="26pt">
			                        ${item.quantity}
			                      </fo:block>
			                    </fo:table-cell>
			                  </fo:table-row>
			                 </#if>
		                 </#list>
		                </fo:table-body>
		              </fo:table>
		             </#if>
	            </#if>
	           
	            	
		       
	            <#if vvi gt 0> 
	            	<fo:block space-before="5mm" space-after="5mm" font-size="35pt">
		            	${VV}
		        	</fo:block>
	              <fo:table width="100%" table-layout="fixed">
	              	<fo:table-column column-width="40%" border-width="1px" border-style="solid" />
	              	<fo:table-column column-width="30%" border-width="1px" border-style="solid" />
					<fo:table-column column-width="30%" border-width="1px" border-style="solid" />           
	                <fo:table-body>
	                <#list listIt2 as item>
	                	<#if item.supplier?has_content> 
		                  <fo:table-row>
		                    <fo:table-cell border-style="solid" border-width="0.3mm">
		                      <fo:block font-size="26pt">
		                        ${item.productName}
		                      </fo:block>
		                    </fo:table-cell>
		                    <fo:table-cell border-style="solid" border-width="0.3mm">
		                      <fo:block font-size="26pt">
		                        ${item.comments}
		                      </fo:block>
		                    </fo:table-cell>
		                    <fo:table-cell border-style="solid" border-width="0.3mm">
		                      <fo:block font-size="26pt">
		                        ${item.quantity}
		                      </fo:block>
		                    </fo:table-cell>
		                  </fo:table-row>
		                 </#if>
	                 </#list>
	                </fo:table-body>
	              </fo:table>
	            </#if>
	            
	            
	            
	            
            </fo:block>
	</fo:flow>

  </fo:page-sequence>


</fo:root>
</#escape>
