/*
Copyright © 2020 Сунцов Михаил Александрович. mihail.suntsov@yandex.ru
Эта программа является свободным программным обеспечением: Вы можете распространять ее и (или) изменять,
соблюдая условия Генеральной публичной лицензии GNU Affero GPL редакции 3 (GNU AGPLv3),
опубликованной Фондом свободного программного обеспечения;
Эта программа распространяется в расчёте на то, что она окажется полезной, но
БЕЗ КАКИХ-ЛИБО ГАРАНТИЙ, включая подразумеваемую гарантию КАЧЕСТВА либо
ПРИГОДНОСТИ ДЛЯ ОПРЕДЕЛЕННЫХ ЦЕЛЕЙ. Ознакомьтесь с Генеральной публичной
лицензией GNU для получения более подробной информации.
Вы должны были получить копию Генеральной публичной лицензии GNU вместе с этой
программой. Если Вы ее не получили, то перейдите по адресу: http://www.gnu.org/licenses
*/
package com.dokio.message.request;

import java.math.BigDecimal;

public class WithdrawalForm {

    private Long id;
    private Long company_id;
    private Long department_id; // отделение в котором установлена касса
    private Long creator_id; // кассир, залогиненный на кассе (Внимание! не в системе! На кассем может залогиниться другой человек)
    private Long kassa_id; // od кассы


    private String description;
    private String doc_number;
    private BigDecimal summ;
    private Boolean is_completed;// -- проведено - всегда true, т.к. выемка создается уже проведенной, не редактируется, не проводится и не удаляется,
    private Boolean is_delivered;// проведён
    private String uid;
    private Long linked_doc_id;//id связанного документа
    private String linked_doc_name;//имя (таблицы) связанного документа
    private String parent_uid;// uid исходящего (родительского) документа
    private String child_uid; // uid дочернего документа. Дочерний - не всегда тот, которого создают из текущего документа. Например, при создании из Отгрузки Счёта покупателю - Отгрузка будет дочерней для него.



/*
    is_delivered boolean,  -- деньги доставлены до кассы предприятия (false = "зависшие деньги" - между кассой ККМ и кассой предприятия)
    is_completed boolean,
    uid varchar (36),
    linked_docs_group_id bigint,*/

}