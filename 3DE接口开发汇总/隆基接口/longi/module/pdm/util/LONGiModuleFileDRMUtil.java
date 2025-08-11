package longi.module.pdm.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.net.drm.edi.client.DrmAgent;
import cn.net.drm.edi.client.DrmAgentInf;

/**
 * @ClassName: LONGiModuleFileDRMUtil
 * @Description: TODO 加密软件工具类
 * @author: Longi.suwei
 * @date: Jul 30, 2020 8:44:04 PM
 */
public class LONGiModuleFileDRMUtil {
	private final static Logger logger = LoggerFactory.getLogger(LONGiModuleFileDRMUtil.class);

	public static void main(String[] args) {

		DrmAgentInf Client = null;
		try {
			Client = DrmAgent.getInstance();
			decryptFolder(Client, "G:\\98_download\\apache-ant-1.10.7\\", "G:\\98_download\\DRM\\");
//            File file = new File("G:\\98_download\\201017.PDM系统快速方案输出模块问题跟踪清单 V01.xlsx");
//
//            File outFile = new File("D:\\suwei3\\Desktop\\TEMP\\SpinnerTypeData_LongiALL_output.xls");
//            String toDecrytPath = "G:\\98_download\\";
//            //decryptFileStream(Client,file,outFile);
//            decryptInputStream(Client, file, toDecrytPath,"201017.PDM系统快速方案输出模块问题跟踪清单 V01解密.xlsx");

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Client.close();
		}
	}

	/**
	 * @Author: Longi.suwei
	 * @Title: decryptInputStream
	 * @Description: TODO
	 * @param: @param Client 加密客户端对象
	 * @param: @param file 待解密的文件对象
	 * @param: @param toFilePath 解密后的文件路径
	 * @date: Jul 30, 2020 8:44:16 PM
	 */
	public static void decryptInputStream(DrmAgentInf Client, File file, String toFilePath, String toFileName) {
		InputStream is = null;
		try {
			FileInputStream underlyingStream = new FileInputStream(file);
			boolean isEncry = Client.isEncrypted(file);
			if (isEncry) {
				logger.info(file.getName() + ":是否是加密的文件=" + isEncry);
				BufferedInputStream bufInputStream = new BufferedInputStream(underlyingStream);
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				OutputStream os = null;
				os = Client.decrypt(bos);
				int len = 0;
				byte[] buffer = new byte[1024];
				while ((len = bufInputStream.read(buffer)) > 0) {
					os.write(buffer, 0, len);
				}
				os.close();
				is = new ByteArrayInputStream(bos.toByteArray());

				File toFile = new File(toFilePath);

				if (!toFile.exists()) {
					toFile.mkdirs();
				}
				FileOutputStream fos = new FileOutputStream(toFilePath + File.separator + toFileName);
				IOUtils.copy(is, fos);
				fos.flush();
				fos.close();
				bufInputStream.close();
				bos.close();
			} else {
				FileUtils.copyFile(file, new File(toFilePath + File.separator + toFileName));
			}
		} catch (Exception e) {
			System.out.println("拷贝文件异常");
			logger.info("拷贝文件异常：" + e.toString());
			e.printStackTrace();
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Client.close();
			System.out.println("is Has Closed");
		}
	}

	/**
	 * @Author: Longi.suwei
	 * @Title: decryptFolder 解密整个文件夹的方案
	 * @Description: TODO
	 * @param: @param sourcePath
	 * @param: @param drmPath
	 * @date: Oct 19, 2020 1:46:33 PM
	 */
	public static void decryptFolder(DrmAgentInf Client, String sourcePath, String drmPath) {
		logger.info("解密原始目录：" + sourcePath + "----解密后目录：" + drmPath);
		File folder = new File(sourcePath);
		File drmFolder = new File(drmPath);
		if (!drmFolder.exists()) {
			drmFolder.mkdir();
		}
		File[] subFiles = folder.listFiles();
		for (int i = 0; i < subFiles.length; i++) {
			File subFile = subFiles[i];
			String subFileName = subFile.getName();
			if (subFile.isDirectory()) {
				decryptFolder(Client, sourcePath + subFileName + File.separator, drmPath + subFileName + File.separator);
			} else {
				decryptInputStream(Client, subFile, drmPath, subFileName);
			}
		}
	}

//    public static void decryptFile(DrmAgentInf Client, File file) {
//        try {
//            if (Client.isEncrypted(file)) {
//                Client.decrypt(file);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

//    public static void decryptFileStream(DrmAgentInf Client, File file, File outFile) {
//        try {
//
//            FileInputStream fis = new FileInputStream(file);
//            OutputStream os = Client.decrypt(new FileOutputStream(outFile));
//            int len = 0;
//            byte[] buffer = new byte[1024];
//            while ((len = fis.read(buffer)) > 0) {
//                os.write(buffer, 0, len);
//            }
//            fis.close();
//            os.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
