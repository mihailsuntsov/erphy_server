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

package com.dokio.repository.Exceptions;

// при расчете средней себестоимости кол-во товара в какой то момент истории его изменения стало отрицательным
// это может случиться, если например отменить проведение приёмки, после которой идет продажа товара
// тогда история изменения товара будет такой
//  0
//  +5
//  +10  Эту приемку отменили
//  -10  В результате этой продажи в данном месте истории кол-во товара стало отрицательным (5 - 10 = -5) и кинется этот эксепшен
//  +3
public class CalculateNetcostNegativeSumException extends Exception {
    @Override
    public void printStackTrace() {
        System.err.println("Can't do operation of calculating netcost because of an negative sum");
    }
}
