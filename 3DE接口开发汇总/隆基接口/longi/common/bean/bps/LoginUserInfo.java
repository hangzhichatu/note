package longi.common.bean.bps;

/**
 * @ClassName LoginUserInfo
 * @Description TODO
 * @Author Longi.liqitong
 * @Date 2022-10-20 9:31
 * Version 1.0
 */
public class LoginUserInfo {

    private String name;
    private String oprId;
    private String emplId;
    private String positionNbrDescr;
    private String deptDescr;
    private String gpsLongDescr;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOprId() {
        return oprId;
    }

    public void setOprId(String oprId) {
        this.oprId = oprId;
    }

    public String getEmplId() {
        return emplId;
    }

    public void setEmplId(String emplId) {
        this.emplId = emplId;
    }

    public String getPositionNbrDescr() {
        return positionNbrDescr;
    }

    public void setPositionNbrDescr(String positionNbrDescr) {
        this.positionNbrDescr = positionNbrDescr;
    }

    public String getDeptDescr() {
        return deptDescr;
    }

    public void setDeptDescr(String deptDescr) {
        this.deptDescr = deptDescr;
    }

    public String getGpsLongDescr() {
        return gpsLongDescr;
    }

    public void setGpsLongDescr(String gpsLongDescr) {
        this.gpsLongDescr = gpsLongDescr;
    }

    @Override
    public String toString() {
        return "LoginUserInfo{" +
                "name='" + name + '\'' +
                ", oprId='" + oprId + '\'' +
                ", emplId='" + emplId + '\'' +
                ", positionNbrDescr='" + positionNbrDescr + '\'' +
                ", deptDescr='" + deptDescr + '\'' +
                ", gpsLongDescr='" + gpsLongDescr + '\'' +
                '}';
    }
}
