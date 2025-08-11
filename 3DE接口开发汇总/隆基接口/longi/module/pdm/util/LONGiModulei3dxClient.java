package longi.module.pdm.util;

// Java standard io/javax library
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.Json;


public class LONGiModulei3dxClient
{
    public final static String ENCODING = "UTF-8";// default encoding

    private static LONGiModuleURLLoader _URL_loader	= null;// unique storage of CAAURLLoader instance
    private String _space3ds_URL		    = null;// 3DSpace Server URL
    private String _pass3ds_URL	            = null;// 3DPassport Server URL
    private String _output_dir				= null;// output directory for traces

    /**
     * CAAi3dxClient constructor
     * @throws Exception
     */
    public LONGiModulei3dxClient(String space3ds_URL,
                         String pass3ds_URL,
                         String output_dir){
        _space3ds_URL = space3ds_URL;
        _pass3ds_URL = pass3ds_URL;
        try {
            _URL_loader = new LONGiModuleURLLoader();
        } catch (Throwable t) {
            System.err.println(t.getMessage());
            t.printStackTrace();
        }
        _output_dir	= output_dir;
    }

    /**
     * getURLLoader default getter
     */
    public LONGiModuleURLLoader getURLLoader() {
        return _URL_loader;
    }

    /**
     * getter
     * @return	3DSwym server
     */
    public String get3DSpaceServer() {
        return _space3ds_URL ;
    }

    /**
     * getter
     * @return	passport server
     */
    public String getPassportServer() {
        return _pass3ds_URL ;
    }

    /**
     * default getter
     */
    public String getOut() {
        return _output_dir;
    }

    /**
     * Casts a byte array to a JsonObject.
     * @param	byte array
     * @return	a JsonObject
     *
     */
    public static final JsonObject getJSON(byte[] b) {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        InputStreamReader isr = null;
		try {
			isr = new InputStreamReader(bais, ENCODING);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        JsonReader jsonReader=Json.createReader(isr);
        JsonObject jsonObj = jsonReader.readObject();
        jsonReader.close();

        return jsonObj;
    }
}