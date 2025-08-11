package longi.bpm.util;
import matrix.db.Context;
import matrix.db.JPOSupport;


//返回admin_platform 上下文
public class LGiM_ContextUtil {
    public static Context getContext() throws Exception{
        System.out.println("getInto");
        //String url = "3de网址";//需要需改成读取配置文件 - 获取网址
        String url = LGiM_Util.getConfigFileWithkey("3de.url");// 获取网址
        //String user = "admin_platform"; //需要需改成读取配置文件 - 获取用户名称
        String user = LGiM_Util.getConfigFileWithkey("3de.admin.user");// 获取用户名称
        //String pass = "Aa123456"; //需要需改成读取配置文件 - 获取用户密码
        String pass = LGiM_Util.getConfigFileWithkey("3de.admin.pass"); //需要需改成读取配置文件 - 获取用户密码
        //String role = "ctx::VPLMAdmin.Company Name.Default";
        String role = LGiM_Util.getConfigFileWithkey("3de.role.admin");//获取用户密码

        System.out.println("Url="+url);
        System.out.println("User="+user);
        System.out.println("pass="+pass);
        System.out.println("role="+role);

        //Context context = new Context("https://plmsiliconwaferdev.longi.com/internal");
        Context context = new Context(url);
        //context.setUser("admin_platform");
        context.setUser(user);
       // context.setPassword("Gp123456");
        context.setPassword(pass);
        context.setLanguage("zh_cn");
        context.setVault("eService Production");
        //context.setRole(role);

        context.connect();
        //注册线程,不添加DomainConstants初始化会报错
        JPOSupport.registerThread(context);
        return  context;

    }
}
