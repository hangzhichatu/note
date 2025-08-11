public Map reloadUnitCombobox(Context context, String[] args) throws Exception
    {
        Map fieldMap = new HashMap();
		System.out.println("reloadUnitCombobox++++++++++++++++++++++++");
        try {
            StringList slUOMActualValList = new StringList();
            StringList slUOMDisplayValList = new StringList();
            String strLanguage = context.getSession().getLanguage();
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            HashMap fieldValues = (HashMap) programMap.get("fieldValues");
			System.out.println("fieldValues++++++++++++++++++++++++"+fieldValues);
			String htid = (String)fieldValues.get("IMPHTCode");
			System.out.println("htid++++++++++++++++++++++++"+htid);
            if(UIUtil.isNotNullAndNotEmpty(htid)){
				String depids=MqlUtil.mqlCommand(context, "print bus "+htid+" select to[IMPPurchaseApprove2IMPContractDocument].from.from[IMPPurchaseApprove2budget].to.to[IMPPurchaseDepartmentToPurchaseItems].from.id dump");
				if(UIUtil.isNotNullAndNotEmpty(depids)){
					String ids="";
					String[] str = depids.split(",");
					for(int i=0;i<str.length;i++){
						String depid=(String)str[i];						
						if(!ids.contains(depid)){
							String depname=MqlUtil.mqlCommand(context, "print bus "+depid+" select attribute[Title] dump");
							String fdepname=MqlUtil.mqlCommand(context, "print bus "+depid+" select to[IMPPurchaseDepartmentToIMPPurchaseDepartment].from.attribute[Title] dump");
							if(UIUtil.isNotNullAndNotEmpty(fdepname)){
								depname=fdepname+"/"+depname;
							}
							slUOMActualValList.add(depname);
							slUOMDisplayValList.add(depname);
						}
						ids=ids+","+depid;						
					}
				}
				
			}
            fieldMap.put("RangeValues", slUOMActualValList);
            fieldMap.put("RangeDisplayValues", slUOMDisplayValList);
            fieldMap.put("Input Type", "combobox");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fieldMap;
    }