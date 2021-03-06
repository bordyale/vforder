/*
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
 */

import java.util.Map;

import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.entity.condition.EntityExpr
import org.apache.ofbiz.entity.condition.EntityFunction
import org.apache.ofbiz.entity.condition.EntityOperator
import org.apache.ofbiz.entity.condition.EntityFieldValue
import org.apache.ofbiz.entity.condition.EntityConditionList
import org.apache.ofbiz.entity.condition.EntityCondition
import org.apache.ofbiz.entity.GenericValue

import org.apache.ofbiz.entity.util.EntityUtil

shipmentId = request.getParameter("shipmentId") ?: ""
justSupplier = request.getParameter("justSupplier") ?: ""



orderItemShippingItem = select("comments","orderId","orderItemSeqId","quantity","productId","productName","pallet","isBoxOrPallet","piecesPerBox","shipmentItemSeqId","productWeight").from("ShippingItemView").where("shipmentId", shipmentId).cache(false).queryList()

orderItemShippingItem = EntityUtil.orderBy(orderItemShippingItem,  ["productName"])

List<HashMap<String,Object>> hashMaps = new ArrayList<HashMap<String,Object>>()
Map<String,HashMap<String,Object>> boxes = new HashMap<String,HashMap<String,Object>>()
BigDecimal totalWeight = BigDecimal.ZERO
int VFitems =0
int VVitems =0
String VV = ""

for (GenericValue entry: orderItemShippingItem){
	Map<String,Object> e = new HashMap<String,Object>()
	e.put("orderItemSeqId",entry.get("orderItemSeqId"))
	e.put("orderId",entry.get("orderId"))
	productId =entry.get("productId")
	e.put("productId",productId)
	e.put("productName",entry.get("productName"))
	comments = entry.get("comments")
	if (comments ==null){
		comments = ""
	}
	e.put("comments", comments)
	//supplier for ExportSummaryPdf
	supplier = select("productId","partyId").from("SupplierProduct").where("productId",productId,"supplierPrefOrderId", "10_MAIN_SUPPL").cache(false).queryList()
	if (supplier.size()>0){
		VV = select("groupName").from("PartyGroup").where("partyId", supplier.getAt(0).get("partyId")).cache(false).queryList().getAt(0).get("groupName")
		e.put("supplier","VV")
		VVitems++
	}else{
		VFitems++
	}
	BigDecimal quantity = entry.get("quantity")
	e.put("quantity",entry.get("quantity"))
	Long piecesPerBox = entry.get("piecesPerBox")
	if (piecesPerBox ==null){
		piecesPerBox = quantity.abs().longValue()
	}
	e.put("piecesPerBox",piecesPerBox)
	e.put("shipmentItemSeqId",entry.get("shipmentItemSeqId"))
	String isBoxOrPallet = entry.get("isBoxOrPallet")
	e.put("isBoxOrPallet",isBoxOrPallet)
	BigDecimal boxWeight = BigDecimal.ZERO
	if (isBoxOrPallet !=null && isBoxOrPallet.equals("R")){
		boxWeight=boxWeight.add(new BigDecimal(20))
	}else{
		boxWeight=boxWeight.add(new BigDecimal(60))
	}
	String pallet = entry.get("pallet")
	e.put("pallet",entry.get("pallet"))
	BigDecimal productWeight = entry.get("productWeight")
	e.put("productWeight", productWeight)
	totalWeight = totalWeight.add(quantity.multiply(productWeight))
	e.put("netWeight", quantity.multiply(productWeight))

	int boxNumber =0
	int boxAlreadyadded=0
	if(pallet!=null){
		String[] token = pallet.split(";");
		int qtySplitpallet = 0
		for (int i=0;i<token.length;i++){
			String[] split = token[i].split(":")
			if (split.length>1){
				qtySplitpallet += Integer.parseInt(split[1])
				if (boxes.get(split[0])==null){
					//new box
					Map<String,Object> box = new HashMap<String,Object>()
					box.put("isBoxOrPallet", entry.get("isBoxOrPallet"))
					List<HashMap<String,Object>> products = new ArrayList<HashMap<String,Object>>()
					Map<String,Object> product = new HashMap<String,Object>()
					product.put("productId",entry.get("productId"))
					product.put("productName",entry.get("productName"))
					product.put("comments",comments)

					BigDecimal qty =new BigDecimal(split[1])
					BigDecimal productNetto = qty.multiply(productWeight)
					box.put("boxWeight", boxWeight.add(productNetto))
					product.put("quantity",qty)
					products.add(product)
					box.put("products", products)
					boxes.put(split[0], box)
					boxNumber++
					boxAlreadyadded++
				}else{
					//existing box
					Map<String,Object> box = boxes.get(split[0])
					List<HashMap<String,Object>> products = box."products"
					String prodId = entry.get("productId")
					BigDecimal qty =new BigDecimal(split[1])

					boolean found=false
					for (HashMap<String,Object> existProduct : products){
						if (prodId.equals(existProduct."productId")){
							BigDecimal prodQty =existProduct."quantity"
							existProduct.put("quantity", prodQty.add(qty))
							found = true
						}

					}
					if (found == false){
						Map<String,Object> product = new HashMap<String,Object>()
						product.put("productId",prodId)
						product.put("productName",entry.get("productName"))
						product.put("comments",comments)
						product.put("quantity",qty)
						products.add(product)
					}

					BigDecimal productNetto = qty.multiply(productWeight)
					BigDecimal weight = box."boxWeight"
					box.put("boxWeight", weight.add(productNetto))

					box.put("products", products)
					boxes.put(split[0], box)
				}
			}
		}
		boxNumber =boxNumber+ ( (quantity.longValue()-qtySplitpallet)/piecesPerBox)
	}else{
		boxNumber = (quantity.longValue())/piecesPerBox
	}
	e.put("boxNumber",boxNumber)
	for (int i=0;i<boxNumber;i++){
		if (boxAlreadyadded==0){
			Map<String,Object> box = new HashMap<String,Object>()
			List<HashMap<String,Object>> products = new ArrayList<HashMap<String,Object>>()
			box.put("isBoxOrPallet", entry.get("isBoxOrPallet"))
			Map<String,Object> product = new HashMap<String,Object>()
			product.put("productId",entry.get("productId"))
			product.put("productName",entry.get("productName"))
			product.put("comments",comments)


			BigDecimal qty =new BigDecimal(piecesPerBox)
			BigDecimal productNetto = qty.multiply(productWeight)
			box.put("boxWeight", boxWeight.add(productNetto))
			product.put("quantity",qty)
			products.add(product)
			box.put("products", products)
			boxes.put(boxes.size(),box)
		}else{
			boxAlreadyadded--
		}
	}

	hashMaps.add(e)
}



//count boxes for totals
int palletNr = 0
long nettoTotalWeight = 0
long boxesWeight = 0
for (e in boxes){
	Map<String,Object> box  = e.value
	String isBoxOrPallet = box."isBoxOrPallet"
	if (isBoxOrPallet !=null && isBoxOrPallet.equals("R")){
		palletNr++
		boxesWeight = boxesWeight + 20
	}else{
		boxesWeight = boxesWeight + 60
	}

	List<HashMap<String,Object>> products = box."products"

}

//ListShippingItemsSummary
orderItemShippingItem = select("comments","productId","productName","quantity").from("ListShippingItemsSummary").where("shipmentId", shipmentId).cache(false).queryList()

orderItemShippingItem = EntityUtil.orderBy(orderItemShippingItem,  ["productName"])

List<HashMap<String,Object>> hashMaps2 = new ArrayList<HashMap<String,Object>>()
int vfi =0
int vvi =0

for (GenericValue entry: orderItemShippingItem){
	Map<String,Object> e = new HashMap<String,Object>()
	productId =entry.get("productId")
	e.put("productId",productId)
	e.put("productName",entry.get("productName"))
	comments = entry.get("comments")
	if (comments ==null){
		comments = ""
	}
	e.put("comments", comments)
	e.put("quantity",entry.get("quantity"))
	//supplier for ExportSummaryPdf
	supplier = select("productId","partyId").from("SupplierProduct").where("productId",productId,"supplierPrefOrderId", "10_MAIN_SUPPL").cache(false).queryList()
	if (supplier.size()>0){
		VV = select("groupName").from("PartyGroup").where("partyId", supplier.getAt(0).get("partyId")).cache(false).queryList().getAt(0).get("groupName")
		e.put("supplier","VV")
		vvi++
	}else{
		vfi++
	}
	hashMaps2.add(e)
}

context.totalNetWeight = totalWeight
context.totalGrosWeight = totalWeight.add(new BigDecimal(boxesWeight))

context.listIt = hashMaps

context.totBoxNumber = boxes.size()-palletNr
context.totPalletNumber = palletNr
context.boxes=boxes.toString()
context.boxesMap=boxes
context.boxesListKey=boxes.keySet()

context.VFitems=VFitems
context.VVitems=VVitems
context.VV=VV
context.justSupplier=justSupplier
context.listIt2 = hashMaps2
context.vfi=vfi
context.vvi=vvi




//multi form in EditShippingItems page
List<HashMap<String,Object>> hashMaps3 = new ArrayList<HashMap<String,Object>>()
Map<String,Object> e = new HashMap<String,Object>()
//e.put("productId","1")
e.put("shipmentId",shipmentId)
hashMaps3.add(e)
context.productLine = hashMaps3





