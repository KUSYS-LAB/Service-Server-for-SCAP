package ac.kr.korea.cdm.serviceserver.util;

import ac.kr.korea.cdm.serviceserver.constants.Constants;
import ac.kr.korea.cdm.serviceserver.dto.ProjectQueueDto;
import ac.kr.korea.cdm.serviceserver.service.ProjectQueueService;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.sql.Timestamp;


public class AnalyzingThread implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(AnalyzingThread.class);
    private String codeToRun;
    private int portNum;
    private String outfolder;
    private int project_id;
    private int file_id;
    private Timestamp request_time;

    private ProjectQueueService projectQueueService;

    public AnalyzingThread(String codeToRun, int portNum, String outfolder, int project_id, int file_id,Timestamp request_time ,ProjectQueueService projectQueueService) {
        this.projectQueueService = projectQueueService;
        this.codeToRun = codeToRun;
        this.portNum = portNum;
        this.outfolder = outfolder;
        this.project_id = project_id;
        this.file_id = file_id;
        this.request_time = request_time;
    }



    @Override
    public void run() {
        this.runCode(this.codeToRun, this.portNum, this.outfolder, this.project_id, this.file_id);
    }

    private void runCode(String CodetoRun,int portnum, String outfolder, int projectid, int fileid) {
        logger.info(CodetoRun);
        logger.info(this.projectQueueService == null ? "pq is null": "pq is not null");
        if (!new File(CodetoRun).exists()) {
            logger.info("no file to run");
            return;
        }
        RConnection connection = null;
        try{
            CodetoRun = CodetoRun.replaceAll("\\\\","//");
            logger.info("rconnection start : "+portnum);
            connection = new RConnection(null,portnum);
            logger.info(""+connection.isConnected());
            logger.info("code to run: " + CodetoRun);
            ProjectQueueDto projectQueueDto = new ProjectQueueDto();
            projectQueueDto.setAnalysisFileId(fileid);
            projectQueueDto.setAnalysisProjectId(projectid);
            projectQueueDto.setRequestTime(request_time);
            projectQueueDto.setStatus("run");
            logger.info(projectQueueDto.toString());
            this.projectQueueService.modifyOne(projectQueueDto);
            logger.info("db modified -> run");
            connection.eval("source('"+CodetoRun+"')");
        } catch (RserveException e) {
            e.printStackTrace();
        } finally{
            connection.close();
            logger.info("connection is connected? : "+connection.isConnected());
            synchronized (Constants.GLOBAL_QUEUE_PORT){
                Constants.GLOBAL_QUEUE_PORT.offer(portnum);
                ProjectQueueDto projectQueueDto = new ProjectQueueDto();
                projectQueueDto.setAnalysisFileId(fileid);
                projectQueueDto.setAnalysisProjectId(projectid);
                projectQueueDto.setRequestTime(request_time);
                projectQueueDto.setStatus("complete");
                this.projectQueueService.modifyOne(projectQueueDto);
            }
            logger.info("connection closed : "+portnum);
            ZipUtil.pack(new File(outfolder), new File(outfolder+".zip"));
            logger.info("output 압축 완료");

        }
    }
}
