package ac.kr.korea.cdm.serviceserver.controller;

import ac.kr.korea.cdm.serviceserver.constants.Constants;
import ac.kr.korea.cdm.serviceserver.dto.ProjectQueueDto;
import ac.kr.korea.cdm.serviceserver.dto.SsResponseDto;
import ac.kr.korea.cdm.serviceserver.dto.SsSecretDto;
import ac.kr.korea.cdm.serviceserver.service.ProjectQueueService;
import ac.kr.korea.cdm.serviceserver.service.VerificationService;
import ac.kr.korea.cdm.serviceserver.util.AnalyzeHelper;
import ac.kr.korea.cdm.serviceserver.util.CryptoHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.rosuda.REngine.Rserve.RserveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.sql.Timestamp;
import java.util.*;

@CrossOrigin("*")
@RestController
public class SsAnalysisController {
    private static final Logger logger = LoggerFactory.getLogger(SsAnalysisController.class);


    @Autowired
    private AnalyzeHelper analyzeHelper;
    @Autowired
    private ProjectQueueService projectQueueService;
    @Autowired
    private CryptoHelper cryptoHelper;
    @Autowired
    private VerificationService verificationService;

//    @PostConstruct
//    public void init() throws IOException, RserveException {
//        for (int port : Constants.PORT_POOL) Constants.GLOBAL_QUEUE_PORT.offer(port);
//        logger.info("큐 세팅 완료!");
//        Thread poolThread = new Thread(new PoolThread(this.projectQueueService));
//        poolThread.start();
//    }

    @RequestMapping(value = "/analyze", method = RequestMethod.POST)
    public SsResponseDto<Boolean> analyze(@RequestPart("ac") MultipartFile ac, @RequestPart("data") Map<String, Object> data) {
        SsSecretDto ssSecretDto = null;
        Map<String, Object> auth = null;

        if (!this.verificationService.verifySignatureSpk(data)) {
//            throw new SsErrorDto("Invalid signature");
            logger.info("Invalid signature");
            return new SsResponseDto<>(false);
        }

        try {
            auth = this.verificationService.getAuth(data);
        } catch (Exception e) {
            e.printStackTrace();
//            throw new SsErrorDto("Invalid AUTH");
            logger.info("Invalid AUTH");
            return new SsResponseDto<>(false);
        }

        try {
            ssSecretDto = this.verificationService.getEsk(data);
        } catch (Exception e) {
            e.printStackTrace();
//            throw new SsErrorDto("Invalid ESK");
            logger.info("Invalid ESK");
            return new SsResponseDto<>(false);
        }

        ssSecretDto.setAuth(auth);

        if (!this.verificationService.verifyTgsTicket(data, ssSecretDto)) {
//            throw new SsErrorDto("Failed to verify the tgs ticket");
            logger.info("Failed to verify the tgs ticket");
            return new SsResponseDto<>(false);
        }

        // verify sig ac
        if (!this.verificationService.verifySigAc(ac, data)) {
//            throw new SsErrorDto("Invalid signature for AC");
            logger.info("Invalid signature for AC");
            return new SsResponseDto<>(false);
        }

        try {
            String saveName = ac.getOriginalFilename();
            File saveFile = new File("C:\\ss_analyze", saveName);
            ac.transferTo(saveFile);
            this.analyzeHelper.unzip("C:\\ss_analyze\\"+saveName,saveName.split("\\.")[0], data, ssSecretDto);
        } catch (IOException | RserveException | JSONException e) {
            e.printStackTrace();
//            throw new SsErrorDto("Failed to unzip AC");
            logger.info("Failed to unzip AC");
            return new SsResponseDto<>(false);
        }

        return new SsResponseDto<>(true);
    }

    @RequestMapping(value="/check-progress", method=RequestMethod.GET)
    public ProjectQueueDto checkProgress(HttpServletRequest request) {
        String userName = request.getParameter(Constants.SS_CNAME);
        int projectId = Integer.parseInt(request.getParameter(Constants.SS_PROJECT_ID));
        int fileId = Integer.parseInt(request.getParameter(Constants.SS_FILE_ID));

        ProjectQueueDto projectQueueDto = new ProjectQueueDto();
        projectQueueDto.setUserName(userName);
        projectQueueDto.setAnalysisProjectId(projectId);
        projectQueueDto.setAnalysisFileId(fileId);

        projectQueueDto = this.projectQueueService.getOneForCheckingStatus(projectQueueDto);
        logger.info(projectQueueDto.toString());
        projectQueueDto.setDirOutput(null);
        projectQueueDto.setDirUnzip(null);

        return projectQueueDto;
    }

    @RequestMapping(value="/get-analysis-result", method=RequestMethod.GET)
    public Map<String, Object> getAnalysisResult(HttpServletRequest request) {
        String userName = request.getParameter(Constants.SS_CNAME);
        int projectId = Integer.parseInt(request.getParameter(Constants.SS_PROJECT_ID));
        int fileId = Integer.parseInt(request.getParameter(Constants.SS_FILE_ID));

        ProjectQueueDto projectQueueDto = new ProjectQueueDto();
        projectQueueDto.setUserName(userName);
        projectQueueDto.setAnalysisProjectId(projectId);
        projectQueueDto.setAnalysisFileId(fileId);

        projectQueueDto = this.projectQueueService.getOneForCheckingStatus(projectQueueDto);

        try {
            return this.projectQueueService.encryptAR(projectQueueDto);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> json = new HashMap<>();
            json.put("status", "error");
            return json;
        }
    }

    @RequestMapping(value="/check-approved-to-read", method=RequestMethod.GET)
    public boolean checkApprovedToRead(HttpServletRequest request) {
        int projectId = Integer.parseInt(request.getParameter(Constants.SS_PROJECT_ID));
        int fileId = Integer.parseInt(request.getParameter(Constants.SS_FILE_ID));
        String cname = request.getParameter(Constants.SS_CNAME);

        ProjectQueueDto projectQueueDto = new ProjectQueueDto();
        projectQueueDto.setAnalysisProjectId(projectId);
        projectQueueDto.setAnalysisFileId(fileId);
        projectQueueDto.setUserName(cname);

        projectQueueDto = this.projectQueueService.getOneForCheckingStatus(projectQueueDto);
        if (projectQueueDto.isApprovedToRead()) {
            return true;
        } else return false;
    }

    @RequestMapping(value = "/statusrequest", method = {RequestMethod.GET, RequestMethod.POST})
    public String statusrequest(HttpServletRequest request) throws JSONException {
        String json = request.getParameter("json");
        logger.info(json);
        List<ProjectQueueDto> projectQueueDtoList = new ArrayList<ProjectQueueDto>();
        JSONArray jsonArray = new JSONArray(json);

        for(int i=0; i< jsonArray.length();i++){
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            Integer project_id = jsonObject.getInt("project_id");
            Integer file_id = jsonObject.getInt("file_id");
            String today = jsonObject.getString("request_time");
            Timestamp request_time = Timestamp.valueOf(today);
            ProjectQueueDto projectQueueDto = new ProjectQueueDto(project_id,file_id,null,request_time,null,null,null);
            projectQueueDto = this.projectQueueService.getOne(projectQueueDto);
            if(projectQueueDto==null){
                ProjectQueueDto projectQueueDto_null = new ProjectQueueDto();
                projectQueueDto_null.setAnalysisProjectId(project_id);
                projectQueueDto_null.setAnalysisFileId(file_id);
                projectQueueDto_null.setStatus("null");
                projectQueueDtoList.add(projectQueueDto_null);
            }
            else {
                projectQueueDtoList.add(projectQueueDto);
            }
        }

        JSONArray jsonArray_result = new JSONArray();
        Iterator iterator = projectQueueDtoList.iterator();
        while (iterator.hasNext()){
            JSONObject data = new JSONObject();
            ProjectQueueDto projectQueueDto_result = (ProjectQueueDto)iterator.next();
            data.put("project_id",projectQueueDto_result.getAnalysisProjectId());
            data.put("file_id",projectQueueDto_result.getAnalysisFileId());
            data.put("request_time",projectQueueDto_result.getRequestTime());
            data.put("status",projectQueueDto_result.getStatus());
            jsonArray_result.put(data);
        }

        return String.valueOf(jsonArray_result);
    }

    @RequestMapping(value="/analysis/approve-run", method=RequestMethod.GET)
    public boolean approveRun(HttpServletRequest request) {
        int requestId = Integer.parseInt(request.getParameter(Constants.SS_REQUEST_ID));

        ProjectQueueDto projectQueueDto = new ProjectQueueDto();
        projectQueueDto.setId(requestId);

        try {
            this.projectQueueService.approveRun(projectQueueDto);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @RequestMapping(value="/analysis/approve-read", method=RequestMethod.GET)
    public boolean approveRead(HttpServletRequest request) {
        int requestId = Integer.parseInt(request.getParameter(Constants.SS_REQUEST_ID));

        ProjectQueueDto projectQueueDto = new ProjectQueueDto();
        projectQueueDto.setId(requestId);

        try {
            this.projectQueueService.approveRead(projectQueueDto);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @RequestMapping(value="/get-secret", method=RequestMethod.POST)
    public Map<String, Object> getSecret(HttpServletRequest request, @RequestBody Map<String, Object> json) throws Exception {
        Map<String, Object> body = (Map<String, Object>) json.get(Constants.SS_BODY);
        Map<String, Object> response = new HashMap<String, Object>();
        Map<String, Object> respBody = new HashMap<String, Object>();
        ObjectMapper objectMapper = new ObjectMapper();
        PrivateKey privateKey = this.cryptoHelper.getPrivate("SS-KeyPair/SS-PrivateKey", Constants.TYPE_PKI);
        PublicKey publicKey = this.cryptoHelper.getPublic("SS-KeyPair/SS-PublicKey", Constants.TYPE_PKI);

        if (!this.verificationService.verifySignatureCpk(json)) {
            respBody.put("status", false);
            respBody.put("spk", Base64Utils.encodeToString(publicKey.getEncoded()));
            String respBodyStr = objectMapper.writeValueAsString(respBody);
            String signature = Base64Utils.encodeToString(this.cryptoHelper.getSignature(respBodyStr.getBytes(), privateKey));
            response.put(Constants.SS_BODY, respBody);
            response.put(Constants.SS_SIGNATURE, signature);
            return response;
        }

        int projectId = (Integer)body.get(Constants.SS_PROJECT_ID);
        int fileId = (Integer)body.get(Constants.SS_FILE_ID);
        String cname = (String) body.get(Constants.SS_CNAME);

        ProjectQueueDto projectQueueDto = new ProjectQueueDto();
        projectQueueDto.setAnalysisProjectId(projectId);
        projectQueueDto.setAnalysisFileId(fileId);
        projectQueueDto.setUserName(cname);

        projectQueueDto = this.projectQueueService.getSecret(projectQueueDto);
        if (projectQueueDto != null) {
            respBody.put("sk", projectQueueDto.getSk());
            respBody.put("iv", projectQueueDto.getIv());
        } else {
            respBody.put("status", false);
        }

        respBody.put("spk", Base64Utils.encodeToString(publicKey.getEncoded()));
        String respBodyStr = objectMapper.writeValueAsString(respBody);
        String signature = Base64Utils.encodeToString(this.cryptoHelper.getSignature(respBodyStr.getBytes(), privateKey));
        response.put(Constants.SS_BODY, respBody);
        response.put(Constants.SS_SIGNATURE, signature);
        return response;
    }

    @RequestMapping(value="/get-cert", method=RequestMethod.GET)
    public SsResponseDto<Map<String, Object>> getCert() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        PublicKey publicKey = this.cryptoHelper.getPublic("SS-KeyPair/SS-PublicKey", Constants.TYPE_PKI);

        Map<String, Object> data = new HashMap<>();
        data.put("certificate", Base64Utils.encodeToString(publicKey.getEncoded()));

        return new SsResponseDto<Map<String, Object>>(data);
    }

    @RequestMapping(value="/test-analyze", method=RequestMethod.POST)
    public String testAnalyze(@RequestPart("ac") MultipartFile ac, @RequestPart("data")Map<String, Object> data) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        logger.info(ac.getName());
        logger.info(ac.getOriginalFilename());
        logger.info(objectMapper.writeValueAsString(data));
        return "hello";
    }
}
