package com.dokio.controller;

import com.dokio.message.request.additional.AppointmentMainInfoForm;
import com.dokio.message.request.additional.calendar.CalendarEventsQueryForm;
import com.dokio.repository.OnlineSchedulingRepositoryJPA;
import com.dokio.message.response.onlineScheduling.CompanyParamsJSON;
import com.dokio.repository.CalendarRepositoryJPA;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class OnlineSchedulingController {

    Logger logger = Logger.getLogger(OnlineSchedulingController.class);

    @Autowired
    OnlineSchedulingRepositoryJPA onlineSchedulingRepository;
    @Autowired
    CalendarRepositoryJPA calendarRepository;

    @RequestMapping(
            value = "/api/public/getOnlineSchedulingSettings",
            params = {"company_id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getDescriptionDefaultTemplate(@RequestParam("company_id") String company_id) {
        logger.info("Processing GET request for path /api/public/getOnlineSchedulingSettings with company_id=" + company_id);
        try {return new ResponseEntity<>(onlineSchedulingRepository.getOnlineSchedulingSettings(company_id), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller getOnlineSchedulingSettings error with company_id=" + company_id, e);
            return new ResponseEntity<>("Controller getOnlineSchedulingSettings error", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @PostMapping("/api/public/getEmployeesWithSchedulingList")
    public  ResponseEntity<?> getEmployeesWithSchedulingList(@RequestBody AppointmentMainInfoForm request) {
        logger.info("Processing post request for path /api/public/getEmployeesWithSchedulingList: " + request.toString());
        try {return new ResponseEntity<>(onlineSchedulingRepository.getEmployeesWithSchedulingList(request), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller getEmployeesWithSchedulingList error", e);
            return new ResponseEntity<>("Controller getEmployeesWithSchedulingList error", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @RequestMapping(
            value = "/api/public/getDepartmentsWithPartsList",
            params = {"company_id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getDepartmentsWithPartsList(@RequestParam("company_id") String company_id) {
        logger.info("Processing GET request for path /api/public/getDepartmentsWithPartsList with company_id=" + company_id);
        try {return new ResponseEntity<>(onlineSchedulingRepository.getDepartmentsWithPartsList(company_id), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller getDepartmentsWithPartsList error with company_id=" + company_id, e);
            return new ResponseEntity<>("Controller getDepartmentsWithPartsList error", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @PostMapping("/api/public/getEmployeeWorkingTimeAndPlaces")
    public  ResponseEntity<?> getEmployeeWorkingTimeAndPlaces(@RequestBody CalendarEventsQueryForm request) {
        logger.info("Processing post request for path /api/public/getEmployeeWorkingTimeAndPlaces: " + request.toString());
        try {
            CompanyParamsJSON params = onlineSchedulingRepository.getCompanyParamsBySlug(request.getCompanyUrlSlug());
            String myTimeZone = params.getTime_zone_name();
            Long masterId = params.getMasterId();
            request.setCompanyId(params.getCompanyId());
            return new ResponseEntity<>(onlineSchedulingRepository.getEmployeeWorkingTimeAndPlaces(request,myTimeZone,masterId), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller getEmployeeWorkingTimeAndPlaces error", e);
            return new ResponseEntity<>("Controller getEmployeeWorkingTimeAndPlaces error", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @RequestMapping(
            value = "/api/public/getOnlineSchedulingProductCategories",
            params = {"companyUrlSlug","langCode"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getOnlineSchedulingProductCategories(
            @RequestParam("companyUrlSlug") String companyUrlSlug,
            @RequestParam("langCode") String langCode) {
        logger.info("Processing GET request for path /api/public/getOnlineSchedulingProductCategories with companyUrlSlug=" + companyUrlSlug +", langCode="+langCode);
        try {return new ResponseEntity<>(onlineSchedulingRepository.getOnlineSchedulingProductCategories(companyUrlSlug, langCode), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller getOnlineSchedulingProductCategories error with companyUrlSlug=" + companyUrlSlug +", langCode="+langCode, e);
            return new ResponseEntity<>("Controller getOnlineSchedulingProductCategories error", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
}
