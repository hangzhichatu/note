package longi.code.pojo;

/**
 * @ClassName: LONGiCodeInfo
 * @Description:
 * @Author: Longi.liqitong
 * @Date: 2020-4-27 13:26
 */

public class LONGiCode {
    private String name;
    private String sequence;
    private String segment;

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

    public String getSegment() {
        return segment;
    }

    public void setSegment(String segment) {
        this.segment = segment;
    }

    public LONGiCode(String name, String sequence, String segment) {
        this.name = name;
        this.sequence = sequence;
        this.segment = segment;
    }

    @Override
    public String toString() {
        return "LONGiCode{" +
                "name='" + name + '\'' +
                ", sequence='" + sequence + '\'' +
                ", segment='" + segment + '\'' +
                '}';
    }
}
