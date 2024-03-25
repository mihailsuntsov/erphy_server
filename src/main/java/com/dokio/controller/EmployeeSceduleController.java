package com.dokio.controller;

import com.dokio.message.request.additional.EmployeeSceduleForm;
import com.dokio.repository.EmployeeSceduleRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@Controller
public class EmployeeSceduleController {


    Logger logger = Logger.getLogger("EmployeeSceduleController");

    @Autowired
    EmployeeSceduleRepository employeeSceduleRepository;


    @PostMapping("/api/auth/getEmployeesWorkSchedule")
    public  ResponseEntity<?> getEmployeesWorkSchedule(@RequestBody EmployeeSceduleForm request) {
        logger.info("Processing post request for path /api/auth/getEmployeesWorkSchedule: " + request.toString());
        try {return new ResponseEntity<>(employeeSceduleRepository.getEmployeesWorkSchedule(request), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller getEmployeesWorkSchedule error", e);
            return new ResponseEntity<>("Request error", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

}
