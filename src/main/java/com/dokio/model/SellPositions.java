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
