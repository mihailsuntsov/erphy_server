package com.laniakea.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
public class Hello {

        @RequestMapping(value = "/api/auth/welcome1", method = RequestMethod.GET)
        @ResponseBody
        public String printWelcome1(ModelMap model) {

            model.addAttribute("message", "Spring 3 MVC - Hello World");
            return "Unauthorized hello - Welcome1";

        }
        @RequestMapping(value = "/welcome2", method = RequestMethod.GET)
        @PreAuthorize("hasRole('USER')")
        @ResponseBody
        public String printWelcome2(ModelMap model) {

            model.addAttribute("message", "Spring 3 MVC - Hello World");
            return "hello";

        }

    }

