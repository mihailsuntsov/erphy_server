package com.dokio.controller;

import com.dokio.message.request.Settings.SettingsCalendarForm;
import com.dokio.message.request.additional.AppointmentMainInfoForm;
import com.dokio.message.request.additional.calendar.CalendarEventsQueryForm;
import com.dokio.repository.CalendarRepositoryJPA;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class CalendarController {


    Logger logger = Logger.getLogger("CalendarController");

    @Autowired
    CalendarRepositoryJPA calendarRepository;


    @PostMapping("/api/auth/getCalendarEventsList")
    public  ResponseEntity<?> getCalendarEventsList(@RequestBody CalendarEventsQueryForm request) {
        logger.info("Processing post request for path /api/auth/getCalendarEventsList: " + request.toString());
        try {return new ResponseEntity<>(calendarRepository.getCalendarEventsList(request), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller getCalendarEventsList error", e);
            return new ResponseEntity<>("Controller getCalendarEventsList error", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @PostMapping("/api/auth/getCalendarUsersBreaksList")
    public  ResponseEntity<?> getCalendarUsersBreaksList(@RequestBody CalendarEventsQueryForm request) {
        logger.info("Processing post request for path /api/auth/getCalendarUsersBreaksList: " + request.toString());
        try {return new ResponseEntity<>(calendarRepository.getCalendarUsersBreaksList(request), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller getCalendarUsersBreaksList error", e);
            return new ResponseEntity<>("Controller getCalendarUsersBreaksList error", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @PostMapping("/api/auth/getEmployeesList")
    public  ResponseEntity<?> getFreeEmployeesList(@RequestBody AppointmentMainInfoForm request) {
        logger.info("Processing post request for path /api/auth/getEmployeesList: " + request.toString());
        try {return new ResponseEntity<>(calendarRepository.getEmployeesList(request), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller getEmployeesList error", e);
            return new ResponseEntity<>("Controller getEmployeesList error", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @PostMapping("/api/auth/saveSettingsCalendar")
    public ResponseEntity<?> saveSettingsCalendar(@RequestBody SettingsCalendarForm request){
        logger.info("Processing post request for path /api/auth/saveSettingsCalendar: " + request.toString());
        try {return new ResponseEntity<>(calendarRepository.saveSettingsCalendar(request), HttpStatus.OK);}
        catch (Exception e){return new ResponseEntity<>("Controller SettingsCalendarForm error", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @RequestMapping(
            value = "/api/auth/getSettingsCalendar",
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getSettingsCalendar(){
        logger.info("Processing get request for path /api/auth/getSettingsCalendar without request parameters");
        try {return new ResponseEntity<>(calendarRepository.getSettingsCalendar(), HttpStatus.OK);}
        catch (Exception e){return new ResponseEntity<>("Controller getSettingsCalendar error", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
}
