package longi.dataecharts.entity;

import lombok.Builder;
import lombok.Data;

import java.util.Objects;

@Data
@Builder
public class CodeData {
    private String date;
    private String code;
    private String area;

    private String codeDesc;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CodeData codeData = (CodeData) o;
        return Objects.equals(code, codeData.code);
    }
	@Override
    public int hashCode() {
        return Objects.hash(date, code, area);
    }
}
