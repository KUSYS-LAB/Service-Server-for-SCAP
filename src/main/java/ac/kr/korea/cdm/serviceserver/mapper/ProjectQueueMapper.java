package ac.kr.korea.cdm.serviceserver.mapper;

import ac.kr.korea.cdm.serviceserver.dto.ProjectQueueDto;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectQueueMapper {
    public ProjectQueueDto getOne(ProjectQueueDto projectQueueDto);
    public ProjectQueueDto getWaitOne();
    public ProjectQueueDto getOneForCheckingStatus(ProjectQueueDto projectQueueDto);
    public ProjectQueueDto getSecret(ProjectQueueDto projectQueueDto);
    public List<ProjectQueueDto> getAll();
    public void insertOne(ProjectQueueDto projectQueueDto);
    public void modifyOne(ProjectQueueDto projectQueueDto);
    public void updateSecret(ProjectQueueDto projectQueueDto);
    public void approveRun(ProjectQueueDto projectQueueDto);
    public void approveRead(ProjectQueueDto projectQueueDto);
}
