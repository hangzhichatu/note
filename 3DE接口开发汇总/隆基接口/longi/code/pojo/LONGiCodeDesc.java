package longi.code.pojo;

/**
 * @ClassName: LONGiCodeDesc
 * @Description:
 * @Author: Longi.liqitong
 * @Date: 2020-4-27 13:27
 */

public class LONGiCodeDesc {
    private String name;
    private String sequence;
    private String cn_prefix;
    private String cn_suffix;
    private String en_prefix;
    private String en_suffix;
    private String segment;


    public String getSegment() {
        return segment;
    }

    public void setSegment(String segment) {
        this.segment = segment;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public String getCn_prefix() {
        return cn_prefix;
    }

    public void setCn_prefix(String cn_prefix) {
        this.cn_prefix = cn_prefix;
    }

    public String getCn_suffix() {
        return cn_suffix;
    }

    public void setCn_suffix(String cn_suffix) {
        this.cn_suffix = cn_suffix;
    }

    public String getEn_prefix() {
        return en_prefix;
    }

    public void setEn_prefix(String en_prefix) {
        this.en_prefix = en_prefix;
    }

    public String getEn_suffix() {
        return en_suffix;
    }

    public void setEn_suffix(String en_suffix) {
        this.en_suffix = en_suffix;
    }

    public LONGiCodeDesc() {
    }

    public LONGiCodeDesc(String name, String sequence, String cn_prefix, String cn_suffix, String en_prefix, String en_suffix,String segment) {
        this.name = name;
        this.sequence = sequence;
        this.cn_prefix = cn_prefix;
        this.cn_suffix = cn_suffix;
        this.en_prefix = en_prefix;
        this.en_suffix = en_suffix;
        this.segment=segment;
    }

    @Override
    public String toString() {
        return "LONGiCodeDesc{" +
                "name='" + name + '\'' +
                ", sequence='" + sequence + '\'' +
                ", cn_prefix='" + cn_prefix + '\'' +
                ", cn_suffix='" + cn_suffix + '\'' +
                ", en_prefix='" + en_prefix + '\'' +
                ", en_suffix='" + en_suffix + '\'' +
                ", segment='" + segment + '\'' +
                '}';
    }
}
