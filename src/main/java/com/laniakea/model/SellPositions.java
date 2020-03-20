package com.laniakea.model;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name="sell_positions")
public class SellPositions {

    @Id
    @Column(name="id")
    @SequenceGenerator(name="sell_positions_id_seq", sequenceName="sell_positions_id_seq", allocationSize=1)
    @GeneratedValue(generator="sell_positions_id_seq")
    private Long id;

    @Column(name = "sell_price")
    private BigDecimal sellPrice;

    @Column(name = "count_")
    private Long count_;

    @Column(name = "commentary")
    private String commentary;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Products product;

    @ManyToOne
    @JoinColumn(name = "kassa_operation_id", nullable = false)
    private KassaOperations kassaOperation;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(BigDecimal sellPrice) {
        this.sellPrice = sellPrice;
    }

    public Long getCount_() {
        return count_;
    }

    public void setCount_(Long count_) {
        this.count_ = count_;
    }

    public String getCommentary() {
        return commentary;
    }

    public void setCommentary(String commentary) {
        this.commentary = commentary;
    }

    public Products getProduct() {
        return product;
    }

    public void setProduct(Products product) {
        this.product = product;
    }

    public KassaOperations getKassaOperation() {
        return kassaOperation;
    }

    public void setKassaOperation(KassaOperations kassaOperation) {
        this.kassaOperation = kassaOperation;
    }
}
