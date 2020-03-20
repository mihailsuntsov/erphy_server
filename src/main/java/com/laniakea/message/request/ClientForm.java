//Класс для преобразования CRUD-запросов в объект
//Например, в Контроллере:
//    @PostMapping("/api/auth/getClientById")
//    public ResponseEntity<?> getClientById(@RequestBody ClientForm clientRequest) {
//        int id = clientRequest.getId();
//        ...

package com.laniakea.message.request;

public class ClientForm {
    private int id; //ФИО
    private String name; //ФИО
    private String lastname; //
    private String dateBirth; //
    private String register; //
    private String telephone;
    private String email;
    private String additional;
    private String checked;//для массива удаления

    public String getAdditional() {
        return additional;
    }

    public void setAdditional(String additional) {
        this.additional = additional;
    }

    public String getChecked() {
        return checked;
    }

    public void setChecked(String checked) {
        this.checked = checked;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getDateBirth() {
        return dateBirth;
    }

    public void setDateBirth(String dateBirth) {
        this.dateBirth = dateBirth;
    }

    public String getRegister() {
        return register;
    }

    public void setRegister(String register) {
        this.register = register;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
