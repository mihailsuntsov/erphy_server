package com.laniakea.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.laniakea.model.Sprav.SpravSysChequeTypes;
import com.laniakea.model.Sprav.SpravSysKassaOperations;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="kassa_operations")
public class KassaOperations {

    @Id
    @Column(name="id")
    @SequenceGenerator(name="kassa_operations_id_seq", sequenceName="kassa_operations_id_seq", allocationSize=1)
    @GeneratedValue(generator="kassa_operations_id_seq")
    private Long id;

    @Column(name="date_")
    @JsonSerialize(using = com.laniakea.util.JSONSerializer.class)
    @JsonDeserialize(using = com.laniakea.util.JSONDeserialize.class)
    private Date date_;

    @Column(name = "commentary")
    private String commentary;

    @ManyToOne
    @JoinColumn(name = "cheque_type_id", nullable = false)
    private SpravSysChequeTypes spravSysChequeType;

    @ManyToOne
    @JoinColumn(name = "operation_id", nullable = false)
    private SpravSysKassaOperations spravSysKassaOperation;

    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    private Sessions session;

    @OneToMany(mappedBy = "kassaOperation", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<SellPositions> sellPositions = new HashSet<SellPositions>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDate_() {
        return date_;
    }

    public void setDate_(Date date_) {
        this.date_ = date_;
    }

    public String getCommentary() {
        return commentary;
    }

    public void setCommentary(String commentary) {
        this.commentary = commentary;
    }

    public SpravSysChequeTypes getSpravSysChequeType() {
        return spravSysChequeType;
    }

    public void setSpravSysChequeType(SpravSysChequeTypes spravSysChequeType) {
        this.spravSysChequeType = spravSysChequeType;
    }

    public SpravSysKassaOperations getSpravSysKassaOperation() {
        return spravSysKassaOperation;
    }

    public void setSpravSysKassaOperation(SpravSysKassaOperations spravSysKassaOperation) {
        this.spravSysKassaOperation = spravSysKassaOperation;
    }

    public Sessions getSession() {
        return session;
    }

    public void setSession(Sessions session) {
        this.session = session;
    }

    public Set<SellPositions> getSellPositions() {
        return sellPositions;
    }

    public void setSellPositions(Set<SellPositions> sellPositions) {
        this.sellPositions = sellPositions;
    }
}
