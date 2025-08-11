package longi.module.pdm.util;


// Java standard io library
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

// Java standard net library
import java.net.URL;
import java.net.URI;
import java.net.CookieManager;
import java.net.CookieHandler;

// Java standard javax.net.ssl library
import javax.net.ssl.HttpsURLConnection;

// For certificate
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

// Java standard util library
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class LONGiModuleURLLoader
{
    private static String	_SecurityContext	= null;// - For ENOVIA SecurityContext
    private static String	_content_type		= null;// - Header of the response, Content-Type field
    private static int   	_response_code		= 0   ;// - Response code last call
    private static String   _response_msg	    = null;// - Response msg last call
    private static URL		_last_redirect_url	= null;// - On redirection

    /**
     * default getter
     * @return content type of last URL loaded, null if none has been load
     */
    public String	getContentTypeFromHeader()	{
        return _content_type;
    }

    /**
     * default getter
     * @return content type of last URL loaded, null if none has been load
     */
    public int	getResponseCode()	{
        return _response_code;
    }

    /**
     * default getter
     * @return content type of last URL loaded, null if none has been load
     */
    public String	getResponseMessage()	{
        return _response_msg;
    }

    /**
     * default setter
     */
    public void setSecurityContext( String SC)	{
        _SecurityContext=SC;
    }

    /**
     * default getter
     * @return Last known redirect URL, null if none has been set
     */
    public static URL getLastRedirectUrl() {
        return _last_redirect_url;
    }

    /**
     * Default constructor
     * @throws Exception
     */
    public LONGiModuleURLLoader() throws Exception {
        // - Manage Cookies
        CookieHandler.setDefault( new CookieManager( ) );
        // - Setup a UNIQUE User Agent. java/_version will be append at the end of the String
        System.setProperty("http.agent", "CAA URL Loader");

        // - Bypass certificate errors.
        String no_cert= System.getProperty("bypass.cert");
        if(no_cert != null && no_cert.equals("true")){
            System.out.println("");
            System.out.println("Bypassing certificates");
            System.out.println("");
            CheckingTrustManager tm = null;
            SSLContext context = SSLContext.getInstance("TLS");
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init((KeyStore) null);
            X509TrustManager defaultTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];
            tm = new CheckingTrustManager(defaultTrustManager);
            context.init(null, new TrustManager[] { tm }, null);
            SSLSocketFactory factory = context.getSocketFactory();
            HttpsURLConnection.setDefaultSSLSocketFactory(factory);
        }
    }

    /**
     * Load an URL and catch the response
     * @param url the URL to load.
     * @param method GET or POST
     * @param content_type mandatory when method is POST
     * @param post_data the data to send when using POST method
     * @return response body
     * @throws Exception
     */
    public byte[] loadUrl(String urlAsString, String method, String content_type, byte[] post_data) throws Exception {
        System.out.println("##CLIENT REQUEST");
        System.out.println("##------------------------------------------------------------");

        // Create the URL from the String
        URL url = new URL(urlAsString) ;
        System.out.println( "["+method+"] "+ url.toString() );

        // == == == == == == == == == == == == == == == == == == == == == == == == == == == == == ==
        // == STEP 1   ==
        // == Open the connection
        // - Set up connection
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        // - 3DSwym support automatic redirect
        connection.setInstanceFollowRedirects(true);

        // - Abort the request after 15 seconds
        connection.setConnectTimeout(15000);

        if ( method.equals("PATCH") ) {
            connection.setRequestProperty("X-HTTP-Method-Override", "PATCH");
            System.out.println("  [HEADER] X-HTTP-Method-Override : PATCH");
            connection.setRequestMethod("POST");
        }else {
            // - Add information to request header
            connection.setRequestMethod(method);
        }

        // == == == == == == == == == == == == == == == == == == == == == == == == == == == == == ==

        // == == == == == == == == == == == == == == == == == == == == == == == == == == == == == ==
        // == STEP 2   ==
        // == Request header

        if(_SecurityContext != null && (_SecurityContext.length() !=0) ) {
            connection.setRequestProperty("SecurityContext", _SecurityContext);
            System.out.println("  [HEADER] SecurityContext : "+_SecurityContext);
        }
        connection.setRequestProperty("Accept", "application/json");
        // == == == == == == == == == == == == == == == == == == == == == == == == == == == == == ==

        // == == == == == == == == == == == == == == == == == == == == == == == == == == == == == ==
        // == STEP 3   ==
        // == Post data
        // - If there is data to send, send it!
        if (post_data != null) {
            System.out.println("POST Data : "+new String(post_data));
            // - Tell connection we are going to send data
            connection.setDoOutput(true);
            // - Add POST information to request header
            connection.setRequestProperty("Content-Length", Integer.toString(post_data.length));
            connection.setRequestProperty("Content-Type", content_type);
            System.out.println("Content-Type=" + content_type);
            // - Send data
            OutputStream output = connection.getOutputStream();
            output.write(post_data);
            output.flush();
            output.close();
        }

        // == == == == == == == == == == == == == == == == == == == == == == == == == == == == == ==

        // == == == == == == == == == == == == == == == == == == == == == == == == == == == == == ==
        // == STEP 4   ==
        // == Catching response
        // - connection.getResponseCode() actually perform the request !!!
        System.out.println("");
        System.out.println("##SERVER RESPONSE ["+ connection.getResponseCode() +"] "+ connection.getResponseMessage());

        System.out.println("##--------------------");

        _response_code = connection.getResponseCode() ;
        _response_msg = connection.getResponseMessage();

        // - When InstanceFollowRedirects=true URL might have change due to redirect
        if(! connection.getURL().toString().equals( url.toString() ) ){
            _last_redirect_url= connection.getURL();
            System.out.println("");
            System.out.println("Has been redirected. Last Redirect URL : "+ _last_redirect_url);
        }

        // == == == == == == == == == == == == == == == == == == == == == == == == == == == == == ==

        // == == == == == == == == == == == == == == == == == == == == == == == == == == == == == ==
        // == STEP 5   ==
        // == Response header
        // - Response Header
        System.out.println("");
        System.out.println("#RESPONSE HEADER : ");
        System.out.println("#--------------------");
        System.out.println(connection.getHeaderField(0));
        for (int i = 1;; i++) {
            String header_name = connection.getHeaderFieldKey(i);
            String header_value = connection.getHeaderField(i);
            if (header_name == null && header_value == null)
                break;
            else
                System.out.println(header_name+": "+header_value);

            // - Content Type : for response analysis and casting purpose
            if ( header_name.equals("Content-Type") )
                _content_type= header_value;
        }

        // == == == == == == == == == == == == == == == == == == == == == == == == == == == == == ==

        // == == == == == == == == == == == == == == == == == == == == == == == == == == == == == ==
        // == STEP 6   ==
        // == Response body
        // - Read response body content
        InputStream input = null ;
        if(connection.getResponseCode() != 200 &&
                connection.getResponseCode() != 201 &&
                connection.getResponseCode() != 204 ) {
            input = connection.getErrorStream();
        } else {
            //DELETE no content
            if ( connection.getResponseCode() != 204 ) {
                input = connection.getInputStream();
            }
        }

        byte[] io_buffer = null ;
        if ( input != null ) {
            int read = 0;
            ByteArrayOutputStream ba = new ByteArrayOutputStream();
            io_buffer = new byte[0x10000];
            while ( ( read = input.read(io_buffer) ) >= 0 ) {
                if (read == 0)
                    break;
                ba.write(io_buffer, 0, read);
            }
            io_buffer = ba.toByteArray();
        }
        return io_buffer;
        // == == == == == == == == == == == == == == == == == == == == == == == == == == == == == ==
    }

    /**
     * Wrapper used to intercept the server certificate chain even in case of error
     */
    private static class CheckingTrustManager implements X509TrustManager {
        private final X509TrustManager _tm;

        CheckingTrustManager(X509TrustManager tm) {
            _tm = tm;
        }

        public X509Certificate[] getAcceptedIssuers() {
            return _tm.getAcceptedIssuers();
        }

        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            try {
                _tm.checkClientTrusted(chain, authType);
            } catch (Throwable t) {
                System.out.println("Catching certificate errors.");
            }
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            try {
                _tm.checkServerTrusted(chain, authType);
            } catch (Throwable t) {
                System.out.println("Catching certificate errors.");
            }
        }
    }
}