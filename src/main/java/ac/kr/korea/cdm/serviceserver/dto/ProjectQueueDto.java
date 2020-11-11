package ac.kr.korea.cdm.serviceserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ProjectQueueDto {
    private int id;
    private int analysisProjectId;
    private int analysisFileId;
    private String userName;
    private Timestamp requestTime;
    private String dirUnzip;
    private String dirOutput;
    private String status;
    private String sk;
    private String iv;
    private boolean approvedToRun;
    private boolean approvedToRead;

    public ProjectQueueDto(int analysisProjectId, int analysisFileId, String userName, Timestamp requestTime, String dirUnzip, String dirOutput, String status) {
        this.analysisProjectId = analysisProjectId;
        this.analysisFileId = analysisFileId;
        this.userName = userName;
        this.requestTime = requestTime;
        this.dirUnzip = dirUnzip;
        this.dirOutput = dirOutput;
        this.status = status;
    }

}
