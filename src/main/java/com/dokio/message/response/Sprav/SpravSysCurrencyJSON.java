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
package com.dokio.message.response.Sprav;
    public class SpravSysCurrencyJSON {
        private Long id;
        private String name_okb;
        private String name_short;
        private String name_iso;
        private String name_fraction;
        private String name_fraction_short;
        private String charcode_iso;
        private Integer numcode_iso;
        private String symbol_unicode;
        private Boolean show_in_list;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName_okb() {
            return name_okb;
        }

        public void setName_okb(String name_okb) {
            this.name_okb = name_okb;
        }

        public String getName_short() {
            return name_short;
        }

        public void setName_short(String name_short) {
            this.name_short = name_short;
        }

        public String getName_iso() {
            return name_iso;
        }

        public void setName_iso(String name_iso) {
            this.name_iso = name_iso;
        }

        public String getName_fraction() {
            return name_fraction;
        }

        public void setName_fraction(String name_fraction) {
            this.name_fraction = name_fraction;
        }

        public Boolean getShow_in_list() {
            return show_in_list;
        }

        public void setShow_in_list(Boolean show_in_list) {
            this.show_in_list = show_in_list;
        }

        public String getName_fraction_short() {
            return name_fraction_short;
        }

        public void setName_fraction_short(String name_fraction_short) {
            this.name_fraction_short = name_fraction_short;
        }

        public String getCharcode_iso() {
            return charcode_iso;
        }

        public void setCharcode_iso(String charcode_iso) {
            this.charcode_iso = charcode_iso;
        }

        public Integer getNumcode_iso() {
            return numcode_iso;
        }

        public void setNumcode_iso(Integer numcode_iso) {
            this.numcode_iso = numcode_iso;
        }

        public String getSymbol_unicode() {
            return symbol_unicode;
        }

        public void setSymbol_unicode(String symbol_unicode) {
            this.symbol_unicode = symbol_unicode;
        }

    }

