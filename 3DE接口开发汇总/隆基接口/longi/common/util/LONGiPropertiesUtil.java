package longi.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Properties;

public class LONGiPropertiesUtil {
//    private static Properties props = new Properties();
//    private static Properties yzProps = new Properties();
    private final static Logger logger = LoggerFactory.getLogger(LONGiPropertiesUtil.class);
    
    
//    static {
//        //初始化
//        try{
//           init();
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//    }
//    
//    protected static void init() {
//        try {
//        	System.out.println("init  ---------");
////        	props.load(new InputStreamReader(LONGiPropertiesUtil.class
////                    .getClassLoader().getResourceAsStream("LONGiConfigInfo/Interface.properties"), "UTF-8"));
//        	props.load(new InputStreamReader(new LONGiPropertiesUtil().getClass().getResourceAsStream("LONGiConfigInfo/Interface.properties"), "UTF-8"));
//        	System.out.println("init  ---------2");
//            yzProps.load(new InputStreamReader(LONGiPropertiesUtil.class
//                    .getClassLoader().getResourceAsStream("LONGiConfigInfo/YZFCS.properties"), "UTF-8"));
//            System.out.println("init  ---------3");
//        }
//        catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    public static String readInterfaceProperties(String key) {
//        String value = "";
//        InputStream is = null;
//    	try {
////			props.load(new InputStreamReader(LONGiPropertiesUtil.class
////			      .getClassLoader().getResourceAsStream("LONGiConfigInfo/Interface.properties"), "UTF-8"));
//    		
//    		is = LONGiPropertiesUtil.class.getClassLoader().getResourceAsStream("LONGiConfigInfo/Interface.properties");
//            Properties p = new Properties();
//            p.load(is);
//            value = p.getProperty(key);
//		} catch (IOException e) {
//            e.printStackTrace();
//            final Writer result = new StringWriter();
//			final PrintWriter printWriter = new PrintWriter(result);
//			e.fillInStackTrace().printStackTrace(printWriter);
//            logger.error("加载properties失败LONGiConfigInfo/Interface.properties:"+result.toString());
//        } finally {
//            try {
//                is.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    	
//        return value;
    	
    	return getProperties(key,"LONGiConfigInfo/Interface.properties");
    }

    public static String readYZProperties(String key) {
        return getProperties(key,"LONGiConfigInfo/YZFCS.properties");
    }
    public static String getProperties(String key,String fileName) {
    	String value = "";
        InputStream is = null;
    	try {
//			props.load(new InputStreamReader(LONGiPropertiesUtil.class
//			      .getClassLoader().getResourceAsStream("LONGiConfigInfo/Interface.properties"), "UTF-8"));
    		
    		is = LONGiPropertiesUtil.class.getClassLoader().getResourceAsStream(fileName);
            Properties p = new Properties();
            p.load(is);
            value = p.getProperty(key);
		} catch (IOException e) {
            e.printStackTrace();
            final Writer result = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(result);
			e.fillInStackTrace().printStackTrace(printWriter);
            logger.error("加载properties失败"+fileName+":"+result.toString());
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    	
        return value;
    }
    public static void main(String[] args) {
//		System.out.println(new LONGiPropertiesUtil().getClass().getResourceAsStream("LONGiConfigInfo/Interface.properties"));
    	readInterfaceProperties("11111");
	}
}
