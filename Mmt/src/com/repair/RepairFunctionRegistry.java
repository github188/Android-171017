package com.repair;

import com.maintainproduct.constant.RepairActivityAlias;
import com.maintainproduct.entity.BaseReportEntity;
import com.maintainproduct.module.basereport.BaseReportActivity;
import com.maintainproduct.module.basereport.BaseReportNavigationMenu;
import com.maintainproduct.module.basereport.history.ReportHistoryNavigationMenu;
import com.maintainproduct.module.casehandover.CasehandoverNavigationMenu;
import com.maintainproduct.module.maintenance.MaintenanceNavigationMenu;
import com.maintainproduct.module.maintenance.feedback.MaintenanceFormActivity;
import com.maintainproduct.module.maintenance.history.MaintenanceNavigationHistoryMenu;
import com.maintainproduct.v2.caselist.MaintainGDListNavigationMenu;
import com.mapgis.mmt.constant.ActivityAlias;
import com.mapgis.mmt.constant.ActivityClassRegistry;
import com.mapgis.mmt.constant.MapMenuRegistry;
import com.mapgis.mmt.constant.NavigationMenuRegistry;
import com.mapgis.mmt.db.DatabaseHelper;
import com.mapgis.mmt.module.gis.toolbar.online.query.coord.CoordQueryMapMenu;
import com.mapgis.mmt.module.webappentry.WebAppNavigationMenu;
import com.patrol.module.MyPlanNavigationMenu;
import com.patrol.module.note.NoteNavigationnMenu;
import com.patrol.module.patrolanalyze.PatrolAnalyzeNavigationMenu;
import com.patrol.module.posandpath2.PosAndPathNavigationMenu2;
import com.patrolproduct.module.nearbyquery.NearbyQueryMenu;
import com.patrolproduct.module.projectquery.QueryProjectMenu;
import com.repair.allcase.AllCaseNavigationMenu;
import com.repair.beihai.poj.gdpoj.module.construct.GDConstructNavigationnMenu;
import com.repair.beihai.poj.hbpoj.module.construct.GCConstructNavigationnMenu;
import com.repair.beihai.poj.hbpoj.module.construct.JDConstructNavigationnMenu;
import com.repair.beihai.poj.hbpoj.module.construct.JGConstructNavigationnMenu;
import com.repair.beihai.poj.hbpoj.module.userwaterecheck.UserWatermeterCheckNavigationnMenu;
import com.repair.beihai.poj.hbpoj.module.userwatermeter.UserWatermeterNavigationnMenu;
import com.repair.changzhou.pianguan.PianGuanCaseNavigationMenu;
import com.repair.eventreport.EventReportActivity;
import com.repair.eventreport.EventReportNavigationMenu;
import com.repair.gisdatagather.attrsedit.GisAttrsEditNavigationMenu;
import com.repair.gisdatagather.gisedit.GisEditNavigationMenu;
import com.repair.gisdatagather.product.GisProjectNavigationMenu;
import com.repair.huangdao.MyCaseHDNavigationMenu;
import com.repair.live.player.LivePlayerMenu;
import com.repair.live.publisher.LivePublishMenu;
import com.repair.mycase.MyCaseNavigationMenu;
import com.repair.quanzhou.module.HotLineGDNavigationMenu;
import com.repair.reporthistory.PatrolReportHistoryNavigationMenu;
import com.repair.scada.StationManageNavigatoinMenu;
import com.repair.shaoxin.water.highrisesearch.HighRiseCloseVavleMenu;
import com.repair.shaoxin.water.hotlinetask.HotlineTaskNavigationMenu;
import com.repair.shaoxin.water.repairtask.RepairTaskNavigationMenu;
import com.repair.zhoushan.module.casemanage.caseoverview.CaseOverviewNavigationMenu;
import com.repair.zhoushan.module.casemanage.infotrack.OnlineTrackNavigationMenu;
import com.repair.zhoushan.module.casemanage.mycase.MyCaseZSNavigationMenu;
import com.repair.zhoushan.module.casemanage.mydonecase.MyDoneCaseNavigationMenu;
import com.repair.zhoushan.module.devicecare.DeviceCareNavigationMenu;
import com.repair.zhoushan.module.devicecare.careoverview.CareOverviewNavigationMenu;
import com.repair.zhoushan.module.devicecare.platfromadd.PlatfromNavigationMenu;
import com.repair.zhoushan.module.devicecare.platfromgislink.PlatfromGisLinkNavigationMenu;
import com.repair.zhoushan.module.devicecare.stationaccount.StationAccountCareNavigationMenu;
import com.repair.zhoushan.module.devicecare.stationaccount.StationAccountCheckNavigationMenu;
import com.repair.zhoushan.module.devicecare.stationaccount.VehicleDeviceNavigationMenu;
import com.repair.zhoushan.module.devicecare.szd.SZDCareNavigationMenu;
import com.repair.zhoushan.module.eventmanage.eventbox.EventManageNavigationMenu;
import com.repair.zhoushan.module.eventmanage.eventbox.EventReceiveNavigationMenu;
import com.repair.zhoushan.module.eventmanage.eventoverview.EventOverviewNavigationMenu;
import com.repair.zhoushan.module.eventmanage.eventreporthistory.ERHNavigationMenu;
import com.repair.zhoushan.module.flowcenter.FlowCenterNavigationMenu;
import com.repair.zhoushan.module.flownodecommonhand.FlowNodeReportNavigationnMenu;
import com.repair.zhoushan.module.projectmanage.projectsitereport.ProjectSiteReportNavigationMenu;
import com.repair.zhoushan.module.projectmanage.waterplantpatrolhistory.WaterPlantPatrolHistoryNavigationMenu;

public class RepairFunctionRegistry {

    public static void regist() {
        /**
         * 咸宁开始的第一版自定义表单的工单系统
         */
        NavigationMenuRegistry.getInstance().regist("自定义上报", BaseReportNavigationMenu.class);
        NavigationMenuRegistry.getInstance().regist("自定义上报历史", ReportHistoryNavigationMenu.class);
        NavigationMenuRegistry.getInstance().regist("维修养护", MaintenanceNavigationMenu.class);// 即在办工单
        NavigationMenuRegistry.getInstance().regist("已办工单", MaintenanceNavigationHistoryMenu.class);
        NavigationMenuRegistry.getInstance().regist("案件移交", CasehandoverNavigationMenu.class);

        /**
         * 桂林开始的第二版工单系统
         */
        NavigationMenuRegistry.getInstance().regist("巡线上报", EventReportNavigationMenu.class);
        NavigationMenuRegistry.getInstance().regist("维修工单", MaintainGDListNavigationMenu.class);
        NavigationMenuRegistry.getInstance().regist("巡线历史", PatrolReportHistoryNavigationMenu.class);



        /**
         * 不使用工作流的第三套工单系统
         */
        NavigationMenuRegistry.getInstance().regist("工单任务", MyCaseNavigationMenu.class);
        NavigationMenuRegistry.getInstance().regist("工单浏览", AllCaseNavigationMenu.class);
        NavigationMenuRegistry.getInstance().regist("黄岛在办", MyCaseHDNavigationMenu.class);

        /**
         * 新奥开始的第二套巡检计划
         */
        NavigationMenuRegistry.getInstance().regist("巡线任务", MyPlanNavigationMenu.class);
        NavigationMenuRegistry.getInstance().regist("巡检分析", PatrolAnalyzeNavigationMenu.class);
        NavigationMenuRegistry.getInstance().regist("位置与轨迹", PosAndPathNavigationMenu2.class);
        NavigationMenuRegistry.getInstance().regist("位置轨迹", PosAndPathNavigationMenu2.class);
        // 老的位置与轨迹已废弃
//        NavigationMenuRegistry.getInstance().regist("位置与轨迹", com.patrol.module.posandpath.PosAndPathNavigationMenu.class);

        /**
         * 舟山开始的第四套工单系统
         */
        NavigationMenuRegistry.getInstance().regist("流程中心", FlowCenterNavigationMenu.class);
        NavigationMenuRegistry.getInstance().regist("历史上报", ERHNavigationMenu.class);
        NavigationMenuRegistry.getInstance().regist("事件总览", EventOverviewNavigationMenu.class);

        NavigationMenuRegistry.getInstance().regist("领单箱", EventReceiveNavigationMenu.class);
        NavigationMenuRegistry.getInstance().regist("调度箱", EventManageNavigationMenu.class);

        NavigationMenuRegistry.getInstance().regist("标准工单", MyCaseZSNavigationMenu.class);
        NavigationMenuRegistry.getInstance().regist("已办理工单", MyDoneCaseNavigationMenu.class);
        NavigationMenuRegistry.getInstance().regist("工单总览", CaseOverviewNavigationMenu.class);

        NavigationMenuRegistry.getInstance().regist("台账任务", PlatfromNavigationMenu.class);
        NavigationMenuRegistry.getInstance().regist("台账挂接", PlatfromGisLinkNavigationMenu.class);

        NavigationMenuRegistry.getInstance().regist("设备养护", DeviceCareNavigationMenu.class);
        NavigationMenuRegistry.getInstance().regist("场站养护", StationAccountCareNavigationMenu.class);
        NavigationMenuRegistry.getInstance().regist("车用养护", VehicleDeviceNavigationMenu.class);
        NavigationMenuRegistry.getInstance().regist("养护总览", CareOverviewNavigationMenu.class);

        NavigationMenuRegistry.getInstance().regist("场站设备检定", StationAccountCheckNavigationMenu.class);

        // 增加<自定义上报>表
        DatabaseHelper.getInstance().createTable(BaseReportEntity.class);

        ActivityClassRegistry.getInstance().regist(RepairActivityAlias.CUSTOM_FORM_REPORT, BaseReportActivity.class);
        ActivityClassRegistry.getInstance().regist(RepairActivityAlias.MAINTAIN_FEEDBACK, MaintenanceFormActivity.class);
        ActivityClassRegistry.getInstance().regist(ActivityAlias.PATROL_REPORT_ACTIVITY, EventReportActivity.class);

        /**GIS采集**/
        //产品 管网工程采集
        NavigationMenuRegistry.getInstance().regist("GIS数据采集", GisProjectNavigationMenu.class);
        NavigationMenuRegistry.getInstance().regist("管网采集", GisProjectNavigationMenu.class);

        //仅编辑单点属性
        NavigationMenuRegistry.getInstance().regist("属性编辑", GisAttrsEditNavigationMenu.class);

        //既可编辑单点又可录入单点
        NavigationMenuRegistry.getInstance().regist("单点操作", GisEditNavigationMenu.class);
        /**GIS采集**/

        /**舟山综合**/
        NavigationMenuRegistry.getInstance().regist("热线追踪", OnlineTrackNavigationMenu.class);
        NavigationMenuRegistry.getInstance().regist("工程现场上报", ProjectSiteReportNavigationMenu.class);
        NavigationMenuRegistry.getInstance().regist("三巡历史查看", WaterPlantPatrolHistoryNavigationMenu.class);
        NavigationMenuRegistry.getInstance().regist("水质点养护", SZDCareNavigationMenu.class);
        /**舟山综合**/

        /**泉州供水**/
        NavigationMenuRegistry.getInstance().regist("热线工单", HotLineGDNavigationMenu.class);
        /**泉州供水**/

        /**三亚长丰燃气**/
        NavigationMenuRegistry.getInstance().regist("工程查询", QueryProjectMenu.class);
        /**三亚长丰燃气**/

        /**北海工程**/
        NavigationMenuRegistry.getInstance().regist("户表报装施工监管", JGConstructNavigationnMenu.class);
        NavigationMenuRegistry.getInstance().regist("户表报装施工进度", JDConstructNavigationnMenu.class);
        NavigationMenuRegistry.getInstance().regist("户表报装施工过程", GCConstructNavigationnMenu.class);
        NavigationMenuRegistry.getInstance().regist("管道施工监管", GDConstructNavigationnMenu.class);
        NavigationMenuRegistry.getInstance().regist("水表挂接", UserWatermeterNavigationnMenu.class);
        NavigationMenuRegistry.getInstance().regist("立户审核", UserWatermeterCheckNavigationnMenu.class);
        NavigationMenuRegistry.getInstance().regist("附近查询", NearbyQueryMenu.class);
        /**北海工程**/

        /**绍兴供水**/
        NavigationMenuRegistry.getInstance().regist("抢修工单", RepairTaskNavigationMenu.class);
        NavigationMenuRegistry.getInstance().regist("热线工单绍兴", HotlineTaskNavigationMenu.class);
        MapMenuRegistry.getInstance().regist("高层查询", HighRiseCloseVavleMenu.class);
        MapMenuRegistry.getInstance().regist("坐标查询", CoordQueryMapMenu.class);
        /**绍兴供水**/

        /**常州PDA升级**/
        NavigationMenuRegistry.getInstance().regist("片管工单", PianGuanCaseNavigationMenu.class);
        /**常州PDA升级**/

        NavigationMenuRegistry.getInstance().regist("巡线日志", NoteNavigationnMenu.class);
        NavigationMenuRegistry.getInstance().regist("运维管理", StationManageNavigatoinMenu.class);

        NavigationMenuRegistry.getInstance().regist("流程节点反馈",FlowNodeReportNavigationnMenu.class);

        NavigationMenuRegistry.getInstance().regist("现场直播", LivePublishMenu.class);
        NavigationMenuRegistry.getInstance().regist("全部直播", LivePlayerMenu.class);
        NavigationMenuRegistry.getInstance().regist("Web入口", WebAppNavigationMenu.class);
    }
}
