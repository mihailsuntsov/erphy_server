/*
        Dokio CRM - server part. Sales, finance and warehouse management system
        Copyright (C) Mikhail Suntsov /mihail.suntsov@gmail.com/

        This program is free software: you can redistribute it and/or modify
        it under the terms of the GNU Affero General Public License as
        published by the Free Software Foundation, either version 3 of the
        License, or (at your option) any later version.

        This program is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU Affero General Public License for more details.

        You should have received a copy of the GNU Affero General Public License
        along with this program.  If not, see <https://www.gnu.org/licenses/>
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

