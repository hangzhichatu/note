/**
  * Copyright 2022 bejson.com 
  */
package longi.common.pojo;
import java.util.List;

/**
 * Auto-generated: 2022-04-24 14:27:10
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
public class DMSCreteProcessBean {

    private String Type;
    private String ProcessInstID;
    private String ActivityInstID;
    private String ItemID;
    private String LibraryCode;
    private String FileServerRelativeUrl;
    private String ApplicantNumber;
    private String Opinion;
    private String FileCategory;
    private String FileCategoryWLWJ;
    private String FileType;
    private String SubFileType;
    private String BusinessDomain;
    private String FileSecurityLevel;
    private String FileName;
    private String Version;
    private String DocNo;
    private String RulesAndRegulationsLevel;
    private String ProcessPriority;
    //private List<String> FileRange;
    private String FileRange;
    private String FileTemplate;
    private String FileProperty;
    private String NeedGenerateFile;
    private MainFile MainFile;
    private String IsDocRef;
    private List<RefFiles> RefFiles;
    private String IsDocRel;
    private List<RelFiles> RelFiles;
    private List<Attachment> Attachment;
    private List<AttachTable> AttachTable;
    private String FileSummary;
    private String ContentDescription;
    private String Keyword;
    private List<ResponsibleDept> ResponsibleDept;
    private List<ViewRights> ViewRights;
    private List<TeamLeader> TeamLeader;
    private List<ParallelSignOff> ParallelSignOff;

    private List<ReviewFiles> ReviewFiles;

    public void setReviewFiles(List<ReviewFiles> reviewFiles) {
        ReviewFiles = reviewFiles;
    }

    public List<ReviewFiles> getReviewFiles() {
        return ReviewFiles;
    }


    //是否线下评审
    private String OfflineReviewOrNot;
    public String getOfflineReviewOrNot() {
        return OfflineReviewOrNot;
    }

    public void setOfflineReviewOrNot(String offlineReviewOrNot) {
        OfflineReviewOrNot = offlineReviewOrNot;
    }
    private String IsTemporaryFile;
    private String TemporaryFileTermOfMonth;
    public void setType(String Type) {
         this.Type = Type;
     }
     public String getType() {
         return Type;
     }

    public void setProcessInstID(String ProcessInstID) {
         this.ProcessInstID = ProcessInstID;
     }
     public String getProcessInstID() {
         return ProcessInstID;
     }

    public void setActivityInstID(String ActivityInstID) {
         this.ActivityInstID = ActivityInstID;
     }
     public String getActivityInstID() {
         return ActivityInstID;
     }

    public void setItemID(String ItemID) {
         this.ItemID = ItemID;
     }
     public String getItemID() {
         return ItemID;
     }

    public void setLibraryCode(String LibraryCode) {
         this.LibraryCode = LibraryCode;
     }
     public String getLibraryCode() {
         return LibraryCode;
     }

    public void setFileServerRelativeUrl(String FileServerRelativeUrl) {
         this.FileServerRelativeUrl = FileServerRelativeUrl;
     }
     public String getFileServerRelativeUrl() {
         return FileServerRelativeUrl;
     }

    public void setApplicantNumber(String ApplicantNumber) {
         this.ApplicantNumber = ApplicantNumber;
     }
     public String getApplicantNumber() {
         return ApplicantNumber;
     }

    public void setOpinion(String Opinion) {
         this.Opinion = Opinion;
     }
     public String getOpinion() {
         return Opinion;
     }

    public void setFileCategory(String FileCategory) {
         this.FileCategory = FileCategory;
     }
     public String getFileCategory() {
         return FileCategory;
     }

    public void setFileCategoryWLWJ(String FileCategoryWLWJ) {
         this.FileCategoryWLWJ = FileCategoryWLWJ;
     }
     public String getFileCategoryWLWJ() {
         return FileCategoryWLWJ;
     }

    public void setFileType(String FileType) {
         this.FileType = FileType;
     }
     public String getFileType() {
         return FileType;
     }

    public void setSubFileType(String SubFileType) {
         this.SubFileType = SubFileType;
     }
     public String getSubFileType() {
         return SubFileType;
     }

    public void setBusinessDomain(String BusinessDomain) {
         this.BusinessDomain = BusinessDomain;
     }
     public String getBusinessDomain() {
         return BusinessDomain;
     }

    public void setFileSecurityLevel(String FileSecurityLevel) {
         this.FileSecurityLevel = FileSecurityLevel;
     }
     public String getFileSecurityLevel() {
         return FileSecurityLevel;
     }

    public void setFileName(String FileName) {
         this.FileName = FileName;
     }
     public String getFileName() {
         return FileName;
     }

    public void setVersion(String Version) {
         this.Version = Version;
     }
     public String getVersion() {
         return Version;
     }

    public void setDocNo(String DocNo) {
         this.DocNo = DocNo;
     }
     public String getDocNo() {
         return DocNo;
     }

    public void setRulesAndRegulationsLevel(String RulesAndRegulationsLevel) {
         this.RulesAndRegulationsLevel = RulesAndRegulationsLevel;
     }
     public String getRulesAndRegulationsLevel() {
         return RulesAndRegulationsLevel;
     }

    public void setProcessPriority(String ProcessPriority) {
         this.ProcessPriority = ProcessPriority;
     }
     public String getProcessPriority() {
         return ProcessPriority;
     }


    public String getFileRange() {
		return FileRange;
	}
	public void setFileRange(String fileRange) {
		FileRange = fileRange;
	}
	/* public List<String> getFileRange() {
		return FileRange;
	}
	public void setFileRange(List<String> fileRange) {
		FileRange = fileRange;
	}*/
	public void setFileTemplate(String FileTemplate) {
         this.FileTemplate = FileTemplate;
     }
     public String getFileTemplate() {
         return FileTemplate;
     }

    public void setFileProperty(String FileProperty) {
         this.FileProperty = FileProperty;
     }
     public String getFileProperty() {
         return FileProperty;
     }

    public void setNeedGenerateFile(String NeedGenerateFile) {
         this.NeedGenerateFile = NeedGenerateFile;
     }
     public String getNeedGenerateFile() {
         return NeedGenerateFile;
     }

    public void setMainFile(MainFile MainFile) {
         this.MainFile = MainFile;
     }
     public MainFile getMainFile() {
         return MainFile;
     }

    public void setIsDocRef(String IsDocRef) {
         this.IsDocRef = IsDocRef;
     }
     public String getIsDocRef() {
         return IsDocRef;
     }

    public void setRefFiles(List<RefFiles> RefFiles) {
         this.RefFiles = RefFiles;
     }
     public List<RefFiles> getRefFiles() {
         return RefFiles;
     }

    public void setIsDocRel(String IsDocRel) {
         this.IsDocRel = IsDocRel;
     }
     public String getIsDocRel() {
         return IsDocRel;
     }

    public void setRelFiles(List<RelFiles> RelFiles) {
         this.RelFiles = RelFiles;
     }
     public List<RelFiles> getRelFiles() {
         return RelFiles;
     }

    public void setAttachment(List<Attachment> Attachment) {
         this.Attachment = Attachment;
     }
     public List<Attachment> getAttachment() {
         return Attachment;
     }

    public void setAttachTable(List<AttachTable> AttachTable) {
         this.AttachTable = AttachTable;
     }
     public List<AttachTable> getAttachTable() {
         return AttachTable;
     }

    public void setFileSummary(String FileSummary) {
         this.FileSummary = FileSummary;
     }
     public String getFileSummary() {
         return FileSummary;
     }

    public void setContentDescription(String ContentDescription) {
         this.ContentDescription = ContentDescription;
     }
     public String getContentDescription() {
         return ContentDescription;
     }

    public void setKeyword(String Keyword) {
         this.Keyword = Keyword;
     }
     public String getKeyword() {
         return Keyword;
     }

    public void setResponsibleDept(List<ResponsibleDept> ResponsibleDept) {
         this.ResponsibleDept = ResponsibleDept;
     }
     public List<ResponsibleDept> getResponsibleDept() {
         return ResponsibleDept;
     }

    public void setViewRights(List<ViewRights> ViewRights) {
         this.ViewRights = ViewRights;
     }
     public List<ViewRights> getViewRights() {
         return ViewRights;
     }

    public void setTeamLeader(List<TeamLeader> TeamLeader) {
         this.TeamLeader = TeamLeader;
     }
     public List<TeamLeader> getTeamLeader() {
         return TeamLeader;
     }

    public void setParallelSignOff(List<ParallelSignOff> ParallelSignOff) {
         this.ParallelSignOff = ParallelSignOff;
     }
     public List<ParallelSignOff> getParallelSignOff() {
         return ParallelSignOff;
     }

     public void setIsTemporaryFile(String IsTemporaryFile) {
         this.IsTemporaryFile = IsTemporaryFile;
     }
     public String getIsTemporaryFile() {
         return IsTemporaryFile;
     }
     
     public void setTemporaryFileTermOfMonth(String TemporaryFileTermOfMonth) {
         this.TemporaryFileTermOfMonth = TemporaryFileTermOfMonth;
     }
     public String getTemporaryFileTermOfMonth() {
         return TemporaryFileTermOfMonth;
     }
     
}