package longi.bpm.util;



import com.itextpdf.text.log.Logger;
import com.itextpdf.text.log.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Properties;

public class LGiM_Util {
    private static final Logger logger = LoggerFactory.getLogger(LGiM_Util.class);
    //在不声明FileHandler并设置其为输出路径的话，logger.info("xin",true),会把信息输出到控制台或者其它默认输出位置。

    /**
     *
     * @param key
     * @return KeyValue
     * @author zhenghang
     * @date 2024/8/26
     * @Title getConfigFileWithkey
     * @Description:从配置文件获取信息
     * @throws Exception
     */
    public static String getConfigFileWithkey(String key) throws Exception{
        String resourcePath = LGiM_Util.class.getResource("/").getPath();
        System.out.println("resource path------>"+resourcePath);
        String configPath = resourcePath+"LGiM_BPM.properties";
        if(configPath.contains("Program")){
            configPath = configPath.replace("%20"," ");
        }

        BufferedReader bufferedReader = new BufferedReader(new FileReader(configPath));
        Properties readp = new Properties();
        readp.load(bufferedReader);
        System.out.println("读取配置文件路径---"+configPath);
        System.out.println("配置文件key--"+key);
        return readp.getProperty(key)==null?"":readp.getProperty(key);
    }
}
