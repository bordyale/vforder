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

shipmentId = request.getParameter("shipmentId") ?: ""


orderItemShippingItem = select("orderId","orderItemSeqId","quantity","productId","productName","pallet","isBoxOrPallet","piecesPerBox","shipmentItemSeqId","productWeight").from("ShippingItemView").where("shipmentId", shipmentId).cache(false).queryList()



List<HashMap<String,Object>> hashMaps = new ArrayList<HashMap<String,Object>>()
Map<String,HashMap<String,Object>> boxes = new HashMap<String,HashMap<String,Object>>()

for (GenericValue entry: orderItemShippingItem){
	Map<String,Object> e = new HashMap<String,Object>()
	e.put("orderItemSeqId",entry.get("orderItemSeqId"))
	e.put("orderId",entry.get("orderId"))
	e.put("productId",entry.get("productId"))
	e.put("productName",entry.get("productName"))
	BigDecimal quantity = entry.get("quantity")
	e.put("quantity",entry.get("quantity"))
	Long piecesPerBox = entry.get("piecesPerBox")
	if (piecesPerBox ==null){
		piecesPerBox = 1L
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
					Map<String,Object> box = new HashMap<String,Object>()
					box.put("isBoxOrPallet", entry.get("isBoxOrPallet"))
					List<HashMap<String,Object>> products = new ArrayList<HashMap<String,Object>>()
					Map<String,Object> product = new HashMap<String,Object>()
					product.put("productId",entry.get("productId"))
					product.put("productName",entry.get("productName"))
					products.add(product)
					box.put("products", products)
					BigDecimal productWeight =entry.get("productWeight")
					BigDecimal qty =new BigDecimal(split[1])
					BigDecimal productNetto = qty.multiply(productWeight)
					box.put("boxWeight", boxWeight.add(productNetto))
					boxes.put(split[0], box)
					boxNumber++
					boxAlreadyadded++
				}else{
					Map<String,Object> box = boxes.get(split[0])
					List<HashMap<String,Object>> products = box."products"
					Map<String,Object> product = new HashMap<String,Object>()
					product.put("productId",entry.get("productId"))
					product.put("productName",entry.get("productName"))
					products.add(product)
					box.put("products", products)
					BigDecimal productWeight =entry.get("productWeight")
					BigDecimal qty =new BigDecimal(split[1])
					BigDecimal productNetto = qty.multiply(productWeight)
					BigDecimal weight = box."boxWeight"
					box.put("boxWeight", weight.add(productNetto))

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
			products.add(product)
			BigDecimal productWeight =entry.get("productWeight")
			BigDecimal qty =new BigDecimal(piecesPerBox)
			BigDecimal productNetto = qty.multiply(productWeight)
			box.put("boxWeight", boxWeight.add(productNetto))

			box.put("products", products)
			boxes.put(boxes.size(),box)
		}else{
			boxAlreadyadded--
		}
	}
	hashMaps.add(e)
}



//count boxes for totals
long palletNr = 0
long nettoTotalWeight = 0
long boxesWeight = 0
for (e in boxes){
	Map<String,Object> box  = e.value
	String isBoxOrPallet = box."isBoxOrPallet"
	if (isBoxOrPallet !=null && isBoxOrPallet.equals("R")){
		palletNr++
	}else{
	}

	List<HashMap<String,Object>> products = box."products"

}








context.listIt = hashMaps

context.totBoxNumber = boxes.size()-palletNr
context.totPalletNumber = palletNr
context.boxes=boxes.toString()



