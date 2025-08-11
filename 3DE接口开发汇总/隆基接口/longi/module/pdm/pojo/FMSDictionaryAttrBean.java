package longi.module.pdm.pojo;
public class FMSDictionaryAttrBean
{
    private String ID;

    private String Code;

    private String Name;

    private String ParentID;

    private String IDPath;

    private String NamePath;

    private int DeepLevel;

    private int Sort;

    private boolean IsDel;

    private boolean IsDisable;

    private String CreatedDate;

    private String CreatedBy;

    private String UpdatedDate;

    private String UpdatedBy;

    private String OrgRange;

    private String Children;

    private String OrgRangeIDs;

    public void setID(String ID){
        this.ID = ID;
    }
    public String getID(){
        return this.ID;
    }
    public void setCode(String Code){
        this.Code = Code;
    }
    public String getCode(){
        return this.Code;
    }
    public void setName(String Name){
        this.Name = Name;
    }
    public String getName(){
        return this.Name;
    }
    public void setParentID(String ParentID){
        this.ParentID = ParentID;
    }
    public String getParentID(){
        return this.ParentID;
    }
    public void setIDPath(String IDPath){
        this.IDPath = IDPath;
    }
    public String getIDPath(){
        return this.IDPath;
    }
    public void setNamePath(String NamePath){
        this.NamePath = NamePath;
    }
    public String getNamePath(){
        return this.NamePath;
    }
    public void setDeepLevel(int DeepLevel){
        this.DeepLevel = DeepLevel;
    }
    public int getDeepLevel(){
        return this.DeepLevel;
    }
    public void setSort(int Sort){
        this.Sort = Sort;
    }
    public int getSort(){
        return this.Sort;
    }
    public void setIsDel(boolean IsDel){
        this.IsDel = IsDel;
    }
    public boolean getIsDel(){
        return this.IsDel;
    }
    public void setIsDisable(boolean IsDisable){
        this.IsDisable = IsDisable;
    }
    public boolean getIsDisable(){
        return this.IsDisable;
    }
    public void setCreatedDate(String CreatedDate){
        this.CreatedDate = CreatedDate;
    }
    public String getCreatedDate(){
        return this.CreatedDate;
    }
    public void setCreatedBy(String CreatedBy){
        this.CreatedBy = CreatedBy;
    }
    public String getCreatedBy(){
        return this.CreatedBy;
    }
    public void setUpdatedDate(String UpdatedDate){
        this.UpdatedDate = UpdatedDate;
    }
    public String getUpdatedDate(){
        return this.UpdatedDate;
    }
    public void setUpdatedBy(String UpdatedBy){
        this.UpdatedBy = UpdatedBy;
    }
    public String getUpdatedBy(){
        return this.UpdatedBy;
    }
    public void setOrgRange(String OrgRange){
        this.OrgRange = OrgRange;
    }
    public String getOrgRange(){
        return this.OrgRange;
    }
    public void setChildren(String Children){
        this.Children = Children;
    }
    public String getChildren(){
        return this.Children;
    }
    public void setOrgRangeIDs(String OrgRangeIDs){
        this.OrgRangeIDs = OrgRangeIDs;
    }
    public String getOrgRangeIDs(){
        return this.OrgRangeIDs;
    }
}