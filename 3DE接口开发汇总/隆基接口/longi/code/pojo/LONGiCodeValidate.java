package longi.code.pojo;

/**
 * @ClassName: LONGiCodeValidate
 * @Description:
 * @Author: Longi.liqitong
 * @Date: 2020-10-15 10:37
 */

public class LONGiCodeValidate {
    private String full_regExs;
    private String regExs;
    private String suffix;
    private String message;

    public String getFull_regExs() {
        return full_regExs;
    }

    public void setFull_regExs(String full_regExs) {
        this.full_regExs = full_regExs;
    }

    public String getRegExs() {
        return regExs;
    }

    public void setRegExs(String regExs) {
        this.regExs = regExs;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "LONGiCodeValidate{" +
                "full_regExs='" + full_regExs + '\'' +
                ", regExs='" + regExs + '\'' +
                ", suffix='" + suffix + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
