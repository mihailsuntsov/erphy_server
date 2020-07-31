/*
Приложение Dokio-server - учет продаж, управление складскими остатками, документооборот.
Copyright © 2020 Сунцов Михаил Александрович. mihail.suntsov@yandex.ru
Эта программа является свободным программным обеспечением: Вы можете распространять ее и (или) изменять,
соблюдая условия Генеральной публичной лицензии GNU редакции 3, опубликованной Фондом свободного
программного обеспечения;
Эта программа распространяется в расчете на то, что она окажется полезной, но
БЕЗ КАКИХ-ЛИБО ГАРАНТИЙ, включая подразумеваемую гарантию КАЧЕСТВА либо
ПРИГОДНОСТИ ДЛЯ ОПРЕДЕЛЕННЫХ ЦЕЛЕЙ. Ознакомьтесь с Генеральной публичной
лицензией GNU для получения более подробной информации.
Вы должны были получить копию Генеральной публичной лицензии GNU вместе с этой
программой. Если Вы ее не получили, то перейдите по адресу:
<http://www.gnu.org/licenses/>
 */
package com.dokio.controller;

import org.springframework.security.access.prepost.PreAuthorize;
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

