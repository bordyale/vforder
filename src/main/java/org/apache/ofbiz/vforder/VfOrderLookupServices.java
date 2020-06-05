/*
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
 */

package org.apache.ofbiz.vforder;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.GeneralException;
import org.apache.ofbiz.base.util.ObjectType;
import org.apache.ofbiz.base.util.StringUtil;
import org.apache.ofbiz.base.util.UtilDateTime;
import org.apache.ofbiz.base.util.UtilGenerics;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.base.util.UtilProperties;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.base.util.collections.PagedList;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.condition.EntityComparisonOperator;
import org.apache.ofbiz.entity.condition.EntityCondition;
import org.apache.ofbiz.entity.condition.EntityConditionList;
import org.apache.ofbiz.entity.condition.EntityExpr;
import org.apache.ofbiz.entity.condition.EntityOperator;
import org.apache.ofbiz.entity.model.DynamicViewEntity;
import org.apache.ofbiz.entity.model.ModelKeyMap;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.entity.util.EntityUtil;
import org.apache.ofbiz.security.Security;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceUtil;
import org.apache.ofbiz.widget.renderer.Paginator;
import org.apache.ofbiz.order.order.OrderReadHelper;
import org.apache.ofbiz.order.order.OrderServices;

/**
 * OrderLookupServices
 */
public class VfOrderLookupServices {

	public static final String module = VfOrderLookupServices.class.getName();
	public static final String resource_error = "OrderErrorUiLabels";

	public static Map<String, Object> findOrders(DispatchContext dctx, Map<String, ? extends Object> context) {
		LocalDispatcher dispatcher = dctx.getDispatcher();
		Delegator delegator = dctx.getDelegator();
		Security security = dctx.getSecurity();

		GenericValue userLogin = (GenericValue) context.get("userLogin");
		Integer viewIndex = Paginator.getViewIndex(context, "viewIndex", 1);
		Integer viewSize = Paginator.getViewSize(context, "viewSize");

		String showAll = (String) context.get("showAll");
		String useEntryDate = (String) context.get("useEntryDate");
		Locale locale = (Locale) context.get("locale");
		if (showAll == null) {
			showAll = "N";
		}

		// list of fields to select (initial list)
		Set<String> fieldsToSelect = new LinkedHashSet<String>();
		fieldsToSelect.add("orderId");
		fieldsToSelect.add("orderName");
		fieldsToSelect.add("statusId");
		fieldsToSelect.add("orderTypeId");
		fieldsToSelect.add("orderDate");
		fieldsToSelect.add("currencyUom");
		fieldsToSelect.add("grandTotal");
		fieldsToSelect.add("remainingSubTotal");

		// sorting by order date newest first
		List<String> orderBy = UtilMisc.toList("-orderDate", "-orderId");

		// list to hold the parameters
		List<String> paramList = new LinkedList<String>();

		// list of conditions
		List<EntityCondition> conditions = new LinkedList<EntityCondition>();

		// check security flag for purchase orders
		boolean canViewPo = security.hasEntityPermission("ORDERMGR", "_PURCHASE_VIEW", userLogin);
		if (!canViewPo) {
			conditions.add(EntityCondition.makeCondition("orderTypeId", EntityOperator.NOT_EQUAL, "PURCHASE_ORDER"));
		}

		// dynamic view entity
		DynamicViewEntity dve = new DynamicViewEntity();
		dve.addMemberEntity("OH", "OrderHeader");
		dve.addAliasAll("OH", "", null); // no prefix
		dve.addRelation("one-nofk", "", "OrderType", UtilMisc.toList(new ModelKeyMap("orderTypeId", "orderTypeId")));
		dve.addRelation("one-nofk", "", "StatusItem", UtilMisc.toList(new ModelKeyMap("statusId", "statusId")));

		// start the lookup
		String orderId = (String) context.get("orderId");
		if (UtilValidate.isNotEmpty(orderId)) {
			paramList.add("orderId=" + orderId);
			conditions.add(makeExpr("orderId", orderId));
		}

		// the base order header fields
		List<String> orderTypeList = UtilGenerics.checkList(context.get("orderTypeId"));
		if (orderTypeList != null) {
			List<EntityExpr> orExprs = new LinkedList<EntityExpr>();
			for (String orderTypeId : orderTypeList) {
				paramList.add("orderTypeId=" + orderTypeId);

				if (!"PURCHASE_ORDER".equals(orderTypeId) || ("PURCHASE_ORDER".equals(orderTypeId) && canViewPo)) {
					orExprs.add(EntityCondition.makeCondition("orderTypeId", EntityOperator.EQUALS, orderTypeId));
				}
			}
			conditions.add(EntityCondition.makeCondition(orExprs, EntityOperator.OR));
		}

		String orderName = (String) context.get("orderName");
		if (UtilValidate.isNotEmpty(orderName)) {
			paramList.add("orderName=" + orderName);
			conditions.add(makeExpr("orderName", orderName, true));
		}

		// exclude cancelled orders.
		String excludeCancelled = (String) context.get("excludeCancelled");
		if ("Y".equals(excludeCancelled)) {
			List<EntityCondition> orExprs = new LinkedList<EntityCondition>();
			orExprs.add(EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "ORDER_CANCELLED"));
			conditions.add(EntityCondition.makeCondition(orExprs, EntityOperator.OR));
		}

		List<String> orderStatusList = UtilGenerics.checkList(context.get("orderStatusId"));
		if (orderStatusList != null) {
			List<EntityCondition> orExprs = new LinkedList<EntityCondition>();
			for (String orderStatusId : orderStatusList) {
				paramList.add("orderStatusId=" + orderStatusId);
				if ("PENDING".equals(orderStatusId)) {
					List<EntityExpr> pendExprs = new LinkedList<EntityExpr>();
					pendExprs.add(EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "ORDER_CREATED"));
					pendExprs.add(EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "ORDER_PROCESSING"));
					pendExprs.add(EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "ORDER_APPROVED"));
					orExprs.add(EntityCondition.makeCondition(pendExprs, EntityOperator.OR));
				} else {
					orExprs.add(EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, orderStatusId));
				}
			}
			conditions.add(EntityCondition.makeCondition(orExprs, EntityOperator.OR));
		}

		List<String> productStoreList = UtilGenerics.checkList(context.get("productStoreId"));
		if (productStoreList != null) {
			List<EntityExpr> orExprs = new LinkedList<EntityExpr>();
			for (String productStoreId : productStoreList) {
				paramList.add("productStoreId=" + productStoreId);
				orExprs.add(EntityCondition.makeCondition("productStoreId", EntityOperator.EQUALS, productStoreId));
			}
			conditions.add(EntityCondition.makeCondition(orExprs, EntityOperator.OR));
		}

		List<String> webSiteList = UtilGenerics.checkList(context.get("orderWebSiteId"));
		if (webSiteList != null) {
			List<EntityExpr> orExprs = new LinkedList<EntityExpr>();
			for (String webSiteId : webSiteList) {
				paramList.add("webSiteId=" + webSiteId);
				orExprs.add(EntityCondition.makeCondition("webSiteId", EntityOperator.EQUALS, webSiteId));
			}
			conditions.add(EntityCondition.makeCondition(orExprs, EntityOperator.OR));
		}

		List<String> saleChannelList = UtilGenerics.checkList(context.get("salesChannelEnumId"));
		if (saleChannelList != null) {
			List<EntityExpr> orExprs = new LinkedList<EntityExpr>();
			for (String salesChannelEnumId : saleChannelList) {
				paramList.add("salesChannelEnumId=" + salesChannelEnumId);
				orExprs.add(EntityCondition.makeCondition("salesChannelEnumId", EntityOperator.EQUALS, salesChannelEnumId));
			}
			conditions.add(EntityCondition.makeCondition(orExprs, EntityOperator.OR));
		}

		String createdBy = (String) context.get("createdBy");
		if (UtilValidate.isNotEmpty(createdBy)) {
			paramList.add("createdBy=" + createdBy);
			conditions.add(makeExpr("createdBy", createdBy));
		}

		String terminalId = (String) context.get("terminalId");
		if (UtilValidate.isNotEmpty(terminalId)) {
			paramList.add("terminalId=" + terminalId);
			conditions.add(makeExpr("terminalId", terminalId));
		}

		String transactionId = (String) context.get("transactionId");
		if (UtilValidate.isNotEmpty(transactionId)) {
			paramList.add("transactionId=" + transactionId);
			conditions.add(makeExpr("transactionId", transactionId));
		}

		String externalId = (String) context.get("externalId");
		if (UtilValidate.isNotEmpty(externalId)) {
			paramList.add("externalId=" + externalId);
			conditions.add(makeExpr("externalId", externalId));
		}

		String internalCode = (String) context.get("internalCode");
		if (UtilValidate.isNotEmpty(internalCode)) {
			paramList.add("internalCode=" + internalCode);
			conditions.add(makeExpr("internalCode", internalCode));
		}

		String dateField = "Y".equals(useEntryDate) ? "entryDate" : "orderDate";
		String minDate = (String) context.get("minDate");
		if (UtilValidate.isNotEmpty(minDate) && minDate.length() > 8) {
			minDate = minDate.trim();
			if (minDate.length() < 14)
				minDate = minDate + " " + "00:00:00.000";
			paramList.add("minDate=" + minDate);

			try {
				Object converted = ObjectType.simpleTypeConvert(minDate, "Timestamp", null, null);
				if (converted != null) {
					conditions.add(EntityCondition.makeCondition(dateField, EntityOperator.GREATER_THAN_EQUAL_TO, converted));
				}
			} catch (GeneralException e) {
				Debug.logWarning(e.getMessage(), module);
			}
		}

		String maxDate = (String) context.get("maxDate");
		if (UtilValidate.isNotEmpty(maxDate) && maxDate.length() > 8) {
			maxDate = maxDate.trim();
			if (maxDate.length() < 14)
				maxDate = maxDate + " " + "23:59:59.999";
			paramList.add("maxDate=" + maxDate);

			try {
				Object converted = ObjectType.simpleTypeConvert(maxDate, "Timestamp", null, null);
				if (converted != null) {
					conditions.add(EntityCondition.makeCondition("orderDate", EntityOperator.LESS_THAN_EQUAL_TO, converted));
				}
			} catch (GeneralException e) {
				Debug.logWarning(e.getMessage(), module);
			}
		}

		// party (role) fields
		String userLoginId = (String) context.get("userLoginId");
		String partyId = (String) context.get("partyId");
		List<String> roleTypeList = UtilGenerics.checkList(context.get("roleTypeId"));

		if (UtilValidate.isNotEmpty(userLoginId) && UtilValidate.isEmpty(partyId)) {
			GenericValue ul = null;
			try {
				ul = EntityQuery.use(delegator).from("UserLogin").where("userLoginId", userLoginId).cache().queryOne();
			} catch (GenericEntityException e) {
				Debug.logWarning(e.getMessage(), module);
			}
			if (ul != null) {
				partyId = ul.getString("partyId");
			}
		}

		String isViewed = (String) context.get("isViewed");
		if (UtilValidate.isNotEmpty(isViewed)) {
			paramList.add("isViewed=" + isViewed);
			conditions.add(makeExpr("isViewed", isViewed));
		}

		// Shipment Method
		String shipmentMethod = (String) context.get("shipmentMethod");
		if (UtilValidate.isNotEmpty(shipmentMethod)) {
			String carrierPartyId = shipmentMethod.substring(0, shipmentMethod.indexOf("@"));
			String ShippingMethodTypeId = shipmentMethod.substring(shipmentMethod.indexOf("@") + 1);
			dve.addMemberEntity("OISG", "OrderItemShipGroup");
			dve.addAlias("OISG", "shipmentMethodTypeId");
			dve.addAlias("OISG", "carrierPartyId");
			dve.addViewLink("OH", "OISG", Boolean.FALSE, UtilMisc.toList(new ModelKeyMap("orderId", "orderId")));

			if (UtilValidate.isNotEmpty(carrierPartyId)) {
				paramList.add("carrierPartyId=" + carrierPartyId);
				conditions.add(makeExpr("carrierPartyId", carrierPartyId));
			}

			if (UtilValidate.isNotEmpty(ShippingMethodTypeId)) {
				paramList.add("ShippingMethodTypeId=" + ShippingMethodTypeId);
				conditions.add(makeExpr("shipmentMethodTypeId", ShippingMethodTypeId));
			}
		}
		// PaymentGatewayResponse
		String gatewayAvsResult = (String) context.get("gatewayAvsResult");
		String gatewayScoreResult = (String) context.get("gatewayScoreResult");
		if (UtilValidate.isNotEmpty(gatewayAvsResult) || UtilValidate.isNotEmpty(gatewayScoreResult)) {
			dve.addMemberEntity("OPP", "OrderPaymentPreference");
			dve.addMemberEntity("PGR", "PaymentGatewayResponse");
			dve.addAlias("OPP", "orderPaymentPreferenceId");
			dve.addAlias("PGR", "gatewayAvsResult");
			dve.addAlias("PGR", "gatewayScoreResult");
			dve.addViewLink("OH", "OPP", Boolean.FALSE, UtilMisc.toList(new ModelKeyMap("orderId", "orderId")));
			dve.addViewLink("OPP", "PGR", Boolean.FALSE, UtilMisc.toList(new ModelKeyMap("orderPaymentPreferenceId", "orderPaymentPreferenceId")));
		}

		if (UtilValidate.isNotEmpty(gatewayAvsResult)) {
			paramList.add("gatewayAvsResult=" + gatewayAvsResult);
			conditions.add(EntityCondition.makeCondition("gatewayAvsResult", gatewayAvsResult));
		}

		if (UtilValidate.isNotEmpty(gatewayScoreResult)) {
			paramList.add("gatewayScoreResult=" + gatewayScoreResult);
			conditions.add(EntityCondition.makeCondition("gatewayScoreResult", gatewayScoreResult));
		}

		// add the role data to the view
		if (roleTypeList != null || partyId != null) {
			dve.addMemberEntity("OT", "OrderRole");
			dve.addAlias("OT", "partyId");
			dve.addAlias("OT", "roleTypeId");
			dve.addViewLink("OH", "OT", Boolean.FALSE, UtilMisc.toList(new ModelKeyMap("orderId", "orderId")));
		}

		if (UtilValidate.isNotEmpty(partyId)) {
			paramList.add("partyId=" + partyId);
			fieldsToSelect.add("partyId");
			conditions.add(makeExpr("partyId", partyId));
		}

		if (roleTypeList != null) {
			fieldsToSelect.add("roleTypeId");
			List<EntityExpr> orExprs = new LinkedList<EntityExpr>();
			for (String roleTypeId : roleTypeList) {
				paramList.add("roleTypeId=" + roleTypeId);
				orExprs.add(makeExpr("roleTypeId", roleTypeId));
			}
			conditions.add(EntityCondition.makeCondition(orExprs, EntityOperator.OR));
		}

		// order item fields
		String correspondingPoId = (String) context.get("correspondingPoId");
		String subscriptionId = (String) context.get("subscriptionId");
		String productId = (String) context.get("productId");
		String budgetId = (String) context.get("budgetId");
		String quoteId = (String) context.get("quoteId");

		String goodIdentificationTypeId = (String) context.get("goodIdentificationTypeId");
		String goodIdentificationIdValue = (String) context.get("goodIdentificationIdValue");
		boolean hasGoodIdentification = UtilValidate.isNotEmpty(goodIdentificationTypeId) && UtilValidate.isNotEmpty(goodIdentificationIdValue);

		if (correspondingPoId != null || subscriptionId != null || productId != null || budgetId != null || quoteId != null || hasGoodIdentification) {
			dve.addMemberEntity("OI", "OrderItem");
			dve.addAlias("OI", "correspondingPoId");
			dve.addAlias("OI", "subscriptionId");
			dve.addAlias("OI", "productId");
			dve.addAlias("OI", "budgetId");
			dve.addAlias("OI", "quoteId");
			dve.addViewLink("OH", "OI", Boolean.FALSE, UtilMisc.toList(new ModelKeyMap("orderId", "orderId")));

			if (hasGoodIdentification) {
				dve.addMemberEntity("GOODID", "GoodIdentification");
				dve.addAlias("GOODID", "goodIdentificationTypeId");
				dve.addAlias("GOODID", "idValue");
				dve.addViewLink("OI", "GOODID", Boolean.FALSE, UtilMisc.toList(new ModelKeyMap("productId", "productId")));
				paramList.add("goodIdentificationTypeId=" + goodIdentificationTypeId);
				conditions.add(makeExpr("goodIdentificationTypeId", goodIdentificationTypeId));
				paramList.add("goodIdentificationIdValue=" + goodIdentificationIdValue);
				conditions.add(makeExpr("idValue", goodIdentificationIdValue));
			}
		}

		if (UtilValidate.isNotEmpty(correspondingPoId)) {
			paramList.add("correspondingPoId=" + correspondingPoId);
			conditions.add(makeExpr("correspondingPoId", correspondingPoId));
		}

		if (UtilValidate.isNotEmpty(subscriptionId)) {
			paramList.add("subscriptionId=" + subscriptionId);
			conditions.add(makeExpr("subscriptionId", subscriptionId));
		}

		if (UtilValidate.isNotEmpty(productId)) {
			paramList.add("productId=" + productId);
			if (productId.startsWith("%") || productId.startsWith("*") || productId.endsWith("%") || productId.endsWith("*")) {
				conditions.add(makeExpr("productId", productId));
			} else {
				GenericValue product = null;
				try {
					product = EntityQuery.use(delegator).from("Product").where("productId", productId).queryOne();
				} catch (GenericEntityException e) {
					Debug.logWarning(e.getMessage(), module);
				}
				if (product != null) {
					String isVirtual = product.getString("isVirtual");
					if (isVirtual != null && "Y".equals(isVirtual)) {
						List<EntityExpr> orExprs = new LinkedList<EntityExpr>();
						orExprs.add(EntityCondition.makeCondition("productId", EntityOperator.EQUALS, productId));

						Map<String, Object> varLookup = null;
						try {
							varLookup = dispatcher.runSync("getAllProductVariants", UtilMisc.toMap("productId", productId));
						} catch (GenericServiceException e) {
							Debug.logWarning(e.getMessage(), module);
						}
						List<GenericValue> variants = UtilGenerics.checkList(varLookup.get("assocProducts"));
						if (variants != null) {
							for (GenericValue v : variants) {
								orExprs.add(EntityCondition.makeCondition("productId", EntityOperator.EQUALS, v.getString("productIdTo")));
							}
						}
						conditions.add(EntityCondition.makeCondition(orExprs, EntityOperator.OR));
					} else {
						conditions.add(EntityCondition.makeCondition("productId", EntityOperator.EQUALS, productId));
					}
				} else {
					String failMsg = UtilProperties.getMessage("OrderErrorUiLabels", "OrderFindOrderProductInvalid", UtilMisc.toMap("productId", productId), locale);
					return ServiceUtil.returnFailure(failMsg);
				}
			}
		}

		if (UtilValidate.isNotEmpty(budgetId)) {
			paramList.add("budgetId=" + budgetId);
			conditions.add(makeExpr("budgetId", budgetId));
		}

		if (UtilValidate.isNotEmpty(quoteId)) {
			paramList.add("quoteId=" + quoteId);
			conditions.add(makeExpr("quoteId", quoteId));
		}

		// payment preference fields
		String billingAccountId = (String) context.get("billingAccountId");
		String finAccountId = (String) context.get("finAccountId");
		String cardNumber = (String) context.get("cardNumber");
		String accountNumber = (String) context.get("accountNumber");
		String paymentStatusId = (String) context.get("paymentStatusId");

		if (UtilValidate.isNotEmpty(paymentStatusId)) {
			paramList.add("paymentStatusId=" + paymentStatusId);
			conditions.add(makeExpr("paymentStatusId", paymentStatusId));
		}
		if (finAccountId != null || cardNumber != null || accountNumber != null || paymentStatusId != null) {
			dve.addMemberEntity("OP", "OrderPaymentPreference");
			dve.addAlias("OP", "finAccountId");
			dve.addAlias("OP", "paymentMethodId");
			dve.addAlias("OP", "paymentStatusId", "statusId", null, false, false, null);
			dve.addViewLink("OH", "OP", Boolean.FALSE, UtilMisc.toList(new ModelKeyMap("orderId", "orderId")));
		}

		// search by billing account ID
		if (UtilValidate.isNotEmpty(billingAccountId)) {
			paramList.add("billingAccountId=" + billingAccountId);
			conditions.add(makeExpr("billingAccountId", billingAccountId));
		}

		// search by fin account ID
		if (UtilValidate.isNotEmpty(finAccountId)) {
			paramList.add("finAccountId=" + finAccountId);
			conditions.add(makeExpr("finAccountId", finAccountId));
		}

		// search by card number
		if (UtilValidate.isNotEmpty(cardNumber)) {
			dve.addMemberEntity("CC", "CreditCard");
			dve.addAlias("CC", "cardNumber");
			dve.addViewLink("OP", "CC", Boolean.FALSE, UtilMisc.toList(new ModelKeyMap("paymentMethodId", "paymentMethodId")));

			paramList.add("cardNumber=" + cardNumber);
			conditions.add(makeExpr("cardNumber", cardNumber));
		}

		// search by eft account number
		if (UtilValidate.isNotEmpty(accountNumber)) {
			dve.addMemberEntity("EF", "EftAccount");
			dve.addAlias("EF", "accountNumber");
			dve.addViewLink("OP", "EF", Boolean.FALSE, UtilMisc.toList(new ModelKeyMap("paymentMethodId", "paymentMethodId")));

			paramList.add("accountNumber=" + accountNumber);
			conditions.add(makeExpr("accountNumber", accountNumber));
		}

		// shipment/inventory item
		String inventoryItemId = (String) context.get("inventoryItemId");
		String softIdentifier = (String) context.get("softIdentifier");
		String serialNumber = (String) context.get("serialNumber");
		String shipmentId = (String) context.get("shipmentId");

		if (shipmentId != null || inventoryItemId != null || softIdentifier != null || serialNumber != null) {
			dve.addMemberEntity("II", "ItemIssuance");
			dve.addAlias("II", "shipmentId");
			dve.addAlias("II", "inventoryItemId");
			dve.addViewLink("OH", "II", Boolean.FALSE, UtilMisc.toList(new ModelKeyMap("orderId", "orderId")));

			if (softIdentifier != null || serialNumber != null) {
				dve.addMemberEntity("IV", "InventoryItem");
				dve.addAlias("IV", "softIdentifier");
				dve.addAlias("IV", "serialNumber");
				dve.addViewLink("II", "IV", Boolean.FALSE, UtilMisc.toList(new ModelKeyMap("inventoryItemId", "inventoryItemId")));
			}
		}

		if (UtilValidate.isNotEmpty(inventoryItemId)) {
			paramList.add("inventoryItemId=" + inventoryItemId);
			conditions.add(makeExpr("inventoryItemId", inventoryItemId));
		}

		if (UtilValidate.isNotEmpty(softIdentifier)) {
			paramList.add("softIdentifier=" + softIdentifier);
			conditions.add(makeExpr("softIdentifier", softIdentifier, true));
		}

		if (UtilValidate.isNotEmpty(serialNumber)) {
			paramList.add("serialNumber=" + serialNumber);
			conditions.add(makeExpr("serialNumber", serialNumber, true));
		}

		if (UtilValidate.isNotEmpty(shipmentId)) {
			paramList.add("shipmentId=" + shipmentId);
			conditions.add(makeExpr("shipmentId", shipmentId));
		}

		// back order checking
		String hasBackOrders = (String) context.get("hasBackOrders");
		if (UtilValidate.isNotEmpty(hasBackOrders)) {
			dve.addMemberEntity("IR", "OrderItemShipGrpInvRes");
			dve.addAlias("IR", "quantityNotAvailable");
			dve.addViewLink("OH", "IR", Boolean.FALSE, UtilMisc.toList(new ModelKeyMap("orderId", "orderId")));

			paramList.add("hasBackOrders=" + hasBackOrders);
			if ("Y".equals(hasBackOrders)) {
				conditions.add(EntityCondition.makeCondition("quantityNotAvailable", EntityOperator.NOT_EQUAL, null));
				conditions.add(EntityCondition.makeCondition("quantityNotAvailable", EntityOperator.GREATER_THAN, BigDecimal.ZERO));
			} else if ("N".equals(hasBackOrders)) {
				List<EntityExpr> orExpr = new LinkedList<EntityExpr>();
				orExpr.add(EntityCondition.makeCondition("quantityNotAvailable", EntityOperator.EQUALS, null));
				orExpr.add(EntityCondition.makeCondition("quantityNotAvailable", EntityOperator.EQUALS, BigDecimal.ZERO));
				conditions.add(EntityCondition.makeCondition(orExpr, EntityOperator.OR));
			}
		}

		// Get all orders according to specific ship to country with
		// "Only Include" or "Do not Include".
		String countryGeoId = (String) context.get("countryGeoId");
		String includeCountry = (String) context.get("includeCountry");
		if (UtilValidate.isNotEmpty(countryGeoId) && UtilValidate.isNotEmpty(includeCountry)) {
			paramList.add("countryGeoId=" + countryGeoId);
			paramList.add("includeCountry=" + includeCountry);
			// add condition to dynamic view
			dve.addMemberEntity("OCM", "OrderContactMech");
			dve.addMemberEntity("PA", "PostalAddress");
			dve.addAlias("OCM", "contactMechId");
			dve.addAlias("OCM", "contactMechPurposeTypeId");
			dve.addAlias("PA", "countryGeoId");
			dve.addViewLink("OH", "OCM", Boolean.FALSE, ModelKeyMap.makeKeyMapList("orderId"));
			dve.addViewLink("OCM", "PA", Boolean.FALSE, ModelKeyMap.makeKeyMapList("contactMechId"));

			EntityConditionList<EntityExpr> exprs = null;
			if ("Y".equals(includeCountry)) {
				exprs = EntityCondition.makeCondition(
						UtilMisc.toList(EntityCondition.makeCondition("contactMechPurposeTypeId", "SHIPPING_LOCATION"), EntityCondition.makeCondition("countryGeoId", countryGeoId)),
						EntityOperator.AND);
			} else {
				exprs = EntityCondition.makeCondition(
						UtilMisc.toList(EntityCondition.makeCondition("contactMechPurposeTypeId", "SHIPPING_LOCATION"),
								EntityCondition.makeCondition("countryGeoId", EntityOperator.NOT_EQUAL, countryGeoId)), EntityOperator.AND);
			}
			conditions.add(exprs);
		}

		// create the main condition
		EntityCondition cond = null;
		if (conditions.size() > 0 || showAll.equalsIgnoreCase("Y")) {
			cond = EntityCondition.makeCondition(conditions, EntityOperator.AND);
		}

		if (Debug.verboseOn()) {
			Debug.logInfo("Find order query: " + cond.toString(), module);
		}

		List<GenericValue> orderList = new LinkedList<GenericValue>();
		int orderCount = 0;

		// get the index for the partial list
		int lowIndex = 0;
		int highIndex = 0;

		if (cond != null) {
			PagedList<GenericValue> pagedOrderList = null;
			try {
				// do the lookup
				pagedOrderList = EntityQuery.use(delegator).select(fieldsToSelect).from(dve).where(cond).orderBy(orderBy).distinct() // set
																																		// distinct
																																		// on
																																		// so
																																		// we
																																		// only
																																		// get
																																		// one
																																		// row
																																		// per
																																		// order
						.cursorScrollInsensitive().queryPagedList(viewIndex - 1, viewSize);

				orderCount = pagedOrderList.getSize();
				lowIndex = pagedOrderList.getStartIndex();
				highIndex = pagedOrderList.getEndIndex();
				orderList = pagedOrderList.getData();
			} catch (GenericEntityException e) {
				Debug.logError(e, module);
				return ServiceUtil.returnError(e.getMessage());
			}
		}

		// create the result map
		Map<String, Object> result = ServiceUtil.returnSuccess();

		// filter out requested inventory problems
		filterInventoryProblems(context, result, orderList, paramList);

		// format the param list
		String paramString = StringUtil.join(paramList, "&amp;");

		result.put("highIndex", Integer.valueOf(highIndex));
		result.put("lowIndex", Integer.valueOf(lowIndex));
		result.put("viewIndex", viewIndex);
		result.put("viewSize", viewSize);
		result.put("showAll", showAll);

		result.put("paramList", (paramString != null ? paramString : ""));
		result.put("orderList", orderList);
		result.put("orderListSize", Integer.valueOf(orderCount));

		return result;
	}

	public static void filterInventoryProblems(Map<String, ? extends Object> context, Map<String, Object> result, List<GenericValue> orderList, List<String> paramList) {
		List<String> filterInventoryProblems = new LinkedList<String>();

		String doFilter = (String) context.get("filterInventoryProblems");
		if (doFilter == null) {
			doFilter = "N";
		}

		if ("Y".equals(doFilter) && orderList.size() > 0) {
			paramList.add("filterInventoryProblems=Y");
			for (GenericValue orderHeader : orderList) {
				OrderReadHelper orh = new OrderReadHelper(orderHeader);
				BigDecimal backorderQty = orh.getOrderBackorderQuantity();
				if (backorderQty.compareTo(BigDecimal.ZERO) == 1) {
					filterInventoryProblems.add(orh.getOrderId());
				}
			}
		}

		List<String> filterPOsOpenPastTheirETA = new LinkedList<String>();
		List<String> filterPOsWithRejectedItems = new LinkedList<String>();
		List<String> filterPartiallyReceivedPOs = new LinkedList<String>();

		String filterPOReject = (String) context.get("filterPOsWithRejectedItems");
		String filterPOPast = (String) context.get("filterPOsOpenPastTheirETA");
		String filterPartRec = (String) context.get("filterPartiallyReceivedPOs");
		if (filterPOReject == null) {
			filterPOReject = "N";
		}
		if (filterPOPast == null) {
			filterPOPast = "N";
		}
		if (filterPartRec == null) {
			filterPartRec = "N";
		}

		boolean doPoFilter = false;
		if ("Y".equals(filterPOReject)) {
			paramList.add("filterPOsWithRejectedItems=Y");
			doPoFilter = true;
		}
		if ("Y".equals(filterPOPast)) {
			paramList.add("filterPOsOpenPastTheirETA=Y");
			doPoFilter = true;
		}
		if ("Y".equals(filterPartRec)) {
			paramList.add("filterPartiallyReceivedPOs=Y");
			doPoFilter = true;
		}

		if (doPoFilter && orderList.size() > 0) {
			for (GenericValue orderHeader : orderList) {
				OrderReadHelper orh = new OrderReadHelper(orderHeader);
				String orderType = orh.getOrderTypeId();
				String orderId = orh.getOrderId();

				if ("PURCHASE_ORDER".equals(orderType)) {
					if ("Y".equals(filterPOReject) && orh.getRejectedOrderItems()) {
						filterPOsWithRejectedItems.add(orderId);
					} else if ("Y".equals(filterPOPast) && orh.getPastEtaOrderItems(orderId)) {
						filterPOsOpenPastTheirETA.add(orderId);
					} else if ("Y".equals(filterPartRec) && orh.getPartiallyReceivedItems()) {
						filterPartiallyReceivedPOs.add(orderId);
					}
				}
			}
		}

		result.put("filterInventoryProblemsList", filterInventoryProblems);
		result.put("filterPOsWithRejectedItemsList", filterPOsWithRejectedItems);
		result.put("filterPOsOpenPastTheirETAList", filterPOsOpenPastTheirETA);
		result.put("filterPartiallyReceivedPOsList", filterPartiallyReceivedPOs);
	}

	protected static EntityExpr makeExpr(String fieldName, String value) {
		return makeExpr(fieldName, value, false);
	}

	protected static EntityExpr makeExpr(String fieldName, String value, boolean forceLike) {
		EntityComparisonOperator<?, ?> op = forceLike ? EntityOperator.LIKE : EntityOperator.EQUALS;

		if (value.startsWith("*")) {
			op = EntityOperator.LIKE;
			value = "%" + value.substring(1);
		} else if (value.startsWith("%")) {
			op = EntityOperator.LIKE;
		}

		if (value.endsWith("*")) {
			op = EntityOperator.LIKE;
			value = value.substring(0, value.length() - 1) + "%";
		} else if (value.endsWith("%")) {
			op = EntityOperator.LIKE;
		}

		if (forceLike) {
			if (!value.startsWith("%")) {
				value = "%" + value;
			}
			if (!value.endsWith("%")) {
				value = value + "%";
			}
		}

		return EntityCondition.makeCondition(fieldName, op, value);
	}

	/** Service for changing the status on an order header */
	public static Map<String, Object> setOrderStatus(DispatchContext ctx, Map<String, ? extends Object> context) {
		LocalDispatcher dispatcher = ctx.getDispatcher();
		Delegator delegator = ctx.getDelegator();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String orderId = (String) context.get("orderId");
		String statusId = (String) context.get("statusId");
		String changeReason = (String) context.get("changeReason");
		Map<String, Object> successResult = ServiceUtil.returnSuccess();
		Locale locale = (Locale) context.get("locale");

		// check and make sure we have permission to change the order
		Security security = ctx.getSecurity();
		boolean hasPermission = VfOrderLookupServices.hasPermission(orderId, userLogin, "UPDATE", security, delegator);
		if (!hasPermission) {
			return ServiceUtil.returnError(UtilProperties.getMessage(resource_error, "OrderYouDoNotHavePermissionToChangeThisOrdersStatus", locale));
		}

		try {
			GenericValue orderHeader = EntityQuery.use(delegator).from("OrderHeader").where("orderId", orderId).queryOne();

			if (orderHeader == null) {
				return ServiceUtil.returnError(UtilProperties.getMessage(resource_error, "OrderErrorCouldNotChangeOrderStatusOrderCannotBeFound", locale));
			}
			// first save off the old status
			successResult.put("oldStatusId", orderHeader.get("statusId"));
			successResult.put("orderTypeId", orderHeader.get("orderTypeId"));

			if (Debug.verboseOn()) {
				Debug.logVerbose("[OrderServices.setOrderStatus] : From Status : " + orderHeader.getString("statusId"), module);
			}
			if (Debug.verboseOn()) {
				Debug.logVerbose("[OrderServices.setOrderStatus] : To Status : " + statusId, module);
			}

			if (orderHeader.getString("statusId").equals(statusId)) {
				Debug.logWarning(
						UtilProperties.getMessage(resource_error, "OrderTriedToSetOrderStatusWithTheSameStatusIdforOrderWithId", UtilMisc.toMap("statusId", statusId, "orderId", orderId), locale),
						module);
				return successResult;
			}
			try {
				GenericValue statusChange = EntityQuery.use(delegator).from("StatusValidChange").where("statusId", orderHeader.getString("statusId"), "statusIdTo", statusId).cache(true).queryOne();
				if (statusChange == null) {
					return ServiceUtil.returnError(UtilProperties.getMessage(resource_error, "OrderErrorCouldNotChangeOrderStatusStatusIsNotAValidChange", locale) + ": ["
							+ orderHeader.getString("statusId") + "] -> [" + statusId + "]");
				}
			} catch (GenericEntityException e) {
				return ServiceUtil.returnError(UtilProperties.getMessage(resource_error, "OrderErrorCouldNotChangeOrderStatus", locale) + e.getMessage() + ").");
			}

			// update the current status
			orderHeader.set("statusId", statusId);

			// now create a status change
			GenericValue orderStatus = delegator.makeValue("OrderStatus");
			orderStatus.put("orderStatusId", delegator.getNextSeqId("OrderStatus"));
			orderStatus.put("statusId", statusId);
			orderStatus.put("orderId", orderId);
			orderStatus.put("statusDatetime", UtilDateTime.nowTimestamp());
			orderStatus.put("statusUserLogin", userLogin.getString("userLoginId"));
			orderStatus.put("changeReason", changeReason);

			orderHeader.store();
			orderStatus.create();

			successResult.put("needsInventoryIssuance", orderHeader.get("needsInventoryIssuance"));
			successResult.put("grandTotal", orderHeader.get("grandTotal"));
		} catch (GenericEntityException e) {
			return ServiceUtil.returnError(UtilProperties.getMessage(resource_error, "OrderErrorCouldNotChangeOrderStatus", locale) + e.getMessage() + ").");
		}

		/*
		 * if ("Y".equals(context.get("setItemStatus"))) { String
		 * newItemStatusId = null; if ("ORDER_APPROVED".equals(statusId)) {
		 * newItemStatusId = "ITEM_APPROVED"; } else if
		 * ("ORDER_COMPLETED".equals(statusId)) { newItemStatusId =
		 * "ITEM_COMPLETED"; } else if ("ORDER_CANCELLED".equals(statusId)) {
		 * newItemStatusId = "ITEM_CANCELLED"; }
		 * 
		 * if (newItemStatusId != null) { try { Map<String, Object> resp =
		 * dispatcher.runSync("changeOrderItemStatus", UtilMisc.<String,
		 * Object>toMap("orderId", orderId, "statusId", newItemStatusId,
		 * "userLogin", userLogin)); if (ServiceUtil.isError(resp)) { return
		 * ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
		 * "OrderErrorCouldNotChangeItemStatus", locale) + newItemStatusId,
		 * null, null, resp); } } catch (GenericServiceException e) {
		 * Debug.logError(e, "Error changing item status to " + newItemStatusId
		 * + ": " + e.toString(), module); return
		 * ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
		 * "OrderErrorCouldNotChangeItemStatus", locale) + newItemStatusId +
		 * ": " + e.toString()); } } }
		 */

		successResult.put("orderStatusId", statusId);
		return successResult;
	}

	private static boolean hasPermission(String orderId, GenericValue userLogin, String action, Security security, Delegator delegator) {
		OrderReadHelper orh = new OrderReadHelper(delegator, orderId);
		String orderTypeId = orh.getOrderTypeId();
		String partyId = null;
		GenericValue orderParty = orh.getEndUserParty();
		if (UtilValidate.isEmpty(orderParty)) {
			orderParty = orh.getPlacingParty();
		}
		if (orderParty != null) {
			partyId = orderParty.getString("partyId");
		}
		boolean hasPermission = hasPermission(orderTypeId, partyId, userLogin, action, security);
		if (!hasPermission) {
			GenericValue placingCustomer = null;
			try {
				placingCustomer = EntityQuery.use(delegator).from("OrderRole").where("orderId", orderId, "partyId", userLogin.getString("partyId"), "roleTypeId", "PLACING_CUSTOMER").queryOne();
			} catch (GenericEntityException e) {
				Debug.logError("Could not select OrderRoles for order " + orderId + " due to " + e.getMessage(), module);
			}
			hasPermission = (placingCustomer != null);
		}
		return hasPermission;
	}

	private static boolean hasPermission(String orderTypeId, String partyId, GenericValue userLogin, String action, Security security) {
		boolean hasPermission = security.hasEntityPermission("ORDERMGR", "_" + action, userLogin);
		if (!hasPermission) {
			if ("SALES_ORDER".equals(orderTypeId)) {
				if (security.hasEntityPermission("ORDERMGR", "_SALES_" + action, userLogin)) {
					hasPermission = true;
				} else {
					// check sales agent/customer relationship
					List<GenericValue> repsCustomers = new LinkedList<>();
					try {
						repsCustomers = EntityUtil.filterByDate(userLogin.getRelatedOne("Party", false).getRelated("FromPartyRelationship",
								UtilMisc.toMap("roleTypeIdFrom", "AGENT", "roleTypeIdTo", "CUSTOMER", "partyIdTo", partyId), null, false));
					} catch (GenericEntityException ex) {
						Debug.logError("Could not determine if " + partyId + " is a customer of user " + userLogin.getString("userLoginId") + " due to " + ex.getMessage(), module);
					}
					if ((repsCustomers != null) && (repsCustomers.size() > 0) && (security.hasEntityPermission("ORDERMGR", "_ROLE_" + action, userLogin))) {
						hasPermission = true;
					}
					if (!hasPermission) {
						// check sales sales rep/customer relationship
						try {
							repsCustomers = EntityUtil.filterByDate(userLogin.getRelatedOne("Party", false).getRelated("FromPartyRelationship",
									UtilMisc.toMap("roleTypeIdFrom", "SALES_REP", "roleTypeIdTo", "CUSTOMER", "partyIdTo", partyId), null, false));
						} catch (GenericEntityException ex) {
							Debug.logError("Could not determine if " + partyId + " is a customer of user " + userLogin.getString("userLoginId") + " due to " + ex.getMessage(), module);
						}
						if ((repsCustomers != null) && (repsCustomers.size() > 0) && (security.hasEntityPermission("ORDERMGR", "_ROLE_" + action, userLogin))) {
							hasPermission = true;
						}
					}
				}
			} else if (("PURCHASE_ORDER".equals(orderTypeId) && (security.hasEntityPermission("ORDERMGR", "_PURCHASE_" + action, userLogin)))) {
				hasPermission = true;
			}
		}
		return hasPermission;
	}
}
