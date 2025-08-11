package longi.module.pdm.util;

// Java standard io library
import java.io.PrintStream;

// Java standard net library
import java.net.URLEncoder;

public class LONGiModulei3dxLoginClient
{
    private LONGiModulei3dxClient _client;// common client toolbox

    /**
     * default constructor
     */
    public LONGiModulei3dxLoginClient(LONGiModulei3dxClient client) {
        _client = client;
    }

    /**
     * log in to 3DPassport
     */
    public void login(String username, String password) throws Exception {
        String response_str;
        byte [] response;

        // - Redirect System.out
        PrintStream output = null;
        PrintStream old_output = System.out;
        String output_dir= _client.getOut();
        if (output_dir != null) {
            output = new PrintStream(output_dir + "login.traces", LONGiModulei3dxClient.ENCODING);
            System.setOut(output);
        }

        // - Catch 3DPassport login web service URL from last redirect URL
        String passport3ds_url_str = _client.getPassportServer();

        // == == == == == == == == == == == == == == == == == == == == == == == == == == == == == ==
        // == STEP 1   Retrieve the login ticket ==
        // == GET <3DPassport server>/login?action=get_auth_params

        String passport3ds_lt_url_str = passport3ds_url_str + "/login?action=get_auth_params";
        // - Load URL
        response = _client.getURLLoader().loadUrl(passport3ds_lt_url_str,"GET",null,null);
        
        // - Display Response
        response_str = new String(response, LONGiModulei3dxClient.ENCODING);
        System.out.println("");
        System.out.println("#RESPONSE BODY");
        System.out.println("#--------------------");
        System.out.println(response_str.toString());

        // - Catch Login Ticket LT from response body
        String lt = response_str.substring(response_str.indexOf("lt")+5);
        lt = lt.substring(0, lt.indexOf("\""));
        System.out.println("");
        System.out.println(" --> Login Ticket : " + lt);
        System.out.println("");
        // == == == == == == == == == == == == == == == == == == == == == == == == == == == == == ==

        // == == == == == == == == == == == == == == == == == == == == == == == == == == == == == ==
        // == STEP 2   ==
        // == POST <3DPassport CAS server>/login
        // - Prepare data to send
        String post_data_str ;
        String usernameEnc= URLEncoder.encode(username,LONGiModulei3dxClient.ENCODING) ;
        String passwordEnc= URLEncoder.encode(password,LONGiModulei3dxClient.ENCODING) ;
        post_data_str = "lt=" + lt + "&username=" + usernameEnc + "&password=" + passwordEnc ;
        byte [] post_data = post_data_str.getBytes();
        // - Build 3DPassport login web service URL
        String space3ds_url_str = _client.get3DSpaceServer() + '/' ;
        String passport3ds_login_url_str= passport3ds_url_str + "/login?service=" + URLEncoder.encode(space3ds_url_str,LONGiModulei3dxClient.ENCODING) ;
        // - Load URL
        String encodageForm="application/x-www-form-urlencoded;charset=UTF-8" ;
        response= _client.getURLLoader().loadUrl(passport3ds_login_url_str, "POST", encodageForm, post_data);
        // - Display Response
        response_str = new String(response, LONGiModulei3dxClient.ENCODING);
        System.out.println("");
        System.out.println("#RESPONSE BODY");
        System.out.println("#--------------------");
        System.out.println(response_str.toString());
        // == == == == == == == == == == == == == == == == == == == == == == == == == == == == == ==

        //Pay attention the previous call can return 200 while the authentication has failed.
        int indexError= response_str.indexOf("error.authentication.credentials.bad");
        if (indexError > 0) {
            System.out.println("#--------------------");
            System.out.println("error.authentication.credentials.bad");
            throw new Exception("Login error: check login or password values" );
        }

        // - Restore System.out
        if (output_dir != null) {
            output.flush();
            output.close();
            System.setOut(old_output);
        }

    }

    /**
     * log out from 3DPassport
     */
    public void logout() throws Exception {
        // - Redirect System.out
        PrintStream output = null;
        PrintStream old_output = System.out;
        String output_dir= _client.getOut();
        if (output_dir != null) {
            output = new PrintStream(output_dir + "logout.traces", LONGiModulei3dxClient.ENCODING);
            System.setOut(output);
        }

        // - Remove the SecurityContext
        _client.getURLLoader().setSecurityContext(null);

        // - Execute Logout
        System.out.println("## logout:");
        String swym_logout = _client.getPassportServer() + "/logout" ;

        byte[] response = _client.getURLLoader().loadUrl(swym_logout,"GET",null,null);

        System.out.println("");
        System.out.println("#RESPONSE BODY");
        System.out.println("#--------------------");
        String response_str = new String(response, LONGiModulei3dxClient.ENCODING);
        System.out.println(response_str.toString());
        System.out.println("## End Of logout .");

        // - Restore System.out
        if (output_dir != null) {
            output.flush();
            output.close();
            System.setOut(old_output);
        }
    }
}