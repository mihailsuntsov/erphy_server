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

package com.dokio.message.response.additional;

import java.util.List;

public class DeleteDocsReport {

    private Integer                 result;     // Код результата. 0 - Ok, 1 - ошибка, 2 - нет прав, 3 - есть связанные документы
    private List<LinkedDocsJSON>    docs;       // Информация о документах (например, документы, у которых есть дочерние связи)

    public Integer getResult() {
        return result;
    }

    public void setResult(Integer result) {
        this.result = result;
    }

    public List<LinkedDocsJSON> getDocs() {
        return docs;
    }

    public void setDocs(List<LinkedDocsJSON> docs) {
        this.docs = docs;
    }
}
