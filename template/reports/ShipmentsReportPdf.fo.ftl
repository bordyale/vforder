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
        margin-top="0.1in" margin-bottom="1in" margin-left=".5in" margin-right="1in">
          <fo:region-body margin-top="0.2in"/>
          <fo:region-before extent="1in"/>
          <fo:region-after extent="1in"/>
      </fo:simple-page-master>
    </fo:layout-master-set>


  
  <fo:page-sequence master-reference="main">
  	
	
	<fo:flow flow-name="xsl-region-body" font-family="NotoSans">
            <fo:block>
              <#if orderItems?has_content>
			  <fo:block font-size="13pt" font-weight="bold" text-align="center">
					${uiLabelMap.ShippingProgram} 
			  </fo:block>
              <fo:table width="100%" table-layout="fixed"> 
	              <fo:table-column column-width="8%" border-width="1px" border-style="solid" />
		          <fo:table-column column-width="10%" border-width="1px" border-style="solid" />
				  <fo:table-column column-width="8%" border-width="1px" border-style="solid" />
				  <fo:table-column column-width="25%" border-width="1px" border-style="solid" />
		          <fo:table-column column-width="8%" border-width="1px" border-style="solid" />
				  <fo:table-column column-width="8%" border-width="1px" border-style="solid" /> 
				  <fo:table-column column-width="8%" border-width="1px" border-style="solid" />
				  <fo:table-column column-width="8%" border-width="1px" border-style="solid" /> 
				  <fo:table-column column-width="8%" border-width="1px" border-style="solid" />           
                <fo:table-header text-align="center" background-color="silver">
					<fo:table-row>
						<fo:table-cell padding="1mm" border-width="0.3mm" border-style="solid" font-size="8pt">
							<fo:block font-weight="bold">${uiLabelMap.OrderOrderId}</fo:block>
						</fo:table-cell>
						<fo:table-cell padding="1mm" border-width="0.3mm" border-style="solid" font-size="8pt">
							<fo:block font-weight="bold">${uiLabelMap.ShippingDate}</fo:block>
						</fo:table-cell>
						<fo:table-cell padding="1mm" border-width="0.3mm" border-style="solid" font-size="8pt">
							<fo:block font-weight="bold">${uiLabelMap.ProductProductId}</fo:block>
						</fo:table-cell>					
						<fo:table-cell padding="1mm" border-width="0.3mm" border-style="solid" font-size="8pt">
							<fo:block font-weight="bold">${uiLabelMap.ProductName}</fo:block>
						</fo:table-cell>
						<fo:table-cell padding="1mm" border-width="0.3mm" border-style="solid" font-size="8pt">
							<fo:block font-weight="bold">${uiLabelMap.Quantity}</fo:block>
						</fo:table-cell>
						<fo:table-cell padding="1mm" border-width="0.3mm" border-style="solid" font-size="8pt">
							<fo:block font-weight="bold">${uiLabelMap.QuantityShipped}</fo:block>
						</fo:table-cell>
						<fo:table-cell padding="1mm" border-width="0.3mm" border-style="solid" font-size="8pt">
							<fo:block font-weight="bold">${uiLabelMap.QuantityShippable}</fo:block>
						</fo:table-cell>
						<fo:table-cell padding="1mm" border-width="0.3mm" border-style="solid" font-size="8pt">
							<fo:block font-weight="bold">${uiLabelMap.StatusPartial}</fo:block>
						</fo:table-cell>
						<fo:table-cell padding="1mm" border-width="0.3mm" border-style="solid" font-size="8pt">
							<fo:block font-weight="bold">${uiLabelMap.StatusNone}</fo:block>
						</fo:table-cell>
						
					</fo:table-row>
				</fo:table-header>
                <fo:table-body>
                
	                <#list orderItems as item>
		                
		                  <fo:table-row>
		                  	<fo:table-cell border-style="solid" border-width="0.3mm" font-size="8pt">
		                      <fo:block>
		                        ${item.orderId}
		                      </fo:block>
		                    </fo:table-cell>
		                    <fo:table-cell border-style="solid" border-width="0.3mm" font-size="8pt">
		                      <fo:block>
		                        ${item.estimatedShipDate?if_exists?string("yyyy.MM.dd")}
		                      </fo:block>
		                    </fo:table-cell>
		                    <fo:table-cell border-style="solid" border-width="0.3mm" font-size="8pt">
		                      <fo:block>
		                        ${item.productId}
		                      </fo:block>
		                    </fo:table-cell>
		                    <fo:table-cell border-style="solid" border-width="0.3mm" font-size="8pt">
		                      <fo:block>
		                        ${item.productName}
		                      </fo:block>
		                    </fo:table-cell>
		                    <fo:table-cell border-style="solid" border-width="0.3mm" font-size="8pt">
		                      <fo:block>
		                        ${item.quantity}
		                      </fo:block>
		                    </fo:table-cell>
		                    <fo:table-cell border-style="solid" border-width="0.3mm" font-size="8pt">
		                      <fo:block>
		                        ${item.quantityShipped}
		                      </fo:block>
		                    </fo:table-cell>
		                    <fo:table-cell border-style="solid" border-width="0.3mm" font-size="8pt">
		                      <fo:block>
		                        ${item.quantityShippable}
		                      </fo:block>
		                    </fo:table-cell>
		                    <fo:table-cell border-style="solid" border-width="0.3mm" font-size="8pt">
		                      <fo:block>
		                        
		                      </fo:block>
		                    </fo:table-cell>
		                    <fo:table-cell border-style="solid" border-width="0.3mm" font-size="8pt">
		                      <fo:block>
		                        
		                      </fo:block>
		                    </fo:table-cell>
		                  </fo:table-row>		         
	                 </#list>
     
                </fo:table-body>
              </fo:table>
              </#if>
              <#if shipWeights?has_content>
              <fo:block font-size="13pt" font-weight="bold" text-align="center">
					${uiLabelMap.NetWeight} 
			  </fo:block>
              <fo:table width="100%" table-layout="fixed"> 
	              <fo:table-column column-width="8%" border-width="1px" border-style="solid" />
		          <fo:table-column column-width="10%" border-width="1px" border-style="solid" />
				  <fo:table-column column-width="8%" border-width="1px" border-style="solid" />
				        
                <fo:table-header text-align="center" background-color="silver">
					<fo:table-row>
						<fo:table-cell padding="1mm" border-width="0.3mm" border-style="solid" font-size="8pt">
							<fo:block font-weight="bold">${uiLabelMap.ShippingId}</fo:block>
						</fo:table-cell>
						<fo:table-cell padding="1mm" border-width="0.3mm" border-style="solid" font-size="8pt">
							<fo:block font-weight="bold">${uiLabelMap.ShippingDate}</fo:block>
						</fo:table-cell>
						<fo:table-cell padding="1mm" border-width="0.3mm" border-style="solid" font-size="8pt">
							<fo:block font-weight="bold">${uiLabelMap.NetWeight}</fo:block>
						</fo:table-cell>					
						
					</fo:table-row>
				</fo:table-header>
                <fo:table-body>
                
	                <#list shipWeights as item>
		                
		                  <fo:table-row>
		                  	<fo:table-cell border-style="solid" border-width="0.3mm" font-size="8pt">
		                      <fo:block>
		                        ${item.shipmentId}
		                      </fo:block>
		                    </fo:table-cell>
		                    <fo:table-cell border-style="solid" border-width="0.3mm" font-size="8pt">
		                      <fo:block>
		                        ${item.estimatedShipDate?if_exists?string("yyyy.MM.dd")}
		                      </fo:block>
		                    </fo:table-cell>
		                    <fo:table-cell border-style="solid" border-width="0.3mm" font-size="8pt">
		                      <fo:block>
		                        ${item.netWeight}
		                      </fo:block>
		                    </fo:table-cell>
		                  </fo:table-row>		         
	                 </#list>
	                 	<fo:table-row>
		                  	<fo:table-cell border-style="solid" border-width="0.3mm" font-size="8pt">
		                      <fo:block>
		                        ${uiLabelMap.TotalNetWeight}
		                      </fo:block>
		                    </fo:table-cell>
		                    <fo:table-cell border-style="solid" border-width="0.3mm" font-size="8pt">
		                      <fo:block>
		                        ${totalShippedWeight}
		                      </fo:block>
		                    </fo:table-cell>
		                    <fo:table-cell border-style="solid" border-width="0.3mm" font-size="8pt">
		                      <fo:block>
		                        
		                      </fo:block>
		                    </fo:table-cell>
		                  </fo:table-row>	
	                 
           
                </fo:table-body>
              </fo:table>
              </#if>
              
              
              
              <#if orderItems2?has_content>
              <fo:block  font-size="13pt" font-weight="bold" text-align="center">
					${uiLabelMap.OrdersNotShipped} 
			  </fo:block>
              <fo:table width="100%" table-layout="fixed"> 
	              <fo:table-column column-width="6%" border-width="1px" border-style="solid" />
	              <#if allInfo == "N">
				  <fo:table-column column-width="20%" border-width="1px" border-style="solid" />   
				 </#if>
		          <fo:table-column column-width="10%" border-width="1px" border-style="solid" />
				  <fo:table-column column-width="6%" border-width="1px" border-style="solid" />
				  <fo:table-column column-width="27%" border-width="1px" border-style="solid" />
		          <fo:table-column column-width="6%" border-width="1px" border-style="solid" />
				  <fo:table-column column-width="6%" border-width="1px" border-style="solid" /> 
				  <fo:table-column column-width="6%" border-width="1px" border-style="solid" />
				  <#if allInfo == "Y">
				  <fo:table-column column-width="6%" border-width="1px" border-style="solid" /> 
				  <fo:table-column column-width="6%" border-width="1px" border-style="solid" />  
				  <fo:table-column column-width="12%" border-width="1px" border-style="solid" /> 
				  <fo:table-column column-width="8%" border-width="1px" border-style="solid" />     
				 </#if>
				 
                <fo:table-header text-align="center" background-color="silver">
					<fo:table-row>
						<fo:table-cell padding="1mm" border-width="0.3mm" border-style="solid" font-size="8pt">
							<fo:block font-weight="bold">${uiLabelMap.OrderOrderId}</fo:block>
						</fo:table-cell>
						<#if allInfo == "N">
						<fo:table-cell padding="1mm" border-width="0.3mm" border-style="solid" font-size="8pt">
							<fo:block font-weight="bold">${uiLabelMap.OrderOrderName}</fo:block>
						</fo:table-cell>
						</#if>	
						<fo:table-cell padding="1mm" border-width="0.3mm" border-style="solid" font-size="8pt">
							<fo:block font-weight="bold">${uiLabelMap.ShippingDate}</fo:block>
						</fo:table-cell>
						<fo:table-cell padding="1mm" border-width="0.3mm" border-style="solid" font-size="8pt">
							<fo:block font-weight="bold">${uiLabelMap.ProductProductId}</fo:block>
						</fo:table-cell>					
						<fo:table-cell padding="1mm" border-width="0.3mm" border-style="solid" font-size="8pt">
							<fo:block font-weight="bold">${uiLabelMap.ProductName}</fo:block>
						</fo:table-cell>
						<fo:table-cell padding="1mm" border-width="0.3mm" border-style="solid" font-size="8pt">
							<fo:block font-weight="bold">${uiLabelMap.Quantity}</fo:block>
						</fo:table-cell>
						<fo:table-cell padding="1mm" border-width="0.3mm" border-style="solid" font-size="8pt">
							<fo:block font-weight="bold">${uiLabelMap.QuantityShipped}</fo:block>
						</fo:table-cell>
						<fo:table-cell padding="1mm" border-width="0.3mm" border-style="solid" font-size="8pt">
							<fo:block font-weight="bold">${uiLabelMap.QuantityShippable}</fo:block>
						</fo:table-cell>
						<#if allInfo == "Y">
						<fo:table-cell padding="1mm" border-width="0.3mm" border-style="solid" font-size="8pt">
							<fo:block font-weight="bold">${uiLabelMap.NetWeight}</fo:block>
						</fo:table-cell>
						<fo:table-cell padding="1mm" border-width="0.3mm" border-style="solid" font-size="8pt">
							<fo:block font-weight="bold">${uiLabelMap.ProgresNetWeight}</fo:block>
						</fo:table-cell>
						<fo:table-cell padding="1mm" border-width="0.3mm" border-style="solid" font-size="8pt">
							<fo:block font-weight="bold">${uiLabelMap.StatusPartial}</fo:block>
						</fo:table-cell>
						<fo:table-cell padding="1mm" border-width="0.3mm" border-style="solid" font-size="8pt">
							<fo:block font-weight="bold">${uiLabelMap.StatusNone}</fo:block>
						</fo:table-cell>	
						 </#if>		
						
					</fo:table-row>
				</fo:table-header>
                <fo:table-body>
                
	                <#list orderItems2 as item>
		                
		                  <fo:table-row>
		                  	<fo:table-cell border-style="solid" border-width="0.3mm" font-size="8pt">
		                      <fo:block>
		                        ${item.orderId}
		                      </fo:block>
		                    </fo:table-cell>
		                    <#if allInfo == "N">
		                    <fo:table-cell border-style="solid" border-width="0.3mm" font-size="8pt">
		                      <fo:block>
		                        ${item.orderName}
		                      </fo:block>
		                    </fo:table-cell>
		                     </#if>
		                    <fo:table-cell border-style="solid" border-width="0.3mm" font-size="8pt">
		                      <fo:block>
		                        ${item.shipBeforeDate?if_exists?string("yyyy.MM.dd")}
		                      </fo:block>
		                    </fo:table-cell>
		                    <fo:table-cell border-style="solid" border-width="0.3mm" font-size="8pt">
		                      <fo:block>
		                        ${item.productId}
		                      </fo:block>
		                    </fo:table-cell>
		                    <fo:table-cell border-style="solid" border-width="0.3mm" font-size="8pt">
		                      <fo:block>
		                        ${item.productName}
		                      </fo:block>
		                    </fo:table-cell>
		                    <fo:table-cell border-style="solid" border-width="0.3mm" font-size="8pt">
		                      <fo:block>
		                        ${item.quantity}
		                      </fo:block>
		                    </fo:table-cell>
		                    <fo:table-cell border-style="solid" border-width="0.3mm" font-size="8pt">
		                      <fo:block>
		                        ${item.quantityShipped}
		                      </fo:block>
		                    </fo:table-cell>
		                    <fo:table-cell border-style="solid" border-width="0.3mm" font-size="8pt">
		                      <fo:block font-weight="bold">
		                        ${item.quantityShippable}
		                      </fo:block>
		                    </fo:table-cell>
		                    <#if allInfo == "Y">
		                    <fo:table-cell border-style="solid" border-width="0.3mm" font-size="8pt">
		                      <fo:block>
		                        ${item.netWeight?round}
		                      </fo:block>
		                    </fo:table-cell>
		                    <fo:table-cell border-style="solid" border-width="0.3mm" font-size="8pt">
		                      <fo:block>
		                        ${item.progresNetWeight?round}
		                      </fo:block>
		                    </fo:table-cell>
		                    <fo:table-cell border-style="solid" border-width="0.3mm" font-size="8pt">
		                      <fo:block>
		                        
		                      </fo:block>
		                    </fo:table-cell>
		                    <fo:table-cell border-style="solid" border-width="0.3mm" font-size="8pt">
		                      <fo:block>
		                        
		                      </fo:block>
		                    </fo:table-cell>
		                     </#if>
		                  </fo:table-row>		         
	                 </#list>
           
                </fo:table-body>
              </fo:table>
              </#if>
              
              
              
              
              
              
              
              
              
              
              
              
              
              
              
              
              
              
              
              
              
              
              
              
              
              <#if exShippedPr?has_content>
              <fo:block font-size="13pt" font-weight="bold" text-align="center">
					${uiLabelMap.ExtraShipped} 
			  </fo:block>
              <fo:table width="100%" table-layout="fixed"> 
	           
				  <fo:table-column column-width="8%" border-width="1px" border-style="solid" />
				  <fo:table-column column-width="27%" border-width="1px" border-style="solid" />
				  <fo:table-column column-width="8%" border-width="1px" border-style="solid" />
				  <fo:table-column column-width="8%" border-width="1px" border-style="solid" />
				  <fo:table-column column-width="8%" border-width="1px" border-style="solid" />
				        
                <fo:table-header text-align="center" background-color="silver">
					<fo:table-row>
						
						
						<fo:table-cell padding="1mm" border-width="0.3mm" border-style="solid" font-size="8pt">
							<fo:block font-weight="bold">${uiLabelMap.ProductProductId}</fo:block>
						</fo:table-cell>					
						<fo:table-cell padding="1mm" border-width="0.3mm" border-style="solid" font-size="8pt">
							<fo:block font-weight="bold">${uiLabelMap.ProductName}</fo:block>
						</fo:table-cell>
						<fo:table-cell padding="1mm" border-width="0.3mm" border-style="solid" font-size="8pt">
							<fo:block font-weight="bold">${uiLabelMap.Quantity}</fo:block>
						</fo:table-cell>
						<fo:table-cell padding="1mm" border-width="0.3mm" border-style="solid" font-size="8pt">
							<fo:block font-weight="bold">${uiLabelMap.StatusPartial}</fo:block>
						</fo:table-cell>
						<fo:table-cell padding="1mm" border-width="0.3mm" border-style="solid" font-size="8pt">
							<fo:block font-weight="bold">${uiLabelMap.StatusNone}</fo:block>
						</fo:table-cell>				
						
					</fo:table-row>
				</fo:table-header>
                <fo:table-body>
                
	                <#list exShippedPr as item>
		                
		                  <fo:table-row>
		                  	
		                    
		                    <fo:table-cell border-style="solid" border-width="0.3mm" font-size="8pt">
		                      <fo:block>
		                        ${item.productId}
		                      </fo:block>
		                    </fo:table-cell>
		                    <fo:table-cell border-style="solid" border-width="0.3mm" font-size="8pt">
		                      <fo:block>
		                        ${item.productName}
		                      </fo:block>
		                    </fo:table-cell>
		                    <fo:table-cell border-style="solid" border-width="0.3mm" font-size="8pt">
		                      <fo:block>
		                        ${item.quantity}
		                      </fo:block>
		                    </fo:table-cell>
		                    <fo:table-cell border-style="solid" border-width="0.3mm" font-size="8pt">
		                      <fo:block>
		                        
		                      </fo:block>
		                    </fo:table-cell>
		                    <fo:table-cell border-style="solid" border-width="0.3mm" font-size="8pt">
		                      <fo:block>
		                        
		                      </fo:block>
		                    </fo:table-cell>
		                  </fo:table-row>		         
	                 </#list>
           
                </fo:table-body>
              </fo:table>
              </#if>
              
              
              
              
              <#if prodQty?has_content>
              <fo:block font-size="13pt" font-weight="bold" text-align="center">
					${uiLabelMap.ProdShippedQuantity} 
			  </fo:block>
              <fo:table width="100%" table-layout="fixed"> 
	           
				  <fo:table-column column-width="8%" border-width="1px" border-style="solid" />
				  <fo:table-column column-width="27%" border-width="1px" border-style="solid" />
				  <fo:table-column column-width="8%" border-width="1px" border-style="solid" />
				  
				        
                <fo:table-header text-align="center" background-color="silver">
					<fo:table-row>
						
						
						<fo:table-cell padding="1mm" border-width="0.3mm" border-style="solid" font-size="8pt">
							<fo:block font-weight="bold">${uiLabelMap.ProductProductId}</fo:block>
						</fo:table-cell>					
						<fo:table-cell padding="1mm" border-width="0.3mm" border-style="solid" font-size="8pt">
							<fo:block font-weight="bold">${uiLabelMap.ProductName}</fo:block>
						</fo:table-cell>
						<fo:table-cell padding="1mm" border-width="0.3mm" border-style="solid" font-size="8pt">
							<fo:block font-weight="bold">${uiLabelMap.Quantity}</fo:block>
						</fo:table-cell>				
						
					</fo:table-row>
				</fo:table-header>
                <fo:table-body>
                
	                <#list prodQty as item>
		                
		                  <fo:table-row>
		                  	
		                    
		                    <fo:table-cell border-style="solid" border-width="0.3mm" font-size="8pt">
		                      <fo:block>
		                        ${item.productId}
		                      </fo:block>
		                    </fo:table-cell>
		                    <fo:table-cell border-style="solid" border-width="0.3mm" font-size="8pt">
		                      <fo:block>
		                        ${item.productName}
		                      </fo:block>
		                    </fo:table-cell>
		                    <fo:table-cell border-style="solid" border-width="0.3mm" font-size="8pt">
		                      <fo:block>
		                        ${item.quantity}
		                      </fo:block>
		                    </fo:table-cell>
		                    
		                  </fo:table-row>		         
	                 </#list>
           
                </fo:table-body>
              </fo:table>
              </#if>
                        
            </fo:block>
	</fo:flow>

  </fo:page-sequence>


</fo:root>
</#escape>
