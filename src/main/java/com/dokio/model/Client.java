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
package com.dokio.model;

//import java.math.BigDecimal;
//import java.text.DateFormat;
import java.util.Date;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;


import javax.persistence.*;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@NamedQueries({
        @NamedQuery(name="Client.deleteClientsByNumber", query="delete from com.dokio.model.Client obj where obj.cliNumber in(:cliNumbers) ")
})

@Entity
@Table(name="CLIENT", schema = "public")
public class Client implements java.io.Serializable {

    private static final long serialVersionUID = 1L;


    @Id
    @Column(name="CLI_NUMBER")
    @SequenceGenerator(name="CLIENTE_SEQ", sequenceName="CLIENTE_SEQ", allocationSize=1)
    @GeneratedValue(generator="CLIENTE_SEQ")
    private Long cliNumber;

    @Column(name="CLI_NAME")
    private String cliName;

    @Column(name="CLI_TELEPHONE")
    private String cliTelephone;

    @Column(name="CLI_EMAIL")
    private String cliEmail;

    @Column(name="CLI_DATEBIRTH")
    @JsonSerialize(using = com.dokio.util.JSONSerializer.class)
    @JsonDeserialize(using = com.dokio.util.JSONDeserialize.class)
    private Date cliDatebirth;

    @Column(name="CLI_LASTNAME")
    private String cliLastname;

    @Column(name="CLI_REGISTER")
    private String cliRegister;

    @Column(name="CLI_ADDITIONAL")
    private String cliAdditional;

    @ManyToOne
    @JoinColumn(name = "added_by_user_id")
    private User user;


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
//        DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
//        String date_s = "2011-01-18 00:00:00.0";
//
//        // *** note that it's "yyyy-MM-dd hh:mm:ss" not "yyyy-mm-dd hh:mm:ss"
//        SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
//        Date date = dt.parse(date_s);
//
//        // *** same for the format String below
//        SimpleDateFormat dt1 = new SimpleDateFormat("yyyy-MM-dd");
//        System.out.println(dt1.format(date));
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setSampleData(String id){
        this.setCliName("Mikhail_"+id);
        this.setCliDatebirth(new Date());
        this.setCliLastname("Suntsov_"+1);
        this.setCliRegister("333.222.111-"+id);
    }

}