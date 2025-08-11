package longi.common.util;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
* @ClassName: FileDirectoryUtil
* @Description: TODO 文件夹操作的工具类
* @author: Longi.suwei
* @date: 2021 Jul 7 13:48:26
*/
public class FileDirectoryUtil {
	private final static Logger logger = LoggerFactory.getLogger(FileDirectoryUtil.class);
    /**
    * @Author: Longi.suwei
    * @Title: doDeleteEmptyDir
    * @Description: TODO 删除空目录
    * @param: @param dir 将要删除的目录路径
    * @date: 2021 Jul 7 13:48:15
    */
    public static void doDeleteEmptyDir(String dir) {
        boolean success = (new File(dir)).delete();
        if (success) {
        	logger.info("删除文件夹成功: " + dir);
        } else {
        	logger.info("删除文件夹失败: " + dir);
        }
    }

    /**
    * @Author: Longi.suwei
    * @Title: deleteDir
    * @Description: TODO 递归删除目录下的所有文件及子目录下所有文件
    * @param: @param dir 将要删除的文件目录
    * @param: @return boolean Returns "true" if all deletions were successful.     *If a deletion fails, the method stops attempting to delete and returns "false".
    * @date: 2021 Jul 7 13:47:26
    */
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            //递归删除目录中的子目录下
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    logger.error("删除文件失败: " + dir.getAbsolutePath());
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除

        return dir.delete();
    }

    /**
     * 测试
     */
    public static void main(String[] args) {
        //doDeleteEmptyDir("new_dir1");
        String newDir2 = "D:\\suwei3\\Desktop\\TEMP\\Test";
        boolean success = deleteDir(new File(newDir2));
        if (success) {
            System.out.println("Successfully deleted populated directory: " + newDir2);
        } else {
            System.out.println("Failed to delete populated directory: " + newDir2);
        }
    }
}