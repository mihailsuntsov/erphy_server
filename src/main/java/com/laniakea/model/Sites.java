package com.laniakea.model;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name="sites")
public class Sites {

    @Id
    @Column(name="id")
    @SequenceGenerator(name="traderesults_id_seq", sequenceName="traderesults_id_seq", allocationSize=1)
    @GeneratedValue(generator="traderesults_id_seq")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Companies company;

    @ManyToOne
    @JoinColumn(name = "master_id", nullable = false)
    private User master;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @ManyToOne
    @JoinColumn(name = "changer_id")
    private User changer;

    @Column(name="date_time_created", nullable = false)
    @JsonSerialize(using = com.laniakea.util.JSONSerializer.class)
    @JsonDeserialize(using = com.laniakea.util.JSONDeserialize.class)
    private Timestamp date_time_created;

    @Column(name="date_time_changed")
    @JsonSerialize(using = com.laniakea.util.JSONSerializer.class)
    @JsonDeserialize(using = com.laniakea.util.JSONDeserialize.class)
    private Timestamp date_time_changed;

//    @Column(name = "uid")
//    @Size(max = 36)
    @Column(name="uid", columnDefinition = "UUID")
    private UUID uid; //uid типа a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11, не используется сейчас

    @Column(name = "name")
    @Size(max = 128)
    private String name;

    @Column(name = "description")
    @Size(max = 512)
    private String description;

    @Column(name = "stopped")//Остановлен
    private Boolean stopped;

    @Column(name = "domain_associated")//Связан с доменом
    private Boolean domain_associated;

    @Column(name = "domain")//Домен (например site.ru)
    @Size(max = 255)
    private String domain;

    @Column(name = "is_archive")//Удалён
    private Boolean is_archive;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Companies getCompany() {
        return company;
    }

    public void setCompany(Companies company) {
        this.company = company;
    }

    public User getMaster() {
        return master;
    }

    public void setMaster(User master) {
        this.master = master;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public User getChanger() {
        return changer;
    }

    public void setChanger(User changer) {
        this.changer = changer;
    }

    public Timestamp getDate_time_created() {
        return date_time_created;
    }

    public void setDate_time_created(Timestamp date_time_created) {
        this.date_time_created = date_time_created;
    }

    public Timestamp getDate_time_changed() {
        return date_time_changed;
    }

    public void setDate_time_changed(Timestamp date_time_changed) {
        this.date_time_changed = date_time_changed;
    }

    public String getUid() {
        return uid.toString();
    }

    public void setUid(UUID uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getStopped() {
        return stopped;
    }

    public void setStopped(Boolean stopped) {
        this.stopped = stopped;
    }

    public Boolean getDomain_associated() {
        return domain_associated;
    }

    public void setDomain_associated(Boolean domain_associated) {
        this.domain_associated = domain_associated;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public Boolean getIs_archive() {
        return is_archive;
    }

    public void setIs_archive(Boolean is_archive) {
        this.is_archive = is_archive;
    }
}
