package longi.common.bean.bps;

import java.util.ArrayList;
import java.util.List;

/**
 * Auto-generated: 2022-03-22 13:42:41
 *
 * @author www.jsons.cn
 * @website http://www.jsons.cn/json2java/
 */
public class DpInfo {

	private String id;
	private String text;
	private String state = "";// 节点状态，'open' 或 'closed'，默认是 'open'。当设置为 'closed' 时，该节点有子节点，并且将从远程站点加载它们。
	private CustomAttr attributes;
	public List<DpInfo> children = new ArrayList<DpInfo>();
	
	public CustomAttr getAttributes() {
		return attributes;
	}

	public void setAttributes(CustomAttr attributes) {
		this.attributes = attributes;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public void setChildren(List<DpInfo> children) {
		this.children = children;
	}

	public List<DpInfo> getChildren() {
		return children;
	}

	public void addChildren(List<DpInfo> newChildren) {
		newChildren.addAll(children);
		this.children = newChildren;
	}

}