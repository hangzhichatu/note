package longi.code.pojo;

import java.util.List;

public class BPMMaterialRequisitionRouteBean {
    private String fd_system_code; // 系统code
    private String fd_batchmodification; // 批量编辑
    private List fd_item_list; // 明细表id
	private String fd_edit_process_type; //变更类型

    public String getFd_edit_process_type() {
		return fd_edit_process_type;
	}

	public void setFd_edit_process_type(String fd_edit_process_type) {
		this.fd_edit_process_type = fd_edit_process_type;
	}
    public String getFd_system_code() {
        return fd_system_code;
    }

    public void setFd_system_code(String fd_system_code) {
        this.fd_system_code = fd_system_code;
    }

    public String getFd_batchmodification() {
        return fd_batchmodification;
    }

    public void setFd_batchmodification(String fd_batchmodification) {
        this.fd_batchmodification = fd_batchmodification;
    }

    public List getFd_item_list() {
        return fd_item_list;
    }

    public void setFd_item_list(List fd_item_list) {
        this.fd_item_list = fd_item_list;
    }
}
