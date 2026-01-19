public     public void test(Context context, String[] args) throws Exception {
        HashMap param = new HashMap();
        param.put("autoNameCheck", "true");
        param.put("selscope", "Organization");
        param.put("RouteCompletionAction", "Promote Connected Object");
        param.put("RouteBasePurpose", "Standard");
        param.put("languageStr", "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2");
        param.put("AutoStopOnRejection", "Immediate");
        param.put("PreserveTaskOwner", "False");
        param.put("Description", "\u5de5\u827a\u6307\u5bfc\u5361\u5ba1\u6838\u6d41\u7a0b");
        StringList selectlist = new StringList();
        selectlist.add(DomainConstants.SELECT_ID);
        StringBuffer where = new StringBuffer("");
        where.append("attribute[Title]==\u5de5\u827a\u6307\u5bfc\u5361\u5ba1\u6838\u6d41\u7a0b");
        where.append("&&policy != 'Version' && revision == last && current == Active");
        MapList list = DomainObject.findObjects(context, "Route Template",
                "eService Production",
                where.toString(),
                selectlist);
        if (list.size() > 0) {
            Map map = (Map) list.get(0);
            String TemplateOID = (String) map.get("id");
            param.put("TemplateOID", TemplateOID);
            HashMap resultMap = createRouteProcess(context, JPO.packArgs(param));
            System.out.println("resultMap---->" + resultMap);
            String routeId = (String) resultMap.get("id");
            DomainObject routeObj = new DomainObject(routeId);
            routeObj.setAttributeValue(context, "Title", "test");
            ContextUtil.pushContext(context);
            routeObj.setOwner(context, "admin_platform");
            MqlUtil.mqlCommand(context, "mod bus " + routeId + " Vault 'eService Production'");
            MqlUtil.mqlCommand(context, "mod bus " + routeId + "  project 'Common Space'");
            ContextUtil.popContext(context);
        }
    }

    @com.matrixone.apps.framework.ui.CreateProcessCallable
    @com.matrixone.apps.framework.ui.PostProcessCallable
    public HashMap createRouteProcess(Context context, String[] args) throws Exception {
        System.out.println("<--------success--------->");
        HashMap requestMap = (HashMap) JPO.unpackArgs(args);
        System.out.println("requestMap----->" + requestMap);
        String name = (String) requestMap.get("Name");
        String autoNameCheck = (String) requestMap.get("autoNameCheck");
        String vault = (String) requestMap.get("Vault");
        String revision = (String) requestMap.get("Revision");
        String strLanguage = (String) requestMap.get("languageStr");
        String objectId = (String) requestMap.get("objectId");
        String routeId = "";
        String restrictMembers = (String) requestMap.get("selscope");
        String selscopeId = "";
        String scopeName = "";
        String strTypeName = "";
        boolean WSNotSelected = true;
        boolean isCompletedTask = false;
        Hashtable routeDetails = new Hashtable();
        HashMap resultMap = new HashMap();
        i18nNow i18nnow = new i18nNow();
        String esignConfigSetting = "None";
        String errorMessage = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", new Locale(strLanguage), "emxComponents.CreateRoute.OnCompleteTaskError");
        com.matrixone.apps.common.Person person = (com.matrixone.apps.common.Person) DomainObject.newInstance(context, DomainConstants.TYPE_PERSON);
        Route route = (Route) DomainObject.newInstance(context, DomainConstants.TYPE_ROUTE);
        if (UIUtil.isNullOrEmpty(revision)) {
            revision = new Policy(DomainConstants.POLICY_ROUTE).getSequence(context);
        }
        String groupType = PropertyUtil.getSchemaProperty(context, "type_Group");
        String proxyGoupType = PropertyUtil.getSchemaProperty(context, "type_GroupProxy");
        BusinessObject routeObject = new BusinessObject(DomainConstants.TYPE_ROUTE, name, revision, vault);
        boolean isExists = routeObject.exists(context);
        System.out.println("isExists----->" + isExists);
        if (isExists) {
            resultMap.put("ErrorMessage", i18nNow.getTypeI18NString(DomainConstants.TYPE_ROUTE, strLanguage) + " " + name + " " + EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", new Locale(strLanguage), "emxComponents.Common.AlreadyExists"));
        } else {
            if ("true".equalsIgnoreCase(autoNameCheck) || UIUtil.isNullOrEmpty(name)) {
                String typeAlias = FrameworkUtil.getAliasForAdmin(context, "type", DomainConstants.TYPE_ROUTE, true);
                String policyAlias = FrameworkUtil.getAliasForAdmin(context, "policy", DomainConstants.POLICY_ROUTE, true);
                name = FrameworkUtil.autoName(context, typeAlias, new Policy(DomainObject.POLICY_ROUTE).getFirstInSequence(context), policyAlias,
                        null,
                        null,
                        true,
                        true);
                ContextUtil.pushContext(context);
                route.createObject(context, DomainConstants.TYPE_ROUTE, name, null, DomainObject.POLICY_ROUTE, null);
                System.out.println("aaaaaqqqq-----");
                ContextUtil.popContext(context);
                routeId = route.getId(context);
            } else {
                ContextUtil.pushContext(context);
                route.createObject(context, DomainConstants.TYPE_ROUTE, name, revision, DomainConstants.POLICY_ROUTE, vault);
                System.out.println("qqqqqqqqqqqqqqqqqq---");
                ContextUtil.popContext(context);
                routeId = route.getObjectId(context);
            }
            route.setId(routeId);

            BusinessObject personObject = (BusinessObject) person.getPerson(context);
            DomainObject dmoRequest = new DomainObject(routeId);

            if (restrictMembers.equals("ScopeName")) {
                selscopeId = (String) requestMap.get("folderId");
                scopeName = (String) requestMap.get("txtWSFolder");
                if (UIUtil.isNullOrEmpty(scopeName) && UIUtil.isNullOrEmpty(selscopeId)) {
                    resultMap.put("ErrorMessage", i18nnow.getI18nString("emxComponents.CreateRoute.EnterRouteScope", "emxComponentsStringResource", strLanguage));
                    WSNotSelected = false;
                }
            } else if (restrictMembers.equals("Organization")) {
                selscopeId = restrictMembers;
            }
            if (objectId != null && !"".equals(objectId) && !"null".equals(objectId)) {
                DomainObject boProject = new DomainObject(objectId);
                strTypeName = boProject.getInfo(context, "type");
            }
            if ((strTypeName != null || !"".equals(strTypeName) || !"null".equals(strTypeName)) && DomainObject.TYPE_DOCUMENT.equals(strTypeName) && com.matrixone.apps.domain.util.mxType.isOfParentType(context, strTypeName, com.matrixone.apps.domain.DomainObject.TYPE_WORKSPACE_VAULT)) {
                selscopeId = objectId;
            }
            String routeCompletionAction = (String) requestMap.get("RouteCompletionAction");
            String routeDescription = (String) requestMap.get("Description");
            String routeBasePurpose = (String) requestMap.get("RouteBasePurpose");
            String sTemplateId = (String) requestMap.get("TemplateOID");
            String visblToParent = (String) requestMap.get("VisbleToParent");
            String strAutoStopOnRejection = (String) requestMap.get("AutoStopOnRejection");
            String strPreserveTaskOwner = (String) requestMap.get("PreserveTaskOwner");
            String routeRequiresESign = "False";
            String sAttrRouteRequiresESignature = PropertyUtil.getSchemaProperty(context, "attribute_RequiresESign");

            if (sTemplateId != null && !"null".equals(sTemplateId) && !sTemplateId.equals("")) {

                String SELECT_ATTRIBUTE_ROUTE_TEMPLATE_REQUIRES_ESIGN = "attribute[" + sAttrRouteRequiresESignature + "]";
                System.out.println("sTemplateId------>" + sTemplateId);
                DomainObject dmoRouteTemplate = new DomainObject(sTemplateId);
                String rTemplateRequiresESign = dmoRouteTemplate.getInfo(context, SELECT_ATTRIBUTE_ROUTE_TEMPLATE_REQUIRES_ESIGN);
                if (routeBasePurpose != null && ("Approval".equalsIgnoreCase(routeBasePurpose) || "Standard".equalsIgnoreCase(routeBasePurpose)) && "True".equalsIgnoreCase(rTemplateRequiresESign)) {
                    routeRequiresESign = "True";
                }
                System.out.println("sTemplateId2------>" + sTemplateId);
            } else {
                try {
                    esignConfigSetting = MqlUtil.mqlCommand(context, "list expression $1 select $2 dump", "ENXESignRequiresESign", "value");
                    if (UIUtil.isNullOrEmpty(esignConfigSetting))
                        esignConfigSetting = "None";
                } catch (Exception e) {
                    esignConfigSetting = "None";
                }
                if (routeBasePurpose != null && ("Approval".equalsIgnoreCase(routeBasePurpose) || "Standard".equalsIgnoreCase(routeBasePurpose)) && "All".equalsIgnoreCase(esignConfigSetting)) {
                    routeRequiresESign = "True";
                }
            }
            if (routeBasePurpose != null)
                routeDetails.put("routeBasePurpose", routeBasePurpose);

            if (visblToParent == null || visblToParent.equals("null")) {
                visblToParent = "";
            }
            boolean rtSelected = (sTemplateId != null && !"null".equals(sTemplateId) && !sTemplateId.equals(""));
            if (rtSelected)
                new com.matrixone.apps.common.RouteTemplate(sTemplateId).checksToUseRouteTemplateInRoute(context);

            String sAttrRestrictMembers = PropertyUtil.getSchemaProperty(context, "attribute_RestrictMembers");
            String sAttrRouteBasePurpose = PropertyUtil.getSchemaProperty(context, "attribute_RouteBasePurpose");
            String sAttrPreserveTaskOwner = PropertyUtil.getSchemaProperty(context, "attribute_PreserveTaskOwner");
            String sAttrRouteCompletionAction = PropertyUtil.getSchemaProperty(context, "attribute_RouteCompletionAction");
            String attrOriginator = PropertyUtil.getSchemaProperty(context, "attribute_Originator");
            final String ATTRIBUTE_AUTO_STOP_ON_REJECTION = PropertyUtil.getSchemaProperty(context, "attribute_AutoStopOnRejection");
            String routeAutoNameId = null;
            String strProjectVault = "";
            String revisionSequence = "";
            if (WSNotSelected) {
                if ((objectId != null && !"".equals(objectId) && !"null".equals(objectId))) {
                    DomainObject boObject = new DomainObject(objectId);
                    String sType = boObject.getType(context);
                    String objState = boObject.getInfo(context, DomainConstants.SELECT_CURRENT);
                    if (DomainObject.TYPE_INBOX_TASK.equalsIgnoreCase(sType) && DomainObject.STATE_INBOX_TASK_COMPLETE.equalsIgnoreCase(objState)) {
                        isCompletedTask = true;
                    }
                    routeDetails.put(objectId, objState);
                    boolean isProjId = false;
                    try {
                        Route.routeWithScope(context, objectId, routeId, routeDetails);
                    } catch (FrameworkException ranc) {
                        if (isCompletedTask && ranc.getMessage().indexOf("fromconnect") > 0) {
                            throw new FrameworkException(errorMessage);
                        } else {
                            throw new FrameworkException(ranc.getMessage());
                        }
                    }
                } else {
                    if (restrictMembers.equals("All") || restrictMembers.equals("Organization")) {
                        ContextUtil.pushContext(context);
                        route.connect(context, new RelationshipType(DomainObject.RELATIONSHIP_PROJECT_ROUTE), true, personObject);
                        ContextUtil.popContext(context);
                    } else {


                        try {
                            Route.routeWithScope(context, selscopeId, routeId, routeDetails);

                        } catch (Exception ranc) {
                            throw new FrameworkException(ranc.getMessage());
                        }
                    }
                }

                AttributeList routeAttrList = new AttributeList();
                routeAttrList.addElement(new Attribute(new AttributeType(attrOriginator), context.getUser()));
                System.out.println("user---->" + context.getUser());
                //routeAttrList.addElement(new Attribute(new AttributeType(attrOriginator), "admin_platform"));
                routeAttrList.addElement(new Attribute(new AttributeType(sAttrRouteCompletionAction), routeCompletionAction));
                routeAttrList.addElement(new Attribute(new AttributeType(sAttrRouteBasePurpose), routeBasePurpose));
                routeAttrList.addElement(new Attribute(new AttributeType(sAttrRouteRequiresESignature), routeRequiresESign));

                if (UIUtil.isNotNullAndNotEmpty(strAutoStopOnRejection)) {
                    routeAttrList.addElement(new Attribute(new AttributeType(ATTRIBUTE_AUTO_STOP_ON_REJECTION), strAutoStopOnRejection));
                }
                if (UIUtil.isNotNullAndNotEmpty(strPreserveTaskOwner)) {
                    routeAttrList.addElement(new Attribute(new AttributeType(sAttrPreserveTaskOwner), strPreserveTaskOwner));// getting Auto Stop Attribute
                }
                if (UIUtil.isNotNullAndNotEmpty(name)) {
                    System.out.println("name----->" + name);
                    String ATTRIBUTE_TITLE = PropertyUtil.getSchemaProperty("attribute_Title");
                    routeAttrList.add(new Attribute(new AttributeType(ATTRIBUTE_TITLE), name));// getting Auto Stop Attribute
                }

                if ((selscopeId != null) && (!selscopeId.equals(""))) {
                    if (FrameworkUtil.isObjectId(context, selscopeId)) {
                        DomainObject boscope = new DomainObject(selscopeId);
                        selscopeId = boscope.getInfo(context, "physicalid");
                    }
                    routeAttrList.addElement(new Attribute(new AttributeType(sAttrRestrictMembers), selscopeId));
                }
                route.setId(routeId);
                route.setAttributes(context, routeAttrList);
                route.setDescription(context, routeDescription);
                route.update(context);
                if (visblToParent != null && !"null".equals(visblToParent) && !"".equals(visblToParent) && "Yes".equalsIgnoreCase(visblToParent)) {
                    routeAttrList.addElement(new Attribute(new AttributeType(DomainObject.ATTRIBUTE_SUBROUTE_VISIBILITY), "Yes"));
                    DomainObject taskObj = new DomainObject(objectId);
                    String originator = taskObj.getInfo(context, DomainConstants.SELECT_ORIGINATOR);
                    StringList accessNames = DomainAccess.getLogicalNames(context, routeId);
                    try {
                        ContextUtil.pushContext(context);
                        DomainAccess.createObjectOwnership(context, routeId, null, originator + "_PRJ", (String) accessNames.get(0), DomainAccess.COMMENT_MULTIPLE_OWNERSHIP, false);
                    } catch (Exception ex) {
                        throw new FrameworkException(ex);
                    } finally {
                        ContextUtil.popContext(context);
                    }
                }

                BusinessObject routeTemplateObj = null;
                BusinessObject personObj = null;

                SelectList selectPersonStmts = null;
                SelectList selectPersonRelStmts = null;
                ExpansionWithSelect personSelect = null;
                RelationshipWithSelectItr relPersonItr = null;
                Relationship relationShipRouteNode = null;

                String routeActionValueStr = null;
                String routeSequenceValueStr = null;
                String routeInstructionsValueStr = null;
                String sRouteTitle = null;
                String routeTaskScheduleDate = null;
                String routeTaskNameValueStr = null;
                String routeTaskUser = null;
                String routeAssigneeDueDateOptStr = null;
                String dueDateOffset = null;
                String dueDateOffsetFrom = null;
                String parallelNodeProcessionRule = null;
                String reviewTask = "";
                String allowDelegation = "";
                String chooseUserGroupStr = null;
                String routeOwnerTaskStr = null;
                String routeOwnerUGChoiceStr = null;
                String chooseUserGroupAction = null;
                String chooseUserGrouplevel = null;
                Attribute routeTitle = null;
                Attribute routeActionAttribute = null;
                Attribute routeOrderAttribute = null;
                Attribute routeInstructionsAttribute = null;
                Attribute templateTaskAttribute = null;
                AttributeList attrList = null;
                Attribute routeAssigneeDueDateOptAttribute = null;
                Attribute routeDueDateOffsetAttribute = null;
                Attribute routeDateOffsetFromAttribute = null;
                Attribute routeTaskUserAttribute = null;
                Attribute parallelNodeProcessionRuleAttrib = null;
                Attribute reviewTaskAttribute = null;
                Attribute allowDelegationAttribute = null;
                Attribute routeTaskScheduleDateAttribute = null;
                Attribute chooseUserGroupAttribute = null;
                Attribute chooseUserGroupActionAttribute = null;
                Attribute chooseUserGrouplevelAttribute = null;

                String templateTaskStr = PropertyUtil.getSchemaProperty(context, "attribute_TemplateTask");
                String ATTRIBUTE_UserGroupAction = PropertyUtil.getSchemaProperty(context, "attribute_UserGroupAction");
                String ATTRIBUTE_UserGroupLevelInfo = PropertyUtil.getSchemaProperty(context, "attribute_UserGroupLevelInfo");
                Hashtable routeNodeAttributesTable = new Hashtable();

                if (rtSelected) {

                    selectPersonStmts = new SelectList();
                    AccessUtil accessUtil = new AccessUtil();

                    selectPersonRelStmts = new SelectList();
                    selectPersonRelStmts.addAttribute(DomainObject.ATTRIBUTE_ROUTE_SEQUENCE);
                    selectPersonRelStmts.addAttribute(DomainObject.ATTRIBUTE_ROUTE_ACTION);
                    selectPersonRelStmts.addAttribute(DomainObject.ATTRIBUTE_ROUTE_INSTRUCTIONS);
                    selectPersonRelStmts.addAttribute(DomainObject.ATTRIBUTE_TITLE);
                    selectPersonRelStmts.addAttribute(DomainObject.ATTRIBUTE_ASSIGNEE_SET_DUEDATE);
                    selectPersonRelStmts.addAttribute(DomainObject.ATTRIBUTE_DUEDATE_OFFSET);
                    selectPersonRelStmts.addAttribute(DomainObject.ATTRIBUTE_DATE_OFFSET_FROM);
                    selectPersonRelStmts.addAttribute(DomainObject.ATTRIBUTE_ROUTE_TASK_USER);
                    selectPersonRelStmts.addAttribute(DomainObject.ATTRIBUTE_SCHEDULED_COMPLETION_DATE);
                    String strParallelNodeProscessionRule = PropertyUtil.getSchemaProperty(context, "attribute_ParallelNodeProcessionRule");
                    String sAttReviewTask = PropertyUtil.getSchemaProperty(context, "attribute_ReviewTask");

                    selectPersonRelStmts.addAttribute(strParallelNodeProscessionRule);
                    selectPersonRelStmts.addAttribute(sAttReviewTask);
                    selectPersonRelStmts.addAttribute(DomainObject.ATTRIBUTE_ALLOW_DELEGATION);
                    selectPersonRelStmts.addAttribute("Choose Users From User Group");
                    //TODO p9y
                    selectPersonRelStmts.addAttribute("Route Owner Task");
                    selectPersonRelStmts.addAttribute("Route Owner UG Choice");
                    selectPersonRelStmts.addAttribute(ATTRIBUTE_UserGroupAction);
                    selectPersonRelStmts.addAttribute(ATTRIBUTE_UserGroupLevelInfo);

                    routeTemplateObj = new BusinessObject(sTemplateId);
                    routeTemplateObj.open(context);
                    try {
                        route.connectTemplate(context, sTemplateId);
                    } catch (Exception e) {
                        resultMap.put("Message", e.getMessage());
                    }
                    Pattern typePattern = new Pattern(DomainObject.TYPE_PERSON);
                    typePattern.addPattern(DomainObject.TYPE_ROUTE_TASK_USER);
                    typePattern.addPattern(proxyGoupType);
                    personSelect = routeTemplateObj.expandSelect(context, DomainObject.RELATIONSHIP_ROUTE_NODE, typePattern.getPattern(),
                            selectPersonStmts, selectPersonRelStmts, false, true, (short) 1);

                    routeTemplateObj.close(context);
                    relPersonItr = new RelationshipWithSelectItr(personSelect.getRelationships());
                    // loop thru the rels and get the route object
                    while ((relPersonItr != null) && relPersonItr.next()) {
                        if (relPersonItr.obj().getTypeName().equals(DomainObject.RELATIONSHIP_ROUTE_NODE)) {
                            personObj = relPersonItr.obj().getTo();
                            if (personObj != null) {
                                personObj.open(context);
                                String objectType = personObj.getTypeName();
                                if (DomainObject.TYPE_ROUTE_TASK_USER.equals(objectType) || DomainObject.TYPE_PERSON.equals(objectType) || groupType.equals(objectType) || proxyGoupType.equals(objectType)) {

                                    try {
                                        relationShipRouteNode = route.connect(context, new RelationshipType(DomainObject.RELATIONSHIP_ROUTE_NODE), true, personObj);
                                    } catch (Exception ex) {
                                        resultMap.put("Message", ex.getMessage());
                                    }

                                    routeNodeAttributesTable = relPersonItr.obj().getRelationshipData();
                                    routeSequenceValueStr = (String) routeNodeAttributesTable.get("attribute[" + DomainObject.ATTRIBUTE_ROUTE_SEQUENCE + "]");
                                    sRouteTitle = (String) routeNodeAttributesTable.get("attribute[" + DomainObject.ATTRIBUTE_TITLE + "]");
                                    routeActionValueStr = (String) routeNodeAttributesTable.get("attribute[" + DomainObject.ATTRIBUTE_ROUTE_ACTION + "]");
                                    routeInstructionsValueStr = (String) routeNodeAttributesTable.get("attribute[" + DomainObject.ATTRIBUTE_ROUTE_INSTRUCTIONS + "]");
                                    routeTaskNameValueStr = (String) routeNodeAttributesTable.get("attribute[" + DomainObject.ATTRIBUTE_TITLE + "]");
                                    routeAssigneeDueDateOptStr = (String) routeNodeAttributesTable.get("attribute[" + DomainObject.ATTRIBUTE_ASSIGNEE_SET_DUEDATE + "]");
                                    dueDateOffset = (String) routeNodeAttributesTable.get("attribute[" + DomainObject.ATTRIBUTE_DUEDATE_OFFSET + "]");
                                    dueDateOffsetFrom = (String) routeNodeAttributesTable.get("attribute[" + DomainObject.ATTRIBUTE_DATE_OFFSET_FROM + "]");
                                    routeTaskUser = (String) routeNodeAttributesTable.get("attribute[" + DomainObject.ATTRIBUTE_ROUTE_TASK_USER + "]");
                                    routeTaskScheduleDate = (String) routeNodeAttributesTable.get("attribute[" + DomainObject.ATTRIBUTE_SCHEDULED_COMPLETION_DATE + "]");
                                    chooseUserGroupStr = (String) routeNodeAttributesTable.get("attribute[Choose Users From User Group]");
                                    //TODO p9y
                                    routeOwnerTaskStr = (String) routeNodeAttributesTable.get("attribute[Route Owner Task]");
                                    routeOwnerUGChoiceStr = (String) routeNodeAttributesTable.get("attribute[Route Owner UG Choice]");
                                    chooseUserGroupAction = (String) routeNodeAttributesTable.get("attribute[" + ATTRIBUTE_UserGroupAction + "]");
                                    chooseUserGrouplevel = (String) routeNodeAttributesTable.get("attribute[" + ATTRIBUTE_UserGroupLevelInfo + "]");
                                    // Added by Infosys for Bug # 303103 Date 05/11/2005
                                    parallelNodeProcessionRule = (String) routeNodeAttributesTable.get("attribute[" + strParallelNodeProscessionRule + "]");
                                    // Added for the bug 301391
                                    reviewTask = (String) routeNodeAttributesTable.get("attribute[" + sAttReviewTask + "]");
                                    allowDelegation = (String) routeNodeAttributesTable.get("attribute[" + DomainObject.ATTRIBUTE_ALLOW_DELEGATION + "]");

                                    attrList = new AttributeList();
                                    relationShipRouteNode.open(context);

                                    // Added by Infosys for Bug # 303103 Date 05/11/2005
                                    // set parallelNodeProcessionRule
                                    parallelNodeProcessionRuleAttrib = new Attribute(new AttributeType(strParallelNodeProscessionRule), parallelNodeProcessionRule);
                                    attrList.addElement(parallelNodeProcessionRuleAttrib);

                                    // set title
                                    routeTitle = new Attribute(new AttributeType(DomainObject.ATTRIBUTE_TITLE), sRouteTitle);
                                    attrList.addElement(routeTitle);

                                    // set route action
                                    if (routeActionValueStr != null) {
                                        routeActionAttribute = new Attribute(new AttributeType(DomainObject.ATTRIBUTE_ROUTE_ACTION), routeActionValueStr);
                                        attrList.addElement(routeActionAttribute);
                                    }
                                    if (chooseUserGrouplevel != null) {
                                        chooseUserGrouplevelAttribute = new Attribute(new AttributeType(ATTRIBUTE_UserGroupLevelInfo), chooseUserGrouplevel);
                                        attrList.addElement(chooseUserGrouplevelAttribute);
                                    }
                                    if (chooseUserGroupAction != null) {
                                        chooseUserGroupActionAttribute = new Attribute(new AttributeType(ATTRIBUTE_UserGroupAction), chooseUserGroupAction);
                                        attrList.addElement(chooseUserGroupActionAttribute);
                                    }

                                    // set route order
                                    routeOrderAttribute = new Attribute(new AttributeType(DomainObject.ATTRIBUTE_ROUTE_SEQUENCE), routeSequenceValueStr);
                                    attrList.addElement(routeOrderAttribute);

                                    // set route instructions
                                    if (routeInstructionsValueStr != null) {
                                        routeInstructionsAttribute = new Attribute(new AttributeType(DomainObject.ATTRIBUTE_ROUTE_INSTRUCTIONS), routeInstructionsValueStr);
                                        attrList.addElement(routeInstructionsAttribute);
                                    }

                                    templateTaskAttribute = new Attribute(new AttributeType(templateTaskStr), "Yes");
                                    attrList.addElement(templateTaskAttribute);

                                    // set route assignee due date option
                                    if (routeAssigneeDueDateOptStr != null) {
                                        routeAssigneeDueDateOptAttribute = new Attribute(new AttributeType(DomainObject.ATTRIBUTE_ASSIGNEE_SET_DUEDATE), routeAssigneeDueDateOptStr);
                                        attrList.addElement(routeAssigneeDueDateOptAttribute);
                                    }

                                    // set route due date offset
                                    if (dueDateOffset != null) {
                                        routeDueDateOffsetAttribute = new Attribute(new AttributeType(DomainObject.ATTRIBUTE_DUEDATE_OFFSET), dueDateOffset);
                                        attrList.addElement(routeDueDateOffsetAttribute);
                                    }


                                    // set route due date offset from
                                    if (dueDateOffsetFrom != null) {
                                        routeDateOffsetFromAttribute = new Attribute(new AttributeType(DomainObject.ATTRIBUTE_DATE_OFFSET_FROM), dueDateOffsetFrom);
                                        attrList.addElement(routeDateOffsetFromAttribute);
                                    }

                                    // set route task user attribute
                                    if (routeTaskUser != null) {
                                        routeTaskUserAttribute = new Attribute(new AttributeType(DomainObject.ATTRIBUTE_ROUTE_TASK_USER), routeTaskUser);
                                        attrList.addElement(routeTaskUserAttribute);
                                    }
                                    // Added for the bug 301391
                                    // set Review Task attribute
                                    if (reviewTask != null) {
                                        reviewTaskAttribute = new Attribute(new AttributeType(sAttReviewTask), reviewTask);
                                        attrList.addElement(reviewTaskAttribute);
                                    }
                                    // set Allow Delegation attribute
                                    if (allowDelegation != null) {
                                        allowDelegationAttribute = new Attribute(new AttributeType(DomainObject.ATTRIBUTE_ALLOW_DELEGATION), allowDelegation);
                                        attrList.addElement(allowDelegationAttribute);
                                    }

                                    // set Schedule Date attribute
                                    if (UIUtil.isNotNullAndNotEmpty(routeTaskScheduleDate)) {
                                        routeTaskScheduleDateAttribute = new Attribute(new AttributeType(DomainObject.ATTRIBUTE_SCHEDULED_COMPLETION_DATE), routeTaskScheduleDate);
                                        attrList.addElement(routeTaskScheduleDateAttribute);
                                    }
                                    if (UIUtil.isNotNullAndNotEmpty(chooseUserGroupStr)) {
                                        chooseUserGroupAttribute = new Attribute(new AttributeType("Choose Users From User Group"), chooseUserGroupStr);
                                        attrList.addElement(chooseUserGroupAttribute);
                                    }

                                    if (UIUtil.isNotNullAndNotEmpty(routeOwnerTaskStr) && "TRUE".equalsIgnoreCase(routeOwnerTaskStr)) {
                                        chooseUserGroupAttribute = new Attribute(new AttributeType("Route Owner Task"), routeOwnerTaskStr);
                                        attrList.addElement(chooseUserGroupAttribute);
                                        if (UIUtil.isNotNullAndNotEmpty(routeOwnerUGChoiceStr)) {
                                            chooseUserGroupAttribute = new Attribute(new AttributeType("Route Owner UG Choice"), routeOwnerUGChoiceStr);
                                            attrList.addElement(chooseUserGroupAttribute);
                                        }
                                    }

                                    relationShipRouteNode.setAttributes(context, attrList);
                                    relationShipRouteNode.close(context);
                                    if (groupType.equals(objectType) || proxyGoupType.equals(objectType)) {
                                        StringList accessNames = DomainAccess.getLogicalNames(context, routeId);
                                        String userGroupName = personObj.getName();
                                        String defaultAccess = (String) accessNames.get(0);
                                        DomainAccess.createObjectOwnershipForUserGroups(context, routeId, userGroupName, defaultAccess, DomainAccess.COMMENT_MULTIPLE_OWNERSHIP);
                                    }
                                    // Added for bug 376886
                                    if (((DomainObject.TYPE_ROUTE_TASK_USER).equals(personObj.getTypeName()))) {
                                        String personName = PropertyUtil.getSchemaProperty(context, routeTaskUser);
                                        try {
                                            if (!UIUtil.isNullOrEmpty(personName)) {
                                                accessUtil.setAccess(personName, AccessUtil.ROUTE_ACCESS_GRANTOR, accessUtil.getReadAccess());
                                            }
                                        } catch (MatrixException e) {
                                            throw new FrameworkException(e.toString());
                                        }
                                    }
                                    // Ended
                                }
                                personObj.close(context);
                            }

                        }
                    }//End while

                    if (accessUtil.getAccessList().size() > 0) {
                        String[] strArgs = new String[]{route.getObjectId()};
                        JPO.invoke(context, "emxWorkspaceConstants", strArgs, "grantAccess", JPO.packArgs(accessUtil.getAccessList()));
                    }
                    if (UIUtil.isNullOrEmpty(strAutoStopOnRejection)) {
                        final String SELECT_ATTRIBUTE_AUTO_STOP_ON_REJECTION = "attribute[" + ATTRIBUTE_AUTO_STOP_ON_REJECTION + "]";
                        DomainObject dmoRouteTemplate = new DomainObject(routeTemplateObj);
                        strAutoStopOnRejection = dmoRouteTemplate.getInfo(context, SELECT_ATTRIBUTE_AUTO_STOP_ON_REJECTION);
                        if (strAutoStopOnRejection != null && !"".equals(strAutoStopOnRejection) && !"null".equalsIgnoreCase(strAutoStopOnRejection)) {
                            route.setAttributeValue(context, ATTRIBUTE_AUTO_STOP_ON_REJECTION, strAutoStopOnRejection);
                        }
                    }
                    routeTemplateObj.close(context);
                }
            }
            resultMap.put("id", routeId);
        }
        return resultMap;
    }
 {
    
}
