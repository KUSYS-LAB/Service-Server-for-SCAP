package ac.kr.korea.cdm.serviceserver.service;

import ac.kr.korea.cdm.serviceserver.constants.Constants;
import ac.kr.korea.cdm.serviceserver.dto.ProjectQueueDto;
import ac.kr.korea.cdm.serviceserver.mapper.ProjectQueueMapper;
import ac.kr.korea.cdm.serviceserver.util.CryptoHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProjectQueueService {

    @Autowired
    private ProjectQueueMapper projectQueueMapper;
    @Autowired
    private CryptoHelper cryptoHelper;

    public ProjectQueueDto getOne(ProjectQueueDto projectQueueDto){return this.projectQueueMapper.getOne(projectQueueDto);};

    public ProjectQueueDto getOneForCheckingStatus(ProjectQueueDto projectQueueDto) {
        return this.projectQueueMapper.getOneForCheckingStatus(projectQueueDto);
    }

    public ProjectQueueDto getWaitOne() {
        return this.projectQueueMapper.getWaitOne();
    }

    public List<ProjectQueueDto> getAll(){return this.projectQueueMapper.getAll();}

    public ProjectQueueDto getSecret(ProjectQueueDto projectQueueDto) {
        return this.projectQueueMapper.getSecret(projectQueueDto);
    }

    public void insertOne(ProjectQueueDto projectQueueDto){this.projectQueueMapper.insertOne(projectQueueDto);}

    public void modifyOne(ProjectQueueDto projectQueueDto){this.projectQueueMapper.modifyOne(projectQueueDto);}

    public void approveRun(ProjectQueueDto projectQueueDto) {
        this.projectQueueMapper.approveRun(projectQueueDto);
    }

    public void approveRead(ProjectQueueDto projectQueueDto) {
        this.projectQueueMapper.approveRead(projectQueueDto);
    }

    public Map<String, Object> encryptAR(ProjectQueueDto projectQueueDto) throws Exception {

        // should get the file from dir_output
        // but for simplification, I used the simple text file.
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
        String now = simpleDateFormat.format(Calendar.getInstance().getTime());

        File file = new File("c:/ss_queue_db.txt");
        byte[] data = FileUtils.readFileToByteArray(file);

        SecretKey sk = this.cryptoHelper.getSecretKey();
        IvParameterSpec iv = this.cryptoHelper.getIvParameterSpec();
        PrivateKey privateKey = this.cryptoHelper.getPrivate("SS-KeyPair/SS-PrivateKey",Constants.TYPE_PKI);
        PublicKey publicKey = this.cryptoHelper.getPublic("SS-KeyPair/SS-PublicKey", Constants.TYPE_PKI);

        byte[] encryptedData = this.cryptoHelper.encryptWithAes(data, sk, iv);

        projectQueueDto.setSk(Base64Utils.encodeToString(sk.getEncoded()));
        projectQueueDto.setIv(Base64Utils.encodeToString(iv.getIV()));
        this.projectQueueMapper.updateSecret(projectQueueDto);

        Map<String, Object> body = new HashMap<String, Object>();
        body.put("ear", Base64Utils.encodeToString(encryptedData));
        body.put(Constants.SS_TS, now);
        body.put(Constants.SS_SPK, Base64Utils.encodeToString(publicKey.getEncoded()));

        ObjectMapper objectMapper = new ObjectMapper();
        byte[] bodyBytes = objectMapper.writeValueAsBytes(body);
        byte[] signature = this.cryptoHelper.getSignature(bodyBytes, privateKey);

        Map<String, Object> json = new HashMap<String, Object>();
        json.put(Constants.SS_BODY, body);
        json.put(Constants.SS_SIGNATURE, Base64Utils.encodeToString(signature));

        return json;
    }
}
