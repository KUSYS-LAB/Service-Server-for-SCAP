package ac.kr.korea.cdm.serviceserver.constants;

import java.sql.Timestamp;

public class ProjectRun {
    String filename;
    String outputfolder;
    Integer project_id;
    Integer file_id;
    Timestamp request_time;

    public ProjectRun(String filename, String outputfolder, Integer project_id, Integer file_id, Timestamp request_time) {
        this.filename = filename;
        this.outputfolder = outputfolder;
        this.project_id = project_id;
        this.file_id = file_id;
        this.request_time = request_time;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getOutputfolder() {
        return outputfolder;
    }

    public void setOutputfolder(String outputfolder) {
        this.outputfolder = outputfolder;
    }

    public Integer getProject_id() {
        return project_id;
    }

    public void setProject_id(Integer project_id) {
        this.project_id = project_id;
    }

    public Integer getFile_id() {
        return file_id;
    }

    public void setFile_id(Integer file_id) {
        this.file_id = file_id;
    }

    public Timestamp getRequest_time() {
        return request_time;
    }

    public void setRequest_time(Timestamp request_time) {
        this.request_time = request_time;
    }
}
