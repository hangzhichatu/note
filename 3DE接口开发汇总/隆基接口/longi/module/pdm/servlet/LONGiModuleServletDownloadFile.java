package longi.module.pdm.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import longi.module.pdm.restService.LONGiModuleProductPackageDownloadBase;

import java.io.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;  
import javax.servlet.http.HttpServlet;  
import javax.servlet.http.HttpServletRequest;  
import javax.servlet.http.HttpServletResponse;

/**
* Servlet implementation class ServletDownload
*/  
@WebServlet(asyncSupported = true, urlPatterns = { "/ServletDownload" })  
public class LONGiModuleServletDownloadFile extends HttpServlet {
    private final static Logger logger = LoggerFactory.getLogger(LONGiModuleServletDownloadFile.class);
   private static final long serialVersionUID = 1L;
       
   /**
    * @see HttpServlet#HttpServlet()
    */ 
   
   public LONGiModuleServletDownloadFile() {
       super();  
       // TODO Auto-generated constructor stub  
   }  
 
   /**
    * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
    */  
   protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {  
       // TODO Auto-generated method stub  
         
       //获得请求文件名  
       String fileName = request.getParameter("fileName");
       String filePath = request.getParameter("filePath");

       logger.info("filename");
       System.out.println(fileName);
         
       //设置文件MIME类型  
       response.setContentType(getServletContext().getMimeType(fileName));
       //设置Content-Disposition  
       response.setHeader("Content-Disposition", "attachment;fileName="+new String(fileName.getBytes("GBK"),"ISO8859_1"));
       //读取目标文件，通过response将目标文件写到客户端  
       //获取目标文件的绝对路径
       //读取文件
       InputStream in = new FileInputStream(filePath);
       OutputStream out = response.getOutputStream();  
         
       //写文件  
       int b;  
       while((b=in.read())!= -1)  
       {  
           out.write(b);  
       }  
         
       in.close();  
       out.close();
   }  
 
   /**
    * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
    */  
   protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {  
       // TODO Auto-generated method stub  
   }


 
}