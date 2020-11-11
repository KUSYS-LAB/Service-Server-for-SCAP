package ac.kr.korea.cdm.serviceserver.util;

import ac.kr.korea.cdm.serviceserver.constants.Constants;
import ac.kr.korea.cdm.serviceserver.constants.ProjectRun;
import ac.kr.korea.cdm.serviceserver.dto.ProjectQueueDto;
import ac.kr.korea.cdm.serviceserver.service.ProjectQueueService;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.lang.Thread.sleep;

@Component
public class PoolThread implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(PoolThread.class);
    private boolean running = false;
    private ProjectQueueService projectQueueService;

    public PoolThread(ProjectQueueService projectQueueService){
        this.projectQueueService = projectQueueService;
    }

    @Override
    public void run(){
        this.running = true;
        try {
            this.portPool();
        } catch (JSONException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    private void portPool() throws JSONException, InterruptedException {
        logger.info("port pool start");
        while (running){
            synchronized (Constants.GLOBAL_QUEUE_PORT){
                if(!Constants.GLOBAL_QUEUE_PORT.isEmpty()){

//                    ProjectRun projectRun = Constants.GLOBAL_QUEUE.poll();
                    ProjectQueueDto projectQueueDto = this.projectQueueService.getWaitOne();
                    if (projectQueueDto != null) {

                        Thread analyzingThread = new Thread(
                                new AnalyzingThread(
                                        projectQueueDto.getDirUnzip() + "\\extras\\CodeToRun.R",
                                        Constants.GLOBAL_QUEUE_PORT.poll(),
                                        projectQueueDto.getDirOutput(),
                                        projectQueueDto.getAnalysisProjectId(),
                                        projectQueueDto.getAnalysisFileId(),
                                        projectQueueDto.getRequestTime(),
                                        this.projectQueueService
                                )
                        );
                        analyzingThread.start();
                    }

                    sleep(1);
                }
            }
        }
    }
}
