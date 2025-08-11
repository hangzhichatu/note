package longi.common.pojo;

import java.io.Serializable;

public class CodeSystemLoginInfo implements Serializable{
	/**
	* @Fields serialVersionUID : TODO
	*/
	private static final long serialVersionUID = -8210532808334740914L;
	private String name;
	private String fullName;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getFullName() {
		return fullName;
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	
	
}
