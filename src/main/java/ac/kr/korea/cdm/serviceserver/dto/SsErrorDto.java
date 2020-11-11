package ac.kr.korea.cdm.serviceserver.dto;

public class SsErrorDto extends RuntimeException {
    public SsErrorDto(String msg) {super(msg);}
}
