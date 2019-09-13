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
import java.math.BigInteger;
import java.math.MathContext;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.apache.ofbiz.order.shoppingcart.ShoppingCart;
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
		HttpSession session = request.getSession();
		GenericValue userLogin = (GenericValue) session.getAttribute("userLogin");
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

		String controlDirective = null;
		Map<String, Object> result = null;

		// Get the parameters as a MAP, remove the productId and quantity
		// params.
		Map<String, Object> paramMap = UtilHttp.getParameterMap(request);

		String itemGroupNumber = request.getParameter("itemGroupNumber");

		// Get shoppingList info if passed. I think there can only be one
		// shoppingList
		// per request
		// String shoppingListId = request.getParameter("shoppingListId");
		// String shoppingListItemSeqId =
		// request.getParameter("shoppingListItemSeqId");

		// The number of multi form rows is retrieved
		int rowCount = UtilHttp.getMultiFormRowCount(paramMap);
		if (rowCount < 1) {
			Debug.logWarning("No rows to process, as rowCount = " + rowCount, module);
		} else {
			for (int i = 0; i < rowCount; i++) {
				String shipmentId = null;
				String orderId = null;
				String orderItemSeqId = null;
				String productId = null;

				BigDecimal quantity = BigDecimal.ZERO;
				BigDecimal quantityShipped = BigDecimal.ZERO;
				BigDecimal quantityToShip = BigDecimal.ZERO;
				BigDecimal quantityShippable = BigDecimal.ZERO;

				controlDirective = null; // re-initialize each time
				String thisSuffix = UtilHttp.getMultiRowDelimiter() + i;

				// get the params
				if (paramMap.containsKey("shipmentId" + thisSuffix)) {
					shipmentId = (String) paramMap.remove("shipmentId" + thisSuffix);
				}
				if (paramMap.containsKey("orderId" + thisSuffix)) {
					orderId = (String) paramMap.remove("orderId" + thisSuffix);
				}
				request.setAttribute("shipmentId", shipmentId);
				request.setAttribute("orderId", orderId);
				if (paramMap.containsKey("orderItemSeqId" + thisSuffix)) {
					orderItemSeqId = (String) paramMap.remove("orderItemSeqId" + thisSuffix);
				}
				String quantityToShipStr = null;
				if (paramMap.containsKey("quantityToShip" + thisSuffix)) {
					quantityToShipStr = (String) paramMap.remove("quantityToShip" + thisSuffix);
				}
				if ((quantityToShipStr == null) || (quantityToShipStr.equals(""))) {
					quantityToShipStr = "0";
				}
				try {
					quantityToShip = new BigDecimal(quantityToShipStr);
				} catch (Exception e) {
					Debug.logWarning(e, "Problems parsing quantity string: " + quantityToShipStr, module);
					quantityToShip = BigDecimal.ZERO;
				}
				if (quantityToShip.compareTo(BigDecimal.ZERO) == 0) {
					continue;
				}

				GenericValue vfOrdItemShipItem = null;
				GenericValue shipment = null;
				try {
					shipment = delegator.findOne("Shipment", UtilMisc.toMap("shipmentId", shipmentId), false);
				} catch (GenericEntityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Integer shipItemId = 1;
				if (orderId != null) {
					try {
						vfOrdItemShipItem = delegator.findOne("OrderItemShippingItemView", UtilMisc.toMap("orderId", orderId, "orderItemSeqId", orderItemSeqId), false);

						quantity = (BigDecimal) vfOrdItemShipItem.get("quantity");
						productId = (String) vfOrdItemShipItem.get("productId");

						BigDecimal tmp = (BigDecimal) shipment.get("estimatedShipCost");
						shipItemId = (tmp == null) ? null : tmp.toBigInteger().intValue();

					} catch (GenericEntityException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					if (vfOrdItemShipItem != null) {
						quantityShipped = vfOrdItemShipItem.getBigDecimal("quantityShipped");
						if (quantityShipped == null) {
							quantityShipped = BigDecimal.ZERO;
						}
						quantityShippable = quantity.subtract(quantityShipped);
					}
				} else {
					if (paramMap.containsKey("productId" + thisSuffix)) {
						productId = (String) paramMap.remove("productId" + thisSuffix);
					}
				}
				if (orderId != null && quantityToShip.compareTo(quantityShippable) > 0) {
					request.setAttribute("_ERROR_MESSAGE_", "errore");
					return "error";
				} else {

					try {
						/*shipItemId = null;
						List<GenericValue> shipmentItems = EntityQuery.use(delegator).from("ShipmentItem").where(EntityCondition.makeCondition("shipmentId", EntityOperator.EQUALS, shipmentId))
								.orderBy("shipmentItemSeqId").queryList();
						if (shipmentItems != null)
							if (shipItemId == null) {
								// shipItemId = shipmentItems.size();
								shipItemId = new Integer((String) shipmentItems.get(shipmentItems.size() - 1).get("shipmentItemSeqId"));
							}*/

						// /List<GenericValue> vfshipmentItems = delegator
						// .findByAnd("VfShipmentItem", UtilMisc.toMap(
						// "shipmentId", shipmentId, "orderId",
						// orderId, "orderItemSeqId",
						// orderItemSeqId), null, false);
						BigDecimal qty = BigDecimal.ZERO;
						GenericValue vfshipmentItem = null;
						//GenericValue shipmentItem = null;

						//shipmentItem = delegator.makeValue("ShipmentItem");
						vfshipmentItem = delegator.makeValue("VfShipmentItem");
						// GenericValue shippingItem =
						// delegator.makeValue("ShippingItem");
						//shipmentItem.set("shipmentId", shipmentId);
						//shipmentItem.set("productId", productId);
						vfshipmentItem.set("shipmentId", shipmentId);

						vfshipmentItem.set("orderId", orderId);
						vfshipmentItem.set("orderItemSeqId", orderItemSeqId);
						//shipItemId++;
						//vfshipmentItem.set("shipmentItemSeqId", shipItemId.toString());
						//shipmentItem.set("shipmentItemSeqId", shipItemId.toString());
						//shipment.put("estimatedShipCost", new BigDecimal(shipItemId));
						//delegator.createOrStore(shipment);

						qty = qty.add(quantityToShip);
						//shipmentItem.set("quantity", qty);

						if (qty.compareTo(BigDecimal.ZERO) != 0) {
							try {
								Map<String, Object> tmpResult = dispatcher.runSync("createShipmentItem", UtilMisc.<String, Object> toMap("userLogin", userLogin, "shipmentId", shipmentId, "productId",
										productId, "orderId", orderId, "orderItemSeqId", orderItemSeqId, "quantity", qty));
								vfshipmentItem.set("shipmentItemSeqId", (String) tmpResult.get("shipmentItemSeqId"));

							} catch (GenericServiceException e) {
								Debug.logError(e, module);
							}
							// delegator.createOrStore(shipmentItem);
							delegator.createOrStore(vfshipmentItem);
						}
					} catch (GenericEntityException e) {
						Debug.logError(e, module);

					}

				}

			}
			return "success";

		}

		return "error";

	}

	/**
	 * This should be called to translate the error messages of the
	 * <code>ShoppingCartHelper</code> to an appropriately formatted
	 * <code>String</code> in the request object and indicate whether the result
	 * was an error or not and whether the errors were critical or not
	 *
	 * @param result
	 *            The result returned from the <code>ShoppingCartHelper</code>
	 * @param request
	 *            The servlet request instance to set the error messages in
	 * @return one of NON_CRITICAL_ERROR, ERROR or NO_ERROR.
	 */
	private static String processResult(Map<String, Object> result, HttpServletRequest request) {
		// Check for errors
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

		// See whether there was an error
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

	public static String deleteShippingItemOrderItem(HttpServletRequest request, HttpServletResponse response) {

		Delegator delegator = (Delegator) request.getAttribute("delegator");
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
		GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
		String controlDirective = null;
		Map<String, Object> result = null;
		String shipmentId = null;
		String shipmentItemSeqId = null;

		// Get the parameters as a MAP, remove the productId and quantity
		// params.
		Map<String, Object> paramMap = UtilHttp.getParameterMap(request);

		// get the params
		if (paramMap.containsKey("shipmentId")) {
			shipmentId = (String) paramMap.remove("shipmentId");
		}
		if (paramMap.containsKey("shipmentItemSeqId")) {
			shipmentItemSeqId = (String) paramMap.remove("shipmentItemSeqId");
		}

		try {
			Map serviceTwoCtx = UtilMisc.toMap("shipmentId", shipmentId, "shipmentItemSeqId", shipmentItemSeqId, "userLogin", userLogin);
			dispatcher.runSync("deleteVfShipmentItem", serviceTwoCtx);
			dispatcher.runSync("deleteShipmentItem", serviceTwoCtx);

		} catch (GenericServiceException e) {
			Debug.logError(e, module);
		}

		/*
		 * try {
		 * 
		 * List<GenericValue> shipmentItems = delegator.findByAnd(
		 * "ShipmentItem", UtilMisc.toMap("shipmentId", shipmentId),
		 * Arrays.asList("shipmentItemSeqId"), false); List<GenericValue>
		 * vfshipmentItems = delegator.findByAnd( "VfShipmentItem",
		 * UtilMisc.toMap("shipmentId", shipmentId),
		 * Arrays.asList("shipmentItemSeqId"), false); if (shipmentItems !=
		 * null) { Integer i = 1; for (GenericValue entry : shipmentItems) {
		 * GenericValue cl = entry.clone(); delegator.removeValue(entry); Map
		 * serviceTwoCtx = UtilMisc.toMap("shipmentId", entry.get("shipmentId"),
		 * "shipmentItemSeqId", entry.get("shipmentItemSeqId"), "userLogin",
		 * userLogin); // dispatcher.runSync("deleteShipmentItem",
		 * serviceTwoCtx); entry.put("shipmentItemSeqId", i.toString()); i++;
		 * delegator.createOrStore(entry); } i = 1; for (GenericValue entry :
		 * vfshipmentItems) { delegator.removeValue(entry); Map serviceTwoCtx =
		 * UtilMisc.toMap("shipmentId", entry.get("shipmentId"),
		 * "shipmentItemSeqId", entry.get("shipmentItemSeqId"), "userLogin",
		 * userLogin); // dispatcher.runSync("deleteVfShipmentItem", //
		 * serviceTwoCtx); entry.put("shipmentItemSeqId", i.toString()); i++;
		 * delegator.createOrStore(entry); } }
		 * 
		 * } catch (Exception e) { Debug.logError(e, module); }
		 */

		return "success";

	}

	public static String deleteShipping(HttpServletRequest request, HttpServletResponse response) {

		Delegator delegator = (Delegator) request.getAttribute("delegator");
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
		GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
		String shipmentId = null;

		// Get the parameters as a MAP, remove the productId and quantity
		// params.
		Map<String, Object> paramMap = UtilHttp.getParameterMap(request);

		if (paramMap.containsKey("shipmentId")) {
			shipmentId = (String) paramMap.remove("shipmentId");
		}

		try {
			delegator.removeByAnd("shipmentItem", UtilMisc.toMap("shipmentId", shipmentId));
			delegator.removeByAnd("VfShipmentItem", UtilMisc.toMap("shipmentId", shipmentId));

		} catch (GenericEntityException e) {
			Debug.logError(e, module);
			return "error";
		}

		try {
			Map serviceTwoCtx = UtilMisc.toMap("shipmentId", shipmentId, "userLogin", userLogin);
			dispatcher.runSync("deleteShipment", serviceTwoCtx);

		} catch (GenericServiceException e) {
			Debug.logError(e, module);
			return "error";
		}

		return "success";

	}

	public static String updatePallet(HttpServletRequest request, HttpServletResponse response) {

		Delegator delegator = (Delegator) request.getAttribute("delegator");
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
		GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");

		Map<String, Object> paramMap = UtilHttp.getParameterMap(request);
		int rowCount = UtilHttp.getMultiFormRowCount(paramMap);
		if (rowCount < 1) {
			Debug.logWarning("No rows to process, as rowCount = " + rowCount, module);
		} else {
			for (int i = 0; i < rowCount; i++) {
				String shipmentId = null;
				String shipmentItemSeqId = null;
				String pallet = null;
				String isBoxOrPallet = null;
				Long piecesPerBox = 1L;

				String thisSuffix = UtilHttp.getMultiRowDelimiter() + i;

				// get the params
				if (paramMap.containsKey("shipmentId" + thisSuffix)) {
					shipmentId = (String) paramMap.remove("shipmentId" + thisSuffix);

				}
				request.setAttribute("shipmentId", shipmentId);
				if (paramMap.containsKey("shipmentItemSeqId" + thisSuffix)) {
					shipmentItemSeqId = (String) paramMap.remove("shipmentItemSeqId" + thisSuffix);

				}
				if (paramMap.containsKey("pallet" + thisSuffix)) {
					pallet = (String) paramMap.remove("pallet" + thisSuffix);

				}
				if (paramMap.containsKey("isBoxOrPallet" + thisSuffix)) {
					isBoxOrPallet = (String) paramMap.remove("isBoxOrPallet" + thisSuffix);

				}
				if (isBoxOrPallet == null) {
					isBoxOrPallet = "L";
				}
				if (!(isBoxOrPallet.equals("L") || isBoxOrPallet.equals("R"))) {
					isBoxOrPallet = "L";
				}

				String piecesPerBoxStr = null;
				if (paramMap.containsKey("piecesPerBox" + thisSuffix)) {
					piecesPerBoxStr = (String) paramMap.remove("piecesPerBox" + thisSuffix);
				}
				if ((piecesPerBoxStr == null) || (piecesPerBoxStr.equals(""))) {
					piecesPerBoxStr = "1";
				}
				try {
					piecesPerBox = Long.parseLong(piecesPerBoxStr);
				} catch (Exception e) {
					Debug.logWarning(e, "Problems parsing piecesPerBox string: " + piecesPerBoxStr, module);
					piecesPerBox = 1L;
				}

				try {
					GenericValue vfShippingItem = delegator.findOne("VfShipmentItem", UtilMisc.toMap("shipmentId", shipmentId, "shipmentItemSeqId", shipmentItemSeqId), false);
					if (vfShippingItem != null) {
						vfShippingItem.put("pallet", pallet);
						vfShippingItem.put("isBoxOrPallet", isBoxOrPallet);
						vfShippingItem.put("piecesPerBox", piecesPerBox);
						delegator.createOrStore(vfShippingItem);
					}

				} catch (GenericEntityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}

		return "success";

	}

	public static String initializeOrderEntry(HttpServletRequest request, HttpServletResponse response) {
		Delegator delegator = (Delegator) request.getAttribute("delegator");
		HttpSession session = request.getSession();
		Security security = (Security) request.getAttribute("security");
		GenericValue userLogin = (GenericValue) session.getAttribute("userLogin");
		Locale locale = UtilHttp.getLocale(request);

		String productStoreId = request.getParameter("productStoreId");

		if (UtilValidate.isNotEmpty(productStoreId)) {
			session.setAttribute("productStoreId", productStoreId);
		}
		ShoppingCart cart = ShoppingCartEvents.getCartObject(request);

		// TODO: re-factor and move this inside the ShoppingCart constructor
		String orderMode = request.getParameter("orderMode");
		if (orderMode != null) {
			cart.setOrderType(orderMode);
			session.setAttribute("orderMode", orderMode);
		} else {
			request.setAttribute("_ERROR_MESSAGE_", UtilProperties.getMessage(resource_error, "OrderPleaseSelectEitherSaleOrPurchaseOrder", locale));
			return "error";
		}

		// check the selected product store
		GenericValue productStore = null;
		if (UtilValidate.isNotEmpty(productStoreId)) {
			productStore = ProductStoreWorker.getProductStore(productStoreId, delegator);
			if (productStore != null) {

				// check permission for taking the order
				boolean hasPermission = false;
				if ((cart.getOrderType().equals("PURCHASE_ORDER")) && (security.hasEntityPermission("VFORDER", "_PURCHASE_CREATE", session))) {
					hasPermission = true;
				} else if (cart.getOrderType().equals("SALES_ORDER")) {
					if (security.hasEntityPermission("VFORDER", "_SALES_CREATE", session)) {
						hasPermission = true;
					} else {
						// if the user is a rep of the store, then he also has
						// permission
						List<GenericValue> storeReps = null;
						try {
							storeReps = EntityQuery.use(delegator).from("ProductStoreRole")
									.where("productStoreId", productStore.getString("productStoreId"), "partyId", userLogin.getString("partyId"), "roleTypeId", "SALES_REP").filterByDate().queryList();
						} catch (GenericEntityException gee) {
							request.setAttribute("_ERROR_MESSAGE_", gee.getMessage());
							return "error";
						}
						if (UtilValidate.isNotEmpty(storeReps)) {
							hasPermission = true;
						}
					}
				}

				if (hasPermission) {
					cart = ShoppingCartEvents.getCartObject(request, null, productStore.getString("defaultCurrencyUomId"));
				} else {
					request.setAttribute("_ERROR_MESSAGE_", UtilProperties.getMessage(resource_error, "OrderYouDoNotHavePermissionToTakeOrdersForThisStore", locale));
					cart.clear();
					session.removeAttribute("orderMode");
					return "error";
				}
				cart.setProductStoreId(productStoreId);
			} else {
				cart.setProductStoreId(null);
			}
		}

		if ("SALES_ORDER".equals(cart.getOrderType()) && UtilValidate.isEmpty(cart.getProductStoreId())) {
			request.setAttribute("_ERROR_MESSAGE_", UtilProperties.getMessage(resource_error, "OrderAProductStoreMustBeSelectedForASalesOrder", locale));
			cart.clear();
			session.removeAttribute("orderMode");
			return "error";
		}

		String salesChannelEnumId = request.getParameter("salesChannelEnumId");
		if (UtilValidate.isNotEmpty(salesChannelEnumId)) {
			cart.setChannelType(salesChannelEnumId);
		}

		// set party info
		String partyId = request.getParameter("supplierPartyId");
		cart.setAttribute("supplierPartyId", partyId);
		String originOrderId = request.getParameter("originOrderId");
		cart.setAttribute("originOrderId", originOrderId);

		if (UtilValidate.isNotEmpty(request.getParameter("partyId"))) {
			partyId = request.getParameter("partyId");
		}
		String userLoginId = request.getParameter("userLoginId");
		if (partyId != null || userLoginId != null) {
			if (UtilValidate.isEmpty(partyId) && UtilValidate.isNotEmpty(userLoginId)) {
				GenericValue thisUserLogin = null;
				try {
					thisUserLogin = EntityQuery.use(delegator).from("UserLogin").where("userLoginId", userLoginId).queryOne();
				} catch (GenericEntityException gee) {
					request.setAttribute("_ERROR_MESSAGE_", gee.getMessage());
					return "error";
				}
				if (thisUserLogin != null) {
					partyId = thisUserLogin.getString("partyId");
				} else {
					partyId = userLoginId;
				}
			}
			if (UtilValidate.isNotEmpty(partyId)) {
				GenericValue thisParty = null;
				try {
					thisParty = EntityQuery.use(delegator).from("Party").where("partyId", partyId).queryOne();
				} catch (GenericEntityException gee) {
					request.setAttribute("_ERROR_MESSAGE_", gee.getMessage());
					return "error";
				}
				if (thisParty == null) {
					request.setAttribute("_ERROR_MESSAGE_", UtilProperties.getMessage(resource_error, "OrderCouldNotLocateTheSelectedParty", locale));
					return "error";
				} else {
					cart.setOrderPartyId(partyId);
					if ("PURCHASE_ORDER".equals(cart.getOrderType())) {
						cart.setBillFromVendorPartyId(partyId);
					}
				}
			} else if (partyId != null && partyId.length() == 0) {
				cart.setOrderPartyId("_NA_");
				partyId = null;
			}
		} else {
			partyId = cart.getPartyId();
			if (partyId != null && partyId.equals("_NA_"))
				partyId = null;
		}

		return "success";
	}

}
