//Класс для формирования JSON. Пример:
//    @PostMapping("/api/auth/getClientById")
//    public ResponseEntity<?> getClientById(@RequestBody ClientForm clientRequest) {
//        ClientJSON client;
//        int id = clientRequest.getId();
//            client=clientService.getClientById(id);//результат запроса помещается в объект
//            ResponseEntity<ClientJSON> responseEntity = new ResponseEntity<>(client, HttpStatus.OK); //объект помещается в ответ
//            return responseEntity;
//    }
package com.laniakea.message.response;
import java.util.Date;
import javax.persistence.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Entity
public class ClientJSON {

    @Id
    private Long cliNumber;

    private String cliName;

    @JsonSerialize(using = com.laniakea.util.JSONSerializer.class)
    @JsonDeserialize(using = com.laniakea.util.JSONDeserialize.class)
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
