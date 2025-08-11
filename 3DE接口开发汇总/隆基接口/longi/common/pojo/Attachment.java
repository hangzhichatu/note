/**
  * Copyright 2022 bejson.com 
  */
package longi.common.pojo;

/**
 * Auto-generated: 2022-04-24 14:27:10
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
public class Attachment {

    private String PLMKey;
    private String IsChanged;
    private String IsUpgrade;
  
	private String UpgradeList;
    private String Name;
    private String No;
    private String Version;
    
    private String Filetype;
    private String PlMFileMemo;
    public String getFiletype() {
		return Filetype;
	}
	public void setFiletype(String filetype) {
		Filetype = filetype;
	}
	public void setPLMKey(String PLMKey) {
         this.PLMKey = PLMKey;
     }
     public String getPLMKey() {
         return PLMKey;
     }

    public void setIsChanged(String IsChanged) {
         this.IsChanged = IsChanged;
     }
     public String getIsChanged() {
         return IsChanged;
     }
     public String getIsUpgrade() {
 		return IsUpgrade;
 	}
 	public void setIsUpgrade(String isUpgrade) {
 		IsUpgrade = isUpgrade;
 	}
    public void setUpgradeList(String UpgradeList) {
         this.UpgradeList = UpgradeList;
     }
     public String getUpgradeList() {
         return UpgradeList;
     }
     public void setName(String Name) {
         this.Name = Name;
     }
     public String getName() {
         return Name;
     }
     
     
    public void setNo(String No) {
         this.No = No;
     }
     public String getNo() {
         return No;
     }

    public void setVersion(String Version) {
         this.Version = Version;
     }
     public String getVersion() {
         return Version;
     }
	public String getPlMFileMemo() {
		return PlMFileMemo;
	}
	public void setPlMFileMemo(String plMFileMemo) {
		PlMFileMemo = plMFileMemo;
	}

}