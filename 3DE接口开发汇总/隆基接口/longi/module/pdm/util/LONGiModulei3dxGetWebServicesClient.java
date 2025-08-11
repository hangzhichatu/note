package longi.module.pdm.util;

// Java standard io library
import java.io.PrintStream;

public class LONGiModulei3dxGetWebServicesClient
{
    private static LONGiModulei3dxClient _client;
    private static String        _App_Path ;

    /**
     * Default constructor
     */
    public LONGiModulei3dxGetWebServicesClient(LONGiModulei3dxClient client, String App_Path) {
        _client= client;
        _App_Path = App_Path;
    }

    /**
     * Get current user web service method
     * @return a JSON of the current user
     */
    public void Run( String SecurityContext ) throws Exception {
        // - Redirect System.out
        PrintStream output = null;
        /*
        PrintStream old_output = System.out;
        String output_dir= _client.getOut();
        if (output_dir != null) {
            output = new PrintStream(output_dir + "api_get_ws.traces", CAAi3dxClient.ENCODING);
            System.setOut(output);
        }
*/
        // - Set the security context
        _client.getURLLoader().setSecurityContext(SecurityContext);

        // - Build web service url
        String ws_url = _client.get3DSpaceServer() + _App_Path ;

        System.out.println("ws_url="+ws_url);
        // - Send query and catch response
        byte [] response= _client.getURLLoader().loadUrl(ws_url,"GET", null,null);

        
        //Display the response in the output file
        if ( response != null ) {
            System.out.println("#RESPONSE BODY "  );
            System.out.println("#--------------------");
            String response_type= _client.getURLLoader().getContentTypeFromHeader();
            if(response_type.indexOf("json") != -1) {
                System.out.println(LONGiModulei3dxClient.getJSON(response).toString());
            } else {
                System.out.println( "unhandle response Content-Type="+response_type) ;
            }
        }

        //If error, an exception is thrown with the backend code error
        if ( _client.getURLLoader().getResponseCode() != 200 ) {
            int HTTPCode=_client.getURLLoader().getResponseCode() ;
            String HTTPMsg=_client.getURLLoader().getResponseMessage() ;
            throw new Exception("WS Response Code="+ HTTPCode + "- " + HTTPMsg  );
        }

        // - Restore System.out
        /*
        if (output_dir != null) {
            output.flush();
            output.close();
            System.setOut(old_output);
        }
        */

    }
}