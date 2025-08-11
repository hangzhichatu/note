package longi.module.pdm.util;

/**
 *
 */
public class LONGiModuleMain {
	public static void main(String[] args) {
		String[] argStrings = {"https://dev.atoz.com/3dpassport/","admin_platform","Aa123456","https://dev.atoz.com/3dspace/",
				"ctx::VPLMProjectLeader.Company Name.Common Space","G:\\98_download\\R20.2.15","resources/longiPDM/restFul/User?roleSelect=Librarian"};
		connectToPassport(argStrings);
	}
    public static void connectToPassport(String[] args) {
        try {
        	
        	
            String pass3ds_server = args[0] ;
            String username	= args[1];
            String password	= args[2];
            String space3ds_server = args[3] ;
            String security_context = args[4] ;
            String output_dir = args[5] + "\\";
            String app_path = "resources/longiPDM/restFul/User?roleSelect=Librarian";
            if ( args.length >= 7 ) app_path = args[6] ;

            

            System.out.println("## Getting input arguments : ");
            System.out.println(" catch 3DPassport URL="+pass3ds_server);
            System.out.println(" catch user login="+  username);
            System.out.println(" catch user password="+ password);
            System.out.println(" catch 3DSpace URL="+space3ds_server);
            System.out.println(" catch Security Context (role.organization.collabspace) ="+security_context);
            System.out.println(" catch output directory ="+output_dir);
            System.out.println(" catch 3DSpace application path ="+app_path);
            System.out.println("## End Of arguments catching");
            System.out.println("");


            //for fiddler
            System.setProperty("proxyHost", "dev.atoz.com");
            System.setProperty("proxyPort", "8888");

            // - Setting up Clients
            LONGiModulei3dxClient client = new LONGiModulei3dxClient(space3ds_server,pass3ds_server,output_dir);
            LONGiModulei3dxLoginClient client_login = new LONGiModulei3dxLoginClient(client);
            LONGiModulei3dxGetWebServicesClient client_GetWS  = new LONGiModulei3dxGetWebServicesClient(client,app_path);

            // - [STEP 1] : Login
            // -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
            System.out.println("## Login");
            client_login.login(username, password);
            System.out.println("");
            // -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-

            // - [STEP 2] : 3DSpace web service call
            // -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
            System.out.println("## Consume 3DSpace Web Service");
            client_GetWS.Run(security_context) ;
            System.out.println("");
            // -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-

            // - [STEP 3] : Logout
            // -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
            System.out.println("## Logout");
            client_login.logout();
            // -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-

            System.exit(0);
        } catch (ArrayIndexOutOfBoundsException e) {
            String strHelp = "java com.dassault_systemes.caasamples.i3dxsamples.CAAi3dxMain ";
            strHelp += "3DPassportURL  login password  3DSpaceURL Security_Context (role.org.collspace) output_dir  [appl_path (GET ws)]" ;
            System.err.println("##[ERROR] Expected:\n" + strHelp );
        } catch (Throwable t) {
            System.err.println("##[ERROR] " + t.toString());
            //System.err.println("##[Callstack] ");
            //t.printStackTrace(System.err);
        } finally {
            System.err.flush();
            System.exit(1);
        }
    }
}
