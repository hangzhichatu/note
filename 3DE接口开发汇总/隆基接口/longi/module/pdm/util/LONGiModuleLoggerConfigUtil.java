package longi.module.pdm.util;


import java.io.PrintWriter;
import java.io.StringWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;


/**  
* @ClassName: LoggerConfigUtil  
* @Description: TODO(系统日志配置文件.log4j)
* @author suw@avic-digital.com  
* @date 2019年10月14日 
*    
*/  

public class LONGiModuleLoggerConfigUtil {
	private static Level level = Level.INFO;
	
	/**  
	* @Title: getLogger  
	* @Description: TODO(这里用一句话描述这个方法的作用)  
	* @author suw@avic-digital.com  
	* @date 2019年10月14日 
	* @param @return    参数  
	* @return Logger    返回类型 
	* @throws  
	*/  
	public static Logger getLogger(){
		
		if(level == Level.ERROR) {
			return LoggerFactory.getLogger("spdmErrorLogger");
		}
		else {
			return LoggerFactory.getLogger("spdmInfoLogger");
		}
		
	}
	/**  
	* @Title: getStackTrace  
	* @Description: TODO(这里用一句话描述这个方法的作用)  
	* @author suw@avic-digital.com  
	* @date 2019年10月14日 
	* @param @param t
	* @param @return    参数  
	* @return String    返回类型  
	* @throws  
	*/  
	private static String getStackTrace(Throwable t)	{
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		pw.flush();
		sw.flush();
		return sw.toString();
	}

	
	/**  
	* @Title: error  
	* @Description: TODO(logger the error info to the logger file including the exception stack info)  
	* @author suw@avic-digital.com  
	* @date 2019年10月14日 
	* @param @param excepInfo
	* @param @param t    参数  
	* @return void    返回类型  
	* @throws  
	*/  
	public static void error(String excepInfo, Throwable t) {
		Logger logger = getLogger();
		logger.error(excepInfo + "\r\n" + getStackTrace(t));
		
	}
}
