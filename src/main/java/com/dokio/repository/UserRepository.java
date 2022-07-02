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

package com.dokio.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dokio.model.User;


// Первый тип (User), переданный в дженерик JpaRepository — класс-сущность,
// с которым должен работать данный репозиторий, второй (Long) — тип первичного ключа.

//Для создания Repository нужно придерживаться несколько правил:
// 1 – Имя репозитория должно начинаться с имени сущности (ClientRepository) (необязательно).
// 2 – Второй дженерик (например Long) должен быть оберточным типом того типа которым есть
// ID нашей сущности (обязательно).
// 3 – Первый дженерик (User) должен быть объектом нашей сущности для которой мы создали репозиторий,
// это указывает на то, что Spring Data должен предоставить реализацию методов для работы
// с этой сущностью (обязательно).
// 4 – Мы должны унаследовать свой интерфейс от JpaRepository, иначе Spring Data
// не предоставит реализацию для нашего репозитория (обязательно).
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
// Имплементация не требуется. При инициализации контекста приложения Spring Data
// найдёт данный интерфейс и самостоятельно сгенерирует компонент (bean),
// реализующий данный интерфейс.

    Optional<User> findByUsername(String username);
    Optional<User> findById(Long id);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    User findByActivationCode(String code);
    User findByRepairPassCode(String code);
    User findByEmail(String email);
}