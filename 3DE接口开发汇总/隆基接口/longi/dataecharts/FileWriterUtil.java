package longi.dataecharts;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
public class FileWriterUtil {

        //然后通过取其父目录获得resources目录，设置上传文件的目录
	public static void writeFileErp(String filename, String sb, String fileDir) throws IOException {
        String uploadFileSavePath = System.getProperty("user.dir")
                + File.separator + "dbData/"+fileDir+"/";
        File uploadFileSaveDir = new File(uploadFileSavePath);
        if (!uploadFileSaveDir.exists()) {
            // 递归生成文件夹
            uploadFileSaveDir.mkdirs();
        }
        File f = new File(uploadFileSaveDir.getAbsolutePath() + File.separator + filename);
        BufferedWriter output = new BufferedWriter(new FileWriter(f, false));
        output.write(sb);
        output.flush();
        output.close();
    }

    public static void writeFileWms(String filename, String sb, String fileDir) throws IOException {
        //然后通过取其父目录获得resources目录，设置上传文件的目录
        String uploadFileSavePath = System.getProperty("user.dir")
                + File.separator + "dbData/"+fileDir+"/";
        File uploadFileSaveDir = new File(uploadFileSavePath);
        if (!uploadFileSaveDir.exists()) {
            // 递归生成文件夹
            uploadFileSaveDir.mkdirs();
        }
        File f = new File(uploadFileSaveDir.getAbsolutePath() + File.separator + filename);
        BufferedWriter output = new BufferedWriter(new FileWriter(f, false));
        output.write(sb);
        output.flush();
        output.close();
    }

    public static void writeFilePLMDataCode(String filename, String sb) throws IOException {
        //然后通过取其父目录获得resources目录，设置上传文件的目录
        String uploadFileSavePath = System.getProperty("user.dir")
                + File.separator + "dbData/PLMDataCode/";
        System.out.println("uploadFileSavePath==========="+uploadFileSavePath);
        File uploadFileSaveDir = new File(uploadFileSavePath);
        if (!uploadFileSaveDir.exists()) {
            // 递归生成文件夹
            uploadFileSaveDir.mkdirs();
        }
        File f = new File(uploadFileSaveDir.getAbsolutePath() + File.separator + filename);
        BufferedWriter output = new BufferedWriter(new FileWriter(f, false));
        output.write(sb);
        output.flush();
        output.close();
    }

    public static JSONArray parseFile(String fileName,String flag) {
        try {
        	
        	Jedis jedis = new Jedis(longi.common.constants.GlobalConfig.REDIS_SERVER_IP);
        	jedis.auth(longi.common.constants.GlobalConfig.REDIS_SERVER_PASSWORD);
        	
            return JSON.parseArray(jedis.get(fileName));

            /*String path =  System.getProperty("user.dir")
                    + File.separator + "dbData/";
            File file = new File(path+flag+"/"+fileName);
            if (file.isFile() && file.exists() && file.canRead()) {
                String encoding = "UTF-8";
                InputStreamReader in;
                in = new InputStreamReader(new FileInputStream(file), encoding);
                BufferedReader bufferedReader = new BufferedReader(in);
                String lineTxt = "";
                StringBuilder sb = new StringBuilder(lineTxt);
                while ((lineTxt = bufferedReader.readLine()) != null) {
                    if (!lineTxt.trim().equals("")) {
                        sb.append(lineTxt);
                    }
                }
                lineTxt = sb.toString();
                in.close();
                return JSON.parseArray(lineTxt);
            }*/
        } catch (Exception e) {
        	e.printStackTrace();
           //log.error("读取DB文件异常",e);
        }
        return null;
    }

    public static JSONObject parseFile2(String fileName, String flag) {
        try {
        	
        	System.out.println(">>>fileName======"+fileName);
        	Jedis jedis = new Jedis(longi.common.constants.GlobalConfig.REDIS_SERVER_IP);
        	jedis.auth(longi.common.constants.GlobalConfig.REDIS_SERVER_PASSWORD);
        	
        	
            /*String path =  System.getProperty("user.dir")
                    + File.separator + "dbData/";
          
            File file = new File(path+flag+"/"+fileName);
        	
            if (file.isFile() && file.exists() && file.canRead()) {
            	System.out.println("YYYYYYYYYYYYY");
                String encoding = "UTF-8";
                InputStreamReader in;
                in = new InputStreamReader(new FileInputStream(file), encoding);
                BufferedReader bufferedReader = new BufferedReader(in);
                String lineTxt = "";
                StringBuilder sb = new StringBuilder(lineTxt);
                while ((lineTxt = bufferedReader.readLine()) != null) {
                    if (!lineTxt.trim().equals("")) {
                        sb.append(lineTxt);
                    }
                }
                lineTxt = sb.toString();
                in.close();
               
            }*/
            return JSON.parseObject(jedis.get(fileName));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static String getFileNameMonth(){
        return DateTimeFormatter.ofPattern("yyyy-MM").format(
                LocalDate.now().minusMonths(1));
    }


}
