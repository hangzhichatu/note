package longi.common.bean.bps;

import java.util.List;

public class UserListInfo {

	private String total;
	private List<UserInfo> rows;
	public String getTotal() {
		return total;
	}
	public void setTotal(String total) {
		this.total = total;
	}
	public List<UserInfo> getRows() {
		return rows;
	}
	public void setRows(List<UserInfo> rows) {
		this.rows = rows;
	}
}
