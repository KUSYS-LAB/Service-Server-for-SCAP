package ac.kr.korea.cdm.serviceserver.service;

import ac.kr.korea.cdm.serviceserver.constants.Constants;
import ac.kr.korea.cdm.serviceserver.dto.SsSecretDto;
import ac.kr.korea.cdm.serviceserver.util.CryptoHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

@Service
public class VerificationService {
    private static final Logger logger = LoggerFactory.getLogger(VerificationService.class);
    @Autowired
    private CryptoHelper cryptoHelper;

    public boolean verifySignatureSpk(Map<String, Object> data) {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> body = (Map<String, Object>) data.get(Constants.SS_BODY);
            String signature = (String) data.get(Constants.SS_SIGNATURE);
            PublicKey spk = this.cryptoHelper.getPublic(Base64Utils.decodeFromString((String) body.get(Constants.SS_SPK)), Constants.TYPE_PKI);

            return this.cryptoHelper.verifySignature(objectMapper.writeValueAsBytes(body),
                    Base64Utils.decodeFromString(signature), spk);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean verifySignatureCpk(Map<String, Object> data) {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> body = (Map<String, Object>) data.get(Constants.SS_BODY);
            String signature = (String) data.get(Constants.SS_SIGNATURE);
//            PublicKey spk = this.cryptoHelper.getPublic(Base64Utils.decodeFromString((String) body.get(Constants.SS_CPK)), Constants.TYPE_PKI);
            PublicKey cpk = this.cryptoHelper.restorePublicKeyFromPem((String) body.get(Constants.SS_CPK));

            return this.cryptoHelper.verifySignature(objectMapper.writeValueAsBytes(body),
                    Base64Utils.decodeFromString(signature), cpk);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean verifyTgsTicket(Map<String, Object> data, SsSecretDto ssSecretDto) {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Map<String, Object> body = (Map<String, Object>) data.get(Constants.SS_BODY);
            String tgsTicketBase64 = (String) body.get(Constants.SS_TICKET);
            String tgsTicketStr = new String(this.cryptoHelper.decryptWithAes(Base64Utils.decodeFromString(tgsTicketBase64), ssSecretDto.getSk(), ssSecretDto.getIv()));
            Map<String, Object> tgsTicketMap = objectMapper.readValue(tgsTicketStr, Map.class);
            Map<String, Object> timeMap = (Map<String, Object>) tgsTicketMap.get(Constants.SS_TIME);
            Date from = simpleDateFormat.parse((String) timeMap.get(Constants.SS_FROM));
            Date to = simpleDateFormat.parse((String) timeMap.get(Constants.SS_TO));
            Date now = Calendar.getInstance().getTime();

            if (tgsTicketMap.get(Constants.SS_CNAME).equals(ssSecretDto.getAuth().get(Constants.SS_CNAME))) {
                if ((now.after(from)) && (now.before(to))) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean verifySigAc(MultipartFile ac, Map<String, Object> data) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpGet httpGet = new HttpGet(Constants.CS_DOMAIN + "/get-cert");
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String response = httpClient.execute(httpGet, responseHandler);
            Map<String, Object> csData = objectMapper.readValue(response, Map.class);
            Map<String, Object> csBody = (Map<String, Object>) csData.get(Constants.SS_BODY);
            String signature = (String) csData.get(Constants.SS_SIGNATURE);

            PublicKey csPublicKey = this.cryptoHelper.getPublic(
                    Base64Utils.decodeFromString((String) csBody.get(Constants.SS_CERTIFICATE)),
                    Constants.TYPE_PKI);
            boolean verifyCsPublicKey = this.cryptoHelper.verifySignature(
                    objectMapper.writeValueAsString(csBody).getBytes(),
                    Base64Utils.decodeFromString(signature),
                    csPublicKey);
            if (!verifyCsPublicKey) {
                return false;
            }

            Map<String, Object> body = (Map<String, Object>) data.get(Constants.SS_BODY);
            String signatureAc = (String) body.get(Constants.SS_SIG_AC);
            boolean verifySigAc = this.cryptoHelper.verifySignature(ac.getBytes(), Base64Utils.decodeFromString(signatureAc), csPublicKey);
            if (!verifySigAc) {
                return false;
            }

            return true;
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
        }
        return false;
    }

    public SsSecretDto getEsk(Map<String, Object> data) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        PrivateKey privateKey = this.cryptoHelper.getPrivate("SS-KeyPair/SS-PrivateKey", Constants.TYPE_PKI);
        Map<String, Object> body = (Map<String, Object>) data.get(Constants.SS_BODY);
        String eskBase64 = (String) body.get(Constants.SS_ESK);
        String eskStr = new String(this.cryptoHelper.decryptWithRsa(Base64Utils.decodeFromString(eskBase64), privateKey));
        Map<String, Object> eskMap = objectMapper.readValue(eskStr, Map.class);
        SecretKeySpec sk = new SecretKeySpec(
                Base64Utils.decodeFromString((String) eskMap.get(Constants.SS_SK)),
                Constants.SS_AES);
        IvParameterSpec iv = new IvParameterSpec(Base64Utils.decodeFromString((String) eskMap.get(Constants.SS_IV)));

        return new SsSecretDto(sk, iv, null);
    }

    public Map<String, Object> getAuth(Map<String, Object> data) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        PrivateKey privateKey = this.cryptoHelper.getPrivate("SS-KeyPair/SS-PrivateKey", Constants.TYPE_PKI);
        Map<String, Object> body = (Map<String, Object>) data.get(Constants.SS_BODY);
        String authBase64 = (String) body.get(Constants.SS_AUTH);
        String authStr = new String(this.cryptoHelper.decryptWithRsa(Base64Utils.decodeFromString(authBase64), privateKey));

        return objectMapper.readValue(authStr, Map.class);
    }

}
