package ac.kr.korea.cdm.serviceserver.controller;

import ac.kr.korea.cdm.serviceserver.constants.Constants;
import ac.kr.korea.cdm.serviceserver.dto.CustodianDto;
import ac.kr.korea.cdm.serviceserver.service.SignService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@RestController
public class SsSignController {
    private static final Logger logger = LoggerFactory.getLogger(SsSignController.class);

    @Autowired
    private SignService signService;

    @RequestMapping(value="/sign/in", method= RequestMethod.POST)
    public ModelAndView signIn(ModelAndView mv, HttpServletRequest request, @ModelAttribute("custodian")CustodianDto custodianDto) {
        logger.info(custodianDto.toString());

        if (signService.signIn(custodianDto) != null) {
            request.getSession().setAttribute(Constants.SS_CUSTODIAN, custodianDto);
            mv.setViewName("redirect:/index");
        } else mv.setViewName("redirect:/error");
        return mv;
    }

    @RequestMapping(value="/sign/out", method=RequestMethod.GET)
    public ModelAndView signOut(ModelAndView mv, HttpServletRequest request) {
        request.getSession().removeAttribute(Constants.SS_CUSTODIAN);
        mv.setViewName("redirect:/");
        return mv;
    }

}
