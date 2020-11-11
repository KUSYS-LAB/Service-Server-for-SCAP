package ac.kr.korea.cdm.serviceserver.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Random;

import ac.kr.korea.cdm.serviceserver.constants.Constants;
import ac.kr.korea.cdm.serviceserver.constants.ProjectRun;
import ac.kr.korea.cdm.serviceserver.controller.SsAppController;
import ac.kr.korea.cdm.serviceserver.dto.ProjectQueueDto;
import ac.kr.korea.cdm.serviceserver.dto.SsSecretDto;
import ac.kr.korea.cdm.serviceserver.service.ProjectQueueService;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.json.JSONException;
import org.rosuda.REngine.Rserve.RserveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AnalyzeHelper {

    private static final Logger logger = LoggerFactory.getLogger(SsAppController.class);

    private static AnalyzeHelper analyzeHelper = new AnalyzeHelper();

    public static AnalyzeHelper getInstance(){
        return analyzeHelper;
    }

    public AnalyzeHelper() {}

    @Autowired private ProjectQueueService projectQueueService;

    public static Boolean running = true;

    public void unzip(String path, String fileName, Map<String, Object> data, SsSecretDto ssSecretDto) throws IOException, RserveException, JSONException {
        String destination = "C:\\ss_analyze\\unzip\\";
        Map<String, Object> body = (Map<String, Object>) data.get(Constants.SS_BODY);
        int projectId = (int) body.get(Constants.SS_PROJECT_ID);
        int fileId = (int) body.get(Constants.SS_FILE_ID);
        String cname = (String) ssSecretDto.getAuth().get(Constants.SS_CNAME);
        Timestamp timestamp = Timestamp.valueOf((String) body.get(Constants.SS_TS));

        logger.info(projectId +""+ fileId + cname);
        try {
            ZipFile zipFile = new ZipFile(path);
            zipFile.extractAll(destination+fileName);
        } catch (ZipException e) {
            e.printStackTrace();
        }
        String outputFolder = replaceCode(destination+fileName+"\\extras\\CodeToRun.R", destination+fileName);
//            Constants.GLOBAL_QUEUE.offer(new ProjectRun(destination+fileName, outputFolder, projectId, fileId,timestamp));
//            ProjectQueueDto projectQueueDto = new ProjectQueueDto(projectId, fileId, cname, timestamp, destination + fileName, outputFolder, Constants.STATUS.wait.name());
        ProjectQueueDto projectQueueDto = new ProjectQueueDto();
        projectQueueDto.setAnalysisProjectId(projectId);
        projectQueueDto.setAnalysisFileId(fileId);
        projectQueueDto.setUserName(cname);
        projectQueueDto.setRequestTime(timestamp);
        projectQueueDto.setDirUnzip(destination + fileName);
        projectQueueDto.setDirOutput(outputFolder);
        projectQueueDto.setStatus(Constants.STATUS.wait.name());

        switch(Constants.SETUP_MODE) {
            case 1:
                projectQueueDto.setApprovedToRun(true);
                projectQueueDto.setApprovedToRead(false);
                break;
            case 2:
            case 3:
                projectQueueDto.setApprovedToRun(false);
                projectQueueDto.setApprovedToRead(false);
                break;
            default:
                break;
        }

        logger.info(projectQueueDto.toString());
        this.projectQueueService.insertOne(projectQueueDto);
    }


    public static String replaceCode(String path_s, String wd_path) throws IOException {
        Charset charset = StandardCharsets.UTF_8;
        Path path = Paths.get(path_s);
        StringBuffer temp = new StringBuffer();
        Random rnd = new Random();
        for (int i = 0; i < 5; i++) {
            int rIndex = rnd.nextInt(3);
            switch (rIndex) {
                case 0:
                    temp.append((char) ((int) (rnd.nextInt(26)) + 97));
                    break;
                case 1:
                    temp.append((char) ((int) (rnd.nextInt(26)) + 65));
                    break;
                case 2:
                    temp.append((rnd.nextInt(10)));
                    break;
            }
        }
        logger.info("/"+temp);
        //String output = "output_test";
        String cohort_table = "cohorttest";
        String content = new String(Files.readAllBytes(path), charset);
        wd_path = wd_path.replaceAll("\\\\","/");
//        String append_content = "setwd('"+wd_path+"')\n"+"library(devtools)\ninstall(upgrade='never')\n";
        String append_content = "setwd('"+wd_path+"')\n"+"library(devtools)\n";
        content = append_content + content;
        content = content.replaceAll("!!", Constants.Temp_Dir+"/"+temp);
        content = content.replaceAll("@@", "C:/ss_analyze/out/"+temp);
        content = content.replaceAll("##", Constants.Dbms);
        content = content.replaceAll("@#", Constants.Server);
        content = content.replaceAll("%%", Constants.User);
        content = content.replaceAll("@&", Constants.Password);
        content = content.replaceAll("&&", Constants.Port);
        content = content.replaceAll("@~", Constants.Cdm_Schema);
        content = content.replaceAll("~~", Constants.Cohort_Schema);
        content = content.replaceAll("!@", cohort_table);
        Files.write(path, content.getBytes(charset));

        return "C:/ss_analyze/out/"+temp;
    }

    public void setStatus(int projectid, int fileid, String status){
        ProjectQueueDto projectQueueDto = new ProjectQueueDto();
        projectQueueDto.setStatus(status);
        projectQueueDto.setAnalysisProjectId(projectid);
        projectQueueDto.setAnalysisFileId(fileid);
        this.projectQueueService.modifyOne(projectQueueDto);
        logger.info("db modified -> "+status);

    }

}
