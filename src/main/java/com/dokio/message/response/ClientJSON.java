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
//Класс для формирования JSON. Пример:
//    @PostMapping("/api/auth/getClientById")
//    public ResponseEntity<?> getClientById(@RequestBody ClientForm clientRequest) {
//        ClientJSON client;
//        int id = clientRequest.getId();
//            client=clientService.getClientById(id);//результат запроса помещается в объект
//            ResponseEntity<ClientJSON> responseEntity = new ResponseEntity<>(client, HttpStatus.OK); //объект помещается в ответ
//            return responseEntity;
//    }
package com.dokio.message.response;
import java.util.Date;
import javax.persistence.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Entity
public class ClientJSON {

    @Id
    private Long cliNumber;

    private String cliName;

    @JsonSerialize(using = com.dokio.util.JSONSerializer.class)
    @JsonDeserialize(using = com.dokio.util.JSONDeserialize.class)
    private Date cliDatebirth;

    private String cliLastname;

    private String cliRegister;

    private String cliTelephone;

    private String cliEmail;

    private String cliAdditional;

    private  String creator;

    public String getCliAdditional() {
        return cliAdditional;
    }

    public void setCliAdditional(String cliAdditional) {
        this.cliAdditional = cliAdditional;
    }

    public String getCliTelephone() {
        return cliTelephone;
    }

    public void setCliTelephone(String cliTelephone) {
        this.cliTelephone = cliTelephone;
    }

    public String getCliEmail() {
        return cliEmail;
    }

    public void setCliEmail(String cliEmail) {
        this.cliEmail = cliEmail;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Long getCliNumber() {
        return cliNumber;
    }

    public void setCliNumber(Long cliNumber) {
        this.cliNumber = cliNumber;
    }

    public String getCliName() {
        return cliName;
    }

    public void setCliName(String cliName) {
        this.cliName = cliName;
    }

    public Date getCliDatebirth() {
        return cliDatebirth;
    }

    public void setCliDatebirth(Date cliDatebirth) {
        this.cliDatebirth = cliDatebirth;
    }

    public String getCliLastname() {
        return cliLastname;
    }

    public void setCliLastname(String cliLastname) {
        this.cliLastname = cliLastname;
    }

    public String getCliRegister() {
        return cliRegister;
    }

    public void setCliRegister(String cliRegister) {
        this.cliRegister = cliRegister;
    }
}
