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

<#if orderHeader?has_content>
    <div class="screenlet">
        <div class="screenlet-title-bar">
            <ul>
                <li class="h3">&nbsp;${uiLabelMap.OrderOrderItems}</li>
            </ul>
            <br class="clear" />
        </div>
        <div class="screenlet-body">
            <table class="order-items basic-table" cellspacing='0'>
                <tr valign="bottom" class="header-row">
                    <td width="50%">${uiLabelMap.ProductProduct}</td>                    
                    <td width="25%">${uiLabelMap.OrderQuantity}</td>   
                    <td width="25%">${uiLabelMap.OrderExtra}</td>                
                   
                </tr>
                <#if !orderItemList?has_content>
                    <tr>
                        <td colspan="7">
                            <font color="red">${uiLabelMap.checkhelper_sales_order_lines_lookup_failed}</font>
                        </td>
                    </tr>
                <#else>
                    <#assign itemClass = "2">
                    <#list orderItemList as orderItem>
                        <#assign orderItemContentWrapper = Static["org.apache.ofbiz.order.order.OrderContentWrapper"].makeOrderContentWrapper(orderItem, request)>
                        <#assign orderItemShipGrpInvResList = orderReadHelper.getOrderItemShipGrpInvResList(orderItem)>
                        <#if orderHeader.orderTypeId == "SALES_ORDER"><#assign pickedQty = orderReadHelper.getItemPickedQuantityBd(orderItem)></#if>
                        <tr<#if itemClass == "1"> class="alternate-row"</#if>>
                            <#assign orderItemType = orderItem.getRelatedOne("OrderItemType", false)!>
                            <#assign productId = orderItem.productId!>
                            <#if productId?? && productId == "shoppingcart.CommentLine">
                                <td colspan="7" valign="top" class="label"> &gt;&gt; ${orderItem.itemDescription}</td>
                            <#else>
                                <td colspan="1">
                                    <div class="order-item-description">
                                        <#if orderItem.supplierProductId?has_content>
                                         <!--   ${orderItem.supplierProductId} - -->${orderItem.itemDescription!}
                                        <#elseif productId??>
                                           <!-- ${orderItem.productId?default("N/A")} - --> ${orderItem.itemDescription!}
                                            <#if (product.salesDiscontinuationDate)?? && Static["org.apache.ofbiz.base.util.UtilDateTime"].nowTimestamp().after(product.salesDiscontinuationDate)>
                                                <br />
                                                <span style="color: red;">${uiLabelMap.OrderItemDiscontinued}: ${Static["org.apache.ofbiz.base.util.UtilFormatOut"].formatDateTime(product.salesDiscontinuationDate, "", locale, timeZone)!}</span>
                                            </#if>
                                        <#elseif orderItemType??>
                                            ${orderItemType.description} - ${orderItem.itemDescription!}
                                        <#else>
                                            ${orderItem.itemDescription!}
                                        </#if>
                                        <#assign orderItemAttributes = orderItem.getRelated("OrderItemAttribute", null, null, false)/>
                                        <#if orderItemAttributes?has_content>
                                            <ul>
                                            <#list orderItemAttributes as orderItemAttribute>
                                                <li>
                                                    ${orderItemAttribute.attrName} : ${orderItemAttribute.attrValue}
                                                </li>
                                            </#list>
                                            </ul>
                                        </#if>
                                    </div>
                                    
                                </td>
                                <td>${orderItem.quantity?default(0)?string.number}</td>
                                
                                <td><a href="javascript:void(0)" id="showOrderItemShipping_LF_${orderItem.orderItemSeqId}_link" class="buttontext create">${uiLabelMap.ShowShippingDetails} </a></td>
                                <script type="text/javascript">
							            //<![CDATA[
							            function showOrderItemShipping_LF_${orderItem.orderItemSeqId}_data() {
							                var data =  {
							                    "presentation": "layer",
							                    "orderId": "${orderItem.orderId}",
							                    "orderItemSeqId": "${orderItem.orderItemSeqId}"
							                };
							        
							                return data;
							            }
							            jQuery("#showOrderItemShipping_LF_${orderItem.orderItemSeqId}_link").click( function () {
							                $("#showOrderItemShipping_LF_${orderItem.orderItemSeqId}" ).dialog( "open" );
							                
							            });							            
							            $( function() {
										    $( "#showOrderItemShipping_LF_${orderItem.orderItemSeqId}" ).dialog({
										      autoOpen: false,
										      height: 600,
  							                  width: 800,
							                  modal: true,
							                  closeOnEscape: true,
										      show: {
										        effect: "blind",
										        duration: 1000
										      },
										      hide: {
										        effect: "explode",
										        duration: 1000
										      },
								              open: function() {
						                         jQuery.ajax({
						                             url: "ShowOrderItemShippingLayer",
						                             type: "POST",
						                             data: showOrderItemShipping_LF_${orderItem.orderItemSeqId}_data(),
						                             success: function(data) {jQuery("#showOrderItemShipping_LF_${orderItem.orderItemSeqId}").html(data);}
						                         });
						                	  }
		       
										    });
										 										 
										  } );
										  //]]>
							     </script>							     
							     <div id="showOrderItemShipping_LF_${orderItem.orderItemSeqId}" title="${uiLabelMap.ShowShippingDetails}">
  										<p>This is an animated dialog which is useful for displaying information. The dialog window can be moved, resized and closed with the 'x' icon.</p>
								 </div>
							     
                           
                            </#if>
                        </tr>
                         
                       
                        
                        <#-- now show adjustment details per line item -->
                        
                        
                    
                       
                        
                       
                        
                       
                       
                        
                     
                        
                        <#if orderItem.comments?has_content>
                          <tr<#if itemClass == "1"> class="alternate-row"</#if>>
                            <td>&nbsp;</td>
                            <td>
                              <div class= "screenlet">
                                <div class = "screenlet-body">
                                  <table>
                                    <tr>
                                      <td align="right">
                                        <span class="label">${uiLabelMap.CommonComments}</span>
                                      </td>
                                      <td align="">
                                        <span class="label">${uiLabelMap.CommonCurrent}:</span>&nbsp;${orderItem.comments}
                                        <#assign orderItemSeqId = orderItem.orderItemSeqId!>
                                        <#if comments?has_content>
                                          <hr/>
                                          <#list comments as comm>
                                            <#if comm.orderItemSeqId?has_content && orderItemSeqId?has_content && comm.orderItemSeqId == orderItemSeqId>
                                              <#if comm.changeComments?has_content>
                                                <div>
                                                ${(comm.changeComments)!}
                                                &nbsp;
                                                <#if comm.changeDatetime?has_content>${Static["org.apache.ofbiz.base.util.UtilFormatOut"].formatDateTime(comm.changeDatetime, "", locale, timeZone)?default("0000-00-00 00:00:00")}</#if>  &nbsp;  ${uiLabelMap.CommonBy} -  [${(comm.changeUserLogin)!}]
                                                </div>
                                              </#if>
                                            </#if>
                                          </#list>
                                        </#if>
                                      </td>
                                    </tr>
                                  </table>
                                </div>
                              </div>
                            </td>
                            <td></td>
                            <td></td>
                            <td></td>
                            <td></td>
                            <td></td>
                          </tr>
                        </#if>
                        <#if itemClass == "2">
                            <#assign itemClass = "1">
                        <#else>
                            <#assign itemClass = "2">
                        </#if>
                    </#list>
                </#if>
                <tr><td colspan="7"><hr /></td></tr>
                
                
            </table>
        </div>
    </div>
</#if>
