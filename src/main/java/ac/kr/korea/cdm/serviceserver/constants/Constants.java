package ac.kr.korea.cdm.serviceserver.constants;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class Constants {

    public static final String Temp_Dir = "C:/temp";
    public static final String Dbms = "postgresql";
    public static final String Server = "localhost/OHDSI";
    public static final String User = "ohdsi_admin_user";
    public static final String Password = "admin1";
    public static final String Port = "5432";
    public static final String Cdm_Schema = "cdm";
    public static final String Cohort_Schema = "cdm";
    public static final String SS_PROJECT_ID = "project_id";
    public static final String SS_FILE_ID = "file_id";
    public static final String SS_BODY = "body";
    public static final String SS_SIGNATURE = "signature";
    public static final String SS_SPK = "spk";
    public static final String SS_CPK = "cpk";
    public static final String SS_ESK = "esk";
    public static final String SS_TICKET = "ticket";
    public static final String SS_AUTH = "auth";
    public static final String SS_AES = "aes";
    public static final String SS_SK = "sk";
    public static final String SS_IV = "iv";
    public static final String SS_CNAME = "cname";
    public static final String SS_TIME = "time";
    public static final String SS_FROM = "from";
    public static final String SS_TO = "to";
    public static final String SS_CERTIFICATE = "certificate";
    public static final String SS_SIG_AC = "sig_ac";
    public static final String SS_TS = "ts";
    public static final String SS_CUSTODIAN = "user";
    public static final String SS_REQUEST_TIME = "request_time";
    public static final String SS_REQUEST_ID = "request_id";
    public enum STATUS {wait, run, complete};
    public static final int[] PORT_POOL = {6340, 6341};
    public static String TYPE_PKI;
    public static String CS_DOMAIN;
    public static int SETUP_MODE;
//    volatile public static ConcurrentLinkedQueue<ProjectRun> GLOBAL_QUEUE = new ConcurrentLinkedQueue<ProjectRun>();
    volatile public static ConcurrentLinkedQueue<Integer> GLOBAL_QUEUE_PORT = new ConcurrentLinkedQueue<Integer>();

    @Value("${publickey.type}")
    public void setTypePki(String type) {TYPE_PKI = type;}
    @Value("${cs.domain}")
    public void setCsDomain(String domain) {CS_DOMAIN = domain;}
    @Value("${setup.mode}")
    public void setSetupMode(String setupMode) {SETUP_MODE = Integer.parseInt(setupMode);}
}
