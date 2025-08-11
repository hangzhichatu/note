package longi.module.pdm.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @ClassName: OASendToDoBean
 * @Description: TODO 发送待办内容的bean
 * @author: Longi.suwei
 * @date: 2020年5月14日 下午4:28:22
 */
public class LONGiModuleTargetBean {

	/**
	 * @Fields LoginName : TODO EKP系统组织架构个人登录名
	 */
	@JsonProperty("LoginName")
	private String LoginName = "";
	public LONGiModuleTargetBean() {
	}
	public LONGiModuleTargetBean(String loginName) {
		LoginName = loginName;
	}

	public String getLoginName() {
		return LoginName;
	}

	public void setLoginName(String loginName) {
		LoginName = loginName;
	}

}

