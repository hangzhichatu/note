package longi.bpm.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import longi.bpm.service.LGiM_IntergrationService;
import matrix.db.Context;

public class LGiM_IntergrationServiceimpl implements LGiM_IntergrationService {
    @Override
    public String getJPOServiceTest(Context context, String json) throws Exception {
        try{
            JSONObject jsonIn = JSON.parseObject(json);
            String returnMessange = "传入信息："+json;
            jsonIn.put("mess",returnMessange);
            return json.toString();
        }catch (Exception e){
            e.printStackTrace();
            return(e.getMessage());
        }

    }
}
