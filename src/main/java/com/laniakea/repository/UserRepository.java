package com.laniakea.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.laniakea.model.User;


// Первый тип (User), переданный в дженерик JpaRepository — класс-сущность,
// с которым должен работать данный репозиторий, второй (Long) — тип первичного ключа.

//Для создания Repository нужно придерживаться несколько правил:
// 1 – Имя репозитория должно начинаться с имени сущности (ClientReposytory) (необязательно).
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
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);

}