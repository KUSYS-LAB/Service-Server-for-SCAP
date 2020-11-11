package ac.kr.korea.cdm.serviceserver.mapper;

import ac.kr.korea.cdm.serviceserver.dto.CustodianDto;
import org.springframework.stereotype.Repository;

@Repository
public interface CustodianMapper {
    public CustodianDto getOne(CustodianDto custodianDto);
}
