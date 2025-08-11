package longi.dataecharts.sql;

public class QuerySqlConstant {

    public static final String erpSql = "SELECT MM.AA 库存组织" +
            ",MM.BB 工单" +
            ",MM.CC 工单目标料号" +
            ",MM.DD 工单目标功率" +
            ",MM.EE 工单计划块数" +
            ",MM.EEE 工单计划瓦数" +
            ",MM.II 完工子工" +
            ",MM.III 完工子工单创建月" +
            ",MM.F 完工料号国家区域" +
            ",MM.FF 完工产出料号" +
            ",MM.GG 完工产出料号基准" +
            ",MM.HH 完工产出料号功率" +
            ",MM.LL 完工类型" +
            ",sum(MM.JJ) 完工块数" +
            ",sum(MM.KK) 完工瓦数量" +
            " FROM (" +
            "SELECT IO.ORGANIZATION_NAME AA" +
            ",(" +
            "SELECT wdd.WIP_ENTITY_NAME" +
            " FROM wip_discrete_jobs_v wdd" +
            ",cux_wip_job_relaction_t cw" +
            " WHERE cw.wip_entity_id_child = wd.WIP_ENTITY_ID" +
            " AND wdd.WIP_ENTITY_ID = cw.wip_entity_id" +
            ") BB" +
            ",(" +
            "SELECT ms.segment1" +
            " FROM wip_discrete_jobs_v wdd" +
            ",cux_wip_job_relaction_t cw" +
            ",mtl_system_items_b ms" +
            " WHERE cw.wip_entity_id_child = wd.WIP_ENTITY_ID" +
            " AND wdd.WIP_ENTITY_ID = cw.wip_entity_id" +
            " AND ms.inventory_item_id = wdd.PRIMARY_ITEM_ID" +
            " AND ms.organization_id = 85" +
            ") CC" +
            ",(" +
            "SELECT ms.attribute12" +
            " FROM wip_discrete_jobs_v wdd" +
            ",cux_wip_job_relaction_t cw" +
            ",mtl_system_items_b ms" +
            " WHERE cw.wip_entity_id_child = wd.WIP_ENTITY_ID" +
            " AND wdd.WIP_ENTITY_ID = cw.wip_entity_id" +
            " AND ms.inventory_item_id = wdd.PRIMARY_ITEM_ID" +
            " AND ms.organization_id = 85" +
            ") DD" +
            ",(" +
            "SELECT wdd.START_QUANTITY" +
            " FROM wip_discrete_jobs_v wdd" +
            ",cux_wip_job_relaction_t cw" +
            " WHERE cw.wip_entity_id_child = wd.WIP_ENTITY_ID" +
            " AND wdd.WIP_ENTITY_ID = cw.wip_entity_id" +
            ") EE" +
            ",(" +
            "SELECT wdd.START_QUANTITY * ms.attribute12" +
            " FROM wip_discrete_jobs_v wdd" +
            ",cux_wip_job_relaction_t cw" +
            ",mtl_system_items_b ms" +
            " WHERE cw.wip_entity_id_child = wd.WIP_ENTITY_ID" +
            " AND wdd.WIP_ENTITY_ID = cw.wip_entity_id" +
            " AND ms.inventory_item_id = wdd.PRIMARY_ITEM_ID" +
            " AND ms.organization_id = 85" +
            ") EEE" +
            ",(" +
            "SELECT t.property_desc" +
            " FROM cux.cux_mtl_items_extra_info t" +
            " WHERE t.inventory_item_id = msi.inventory_item_id" +
            " AND t.LANGUAGE='ZHS' AND t.property_name = '80_TARGET_SALES_AREA'" +
            ") F" +
            ",msi.segment1 FF" +
            ",msi.attribute14 GG" +
            ",msi.attribute12 HH" +
            ",wd.WIP_ENTITY_NAME II" +
            ",to_char(wd.CREATION_DATE, 'YYYY-MM') III" +
            ",mmt.transaction_quantity JJ" +
            ",(mmt.transaction_quantity * msi.attribute12) KK" +
            ",mtt.transaction_type_name LL" +
            " FROM inv_organization_name_v IO" +
            ",mtl_system_items_b msi" +
            ",mtl_material_transactions mmt" +
            ",mtl_transaction_types mtt" +
            ",wip_discrete_jobs_v wd" +
            ",MTL_ITEM_CATEGORIES_V mic" +
            " WHERE 1 = 1" +
            " AND mic.INVENTORY_ITEM_ID = msi.inventory_item_id" +
            " AND mic.ORGANIZATION_ID = msi.organization_id" +
            " AND mic.STRUCTURE_ID = 101" +
            " AND mic.CATEGORY_SET_ID = 1" +
            " AND mic.SEGMENT1 = '80'" +
            " AND mmt.transaction_source_type_id = 5" +
            " AND mmt.transaction_type_id IN (" +
            "44" +
            ",17" +
            ")" +
            " AND wd.WIP_ENTITY_ID = mmt.transaction_source_id" +
            " AND IO.ORGANIZATION_ID = msi.organization_id" +
            " AND msi.inventory_item_id = mmt.inventory_item_id" +
            " AND mmt.organization_id = IO.ORGANIZATION_ID" +
            " AND mmt.transaction_type_id = mtt.transaction_type_id" +
            " AND wd.creation_date >= to_date('${Ksrq}', 'yyyy/MM/dd')" +
            " AND wd.creation_date <= to_date('${Jsrq}', 'yyyy/MM/dd')" +
            ") MM" +
            " GROUP BY MM.AA" +
            ",MM.BB" +
            ",MM.CC" +
            ",MM.DD" +
            ",MM.EE" +
            ",MM.EEE" +
            ",MM.F" +
            ",MM.FF" +
            ",MM.GG" +
            ",MM.HH" +
            ",MM.II" +
            ",MM.III" +
            ",MM.LL" +
            " ORDER BY MM.BB" +
            ",MM.CC";

    public static final String wmsSql = "SELECT M1.*,M3.CUSTOMERDESCR1,M2.POWER*M1.QTY AS W FROM (" +
            " SELECT '生产入库'LX,T.TOCUSTOMERID AS CUSTOMERID, T.TOSKU AS SKU,CCF.SALES_REGION_NAME, SUM(T.TOQTY) AS QTY" +
            "  FROM ACT_TRANSACTION_LOG T" +
            "  JOIN DOC_ASN_HEADER T1" +
            "    ON T.ORGANIZATIONID = T1.ORGANIZATIONID" +
            "   AND T.WAREHOUSEID = T1.WAREHOUSEID" +
            "   AND T.DOCNO = T1.ASNNO" +
            "  JOIN INV_LOT_ATT ILA ON T.TOLOTNUM=ILA.LOTNUM AND T.ORGANIZATIONID=ILA.ORGANIZATIONID" +
            "  JOIN CUX_CONTRACT_FILES CCF ON ILA.LOTATT04=CCF.CONTRACT_ID" +
            " WHERE T1.ASNTYPE IN ('FPT','AGV') AND T.TRANSACTIONTYPE='IN'" +
            "  AND T.TRANSACTIONTIME >= TO_DATE('${Ksrq}', 'yyyy-MM-dd')" +
            "   AND T.TRANSACTIONTIME < TO_DATE('${Jsrq}', 'yyyy-MM-dd')+1" +
            " GROUP BY T.TOCUSTOMERID, T.TOSKU,CCF.SALES_REGION_NAME" +
            " UNION ALL" +
            " SELECT '销售出库' LX,T.FMCUSTOMERID AS CUSTOMERID, T.FMSKU AS SKU,CCF.SALES_REGION_NAME ,SUM(T.FMQTY) AS QTY" +
            "  FROM ACT_TRANSACTION_LOG T" +
            "  JOIN DOC_ORDER_HEADER T1" +
            "    ON T.ORGANIZATIONID = T1.ORGANIZATIONID" +
            "   AND T.WAREHOUSEID = T1.WAREHOUSEID" +
            "   AND T.DOCNO = T1.ORDERNO" +
            "   JOIN INV_LOT_ATT ILA ON T.FMLOTNUM=ILA.LOTNUM AND T.ORGANIZATIONID=ILA.ORGANIZATIONID" +
            "   JOIN CUX_CONTRACT_FILES CCF ON ILA.LOTATT04=CCF.CONTRACT_ID" +
            " WHERE T1.ORDERTYPE = 'SO'AND T.TRANSACTIONTYPE='PK'" +
            "   AND T.TRANSACTIONTIME >= TO_DATE('${Ksrq}', 'yyyy-MM-dd')" +
            "   AND T.TRANSACTIONTIME <= TO_DATE('${Jsrq}', 'yyyy-MM-dd')" +
            " GROUP BY T.FMCUSTOMERID, T.FMSKU,CCF.SALES_REGION_NAME)M1 JOIN CUX_ITEM_CODEID M2 ON M1.SKU=M2.ITEM_CODE" +
            " JOIN BAS_CUSTOMER M3 ON M1.CUSTOMERID=M3.CUSTOMERID AND M3.CUSTOMERTYPE='OW'" +
            " ORDER BY M1.LX,M1.CUSTOMERID,M1.SKU";


    // oracle 41 预投
    public static final String ytSql = "SELECT" +
            " component_finish_no AS product_no," +
            " sum(schedul_storage_num) AS watt" +
            " FROM" +
            " LDW_APP.APP_SALES_PLAN  " +
            "WHERE" +
            " import_type = '投入'" +
            " AND ORDER_NAME LIKE '%预投%'" +
            " AND order_demand_type = '分布式'" +
            " AND substr( to_char(schedul_storage_date, 'yyyy-mm-dd'),1,10)  between '${DataMonth1}'" +
            " and '${DataMonth2}'" +
            " GROUP BY " +
            " component_finish_no";

    public static final String ytSql3 = "SELECT" +
            " component_finish_no AS product_no," +
            " sum(schedul_storage_num) AS watt," +
            " count(1) AS orderNum," +
            " order_demand_type AS shortTypeCode" +
            " FROM" +
            " LDW_APP.APP_SALES_PLAN" +
            " WHERE" +
            " import_type = '投入'" +
            " AND ORDER_NAME LIKE '%预投%'" +
            " AND substr( to_char(schedul_storage_date, 'yyyy-mm-dd'),1,10)  between '${DataMonth1}'" +
            " and '${DataMonth2}'" +
            " GROUP BY " +
            " component_finish_no,order_demand_type";

    // oracle 41 CMS
    public static final String cmsSql = "SELECT " +
            "SUBSTR(product_no, 0, 10)," +
            "sum(qty) AS watt" +
            " FROM" +
            " ldw_app.sd_cont_details" +
            " WHERE approve_date IS NOT NULL " +
            " AND product_no IS NOT NULL " +
            " AND LENGTH( product_no ) > 10 " +
            " AND ct_version_no = 'V0' " +
            " AND substr(first_effective_date,1,10) between '${DataMonth1}' and '${DataMonth2}'" +
            " GROUP BY " +
            "SUBSTR(product_no, 0, 10)";

    public static final String cmsSql3 = "SELECT " +
            "SUBSTR(product_no, 0, 10) AS code," +
            "sum(qty) AS watt," +
            "count(1) AS orderNum," +
            "short_type_code AS shortTypeCode" +
            " FROM" +
            " ldw_app.sd_cont_details" +
            " WHERE approve_date IS NOT NULL " +
            " AND product_no IS NOT NULL " +
            " AND LENGTH( product_no ) > 10 " +
            " AND ct_version_no = 'V0' " +
            " AND substr(first_effective_date,1,10) between '${DataMonth1}' and '${DataMonth2}'" +
            " GROUP BY " +
            "SUBSTR(product_no, 0, 10),short_type_code";

    public static final String dpSql = "SELECT" +
            " m.sales_region_id," +
            " m.sales_region_name," +
            " m.sales_dept_id," +
            " m.sales_dept_name," +
            " m.busi_object_code," +
            " m.busi_object_type_name," +
            " m.order_status_name," +
            " m.contract_no," +
            " m.org_type_name," +
            " m.busi_type_name," +
            " m.item_code," +
            " m.month_name," +
            " m.plan_qty " +
            "FROM" +
            " (" +
            " SELECT" +
            "  a.sales_region_id," +
            "  a.sales_region_name," +
            "  a.sales_dept_id," +
            "  a.sales_dept_name," +
            "  a.sales_code," +
            "  a.sales_name," +
            "  a.busi_object_code," +
            "  a.period_status," +
            "  a.period_status_name," +
            "  a.busi_object_type," +
            "  a.busi_object_type_name," +
            "  a.order_status_name," +
            "  a.customer_name," +
            "  a.contract_no," +
            "  a.project_name," +
            "  ifnull( a.org_type, 'SALE_MANAGER' ) org_type," +
            " CASE" +
            "   " +
            "   WHEN a.org_type = 'SALE_DIRECTOR' THEN" +
            "   '销售总监' " +
            "  END org_type_name," +
            " a.busi_type_name," +
            " substr(b.item_code,1,10) as item_code," +
            " DATE_FORMAT( c.start_date, '%Y-%m' ) month_name," +
            " sum(" +
            " ifnull( c.plan_qty, 0 )) plan_qty " +
            "FROM" +
            " dp_longi_header_tmp_v a," +
            " ( SELECT @current_lang := 'zh' ll ) lang," +
            " dp_lines_tmp b," +
            " dp_lines_buckets_tmp c " +
            "WHERE" +
            " a.dp_id = b.dp_id " +
            " AND b.dp_line_id = c.dp_line_id " +
            " AND c.plan_qty > 0 " +
            " AND a.header_source_code = 'DP' " +
            " AND a.org_type = 'SALE_DIRECTOR' " +
            " AND DATE_FORMAT( c.start_date, '%Y-%m-%d' ) BETWEEN '${StartMonth}' " +
            " AND '${EndMonth}' " +
            "GROUP BY" +
            " a.sales_region_id," +
            " a.sales_region_name," +
            " a.sales_dept_id," +
            " a.sales_dept_name," +
            " a.sales_code," +
            " a.sales_name," +
            " a.busi_object_code," +
            " a.period_status," +
            " a.period_status_name," +
            " a.busi_object_type," +
            " a.busi_object_type_name," +
            " a.order_status_name," +
            " a.customer_name," +
            " a.contract_no," +
            " a.project_name," +
            " a.org_type," +
            " a.busi_type_name," +
            " substr(b.item_code,1,10)," +
            " c.start_date " +
            " ) m " +
            "ORDER BY" +
            " m.sales_dept_id," +
            " m.org_type," +
            " m.sales_code," +
            " m.month_name," +
            " m.period_status";

}
