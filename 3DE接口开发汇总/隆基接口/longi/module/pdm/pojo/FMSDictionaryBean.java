package longi.module.pdm.pojo;
import java.util.ArrayList;
import java.util.List;
public class FMSDictionaryBean
{
    private List<FMSDictionaryAttrBean> Data;

    private int Code;

    private String Msg;

    private int ServerTimeTimestamp;

    public void setData(List<FMSDictionaryAttrBean> Data){
        this.Data = Data;
    }
    public List<FMSDictionaryAttrBean> getData(){
        return this.Data;
    }
    public void setCode(int Code){
        this.Code = Code;
    }
    public int getCode(){
        return this.Code;
    }
    public void setMsg(String Msg){
        this.Msg = Msg;
    }
    public String getMsg(){
        return this.Msg;
    }
    public void setServerTimeTimestamp(int ServerTimeTimestamp){
        this.ServerTimeTimestamp = ServerTimeTimestamp;
    }
    public int getServerTimeTimestamp(){
        return this.ServerTimeTimestamp;
    }
}