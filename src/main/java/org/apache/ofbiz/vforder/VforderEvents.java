/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package org.apache.ofbiz.vforder;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.ObjectType;
import org.apache.ofbiz.base.util.UtilDateTime;
import org.apache.ofbiz.base.util.UtilFormatOut;
import org.apache.ofbiz.base.util.UtilGenerics;
import org.apache.ofbiz.base.util.UtilHttp;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.base.util.UtilNumber;
import org.apache.ofbiz.base.util.UtilProperties;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericPK;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.condition.EntityCondition;
import org.apache.ofbiz.entity.condition.EntityOperator;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.entity.util.EntityUtil;
import org.apache.ofbiz.order.shoppingcart.ShoppingCartEvents;
import org.apache.ofbiz.order.shoppingcart.product.ProductPromoWorker;
import org.apache.ofbiz.product.catalog.CatalogWorker;
import org.apache.ofbiz.product.config.ProductConfigWorker;
import org.apache.ofbiz.product.config.ProductConfigWrapper;
import org.apache.ofbiz.product.product.ProductWorker;
import org.apache.ofbiz.product.store.ProductStoreSurveyWrapper;
import org.apache.ofbiz.product.store.ProductStoreWorker;
import org.apache.ofbiz.security.Security;
import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ModelService;
import org.apache.ofbiz.service.ServiceUtil;
import org.apache.ofbiz.webapp.control.RequestHandler;

/**
 * Vforder events.
 */
public class VforderEvents {
	
	public static String module = VforderEvents.class.getName();

    public static final String resource = "OrderUiLabels";
    public static final String resource_error = "OrderErrorUiLabels";

    private static final String NO_ERROR = "noerror";
    private static final String NON_CRITICAL_ERROR = "noncritical";
    private static final String ERROR = "error";

    public static final MathContext generalRounding = new MathContext(10);


    public static String updateOrderShippingItems(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        
        String controlDirective = null;
        Map<String, Object> result = null;
        String shippingId = null;
        String orderId = null;
        String orderItemSeqId = null;
        
       
        BigDecimal quantity = BigDecimal.ZERO;
        BigDecimal quantityShipped = BigDecimal.ZERO;
        BigDecimal quantityToShip = BigDecimal.ZERO;
        BigDecimal quantityShippable = BigDecimal.ZERO;
        
        
        // Get the parameters as a MAP, remove the productId and quantity params.
        Map<String, Object> paramMap = UtilHttp.getParameterMap(request);
        
        

        String itemGroupNumber = request.getParameter("itemGroupNumber");

        // Get shoppingList info if passed.  I think there can only be one shoppingList per request
        //String shoppingListId = request.getParameter("shoppingListId");
        //String shoppingListItemSeqId = request.getParameter("shoppingListItemSeqId");

        // The number of multi form rows is retrieved
        int rowCount = UtilHttp.getMultiFormRowCount(paramMap);
        if (rowCount < 1) {
            Debug.logWarning("No rows to process, as rowCount = " + rowCount, module);
        } else {
            for (int i = 0; i < rowCount; i++) {
                controlDirective = null;                // re-initialize each time
                String thisSuffix = UtilHttp.getMultiRowDelimiter() + i;        // current suffix after each field id

                // get the params
                if (paramMap.containsKey("shippingId" + thisSuffix)) {
                	shippingId = (String) paramMap.remove("shippingId" + thisSuffix);
                }
                if (paramMap.containsKey("orderId" + thisSuffix)) {
                	orderId = (String) paramMap.remove("orderId" + thisSuffix);
                }
                request.setAttribute("shippingId", shippingId);
                request.setAttribute("orderId", orderId);
                if (paramMap.containsKey("orderItemSeqId" + thisSuffix)) {
                	orderItemSeqId = (String) paramMap.remove("orderItemSeqId" + thisSuffix);
                }
                
                String quantityStr=null;
                if (paramMap.containsKey("quantity" + thisSuffix)) {
                	quantityStr = (String) paramMap.remove("quantity" + thisSuffix);
                }
                if ((quantityStr == null) || (quantityStr.equals(""))) {    // otherwise, every empty value causes an exception and makes the log ugly
                	quantityStr = "0";  // default quantity is 0, so without a quantity input, this field will not be added
                }
                try {
                	quantity = new BigDecimal(quantityStr);
                } catch (Exception e) {
                    Debug.logWarning(e, "Problems parsing quantity string: " + quantityStr, module);
                    quantity = BigDecimal.ZERO;
                }
                
                
                String quantityShippedStr=null;
                if (paramMap.containsKey("quantityShipped" + thisSuffix)) {
                	quantityShippedStr = (String) paramMap.remove("quantityShipped" + thisSuffix);
                }
                if ((quantityShippedStr == null) || (quantityShippedStr.equals(""))) {    // otherwise, every empty value causes an exception and makes the log ugly
                	quantityShippedStr = "0";  // default quantity is 0, so without a quantity input, this field will not be added
                }
                try {
                	quantityShipped = new BigDecimal(quantityShippedStr);
                } catch (Exception e) {
                    Debug.logWarning(e, "Problems parsing quantity string: " + quantityShippedStr, module);
                    quantityShipped = BigDecimal.ZERO;
                }
                
              
                String quantityToShipStr=null;
                if (paramMap.containsKey("quantityToShip" + thisSuffix)) {
                	quantityToShipStr = (String) paramMap.remove("quantityToShip" + thisSuffix);
                }
                if ((quantityToShipStr == null) || (quantityToShipStr.equals(""))) {    // otherwise, every empty value causes an exception and makes the log ugly
                	quantityToShipStr = "0";  // default quantity is 0, so without a quantity input, this field will not be added
                }

                try {
                	quantityToShip = new BigDecimal(quantityToShipStr);
                } catch (Exception e) {
                    Debug.logWarning(e, "Problems parsing quantity string: " + quantityToShipStr, module);
                    quantityToShip = BigDecimal.ZERO;
                }
                
                String quantityShippableStr=null;
                if (paramMap.containsKey("quantityShippable" + thisSuffix)) {
                	quantityShippableStr = (String) paramMap.remove("quantityShippable" + thisSuffix);
                }
                if ((quantityShippableStr == null) || (quantityShippableStr.equals(""))) {    // otherwise, every empty value causes an exception and makes the log ugly
                	quantityShippableStr = "0";  // default quantity is 0, so without a quantity input, this field will not be added
                }

                try {
                	quantityShippable = new BigDecimal(quantityShippableStr);
                } catch (Exception e) {
                    Debug.logWarning(e, "Problems parsing quantity string: " + quantityShippableStr, module);
                    quantityShippable = BigDecimal.ZERO;
                }
                
                
                
                if (quantityToShip.compareTo(quantityShippable)>0) {
                	request.setAttribute("_ERROR_MESSAGE_","errore");
                    return "error";
                }else {
                	return "success";
                }
                
            }
            
        }
                
        return "error";
                
    }
    
    /**
     * This should be called to translate the error messages of the
     * <code>ShoppingCartHelper</code> to an appropriately formatted
     * <code>String</code> in the request object and indicate whether
     * the result was an error or not and whether the errors were
     * critical or not
     *
     * @param result    The result returned from the
     * <code>ShoppingCartHelper</code>
     * @param request The servlet request instance to set the error messages
     * in
     * @return one of NON_CRITICAL_ERROR, ERROR or NO_ERROR.
     */
    private static String processResult(Map<String, Object> result, HttpServletRequest request) {
        //Check for errors
        StringBuilder errMsg = new StringBuilder();
        if (result.containsKey(ModelService.ERROR_MESSAGE_LIST)) {
            List<String> errorMsgs = UtilGenerics.checkList(result.get(ModelService.ERROR_MESSAGE_LIST));
            Iterator<String> iterator = errorMsgs.iterator();
            errMsg.append("<ul>");
            while (iterator.hasNext()) {
                errMsg.append("<li>");
                errMsg.append(iterator.next());
                errMsg.append("</li>");
            }
            errMsg.append("</ul>");
        } else if (result.containsKey(ModelService.ERROR_MESSAGE)) {
            errMsg.append(result.get(ModelService.ERROR_MESSAGE));
            request.setAttribute("_ERROR_MESSAGE_", errMsg.toString());
        }

        //See whether there was an error
        if (errMsg.length() > 0) {
            request.setAttribute("_ERROR_MESSAGE_", errMsg.toString());
            if (result.get(ModelService.RESPONSE_MESSAGE).equals(ModelService.RESPOND_SUCCESS)) {
                return NON_CRITICAL_ERROR;
            } else {
                return ERROR;
            }
        } else {
            return NO_ERROR;
        }
    }

    
}