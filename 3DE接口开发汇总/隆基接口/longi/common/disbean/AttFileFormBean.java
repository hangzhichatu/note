package longi.common.disbean;




import javax.validation.constraints.NotNull;

/**
 * 文件
 */

public class AttFileFormBean {
    /**
     * 文件id
     */
    @NotNull
    private String id;
    /**
     * 文件名称
     */
    private String name;

    /**
     * 国际化语言版本(zh:中文，en:英文)
     */
    @NotNull
    private String i18n;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getI18n() {
		return i18n;
	}

	public void setI18n(String i18n) {
		this.i18n = i18n;
	}
    
}
