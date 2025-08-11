package longi.bpm.util;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LGiM_IntegrationUtil {
    protected  static Logger logger = Logger.getLogger(LGiM_IntegrationUtil.class.getName());
    private static FileHandler fh;

    /**
     * @author zhenghang
     * @Title log
     * @param logger
     * @date 2024/8/26
     * @Description:记录接口日志
     */
    public static void log(Logger logger){
        try{
            //获取接口路径
            String logPath = LGiM_Util.getConfigFileWithkey("BPM.logger.path");//此处需要将获取路径的方法设置成读取配置文件

            // 根据当前日期生成文件名
            String fileName = logPath + "LGiM_3DEInterfaceLog."+ new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            //创建FileHandler,如果当前日志文件不存在则自动创建
            fh=new FileHandler(fileName,true);//第二个参数选择false的话会覆盖原有日志
            fh.setFormatter(new SimpleFormatter());
            logger.addHandler(fh);

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
