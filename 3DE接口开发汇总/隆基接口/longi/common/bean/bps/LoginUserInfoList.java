package longi.common.bean.bps;

import java.util.List;

/**
 * @ClassName LoginUserInfoList
 * @Description TODO
 * @Author Longi.liqitong
 * @Date 2022-10-20 9:49
 * Version 1.0
 */
public class LoginUserInfoList {

    private String total;
    private List<LoginUserInfo> rows;

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public List<LoginUserInfo> getRows() {
        return rows;
    }

    public void setRows(List<LoginUserInfo> rows) {
        this.rows = rows;
    }
}
