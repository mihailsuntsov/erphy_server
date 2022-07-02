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

package com.dokio.message.request;

import java.util.Set;

public class UniversalForm {

        private Long        id;
        private Long        id1;
        private Long        id2;
        private Long        id3;
        private Long        id4;
        private String      string1;
        private String      string2;
        private String      string3;
        private Set<Long>   setOfLongs1;
        private Set<Long>   setOfLongs2;
        private String      checked;
        private Boolean     yesNo;

    public Long getId1() {
        return id1;
    }

    public void setId1(Long id1) {
        this.id1 = id1;
    }

    public Set<Long> getSetOfLongs1() {
        return setOfLongs1;
    }

    public void setSetOfLongs1(Set<Long> setOfLongs1) {
        this.setOfLongs1 = setOfLongs1;
    }

    public String getString1() {
        return string1;
    }

    public void setString1(String string1) {
        this.string1 = string1;
    }

    public String getString2() {
        return string2;
    }

    public void setString2(String string2) {
        this.string2 = string2;
    }

    public String getString3() {
        return string3;
    }

    public void setString3(String string3) {
        this.string3 = string3;
    }

    public Long getId2() {
        return id2;
    }

    public void setId2(Long id2) {
        this.id2 = id2;
    }

    public Long getId3() {
        return id3;
    }

    public void setId3(Long id3) {
        this.id3 = id3;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId4() {
        return id4;
    }

    public void setId4(Long id4) {
        this.id4 = id4;
    }

    public Boolean getYesNo() {
        return yesNo;
    }

    public void setYesNo(Boolean yesNo) {
        this.yesNo = yesNo;
    }

    public String getChecked() {
        return checked;
    }

    public Set<Long> getSetOfLongs2() {
        return setOfLongs2;
    }

    public void setSetOfLongs2(Set<Long> setOfLongs2) {
        this.setOfLongs2 = setOfLongs2;
    }

    public void setChecked(String checked) {
        this.checked = checked;
    }

    @Override
    public String toString() {
        return "UniversalForm{" +
                "id=" + id +
                ", id1=" + id1 +
                ", id2=" + id2 +
                ", id3=" + id3 +
                ", id4=" + id4 +
                ", string1='" + string1 + '\'' +
                ", string2='" + string2 + '\'' +
                ", string3='" + string3 + '\'' +
                ", setOfLongs1=" + setOfLongs1 +
                ", setOfLongs2=" + setOfLongs2 +
                ", checked='" + checked + '\'' +
                ", yesNo=" + yesNo +
                '}';
    }
}
