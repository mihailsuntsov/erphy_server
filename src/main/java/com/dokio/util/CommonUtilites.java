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
package com.dokio.util;
import org.springframework.stereotype.Repository;
import javax.persistence.*;
@Repository
public class CommonUtilites {
    @PersistenceContext
    private EntityManager entityManager;

    @SuppressWarnings("Duplicates")
    //возвращает id статуса, установленного по-умолчанию
    public Long getDocumentsDefaultStatus(Long companyId, int documentId){
        try {
            String stringQuery;
            stringQuery = "select id from sprav_status_dock where company_id=" + companyId + " and dock_id=" + documentId + " and is_default=true";
            Query query = entityManager.createNativeQuery(stringQuery);
            return Long.parseLong(query.getSingleResult().toString());
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    @SuppressWarnings("Duplicates")
    //возвращает id типа цены, установленного по-умолчанию
    public Long getPriceTypeDefault(Long companyId){
        try {
            String stringQuery;
            stringQuery = "select id from sprav_type_prices where company_id=" + companyId + " and is_default=true ";
            Query query = entityManager.createNativeQuery(stringQuery);
            return Long.parseLong(query.getSingleResult().toString());
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
