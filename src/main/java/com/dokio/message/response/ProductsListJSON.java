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
package com.dokio.message.response;

import java.math.BigDecimal;

public class ProductsListJSON {
    private Long id;
    private String name;
    private Integer nds_id;
    private Long edizm_id;
    private String filename;
    private BigDecimal total;
    private BigDecimal reserved;
    private BigDecimal total_in_all_my_depths;//всего товаров во всех моих отделениях (складах)
    private BigDecimal reserved_in_all_my_depths;//зарезервировано товаров во всех моих отделениях (складах)


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getNds_id() {
        return nds_id;
    }

    public void setNds_id(Integer nds_id) {
        this.nds_id = nds_id;
    }

    public Long getEdizm_id() {
        return edizm_id;
    }

    public void setEdizm_id(Long edizm_id) {
        this.edizm_id = edizm_id;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getReserved() {
        return reserved;
    }

    public void setReserved(BigDecimal reserved) {
        this.reserved = reserved;
    }

    public BigDecimal getTotal_in_all_my_depths() {
        return total_in_all_my_depths;
    }

    public void setTotal_in_all_my_depths(BigDecimal total_in_all_my_depths) {
        this.total_in_all_my_depths = total_in_all_my_depths;
    }

    public BigDecimal getReserved_in_all_my_depths() {
        return reserved_in_all_my_depths;
    }

    public void setReserved_in_all_my_depths(BigDecimal reserved_in_all_my_depths) {
        this.reserved_in_all_my_depths = reserved_in_all_my_depths;
    }
}
