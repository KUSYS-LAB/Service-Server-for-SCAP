package ac.kr.korea.cdm.serviceserver.service;

import ac.kr.korea.cdm.serviceserver.dto.CustodianDto;
import ac.kr.korea.cdm.serviceserver.mapper.CustodianMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SignService {
    private static final Logger logger = LoggerFactory.getLogger(SignService.class);

    @Autowired
    private CustodianMapper custodianMapper;

    public CustodianDto signIn(CustodianDto custodianDto) {

        custodianDto = this.custodianMapper.getOne(custodianDto);
        if (custodianDto != null && custodianDto.check()) return custodianDto;
        return null;
    }
}
