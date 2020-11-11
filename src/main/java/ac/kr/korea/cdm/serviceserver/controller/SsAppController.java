package ac.kr.korea.cdm.serviceserver.controller;

import ac.kr.korea.cdm.serviceserver.dto.CustodianDto;
import ac.kr.korea.cdm.serviceserver.dto.ProjectQueueDto;
import ac.kr.korea.cdm.serviceserver.service.ProjectQueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@RestController
public class SsAppController {
    private Logger logger = LoggerFactory.getLogger(SsAppController.class);

    @Autowired
    private ProjectQueueService projectQueueService;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView home(ModelAndView mv){
        mv.setViewName("home");
        mv.getModel().put("custodianDto", new CustodianDto());
        return mv;
    }

    @RequestMapping(value="/error", method=RequestMethod.GET)
    public ModelAndView error(ModelAndView mv) {
        mv.setViewName("error");
        return mv;
    }

    @RequestMapping(value="/index", method=RequestMethod.GET)
    public ModelAndView index(ModelAndView mv) {
        mv.setViewName("index");
        return mv;
    }

    @RequestMapping(value="/manage-request", method=RequestMethod.GET)
    public ModelAndView manageRequests (ModelAndView mv) {

        List<ProjectQueueDto> requests = projectQueueService.getAll();

        mv.setViewName("managerequests");
        mv.addObject("requests", requests);
        return mv;
    }

}
