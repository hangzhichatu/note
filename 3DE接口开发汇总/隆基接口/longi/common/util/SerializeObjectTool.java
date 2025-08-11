package longi.common.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/** 
* @ClassName: SerializeObjectTool
* @Description: 序列化及反序列化工具类
* @author: Longi.suwei
* @date: 2022年5月30日 上午11:30:55
*/
public class SerializeObjectTool {
	/**
	* @Author: Longi.suwei
	* @Title: serialize
	* @Description: 序列化
	* @param: @param obj
	* @param: @return
	* @date: 2022年5月30日 上午11:31:10
	*/
	public static byte[] serialize(Object obj) {
		ObjectOutputStream obi = null;
		ByteArrayOutputStream bai = null;
		try {
			bai = new ByteArrayOutputStream();
			obi = new ObjectOutputStream(bai);
			obi.writeObject(obj);
			byte[] byt = bai.toByteArray();
			return byt;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	* @Author: Longi.suwei
	* @Title: unserizlize
	* @Description: 反序列化
	* @param: @param byt
	* @param: @return
	* @date: 2022年5月30日 上午11:31:17
	*/
	public static Object unserizlize(byte[] byt) {
		ObjectInputStream oii = null;
		ByteArrayInputStream bis = null;
		bis = new ByteArrayInputStream(byt);
		try {
			oii = new ObjectInputStream(bis);
			Object obj = oii.readObject();
			return obj;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}