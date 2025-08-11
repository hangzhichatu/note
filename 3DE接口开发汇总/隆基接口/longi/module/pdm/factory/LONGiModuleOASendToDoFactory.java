package longi.module.pdm.factory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import longi.cell.pm.impl.PMCellFollowUpAssigneeModify;
import longi.cell.pm.impl.PMCellFollowUpComplete;
import longi.cell.pm.impl.PMCellFollowUpDelete;
import longi.module.pdm.dao.LONGiModuleOASendToDoInterface;
import longi.module.pdm.impl.*;

public class LONGiModuleOASendToDoFactory {

	static Map<String, LONGiModuleOASendToDoInterface> operationMap = new HashMap<>();
    static {
        operationMap.put("InboxTaskNodeDone", new LONGiModuleInboxTaskNodeDoneAction());
        operationMap.put("InboxTaskNodeCreate", new LONGiModuleInboxTaskToDoAction());
        operationMap.put("InboxTaskNodeChangeOwner", new LONGiModuleInboxTaskChangeOwnerAction());
        operationMap.put("RouteRejectToStop", new LONGiModuleRouteRejectToStopAction());
        operationMap.put("ProjectActiveSendToMember", new LONGiModuleProjectReadyMemberNodifyAction());
        operationMap.put("JobTaskInWorkAndReviewSendTodo", new LONGiModuleProjectTaskStatePromoteSendToDoAction());
        operationMap.put("TaskAssgineeSendToDo", new LONGiModuleTaskAssgineeSendToDoAction());
        operationMap.put("TaskAssgineeSendToDoDelete", new LONGiModuleTaskAssgineeSendToDoDeleteAction());
        operationMap.put("ProjectChangeOwnerSendToDo", new LONGiModuleProjectSpaceChangeOwnerAction());
        operationMap.put("ObjectReleaseNotify", new LONGiModuleObjectReleaseNotifyAction());
        operationMap.put("OrderPromoteCreateBPMRoute", new LONGiModuleOrderPromoteCreateBPMRouteAction());
        operationMap.put("OrderPromoteCheckBPMRoute", new LONGiModuleOrderPromoteCheckBPMRouteCheck());
        operationMap.put("ControlledOrderDemoteSendToDo", new LONGiModuleControlledOrderDemoteSendToDoAction());
        operationMap.put("CodeSystemPurchasingManagerToDo", new LONGiModuleCodeSystemPurchasingManagerToDoAction());
        operationMap.put("CodeSystemPurchasingReleaseDone", new LONGiModuleCodeSystemPurchasingDoneAction());
        operationMap.put("CodeRequestToDoDelete", new LONGiModuleCodeRequestToDoDeleteAction());
        operationMap.put("CodeSystemRequestRejectToOwner", new LONGiModuleCodeSystemRequestRejectToOwnerAction());
        operationMap.put("OrderDeleteRouteAbandon", new LONGiModuleOrderDeleteRouteAbandonAction());
        operationMap.put("CodeSystemOwnerModifyToDoDone", new LONGiModuleCodeSystemOwnerModifyToDoDoneAction());
        operationMap.put("OrderDeleteRouteAbandonCheck", new LONGiModuleOrderDeleteRouteAbandonCheck());
        operationMap.put("TaskNotifyAssignedMembers", new LONGiModuleTaskNotifyAction());
        operationMap.put("TaskCompleteToDoDone", new LONGiModuleProjectTaskCompleteDoneToDoAction());
        operationMap.put("TaskManagerRejectToDo", new LONGiModuleProjectTaskManagerRejectAction());
        //add by xuqiang for notice
        operationMap.put("SCCodePromoteNotice", new LONGiModuleObjectProcessAndCAReleaseNotifyAction());

        operationMap.put("LGiC_ModifyFollowUpList", new PMCellFollowUpAssigneeModify());
        operationMap.put("LGiC_DeleteFollowUpList", new PMCellFollowUpDelete());
        operationMap.put("LGiC_CompleteFollowUpList", new PMCellFollowUpComplete());


//        "association", "att", "channel", "command", "form", "format", "group", "inquiry",
//		"menu", "person", "policy", "portal", "relationship", "role", "store", "table", "type", "lattice",
//		"wizard"
        // more operators
    }
 
    public static Optional<LONGiModuleOASendToDoInterface> getOperation(String operator) {
        return Optional.ofNullable(operationMap.get(operator));
    }
}
