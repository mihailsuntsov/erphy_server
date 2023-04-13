alter table sprav_status_dock add column is_default boolean;

**********************************************************************************************************
**********************************************************************************************************
**********************************************************************************************************

alter table customers_orders drop column fio;
alter table customers_orders drop column is_archive;
alter table customers_orders add column is_deleted boolean;
--ALTER TABLE customers_orders DROP COLUMN zip_code; уже сделано
--ALTER TABLE customers_orders ADD COLUMN zip_code varchar(40); уже сделано
--update customers_orders set zip_code = ''; уже сделано
alter table sprav_type_prices add column is_default boolean;

**********************************************************************************************************
**********************************************************************************************************
**********************************************************************************************************

alter table customers_orders_product add column department_id bigint not null;
alter table customers_orders_product add constraint department_id_fkey foreign key (department_id) references departments (id);
alter table customers_orders_product add column shipped_count numeric(15,3);

alter table departments add column is_deleted boolean;

update departments set is_deleted=is_archive;

alter table customers_orders_product add column product_price_of_type_price numeric(12,2);
alter table customers_orders_product add column reserve boolean;
alter table customers_orders_product drop column additional;
alter table customers_orders_product drop column reserved;
alter table customers_orders_product drop column shipped_count;
alter table customers_orders_product drop constraint customers_orders_product_uq;
ALTER TABLE customers_orders_product ADD CONSTRAINT customers_orders_product_uq UNIQUE (customers_orders_id, product_id, department_id) ;

alter table customers_orders_product add column master_id bigint not null;
alter table customers_orders_product add column company_id bigint not null;
ALTER TABLE customers_orders alter COLUMN master_id TYPE bigint USING master_id::bigint;
ALTER TABLE customers_orders alter COLUMN creator_id TYPE bigint USING creator_id::bigint;
ALTER TABLE customers_orders alter COLUMN changer_id TYPE bigint USING changer_id::bigint;
ALTER TABLE customers_orders alter COLUMN company_id TYPE bigint USING company_id::bigint;
ALTER TABLE customers_orders alter COLUMN department_id TYPE bigint USING department_id::bigint;
ALTER TABLE customers_orders alter COLUMN cagent_id TYPE bigint USING cagent_id::bigint;

delete from shipment_product;
alter table shipment_product add column master_id bigint not null;
alter table shipment_product add column company_id bigint not null;
alter table shipment_product drop column additional;
ALTER TABLE shipment alter COLUMN master_id TYPE bigint USING master_id::bigint;
ALTER TABLE shipment alter COLUMN creator_id TYPE bigint USING creator_id::bigint;
ALTER TABLE shipment alter COLUMN changer_id TYPE bigint USING changer_id::bigint;
ALTER TABLE shipment alter COLUMN company_id TYPE bigint USING company_id::bigint;
ALTER TABLE shipment alter COLUMN department_id TYPE bigint USING department_id::bigint;
ALTER TABLE shipment alter COLUMN cagent_id TYPE bigint USING cagent_id::bigint;

alter table customers_orders_product add column id bigint not null;
CREATE SEQUENCE customers_orders_product_id_seq START 1;
alter table customers_orders_product alter column id set default nextval ('customers_orders_product_id_seq');
alter sequence customers_orders_product_id_seq owned by customers_orders_product.id;

alter table customers_orders_product alter price_type_id drop not null;

create table settings_customers_orders (
    id                  bigserial primary key not null,
    master_id           bigint not null, 
    company_id          bigint not null,  
    user_id             bigint not null,
    pricing_type        varchar(16), 
    price_type_id       bigint,
    change_price        numeric(12,2),
    plus_minus          varchar(8),
    change_price_type   varchar(8),
    hide_tenths         boolean,
    save_settings       boolean,
    foreign key (price_type_id) references sprav_type_prices (id),
    foreign key (master_id) references users(id),
    foreign key (user_id) references users(id),
    foreign key (company_id) references companies(id)
);
ALTER TABLE settings_customers_orders ADD CONSTRAINT settings_customers_orders_user_uq UNIQUE (user_id);

alter table settings_customers_orders add column department_id bigint;
alter table settings_customers_orders add constraint department_id_fkey foreign key (department_id) references departments (id);
alter table settings_customers_orders add column customer_id bigint;
alter table settings_customers_orders add constraint customer_id_fkey foreign key (customer_id) references cagents (id);

alter table settings_customers_orders add column priority_type_price_side varchar(8);
alter table settings_customers_orders add column name varchar(120);

--удаление старых таблиц
drop table sell_positions;
drop table kassa_operations;
drop table sessions;
drop table trading_equipment;
drop table sprav_sys_cheque_types;
drop table sprav_sys_kassa_operations;
drop table sprav_sys_trading_equipment;

create table sprav_sys_cheque_types(
    id              int not null,
    name            varchar(100),
    name_api_atol   varchar(32)
);

insert into sprav_sys_cheque_types (id, name, name_api_atol) values
(1,'Чек прихода','sell'),
(2,'Чек возврата прихода','sellReturn'),
(4,'Чек расхода','buy'),
(5,'Чек возврата расхода','buyReturn'),
(7,'Чек коррекции прихода','sellCorrection'),
(8,'Чек коррекции возврата прихода','sellReturnCorrection'),
(9,'Чек коррекции расхода','buyCorrection'),
(10,'Чек коррекции возврата расхода','buyReturnCorrection');

create table sprav_sys_taxation_types(
    id              int not null,
    name            varchar(300) not null,
    name_api_atol   varchar(32) not null,
    is_active       boolean not null
);

insert into sprav_sys_taxation_types (id, name, name_api_atol, is_active) values
(1,'Общая','osn',true),
(2,'Упрощенная доход','usnIncome',true),
(3,'Упрощенная доход минус расход','usnIncomeOutcome',true),
(4,'Единый налог на вменённый доход','envd',false),
(5,'Единый сельскохозяйственный налог','esn',true),
(6,'Патентная система налогообложения','patent',true);

create table sprav_sys_payment_methods(
    id              int not null,
    name            varchar(300) not null,
    id_api_atol     int not null,
    name_api_atol   varchar(32) not null
);

insert into sprav_sys_payment_methods (id, name, id_api_atol, name_api_atol) values
(1,'Наличными',0,'cash'),
(2,'Безналичными',1,'electronically'),
(3,'Предварительная оплата (аванс)',2,'prepaid'),
(4,'Последующая оплата (кредит)',3,'credit'),
(5,'Иная форма оплаты',4,'other');

alter table sprav_sys_nds add column name_api_atol varchar(30);
alter table sprav_sys_nds add column is_active boolean;
update sprav_sys_nds set is_active=true;

update sprav_sys_nds set name_api_atol='none' where id=1;
update sprav_sys_nds set name_api_atol='vat0' where id=4;
update sprav_sys_nds set name_api_atol='vat10' where id=3;
update sprav_sys_nds set name_api_atol='vat20' where id=2;

alter table sprav_sys_nds add column calculated boolean;
update sprav_sys_nds set calculated=false;

insert into sprav_sys_nds (name, name_api_atol, is_active, calculated) values
('10/110','vat110',true,true),
('20/120','vat120',true,true);
update sprav_sys_nds set description='' where calculated=true;

drop table if exists spravsysndsjson;

alter table sprav_sys_ppr add column id_api_atol int;
alter table sprav_sys_ppr add column name_api_atol varchar(100);

update sprav_sys_ppr set name='Товар', id_api_atol=1, name_api_atol='commodity' where id=1;
update sprav_sys_ppr set name='Подакцизный товар', id_api_atol=2, name_api_atol='excise' where id=2;
update sprav_sys_ppr set name='Работа', id_api_atol=3, name_api_atol='job' where id=3;
update sprav_sys_ppr set name='Услуга', id_api_atol=4, name_api_atol='service' where id=4;

insert into sprav_sys_ppr (name, abbreviation, description, id_api_atol, name_api_atol) values
('Ставка азартной игры','','',5,'gamblingBet'),
('Выигрыш азартной игры','','',6,'gamblingPrize'),
('Лотерейный билет','','',7,'lottery'),
('Выигрыш лотереи','','',8,'lotteryPrize'),
('Предост. рез-тов интелл. деятельности','','',9,'intellectualActivity'),
('Платёж','','',10,'payment'),
('Агентское вознаграждение','','',11,'agentCommission'),
('Выплата','','',12,'pay'),
('Иной предмет расчета','','',13,'another'),
('Имущественное право','','',14,'proprietaryLaw'),
('Внереализационный доход','','',15,'nonOperatingIncome'),
('Иные платежи и взносы','','',16,'otherContributions'),
('Торговый сбор','','',17,'merchantTax'),
('Курортный сбор','','',18,'resortFee'),
('Залог','','',19,'deposit'),
('Расход','','',20,'consumption'),
('Взносы на ОПС ИП','','',21,'soleProprietorCPIContributions'),
('Взносы на ОПС','','',22,'cpiContributions'),
('Взносы на ОМС ИП','','',23,'soleProprietorCMIContributions'),
('Взносы на ОМС','','',24,'cmiContributions'),
('Взносы на ОСС','','',25,'csiContributions'),
('Платеж казино','','',26,'casinoPayment');

alter table users add column vatin varchar(30);
------------------------------------------------------------------
ALTER TABLE sprav_sys_taxation_types ADD CONSTRAINT sprav_sys_taxation_types_id_uq UNIQUE (id);
create table kassa(
    id                bigserial primary key not null,
    master_id         bigint not null, 
    creator_id        bigint not null, 
    changer_id        bigint, 
    company_id        bigint not null,
    department_id     bigint not null,
    date_time_created timestamp with time zone not null,
    date_time_changed timestamp with time zone,
    name              varchar(60) not null,
    server_type       varchar(20) not null,
    sno1_id           int not null not null,
    billing_address   varchar(500) not null,
    device_server_uid varchar(20) not null,
    additional        varchar(1000),
    server_address    varchar(300) not null,
    allow_to_use      boolean not null,
    is_deleted        boolean,

    foreign key (master_id) references users(id),
    foreign key (creator_id) references users(id),
    foreign key (changer_id) references users(id),
    foreign key (company_id) references companies(id),
    foreign key (department_id) references departments(id),
    foreign key (sno1_id) references sprav_sys_taxation_types(id)
);

create table kassa_user_settings(
    user_id                bigint primary key not null,
    master_id              bigint not null, 
    company_id             bigint not null,
    selected_kassa_id	   bigint not null,
    --кассир: 'current'-текущая учетная запись, 'another'-другая учетная запись, 'custom' произвольные ФИО
    cashier_value_id       varchar(8),
    customCashierFio       varchar(30),
    customCashierVatin     varchar(12),
    --адрес места расчётов. 'Settings' - как в настройках кассы, 'customer' - брать из адреса заказчика, 'custom' произвольный адрес
    billing_address        varchar(8),
    custom_billing_address varchar(500),
    foreign key (selected_kassa_id) references kassa(id),
    foreign key (user_id) references users(id),
    foreign key (master_id) references users(id),
    foreign key (company_id) references companies(id)
);
ALTER TABLE sprav_sys_taxation_types ADD column short_name varchar(30);
update sprav_sys_taxation_types set short_name='ОСН' where id=1;
update sprav_sys_taxation_types set short_name='УСН доход' where id=2;
update sprav_sys_taxation_types set short_name='УСН доход-расход' where id=3;
update sprav_sys_taxation_types set short_name='ЕНВД' where id=4;
update sprav_sys_taxation_types set short_name='ЕСХН' where id=5;
update sprav_sys_taxation_types set short_name='Патент' where id=6;

ALTER TABLE sprav_sys_ppr ADD column is_material boolean;

update sprav_sys_ppr set is_material=true where id in(1,2,7,13);
update sprav_sys_ppr set is_material=false where id not in(1,2,7,13);


ALTER TABLE customers_orders_product ADD column reserved_current numeric(15,3);

alter table customers_orders_product drop column reserve;


alter table settings_customers_orders add column autocreate_on_start boolean;
alter table settings_customers_orders add column autocreate_on_cheque boolean;

alter table settings_customers_orders add column status_id_on_autocreate_on_cheque bigint;
alter table settings_customers_orders add constraint status_id_on_autocreate_on_cheque_fkey foreign key (status_id_on_autocreate_on_cheque) references sprav_status_dock (id);

-- удалить вручную "product_prices_uq" в product_prices

ALTER TABLE product_prices ADD CONSTRAINT product_prices_uq UNIQUE (product_id, price_type_id) ;

insert into documents (name,page_name,show) values ('Кассы онлайн','kassa',1);

insert into permissions (name,description,document_name,document_id) values
('Боковая панель - отображать в списке документов','Показывать документ в списке документов на боковой панели','Кассы онлайн',24),
('Создание документов по всем предприятиям','Возможность создавать новые документы "Кассы онлайн" по всем предприятиям','Кассы онлайн',24),
('Создание документов своего предприятия','Возможность создавать новые документы "Кассы онлайн" своего предприятия','Кассы онлайн',24),
('Создание документов своих отделений','Возможность создавать новые документы "Кассы онлайн" по своим отделениям','Кассы онлайн',24),
('Удаление документов по всем предприятиям','Возможность удалить документ "Кассы онлайн" в архив по всем предприятиям','Кассы онлайн',24),
('Удаление документов своего предприятия','Возможность удалить документ "Кассы онлайн" своего предприятия в архив','Кассы онлайн',24),
('Удаление документов своих отделений','Возможность удалить документ "Кассы онлайн" одного из своих отделений','Кассы онлайн',24),
('Просмотр документов по всем предприятиям','Прсмотр информации в документах "Кассы онлайн" по всем предприятиям','Кассы онлайн',24),
('Просмотр документов своего предприятия','Прсмотр информации в документах "Кассы онлайн" своего предприятия','Кассы онлайн',24),
('Просмотр документов своих отделений','Прсмотр информации в документах "Кассы онлайн" по своим отделениям','Кассы онлайн',24),
('Редактирование документов по всем предприятиям','Редактирование документов "Кассы онлайн" по всем предприятиям','Кассы онлайн',24),
('Редактирование документов своего предприятия','Редактирование документов "Кассы онлайн" своего предприятия','Кассы онлайн',24),
('Редактирование документов своих отделений','Редактирование документов "Кассы онлайн" по своим отделениям','Кассы онлайн',24);

**********************************************************************************************************
**********************************************************************************************************
**********************************************************************************************************
create table shifts(
 id bigserial primary key not null,
 master_id  bigint not null, 
 creator_id bigint not null,
 closer_id bigint,
 date_time_created timestamp with time zone not null,
 date_time_closed timestamp with time zone,
 company_id bigint not null,  
 department_id bigint not null,
 kassa_id bigint not null,
 shift_number int not null,
 
 foreign key (master_id) references users(id),
 foreign key (creator_id) references users(id),
 foreign key (closer_id) references users(id),
 foreign key (company_id) references companies(id),
 foreign key (department_id) references departments(id)
);

create table retail_sales(
 id bigserial primary key not null,
 master_id  bigint not null, 
 creator_id bigint not null,
 changer_id bigint,
 date_time_created timestamp with time zone not null,
 date_time_changed timestamp with time zone,
 company_id bigint not null,  
 department_id bigint not null,
 customers_orders_id bigint,
 shift_id bigint,
 cagent_id bigint not null,
 status_id bigint,
 doc_number int not null,
 name varchar(120),
 description varchar(2048),
 nds boolean,
 nds_included boolean,
 is_deleted boolean,
 
 foreign key (master_id) references users(id),
 foreign key (creator_id) references users(id),
 foreign key (changer_id) references users(id),
 foreign key (shift_id) references shifts(id),
 foreign key (customers_orders_id) references customers_orders(id),
 foreign key (company_id) references companies(id),
 foreign key (department_id) references departments(id),
 foreign key (cagent_id) references cagents(id),
 foreign key (status_id) references sprav_status_dock (id) ON DELETE SET NULL
);

insert into documents (name,page_name,show) values ('Розничные продажи','retailsales',1);

insert into permissions (name,description,document_name,document_id) values
('Боковая панель - отображать в списке документов','Показывать документ в списке документов на боковой панели','Розничные продажи',25),
('Создание документов по всем предприятиям','Возможность создавать новые документы "Розничные продажи" по всем предприятиям','Розничные продажи',25),
('Создание документов своего предприятия','Возможность создавать новые документы "Розничные продажи" своего предприятия','Розничные продажи',25),
('Создание документов своих отделений','Возможность создавать новые документы "Розничные продажи" по своим отделениям','Розничные продажи',25),
('Удаление документов по всем предприятиям','Возможность удалить документ "Розничные продажи" в архив по всем предприятиям','Розничные продажи',25),
('Удаление документов своего предприятия','Возможность удалить документ "Розничные продажи" своего предприятия в архив','Розничные продажи',25),
('Удаление документов своих отделений','Возможность удалить документ "Розничные продажи" одного из своих отделений','Розничные продажи',25),
('Удаление документов созданных собой','Возможность удалить документ "Розничные продажи", созданных собой','Розничные продажи',25),
('Просмотр документов по всем предприятиям','Прсмотр информации в документах "Розничные продажи" по всем предприятиям','Розничные продажи',25),
('Просмотр документов своего предприятия','Прсмотр информации в документах "Розничные продажи" своего предприятия','Розничные продажи',25),
('Просмотр документов своих отделений','Прсмотр информации в документах "Розничные продажи" по своим отделениям','Розничные продажи',25),
('Просмотр документов созданных собой','Прсмотр информации в документах "Розничные продажи", созданных собой','Розничные продажи',25),
('Редактирование документов по всем предприятиям','Редактирование документов "Розничные продажи" по всем предприятиям','Розничные продажи',25),
('Редактирование документов своего предприятия','Редактирование документов "Розничные продажи" своего предприятия','Розничные продажи',25),
('Редактирование документов своих отделений','Редактирование документов "Розничные продажи" по своим отделениям','Розничные продажи',25),
('Редактирование документов созданных собой','Редактирование документов "Розничные продажи", созданных собой','Розничные продажи',25);

create table retail_sales_product (
 id bigserial primary key not null,
 master_id bigint not null,
 company_id bigint not null,
 product_id bigint not null,
 retail_sales_id bigint not null,
 product_count numeric(15,3) not null,
 product_price numeric(12,2),
 product_sumprice numeric(15,2),
 edizm_id int not null,
 price_type_id bigint,
 nds_id bigint not null,
 department_id bigint not null,
 product_price_of_type_price numeric(12,2),

 foreign key (retail_sales_id) references retail_sales (id),
 foreign key (edizm_id) references sprav_sys_edizm (id),
 foreign key (nds_id) references sprav_sys_nds (id),
 foreign key (price_type_id) references sprav_type_prices (id),
 foreign key (product_id ) references products (id),
 foreign key (department_id ) references departments (id)
);

ALTER TABLE retail_sales_product ADD CONSTRAINT retail_sales_product_uq UNIQUE (product_id, retail_sales_id, department_id);

create table settings_retail_sales (
    id                          bigserial primary key not null,
    master_id                   bigint not null, 
    company_id                  bigint not null,  
    user_id                     bigint not null,
    customer_id                 bigint,
    department_id               bigint,
    name                        varchar(120),
    priority_type_price_side    varchar(8),
    pricing_type                varchar(16), 
    price_type_id               bigint,
    change_price                numeric(12,2),
    plus_minus                  varchar(8),
    change_price_type           varchar(8),
    hide_tenths                 boolean,
    save_settings               boolean,
    autocreate_on_cheque        boolean,
    status_id_on_autocreate_on_cheque bigint,
    foreign key (price_type_id) references sprav_type_prices (id),
    foreign key (master_id) references users(id),
    foreign key (user_id) references users(id),
    foreign key (company_id) references companies(id)
);
alter table settings_retail_sales add constraint settings_retail_sales_user_uq UNIQUE (user_id);
alter table settings_retail_sales add constraint department_id_fkey foreign key (department_id) references departments (id);
alter table settings_retail_sales add constraint customer_id_fkey foreign key (customer_id) references cagents (id);
alter table settings_retail_sales add constraint status_id_on_autocreate_on_cheque_fkey foreign key (status_id_on_autocreate_on_cheque) references sprav_status_dock (id);

---------------  после паузы в программировании  --------------------------------

update documents set name='Розничная продажа' where id=25;

create table receipts(
 id bigserial primary key not null,
 master_id bigint not null,
 creator_id bigint not null,
 company_id bigint not null,
 department_id bigint not null,
 kassa_id bigint not null, -- id кассового аппарата
 shift_id bigint not null, --id смены 
 document_id int not null, -- id документа, в котором был отбит чек (например, розничные продажи - 25)
 retail_sales_id bigint, -- если чек из розничных продаж - ставится id розничной продажи
 date_time_created timestamp with time zone not null, --дата и время создания чека
 operation_id varchar(64), -- sell, buyCorrection, sellReturnCorrection ...
 sno_id int not null, -- id системы налогообложения кассы 
 billing_address varchar(256), -- место расчета
 payment_type varchar(16), -- тип оплаты (нал, бнал, смешанная) cash | electronically | mixed
 cash numeric(15,2), -- сколько оплачено налом
 electronically numeric(15,2), -- склько оплачено безналом
 foreign key (master_id) references users(id),
 foreign key (creator_id) references users(id),
 foreign key (company_id) references companies(id),
 foreign key (department_id) references departments(id),
 foreign key (kassa_id) references kassa(id),
 foreign key (shift_id) references shifts(id),
 foreign key (document_id) references documents(id),
 foreign key (retail_sales_id) references retail_sales(id),
 foreign key (sno_id) references sprav_sys_taxation_types(id)
)

alter table retail_sales add column receipt_id bigint; --id чека, отбитого из розничной продажи
alter table retail_sales add constraint receipt_id_fkey foreign key (receipt_id) references receipts (id);

alter table kassa add column zn_kkt varchar(64); --заводской номер ккт
alter table kassa add constraint znkkt_company_uq UNIQUE (company_id, zn_kkt); -- заводской номер кассы в пределах предприятия должен быть уникальный. Почему в пределах а не вообще? Потому что master (владелец предприятий) может перерегистрировать кассу на другое свое предприятие. Так же, в облачной версии Докио, владелец кассы может снять ее с регистрации и продать другому пользователю Докио.

alter table shifts add column zn_kkt varchar(64) not null; --заводской номер ккт
alter table shifts add column shift_status_id varchar(8) not null; --статус смены: opened closed expired
alter table shifts add column shift_expired_at varchar(32) not null; -- время истечения (экспирации) смены, генерируется ККМ в виде строки
alter table shifts add column fn_serial varchar(32) not null; --серийный номер ФН
alter table shifts add constraint kassaid_shiftnumber_fnserial_uq UNIQUE (kassa_id, shift_number, fn_serial); --по каждой кассе должна быть только одна открытая смена. Номер смены сбрасывается при смене ФН, и он не может обеспечить уникальность смены ККМ, поэтому для уникальности смены также берется серийный номер ФН

CREATE SEQUENCE developer_shiftnum START 1;

create table settings_dashboard (
    id                  bigserial primary key not null,
    master_id           bigint not null, 
    user_id             bigint not null,
    company_id          bigint not null,  
    foreign key (master_id) references users(id),
    foreign key (user_id) references users(id),
    foreign key (company_id) references companies(id)
);

alter table settings_dashboard add constraint settings_dashboard_user_uq UNIQUE (user_id);

insert into documents (name,page_name,show) values ('Стартовая страница','retailsales',1);

insert into permissions (name,description,document_name,document_id) values
('Отображать','Показывать стартовую страницу','Стартовая страница',26),
('Отчёт "Объёмы" - просмотр по всем предприятиям','Возможность построения отчётов по объёмам продаж, закупок и др. по всем предприятиям','Стартовая страница',26);
insert into permissions (name,description,document_name,document_id) values
('Отчёт "Объёмы" - просмотр по своему предприятию','Возможность построения отчётов по объёмам продаж, закупок и др. по всем отделениям своего предпрития','Стартовая страница',26),
('Отчёт "Объёмы" - просмотр по своим отделениям','Возможность построения отчётов по объёмам продаж, закупок и др. по своим отделениям своего предпрития','Стартовая страница',26);


delete from receipts;
delete from shifts;
delete from retail_sales_product;
delete from retail_sales;

ALTER SEQUENCE retail_sales_id_seq RESTART WITH 1;
ALTER SEQUENCE retail_sales_product_id_seq RESTART WITH 1;

--на боевом сервере:
insert into retail_sales(
id,
master_id,
creator_id,
company_id,
department_id,
date_time_created,
cagent_id,
name,
description,
doc_number)
select
id,
master_id,
creator_id,
company_id,
department_id,
date_time_created,
cagent_id,
'Восстановленная',
'Продажа восстановлена по документу Заказ покупателя 21.07.2021',
doc_number
from customers_orders where coalesce(is_deleted,false)!=true
order by date_time_created;

--на боевом сервере:
insert into retail_sales_product(
id,
master_id,
company_id,
department_id,
retail_sales_id,
product_id,
product_count,
product_price,
product_sumprice,
edizm_id,
nds_id,
product_price_of_type_price)
select
id,
master_id,
company_id,
department_id,
customers_orders_id,
product_id,
product_count,
product_price,
product_sumprice,
edizm_id,
nds_id,
product_price_of_type_price
from customers_orders_product 
where customers_orders_id in (select id from customers_orders where coalesce(is_deleted,false)!=true);

ALTER SEQUENCE retail_sales_id_seq RESTART WITH 6000;
ALTER SEQUENCE retail_sales_product_id_seq RESTART WITH 9000;

insert into retail_sales(
master_id,
creator_id,
company_id,
department_id,
date_time_created,
cagent_id,
name,
description,
doc_number)
select
master_id,
creator_id,
company_id,
department_id,
date_time_created,
(select id from cagents where name='Обезличенный покупатель' and company_id=1),
'Восстановленная',
'Продажа восстановлена по истории документа "Итоги смен", товары не совпадают, т.к. в Итогах смен товары не прописывались. На один Итог смены создана одна Розничная продажа.',
0
from traderesults where company_id=1
and date_time_created<to_date('2021-02-01','YYYY-MM-DD');

insert into retail_sales_product(
master_id,
company_id,
department_id,
retail_sales_id,
product_id,
product_count,
product_price,
product_sumprice,
edizm_id,
nds_id)
select
master_id,
company_id,
department_id,
(select id from retail_sales where date_time_created=tr.date_time_created),
(select id from products where name='Предмет расчёта без наименования'),
1,
(incoming_cash_checkout+incoming_cashless_checkout+incoming_cash2+incoming_cashless2)/100,
(incoming_cash_checkout+incoming_cashless_checkout+incoming_cash2+incoming_cashless2)/100,
12,
1
from traderesults tr where company_id=1
and date_time_created<to_date('2021-02-01','YYYY-MM-DD')
and date_time_created>to_date('2019-09-30','YYYY-MM-DD')
order by tr.id;


ALTER SEQUENCE retail_sales_id_seq RESTART WITH 7000;
ALTER SEQUENCE retail_sales_product_id_seq RESTART WITH 10000;



***********************************************************************************************************************************************
***********************************************************************************************************************************************
***********************************************************************************************************************************************
--alter table kassa add column zn_kkt varchar(64); --заводской номер ккт - СДЕЛАТЬ NOT NULL !!!

create table inventory(
 id bigserial primary key not null,
 master_id  bigint not null, 
 company_id bigint not null,  
 department_id bigint not null,
 creator_id bigint not null,
 changer_id bigint,
 date_time_created timestamp with time zone not null,
 date_time_changed timestamp with time zone,
 status_id bigint,
 doc_number int not null,
 name varchar(120),
 description varchar(2048),
 is_deleted boolean,
 foreign key (master_id) references users(id),
 foreign key (creator_id) references users(id),
 foreign key (changer_id) references users(id),
 foreign key (company_id) references companies(id),
 foreign key (department_id) references departments(id),
 foreign key (status_id) references sprav_status_dock (id) ON DELETE SET NULL
);

insert into documents (name,page_name,show) values ('Инвентаризация','inventory',1);

insert into permissions (name,description,document_name,document_id) values
('Боковая панель - отображать в списке документов','Показывать документ в списке документов на боковой панели','Инвентаризация',27),
('Создание документов по всем предприятиям','Возможность создавать новые документы "Инвентаризация" по всем предприятиям','Инвентаризация',27),
('Создание документов своего предприятия','Возможность создавать новые документы "Инвентаризация" своего предприятия','Инвентаризация',27),
('Создание документов своих отделений','Возможность создавать новые документы "Инвентаризация" по своим отделениям','Инвентаризация',27),
('Удаление документов по всем предприятиям','Возможность удалить документ "Инвентаризация" в архив по всем предприятиям','Инвентаризация',27),
('Удаление документов своего предприятия','Возможность удалить документ "Инвентаризация" своего предприятия в архив','Инвентаризация',27),
('Удаление документов своих отделений','Возможность удалить документ "Инвентаризация" одного из своих отделений','Инвентаризация',27),
('Удаление документов созданных собой','Возможность удаления документов "Инвентаризация", созданных собой','Инвентаризация',27),
('Просмотр документов по всем предприятиям','Прсмотр информации в документах "Инвентаризация" по всем предприятиям','Инвентаризация',27),
('Просмотр документов своего предприятия','Прсмотр информации в документах "Инвентаризация" своего предприятия','Инвентаризация',27),
('Просмотр документов своих отделений','Прсмотр информации в документах "Инвентаризация" по своим отделениям','Инвентаризация',27),
('Просмотр документов созданных собой','Прсмотр информации в документах "Инвентаризация", созданных собой','Инвентаризация',27),
('Редактирование документов по всем предприятиям','Редактирование документов "Инвентаризация" по всем предприятиям','Инвентаризация',27),
('Редактирование документов своего предприятия','Редактирование документов "Инвентаризация" своего предприятия','Инвентаризация',27),
('Редактирование документов своих отделений','Редактирование документов "Инвентаризация" по своим отделениям','Инвентаризация',27),
('Редактирование документов созданных собой','Редактирование документов "Инвентаризация", созданных собой','Инвентаризация',27);

create table inventory_product (
 id bigserial primary key not null,
 master_id bigint not null,
 company_id bigint not null,
 product_id bigint not null,
 inventory_id bigint not null,
 estimated_balance numeric(15,3) not null,
 actual_balance numeric(15,3) not null,
 product_price numeric(12,2),
 foreign key (inventory_id) references inventory (id),
 foreign key (master_id ) references users (id),
 foreign key (product_id ) references products (id),
 foreign key (company_id ) references companies (id)
);

ALTER TABLE inventory_product ADD CONSTRAINT inventory_product_uq UNIQUE (product_id, inventory_id);

create table settings_inventory (
    id                          bigserial primary key not null,
    master_id                   bigint not null, 
    company_id                  bigint not null,  
    user_id                     bigint not null,
    department_id               bigint,
    name                        varchar(120),
    pricing_type                varchar(16), 
    price_type_id               bigint,
    change_price                numeric(12,2),
    plus_minus                  varchar(8),
    change_price_type           varchar(8),
    hide_tenths                 boolean,
    status_on_finish_id         bigint,

    foreign key (price_type_id) references sprav_type_prices (id),
    foreign key (master_id) references users(id),
    foreign key (user_id) references users(id),
    foreign key (status_on_finish_id) references sprav_status_dock (id),
    foreign key (department_id) references departments (id),
    foreign key (company_id) references companies(id)
);
alter table settings_inventory add constraint settings_inventory_user_uq UNIQUE (user_id);

alter table inventory add column is_completed boolean;

alter table settings_retail_sales alter column pricing_type TYPE varchar (32) USING pricing_type::varchar (32);
alter table settings_customers_orders alter column pricing_type TYPE varchar (32) USING pricing_type::varchar (32);
alter table settings_inventory alter column pricing_type TYPE varchar (32) USING pricing_type::varchar (32);

alter table settings_inventory add column default_actual_balance varchar(16);
alter table settings_inventory add column other_actual_balance numeric(15,3);
alter table settings_inventory add column auto_add boolean;

alter table writeoff add column inventory_id bigint;
alter table writeoff add constraint inventory_id_fkey foreign key (inventory_id) references inventory (id);

alter table posting add column inventory_id bigint;
alter table posting add constraint inventory_id_fkey foreign key (inventory_id) references inventory (id);

alter table products_history add constraint products_history_quantity_check CHECK (quantity >= 0);
alter table product_quantity add constraint products_quantity_check CHECK (quantity >= 0);

alter table writeoff_product alter column edizm_id drop not null;
alter table posting_product alter column edizm_id drop not null;

ALTER TABLE posting_product ADD CONSTRAINT posting_product_uq UNIQUE (product_id, posting_id);
ALTER TABLE acceptance_product ADD CONSTRAINT acceptance_product_uq UNIQUE (product_id, acceptance_id);
ALTER TABLE writeoff_product ADD CONSTRAINT writeoff_product_uq UNIQUE (product_id, writeoff_id);
***********************************************************************************************************************************************
***********************************************************************************************************************************************
***********************************************************************************************************************************************
create table inventory_files (
                                 inventory_id bigint not null,
                                 file_id bigint not null,
                                 foreign key (file_id) references files (id) ON DELETE CASCADE,
                                 foreign key (inventory_id ) references inventory (id) ON DELETE CASCADE
  );

CREATE INDEX CONCURRENTLY sales_quantity_index ON sales_table (quantity);
CREATE INDEX retail_sales_id_index ON public.retail_sales_product USING btree (retail_sales_id);
***********************************************************************************************************************************************
***********************************************************************************************************************************************
***********************************************************************************************************************************************
create table return(
 id bigserial primary key not null,
 master_id  bigint not null, 
 company_id bigint not null,  
 department_id bigint not null,
 cagent_id bigint not null, 
 creator_id bigint not null,
 changer_id bigint,
 date_time_created timestamp with time zone not null,
 date_time_changed timestamp with time zone,
 status_id bigint,
 doc_number int not null,
 description varchar(2048),
 nds boolean,
 date_return timestamp with time zone not null,
 is_completed boolean,
 is_deleted boolean,
 foreign key (master_id) references users(id),
 foreign key (creator_id) references users(id),
 foreign key (changer_id) references users(id),
 foreign key (cagent_id) references cagents(id),
 foreign key (company_id) references companies(id),
 foreign key (department_id) references departments(id),
 foreign key (status_id) references sprav_status_dock (id) ON DELETE SET NULL
);

insert into documents (name,page_name,show) values ('Возврат покупателя','return',1);

insert into permissions (name,description,document_name,document_id) values
('Боковая панель - отображать в списке документов','Показывать документ в списке документов на боковой панели','Возврат покупателя',28),
('Создание документов по всем предприятиям','Возможность создавать новые документы "Возврат покупателя" по всем предприятиям','Возврат покупателя',28),
('Создание документов своего предприятия','Возможность создавать новые документы "Возврат покупателя" своего предприятия','Возврат покупателя',28),
('Создание документов своих отделений','Возможность создавать новые документы "Возврат покупателя" по своим отделениям','Возврат покупателя',28),
('Удаление документов по всем предприятиям','Возможность удалить документ "Возврат покупателя" в архив по всем предприятиям','Возврат покупателя',28),
('Удаление документов своего предприятия','Возможность удалить документ "Возврат покупателя" своего предприятия в архив','Возврат покупателя',28),
('Удаление документов своих отделений','Возможность удалить документ "Возврат покупателя" одного из своих отделений','Возврат покупателя',28),
('Удаление документов созданных собой','Возможность удаления документов "Возврат покупателя", созданных собой','Возврат покупателя',28),
('Просмотр документов по всем предприятиям','Прсмотр информации в документах "Возврат покупателя" по всем предприятиям','Возврат покупателя',28),
('Просмотр документов своего предприятия','Прсмотр информации в документах "Возврат покупателя" своего предприятия','Возврат покупателя',28),
('Просмотр документов своих отделений','Прсмотр информации в документах "Возврат покупателя" по своим отделениям','Возврат покупателя',28),
('Просмотр документов созданных собой','Прсмотр информации в документах "Возврат покупателя", созданных собой','Возврат покупателя',28),
('Редактирование документов по всем предприятиям','Редактирование документов "Возврат покупателя" по всем предприятиям','Возврат покупателя',28),
('Редактирование документов своего предприятия','Редактирование документов "Возврат покупателя" своего предприятия','Возврат покупателя',28),
('Редактирование документов своих отделений','Редактирование документов "Возврат покупателя" по своим отделениям','Возврат покупателя',28),
('Редактирование документов созданных собой','Редактирование документов "Возврат покупателя", созданных собой','Возврат покупателя',28);

create table return_product (
 id bigserial primary key not null,
 master_id bigint not null,
 company_id bigint not null,
 product_id bigint not null,
 return_id bigint not null,
 product_count numeric(15,3) not null,
 product_price numeric(12,2) not null,
 product_netcost numeric(12,2),
 nds_id int,
 product_sumprice numeric(15,2) not null, 
 product_sumnetcost numeric(15,2) , 
 foreign key (return_id) references return (id),
 foreign key (master_id ) references users (id),
 foreign key (product_id ) references products (id),
 foreign key (nds_id ) references sprav_sys_nds (id),
 foreign key (company_id ) references companies (id)
);

ALTER TABLE return_product ADD CONSTRAINT return_product_uq UNIQUE (product_id, return_id);

create table settings_return (
    id                          bigserial primary key not null,
    master_id                   bigint not null, 
    company_id                  bigint not null,  
    user_id                     bigint not null,
    department_id               bigint,
    status_on_finish_id         bigint,
    auto_add                    boolean,
    foreign key (master_id) references users(id),
    foreign key (user_id) references users(id),
    foreign key (status_on_finish_id) references sprav_status_dock (id),
    foreign key (department_id) references departments (id),
    foreign key (company_id) references companies(id)
);
alter table settings_return add constraint settings_return_user_uq UNIQUE (user_id);


create table return_files (
 return_id bigint not null,
 file_id bigint not null,
 foreign key (file_id) references files (id) ON DELETE CASCADE,
 foreign key (return_id ) references return (id) ON DELETE CASCADE
);

alter table writeoff add column return_id bigint;
alter table writeoff add constraint return_id_fkey foreign key (return_id) references return (id);

-- атрибут неделимости для товара
alter table products add column indivisible boolean;
update products set indivisible = true;
alter table products alter column indivisible set not null;

alter table return add column retail_sales_id bigint;
alter table return add constraint retail_sales_id_fkey foreign key (retail_sales_id) references retail_sales (id);

alter table settings_return add column show_kkm boolean;
alter table settings_retail_sales add column show_kkm boolean;
update settings_retail_sales set show_kkm=true;
alter table settings_retail_sales add column auto_add boolean;
alter table retail_sales add column uid varchar(36);

***********************************************************************************************************************************************
***********************************************************************************************************************************************
***********************************************************************************************************************************************

ALTER TABLE public.documents ALTER COLUMN id DROP DEFAULT;
DROP SEQUENCE documents_id_seq;
ALTER TABLE public.permissions ALTER COLUMN id DROP DEFAULT;
DROP SEQUENCE permissions_id_seq;

create table returnsup(
 id bigserial primary key not null,
 master_id  bigint not null, 
 company_id bigint not null,  
 department_id bigint not null,
 cagent_id bigint not null, 
 creator_id bigint not null,
 changer_id bigint,
 date_time_created timestamp with time zone not null,
 date_time_changed timestamp with time zone,
 status_id bigint,
 doc_number int not null,
 description varchar(2048),
 nds boolean,
 date_return timestamp with time zone not null,
 is_completed boolean,
 is_deleted boolean,
 foreign key (master_id) references users(id),
 foreign key (creator_id) references users(id),
 foreign key (changer_id) references users(id),
 foreign key (cagent_id) references cagents(id),
 foreign key (company_id) references companies(id),
 foreign key (department_id) references departments(id),
 foreign key (status_id) references sprav_status_dock (id) ON DELETE SET NULL
);

insert into documents (id,name,page_name,show) values (29,'Возврат поставщику','returnsup',1);

insert into permissions (id,name,description,document_name,document_id) values
(360,'Боковая панель - отображать в списке документов','Показывать документ в списке документов на боковой панели','Возврат поставщику',29),
(361,'Создание документов по всем предприятиям','Возможность создавать новые документы "Возврат поставщику" по всем предприятиям','Возврат поставщику',29),
(362,'Создание документов своего предприятия','Возможность создавать новые документы "Возврат поставщику" своего предприятия','Возврат поставщику',29),
(363,'Создание документов своих отделений','Возможность создавать новые документы "Возврат поставщику" по своим отделениям','Возврат поставщику',29),
(364,'Удаление документов по всем предприятиям','Возможность удалить документ "Возврат поставщику" в архив по всем предприятиям','Возврат поставщику',29),
(365,'Удаление документов своего предприятия','Возможность удалить документ "Возврат поставщику" своего предприятия в архив','Возврат поставщику',29),
(366,'Удаление документов своих отделений','Возможность удалить документ "Возврат поставщику" одного из своих отделений','Возврат поставщику',29),
(367,'Удаление документов созданных собой','Возможность удаления документов "Возврат поставщику", созданных собой','Возврат поставщику',29),
(368,'Просмотр документов по всем предприятиям','Прсмотр информации в документах "Возврат поставщику" по всем предприятиям','Возврат поставщику',29),
(369,'Просмотр документов своего предприятия','Прсмотр информации в документах "Возврат поставщику" своего предприятия','Возврат поставщику',29),
(370,'Просмотр документов своих отделений','Прсмотр информации в документах "Возврат поставщику" по своим отделениям','Возврат поставщику',29),
(371,'Просмотр документов созданных собой','Прсмотр информации в документах "Возврат поставщику", созданных собой','Возврат поставщику',29),
(372,'Редактирование документов по всем предприятиям','Редактирование документов "Возврат поставщику" по всем предприятиям','Возврат поставщику',29),
(373,'Редактирование документов своего предприятия','Редактирование документов "Возврат поставщику" своего предприятия','Возврат поставщику',29),
(374,'Редактирование документов своих отделений','Редактирование документов "Возврат поставщику" по своим отделениям','Возврат поставщику',29),
(375,'Редактирование документов созданных собой','Редактирование документов "Возврат поставщику", созданных собой','Возврат поставщику',29);

create table returnsup_product (
    id bigserial primary key not null,
    master_id bigint not null,
    company_id bigint not null,
    product_id bigint not null,
    returnsup_id bigint not null,
    product_count numeric(15,3) not null,
    product_price numeric(12,2) not null,
    nds_id int,
    product_sumprice numeric(15,2) not null, 
    foreign key (returnsup_id) references returnsup (id),
    foreign key (master_id ) references users (id),
    foreign key (product_id ) references products (id),
    foreign key (nds_id ) references sprav_sys_nds (id),
    foreign key (company_id ) references companies (id)
);

ALTER TABLE returnsup_product ADD CONSTRAINT returnsup_product_uq UNIQUE (product_id, returnsup_id);

create table settings_returnsup (
    id                          bigserial primary key not null,
    master_id                   bigint not null, 
    company_id                  bigint not null,  
    user_id                     bigint not null,
    department_id               bigint,
    status_on_finish_id         bigint,
    auto_add                    boolean,
    foreign key (master_id) references users(id),
    foreign key (user_id) references users(id),
    foreign key (status_on_finish_id) references sprav_status_dock (id),
    foreign key (department_id) references departments (id),
    foreign key (company_id) references companies(id)
);
alter table settings_returnsup add constraint settings_returnsup_user_uq UNIQUE (user_id);

create table returnsup_files (
    returnsup_id bigint not null,
    file_id bigint not null,
    foreign key (file_id) references files (id) ON DELETE CASCADE,
    foreign key (returnsup_id ) references returnsup (id) ON DELETE CASCADE
);

alter table returnsup add column acceptance_id bigint;
alter table returnsup add constraint acceptance_id_fkey foreign key (acceptance_id) references acceptance (id);

create table settings_acceptance (
    id                          bigserial primary key not null,
    master_id                   bigint not null, 
    company_id                  bigint not null,  
    user_id                     bigint not null,
    department_id               bigint,
    status_on_finish_id         bigint,
    auto_add                    boolean,
    foreign key (master_id) references users(id),
    foreign key (user_id) references users(id),
    foreign key (status_on_finish_id) references sprav_status_dock (id),
    foreign key (department_id) references departments (id),
    foreign key (company_id) references companies(id)
);
alter table settings_acceptance add constraint settings_acceptance_user_uq UNIQUE (user_id);

alter table writeoff add column status_id bigint;
alter table writeoff add constraint status_id_fkey foreign key (status_id) references sprav_status_dock (id);


alter table acceptance add column status_id bigint;
alter table acceptance add constraint status_id_fkey foreign key (status_id) references sprav_status_dock (id);

alter table posting add column status_id bigint;
alter table posting add constraint status_id_fkey foreign key (status_id) references sprav_status_dock (id);

ALTER TABLE acceptance RENAME COLUMN is_archive TO is_deleted;
ALTER TABLE posting RENAME COLUMN is_archive TO is_deleted;
ALTER TABLE writeoff RENAME COLUMN is_archive TO is_deleted;

create table settings_posting (
    id                          bigserial primary key not null,
    master_id                   bigint not null, 
    company_id                  bigint not null,  
    user_id                     bigint not null,
    department_id               bigint,
    status_on_finish_id         bigint,
    auto_add                    boolean,
    foreign key (master_id) references users(id),
    foreign key (user_id) references users(id),
    foreign key (status_on_finish_id) references sprav_status_dock (id),
    foreign key (department_id) references departments (id),
    foreign key (company_id) references companies(id)
);
alter table settings_posting add constraint settings_posting_user_uq UNIQUE (user_id);

create table settings_writeoff (
    id                          bigserial primary key not null,
    master_id                   bigint not null, 
    company_id                  bigint not null,  
    user_id                     bigint not null,
    department_id               bigint,
    status_on_finish_id         bigint,
    auto_add                    boolean,
    foreign key (master_id) references users(id),
    foreign key (user_id) references users(id),
    foreign key (status_on_finish_id) references sprav_status_dock (id),
    foreign key (department_id) references departments (id),
    foreign key (company_id) references companies(id)
);
alter table settings_writeoff add constraint settings_writeoff_user_uq UNIQUE (user_id);

alter table writeoff_product alter reason_id drop not null;

create table moving(
 id bigserial primary key not null,
 master_id  bigint not null, 
 company_id bigint not null,  
 department_from_id bigint not null,
 department_to_id bigint not null,
 creator_id bigint not null,
 changer_id bigint,
 date_time_created timestamp with time zone not null,
 date_time_changed timestamp with time zone,
 status_id bigint,
 doc_number int not null,
 description varchar(2048),
 customers_orders_id bigint,
 is_completed boolean,
 is_deleted boolean,
 overhead numeric(12,2),
 overhead_netcost_method int,
 foreign key (master_id) references users(id),
 foreign key (creator_id) references users(id),
 foreign key (changer_id) references users(id),
 foreign key (customers_orders_id) references customers_orders(id),
 foreign key (company_id) references companies(id),
 foreign key (department_from_id) references departments(id),
 foreign key (department_to_id) references departments(id),
 foreign key (status_id) references sprav_status_dock (id) ON DELETE SET NULL
);

insert into documents (id,name,page_name,show) values (30,'Перемещение','moving',1);

insert into permissions (id,name,description,document_name,document_id) values
(376,'Боковая панель - отображать в списке документов','Показывать документ в списке документов на боковой панели','Перемещение',30),
(377,'Создание документов по всем предприятиям','Возможность создавать новые документы "Перемещение" по всем предприятиям','Перемещение',30),
(378,'Создание документов своего предприятия','Возможность создавать новые документы "Перемещение" своего предприятия','Перемещение',30),
(379,'Создание документов своих отделений','Возможность создавать новые документы "Перемещение" по своим отделениям','Перемещение',30),
(380,'Удаление документов по всем предприятиям','Возможность удалить документ "Перемещение" в архив по всем предприятиям','Перемещение',30),
(381,'Удаление документов своего предприятия','Возможность удалить документ "Перемещение" своего предприятия в архив','Перемещение',30),
(382,'Удаление документов своих отделений','Возможность удалить документ "Перемещение" одного из своих отделений','Перемещение',30),
(383,'Удаление документов созданных собой','Возможность удаления документов "Перемещение", созданных собой','Перемещение',30),
(384,'Просмотр документов по всем предприятиям','Прсмотр информации в документах "Перемещение" по всем предприятиям','Перемещение',30),
(385,'Просмотр документов своего предприятия','Прсмотр информации в документах "Перемещение" своего предприятия','Перемещение',30),
(386,'Просмотр документов своих отделений','Прсмотр информации в документах "Перемещение" по своим отделениям','Перемещение',30),
(387,'Просмотр документов созданных собой','Прсмотр информации в документах "Перемещение", созданных собой','Перемещение',30),
(388,'Редактирование документов по всем предприятиям','Редактирование документов "Перемещение" по всем предприятиям','Перемещение',30),
(389,'Редактирование документов своего предприятия','Редактирование документов "Перемещение" своего предприятия','Перемещение',30),
(390,'Редактирование документов своих отделений','Редактирование документов "Перемещение" по своим отделениям','Перемещение',30),
(391,'Редактирование документов созданных собой','Редактирование документов "Перемещение", созданных собой','Перемещение',30),
(392,'Проведение документов по всем предприятиям','Проведение документов "Перемещение" по всем предприятиям','Перемещение',30),
(393,'Проведение документов своего предприятия','Проведение документов "Перемещение" своего предприятия','Перемещение',30),
(394,'Проведение документов своих отделений','Проведение документов "Перемещение" по своим отделениям','Перемещение',30);

create table moving_product (
    id bigserial primary key not null,
    master_id bigint not null,
    company_id bigint not null,
    product_id bigint not null,
    moving_id bigint not null,
    product_count numeric(15,3) not null,
    product_price numeric(12,2) not null,
    product_sumprice numeric(15,2) not null, 
    product_netcost numeric(12,2),
    foreign key (moving_id) references moving (id),
    foreign key (master_id ) references users (id),
    foreign key (product_id ) references products (id),
    foreign key (company_id ) references companies (id)
);

ALTER TABLE moving_product ADD CONSTRAINT moving_product_uq UNIQUE (product_id, moving_id);

create table settings_moving (
    id                          bigserial primary key not null,
    master_id                   bigint not null, 
    company_id                  bigint not null,  
    user_id                     bigint not null,
    department_from_id          bigint,
    department_to_id            bigint,
    status_on_finish_id         bigint,
    auto_add                    boolean,
    foreign key (master_id) references users(id),
    foreign key (user_id) references users(id),
    foreign key (status_on_finish_id) references sprav_status_dock (id),
    foreign key (department_from_id) references departments(id),
    foreign key (department_to_id) references departments(id),
    foreign key (company_id) references companies(id)
);
alter table settings_moving add constraint settings_moving_user_uq UNIQUE (user_id);

create table moving_files (
    moving_id bigint not null,
    file_id bigint not null,
    foreign key (file_id) references files (id) ON DELETE CASCADE,
    foreign key (moving_id ) references moving (id) ON DELETE CASCADE
);

alter table settings_moving add column pricing_type  varchar(32);
alter table settings_moving add column price_type_id       bigint;
alter table settings_moving add column change_price        numeric(12,2);
alter table settings_moving add column plus_minus          varchar(8);
alter table settings_moving add column change_price_type   varchar(8);
alter table settings_moving add column hide_tenths         boolean;
alter table settings_moving add constraint price_type_id_fkey foreign key (price_type_id) references sprav_type_prices (id);

alter table settings_posting add column pricing_type  varchar(32);
alter table settings_posting add column price_type_id       bigint;
alter table settings_posting add column change_price        numeric(12,2);
alter table settings_posting add column plus_minus          varchar(8);
alter table settings_posting add column change_price_type   varchar(8);
alter table settings_posting add column hide_tenths         boolean;
alter table settings_posting add constraint price_type_id_fkey foreign key (price_type_id) references sprav_type_prices (id);

alter table settings_writeoff add column pricing_type  varchar(32);
alter table settings_writeoff add column price_type_id       bigint;
alter table settings_writeoff add column change_price        numeric(12,2);
alter table settings_writeoff add column plus_minus          varchar(8);
alter table settings_writeoff add column change_price_type   varchar(8);
alter table settings_writeoff add column hide_tenths         boolean;
alter table settings_writeoff add constraint price_type_id_fkey foreign key (price_type_id) references sprav_type_prices (id);

alter table settings_returnsup add column pricing_type  varchar(32);
alter table settings_returnsup add column price_type_id       bigint;
alter table settings_returnsup add column change_price        numeric(12,2);
alter table settings_returnsup add column plus_minus          varchar(8);
alter table settings_returnsup add column change_price_type   varchar(8);
alter table settings_returnsup add column hide_tenths         boolean;
alter table settings_returnsup add constraint price_type_id_fkey foreign key (price_type_id) references sprav_type_prices (id);

alter table settings_acceptance add column auto_price         boolean;

insert into permissions (id,name,description,document_name,document_id) values
(395,'Проведение документов созданных собой','Проведение документов "Перемещение" созданных собой','Перемещение',30);

***********************************************************************************************************************************************
***********************************************************************************************************************************************
***********************************************************************************************************************************************

create table linked_docs_groups (
    id                          bigserial primary key not null,
    master_id                   bigint not null, 
    company_id                  bigint not null,  
    date_time_created timestamp with time zone not null,
    foreign key (master_id) references users(id),
    foreign key (company_id) references companies(id)
);

create table linked_docs (
    id                          bigserial primary key not null,
    master_id                   bigint not null, 
    company_id                  bigint not null,  
    group_id                    bigint not null,
    doc_id                      bigint not null,
    doc_uid                     varchar(36) not null,
    tablename                   varchar(40) not null,
    acceptance_id               bigint,
    customers_orders_id         bigint,
    return_id                   bigint,
    returnsup_id                bigint,
    shipment_id                 bigint,
    retail_sales_id             bigint,
    products_id                 bigint,
    inventory_id                bigint,
    writeoff_id                 bigint,
    posting_id                  bigint,
    moving_id                   bigint,

    foreign key (group_id) references linked_docs_groups(id),
    foreign key (master_id) references users(id),
    foreign key (company_id) references companies(id),
    foreign key (customers_orders_id) references customers_orders(id),
    foreign key (acceptance_id) references acceptance(id),
    foreign key (return_id) references return(id),
    foreign key (returnsup_id) references returnsup(id),
    foreign key (shipment_id) references shipment(id),
    foreign key (retail_sales_id) references retail_sales(id),
    foreign key (products_id) references products(id),
    foreign key (inventory_id) references inventory(id),
    foreign key (writeoff_id) references writeoff(id),
    foreign key (posting_id) references posting(id),
    foreign key (moving_id) references moving(id)
);

create table linked_docs_links(
    id                          bigserial primary key not null,
    master_id                   bigint not null, 
    company_id                  bigint not null,  
    group_id                    bigint not null,
    parent_uid                    varchar(36),
    child_uid                      varchar(36),
    foreign key (group_id) references linked_docs_groups(id),
    foreign key (master_id) references users(id),
    foreign key (company_id) references companies(id)
);
 
ALTER TABLE linked_docs_links ADD CONSTRAINT uuids_uq UNIQUE (parent_uid, child_uid);

alter table customers_orders add column uid varchar(36);
alter table acceptance add column uid varchar(36);
alter table return add column uid varchar(36);
alter table returnsup add column uid varchar(36);
alter table shipment add column uid varchar(36);
alter table products add column uid varchar(36);
alter table inventory add column uid varchar(36);
alter table writeoff add column uid varchar(36);
alter table posting add column uid varchar(36);
alter table moving add column uid varchar(36);

alter table customers_orders add column linked_docs_group_id bigint;
alter table acceptance add column linked_docs_group_id bigint;
alter table return add column linked_docs_group_id bigint;
alter table returnsup add column linked_docs_group_id bigint;
alter table shipment add column linked_docs_group_id bigint;
alter table retail_sales add column linked_docs_group_id bigint;
alter table products add column linked_docs_group_id bigint;
alter table inventory add column linked_docs_group_id bigint;
alter table writeoff add column linked_docs_group_id bigint;
alter table posting add column linked_docs_group_id bigint;
alter table moving add column linked_docs_group_id bigint;

alter table customers_orders add constraint linked_docs_group_id_fkey foreign key (linked_docs_group_id) references linked_docs_groups (id);
alter table acceptance add constraint linked_docs_group_id_fkey foreign key (linked_docs_group_id) references linked_docs_groups (id);
alter table return add constraint linked_docs_group_id_fkey foreign key (linked_docs_group_id) references linked_docs_groups (id);
alter table returnsup add constraint linked_docs_group_id_fkey foreign key (linked_docs_group_id) references linked_docs_groups (id);
alter table shipment add constraint linked_docs_group_id_fkey foreign key (linked_docs_group_id) references linked_docs_groups (id);
alter table retail_sales add constraint linked_docs_group_id_fkey foreign key (linked_docs_group_id) references linked_docs_groups (id);
alter table products add constraint linked_docs_group_id_fkey foreign key (linked_docs_group_id) references linked_docs_groups (id);
alter table inventory add constraint linked_docs_group_id_fkey foreign key (linked_docs_group_id) references linked_docs_groups (id);
alter table writeoff add constraint linked_docs_group_id_fkey foreign key (linked_docs_group_id) references linked_docs_groups (id);
alter table posting add constraint linked_docs_group_id_fkey foreign key (linked_docs_group_id) references linked_docs_groups (id);
alter table moving add constraint linked_docs_group_id_fkey foreign key (linked_docs_group_id) references linked_docs_groups (id);

ALTER TABLE linked_docs ADD CONSTRAINT linked_docs_uq UNIQUE (tablename, doc_id);

ALTER TABLE documents add column table_name varchar(40);
ALTER TABLE documents add column doc_name_ru varchar(40);
update documents set table_name=page_name;
update documents set doc_name_ru=name;
alter table documents alter column doc_name_ru set not null;
update documents set table_name =null where id=19;
update documents set doc_name_ru ='Заказ покупателя' where id=23;

alter table retail_sales add column is_completed boolean;

ALTER TABLE linked_docs ADD CONSTRAINT linked_docs_uid_uq UNIQUE (master_id, doc_uid);

alter table shipment drop column is_archive;
alter table shipment add column is_deleted boolean;
alter table shipment add column status_id bigint;
alter table shipment add constraint status_id_fkey foreign key (status_id) references sprav_status_dock (id);

insert into permissions (id,name,description,document_name,document_id) values
(396,'Проведение документов по всем предприятиям','Проведение документов "Отгрузка" по всем предприятиям','Отгрузка',21),
(397,'Проведение документов своего предприятия','Проведение документов "Отгрузка" своего предприятия','Отгрузка',21),
(398,'Проведение документов своих отделений','Проведение документов "Отгрузка" по своим отделениям','Отгрузка',21),
(399,'Проведение документов созданных собой','Проведение документов "Отгрузка" созданных собой','Отгрузка',21);


create table settings_shipment (
    id                          bigserial primary key not null,
    master_id                   bigint not null, 
    company_id                  bigint not null,  
    user_id                     bigint not null,
    customer_id                 bigint,
    department_id               bigint,
    name                        varchar(120),
    priority_type_price_side    varchar(8),
    pricing_type                varchar(32), 
    price_type_id               bigint,
    change_price                numeric(12,2),
    plus_minus                  varchar(8),
    change_price_type           varchar(8),
    hide_tenths                 boolean,
    save_settings               boolean,
    autocreate                  boolean,
    status_id_on_complete       bigint,
    show_kkm                    boolean,
    auto_add                    boolean,
    foreign key (price_type_id) references sprav_type_prices (id),
    foreign key (master_id) references users(id),
    foreign key (user_id) references users(id),
    foreign key (company_id) references companies(id)
);
alter table settings_shipment add constraint settings_shipment_user_uq UNIQUE (user_id);
alter table settings_shipment add constraint department_id_fkey foreign key (department_id) references departments (id);
alter table settings_shipment add constraint customer_id_fkey foreign key (customer_id) references cagents (id);
alter table settings_shipment add constraint status_id_on_complete_fkey foreign key (status_id_on_complete) references sprav_status_dock (id);

alter table shipment add column shift_id bigint;
alter table shipment add constraint shift_id_fkey foreign key (shift_id) references shifts (id);
alter table shipment add column customers_orders_id bigint;
alter table shipment add constraint customers_orders_id_fkey foreign key (customers_orders_id) references customers_orders (id);
alter table receipts add column shipment_id bigint;
alter table receipts add constraint shipment_id_fkey foreign key (shipment_id) references shipment (id);

drop table shipment_product;

create table shipment_product (
 id bigserial primary key not null,
 master_id bigint not null,
 company_id bigint not null,
 product_id bigint not null,
 shipment_id bigint not null,
 product_count numeric(15,3) not null,
 product_price numeric(12,2),
 product_sumprice numeric(15,2),
 edizm_id int not null,
 price_type_id bigint,
 nds_id bigint not null,
 department_id bigint not null,
 product_price_of_type_price numeric(12,2),

 foreign key (shipment_id) references shipment (id),
 foreign key (edizm_id) references sprav_sys_edizm (id),
 foreign key (nds_id) references sprav_sys_nds (id),
 foreign key (price_type_id) references sprav_type_prices (id),
 foreign key (product_id ) references products (id),
 foreign key (department_id ) references departments (id)
);

ALTER TABLE shipment_product ADD CONSTRAINT shipment_product_uq UNIQUE (product_id, shipment_id, department_id);

alter table shipment_product drop column edizm_id;
update documents set page_name='customersorders' where id=23;
update documents set table_name='retail_sales' where id=25;
alter table retail_sales_product drop column edizm_id;

insert into permissions (id,name,description,document_name,document_id) values
(400,'Проведение документов по всем предприятиям','Проведение документов "Заказ покупателя" по всем предприятиям','Заказ покупателя',23),
(401,'Проведение документов своего предприятия','Проведение документов "Заказ покупателя" своего предприятия','Заказ покупателя',23),
(402,'Проведение документов своих отделений','Проведение документов "Заказ покупателя" по своим отделениям','Заказ покупателя',23),
(403,'Проведение документов созданных собой','Проведение документов "Заказ покупателя" созданных собой','Заказ покупателя',23);

alter table customers_orders drop column is_archive;


insert into documents (id, name, page_name, show, table_name, doc_name_ru) values (31,'Счет покупателю','invoiceout',1,'invoiceout','Счет покупателю');
insert into documents (id, name, page_name, show, table_name, doc_name_ru) values (32,'Счет поставщика','invoicein',1,'invoicein','Счет поставщика');
insert into documents (id, name, page_name, show, table_name, doc_name_ru) values (33,'Входящий платеж','paymentin',1,'paymentin','Входящий платеж');
insert into documents (id, name, page_name, show, table_name, doc_name_ru) values (34,'Исходящий платеж','paymentout',1,'paymentout','Исходящий платеж');
insert into documents (id, name, page_name, show, table_name, doc_name_ru) values (35,'Приходный ордер','orderin',1,'orderin','Приходный ордер');
insert into documents (id, name, page_name, show, table_name, doc_name_ru) values (36,'Расходный ордер','orderout',1,'orderout','Расходный ордер');
insert into documents (id, name, page_name, show, table_name, doc_name_ru) values (37,'Счет-фактура выданный','vatinvoiceout',1,'vatinvoiceout','Счет-фактура выданный');
insert into documents (id, name, page_name, show, table_name, doc_name_ru) values (38,'Счет-фактура полученный','vatinvoicein',1,'vatinvoicein','Счет-фактура полученный');

create table invoiceout(
 id bigserial primary key not null,
 master_id  bigint not null, 
 creator_id bigint not null,
 changer_id bigint,
 date_time_created timestamp with time zone not null,
 date_time_changed timestamp with time zone,
 company_id bigint not null,  
 department_id bigint not null,
 cagent_id bigint not null,
 status_id bigint,
 doc_number int not null,
 description varchar(2048),
 nds boolean,
 nds_included boolean,
 is_deleted boolean,
 invoiceout_date date,
 is_completed boolean,
 uid varchar (36),
 linked_docs_group_id bigint,
 
 foreign key (master_id) references users(id),
 foreign key (creator_id) references users(id),
 foreign key (changer_id) references users(id),
 foreign key (company_id) references companies(id),
 foreign key (department_id) references departments(id),
 foreign key (cagent_id) references cagents(id),
 foreign key (linked_docs_group_id) references linked_docs_groups(id),
 foreign key (status_id) references sprav_status_dock (id) ON DELETE SET NULL
);

alter table linked_docs add column invoiceout_id bigint;

alter table linked_docs add constraint invoiceout_id_fkey foreign key (invoiceout_id) references invoiceout (id);

create table settings_invoiceout (
    id                          bigserial primary key not null,
    master_id                   bigint not null, 
    company_id                  bigint not null,  
    user_id                     bigint  UNIQUE not null,
    customer_id                 bigint,
    department_id               bigint,
    priority_type_price_side    varchar(8),
    pricing_type                varchar(32), 
    price_type_id               bigint,
    change_price                numeric(12,2),
    plus_minus                  varchar(8),
    change_price_type           varchar(8),
    hide_tenths                 boolean,
    save_settings               boolean,
    autocreate                  boolean,
    status_id_on_complete       bigint,
    auto_add                    boolean,
    foreign key (price_type_id) references sprav_type_prices (id),
    foreign key (master_id) references users(id),
    foreign key (department_id) references departments(id),
    foreign key (customer_id) references cagents(id),
    foreign key (user_id) references users(id),
    foreign key (status_id_on_complete) references sprav_status_dock(id),
    foreign key (company_id) references companies(id)
);

create table invoiceout_product (
 id bigserial primary key not null,
 master_id bigint not null,
 company_id bigint not null,
 product_id bigint not null,
 invoiceout_id bigint not null,
 product_count numeric(15,3) not null,
 product_price numeric(12,2),
 product_sumprice numeric(15,2),
 price_type_id bigint,
 nds_id bigint not null,
 department_id bigint not null,
 product_price_of_type_price numeric(12,2),

 foreign key (invoiceout_id) references invoiceout (id), 
 foreign key (nds_id) references sprav_sys_nds (id),
 foreign key (price_type_id) references sprav_type_prices (id),
 foreign key (product_id ) references products (id),
 foreign key (department_id ) references departments (id)
);

ALTER TABLE invoiceout_product ADD CONSTRAINT invoiceout_product_uq UNIQUE (product_id, invoiceout_id, department_id);

insert into permissions (id,name,description,document_name,document_id) values
(404,'Боковая панель - отображать в списке документов','Показывать документ в списке документов на боковой панели','Счет покупателя',31),
(405,'Создание документов по всем предприятиям','Возможность создавать новые документы "Счет покупателя" по всем предприятиям','Счет покупателя',31),
(406,'Создание документов своего предприятия','Возможность создавать новые документы "Счет покупателя" своего предприятия','Счет покупателя',31),
(407,'Создание документов своих отделений','Возможность создавать новые документы "Счет покупателя" по своим отделениям','Счет покупателя',31),
(408,'Удаление документов по всем предприятиям','Возможность удалить документ "Счет покупателя" в архив по всем предприятиям','Счет покупателя',31),
(409,'Удаление документов своего предприятия','Возможность удалить документ "Счет покупателя" своего предприятия в архив','Счет покупателя',31),
(410,'Удаление документов своих отделений','Возможность удалить документ "Счет покупателя" одного из своих отделений','Счет покупателя',31),
(411,'Удаление документов созданных собой','Возможность удаления документов "Счет покупателя", созданных собой','Счет покупателя',31),
(412,'Просмотр документов по всем предприятиям','Прсмотр информации в документах "Счет покупателя" по всем предприятиям','Счет покупателя',31),
(413,'Просмотр документов своего предприятия','Прсмотр информации в документах "Счет покупателя" своего предприятия','Счет покупателя',31),
(414,'Просмотр документов своих отделений','Прсмотр информации в документах "Счет покупателя" по своим отделениям','Счет покупателя',31),
(415,'Просмотр документов созданных собой','Прсмотр информации в документах "Счет покупателя", созданных собой','Счет покупателя',31),
(416,'Редактирование документов по всем предприятиям','Редактирование документов "Счет покупателя" по всем предприятиям','Счет покупателя',31),
(417,'Редактирование документов своего предприятия','Редактирование документов "Счет покупателя" своего предприятия','Счет покупателя',31),
(418,'Редактирование документов своих отделений','Редактирование документов "Счет покупателя" по своим отделениям','Счет покупателя',31),
(419,'Редактирование документов созданных собой','Редактирование документов "Счет покупателя", созданных собой','Счет покупателя',31),
(420,'Проведение документов по всем предприятиям','Проведение документов "Счет покупателя" по всем предприятиям','Счет покупателя',31),
(421,'Проведение документов своего предприятия','Проведение документов "Счет покупателя" своего предприятия','Счет покупателя',31),
(422,'Проведение документов своих отделений','Проведение документов "Счет покупателя" по своим отделениям','Счет покупателя',31),
(423,'Проведение документов созданных собой','Проведение документов "Счет покупателя" созданных собой','Счет покупателя',31);


ALTER TABLE inventory_product ADD column product_sumprice numeric(15,2);

insert into documents (id, name, page_name, show, table_name, doc_name_ru) values (39,'Заказ поставщику','ordersup',1,'ordersup','Заказ поставщику');


create table ordersup(
                       id bigserial primary key not null,
                       master_id  bigint not null,
                       creator_id bigint not null,
                       changer_id bigint,
                       date_time_created timestamp with time zone not null,
                       date_time_changed timestamp with time zone,
                       company_id bigint not null,
                       department_id bigint not null,
                       cagent_id bigint not null,
                       status_id bigint,
                       doc_number int not null,
                       description varchar(2048),
                       nds boolean,
                       nds_included boolean,
                       is_deleted boolean,
                       ordersup_date date,
                       is_completed boolean,
                       uid varchar (36),
                       linked_docs_group_id bigint,

                       foreign key (master_id) references users(id),
                       foreign key (creator_id) references users(id),
                       foreign key (changer_id) references users(id),
                       foreign key (company_id) references companies(id),
                       foreign key (department_id) references departments(id),
                       foreign key (cagent_id) references cagents(id),
                       foreign key (linked_docs_group_id) references linked_docs_groups(id),
                       foreign key (status_id) references sprav_status_dock (id) ON DELETE SET NULL
);

alter table linked_docs add column ordersup_id bigint;

alter table linked_docs add constraint ordersup_id_fkey foreign key (ordersup_id) references ordersup (id);

create table settings_ordersup (
                                 id                          bigserial primary key not null,
                                 master_id                   bigint not null,
                                 company_id                  bigint not null,
                                 user_id                     bigint  UNIQUE not null,
                                 cagent_id                   bigint,
                                 department_id               bigint,
                                 autocreate                  boolean,
                                 status_id_on_complete       bigint,
                                 auto_add                    boolean,
                                 auto_price                  boolean,
                                 name                        varchar(512),
                                 foreign key (master_id) references users(id),
                                 foreign key (department_id) references departments(id),
                                 foreign key (cagent_id) references cagents(id),
                                 foreign key (user_id) references users(id),
                                 foreign key (status_id_on_complete) references sprav_status_dock(id),
                                 foreign key (company_id) references companies(id)
);

create table ordersup_product (
                                id bigserial primary key not null,
                                master_id bigint not null,
                                company_id bigint not null,
                                product_id bigint not null,
                                ordersup_id bigint not null,
                                product_count numeric(15,3) not null,
                                product_price numeric(12,2),
                                product_sumprice numeric(15,2),
                                nds_id bigint not null,

                                foreign key (ordersup_id) references ordersup (id),
                                foreign key (nds_id) references sprav_sys_nds (id),
                                foreign key (product_id ) references products (id)
);

ALTER TABLE ordersup_product ADD CONSTRAINT ordersup_product_uq UNIQUE (product_id, ordersup_id);

insert into permissions (id,name,description,document_name,document_id) values
(424,'Боковая панель - отображать в списке документов','Показывать документ в списке документов на боковой панели','Заказ поставщику',39),
(425,'Создание документов по всем предприятиям','Возможность создавать новые документы "Заказ поставщику" по всем предприятиям','Заказ поставщику',39),
(426,'Создание документов своего предприятия','Возможность создавать новые документы "Заказ поставщику" своего предприятия','Заказ поставщику',39),
(427,'Создание документов своих отделений','Возможность создавать новые документы "Заказ поставщику" по своим отделениям','Заказ поставщику',39),
(428,'Удаление документов по всем предприятиям','Возможность удалить документ "Заказ поставщику" в архив по всем предприятиям','Заказ поставщику',39),
(429,'Удаление документов своего предприятия','Возможность удалить документ "Заказ поставщику" своего предприятия в архив','Заказ поставщику',39),
(430,'Удаление документов своих отделений','Возможность удалить документ "Заказ поставщику" одного из своих отделений','Заказ поставщику',39),
(431,'Удаление документов созданных собой','Возможность удаления документов "Заказ поставщику", созданных собой','Заказ поставщику',39),
(432,'Просмотр документов по всем предприятиям','Прсмотр информации в документах "Заказ поставщику" по всем предприятиям','Заказ поставщику',39),
(433,'Просмотр документов своего предприятия','Прсмотр информации в документах "Заказ поставщику" своего предприятия','Заказ поставщику',39),
(434,'Просмотр документов своих отделений','Прсмотр информации в документах "Заказ поставщику" по своим отделениям','Заказ поставщику',39),
(435,'Просмотр документов созданных собой','Прсмотр информации в документах "Заказ поставщику", созданных собой','Заказ поставщику',39),
(436,'Редактирование документов по всем предприятиям','Редактирование документов "Заказ поставщику" по всем предприятиям','Заказ поставщику',39),
(437,'Редактирование документов своего предприятия','Редактирование документов "Заказ поставщику" своего предприятия','Заказ поставщику',39),
(438,'Редактирование документов своих отделений','Редактирование документов "Заказ поставщику" по своим отделениям','Заказ поставщику',39),
(439,'Редактирование документов созданных собой','Редактирование документов "Заказ поставщику", созданных собой','Заказ поставщику',39),
(440,'Проведение документов по всем предприятиям','Проведение документов "Заказ поставщику" по всем предприятиям','Заказ поставщику',39),
(441,'Проведение документов своего предприятия','Проведение документов "Заказ поставщику" своего предприятия','Заказ поставщику',39),
(442,'Проведение документов своих отделений','Проведение документов "Заказ поставщику" по своим отделениям','Заказ поставщику',39),
(443,'Проведение документов созданных собой','Проведение документов "Заказ поставщику" созданных собой','Заказ поставщику',39);

alter table ordersup add column name varchar(512);

create table ordersup_files (
                               ordersup_id bigint not null,
                               file_id bigint not null,
                               foreign key (file_id) references files (id) ON DELETE CASCADE,
                               foreign key (ordersup_id ) references ordersup (id) ON DELETE CASCADE
);


create table invoicein(
                        id bigserial primary key not null,
                        master_id  bigint not null,
                        creator_id bigint not null,
                        changer_id bigint,
                        date_time_created timestamp with time zone not null,
                        date_time_changed timestamp with time zone,
                        company_id bigint not null,
                        department_id bigint not null,
                        cagent_id bigint not null,
                        status_id bigint,
                        doc_number int not null,
                        description varchar(2048),
                        nds boolean,
                        nds_included boolean,
                        is_deleted boolean,
                        invoicein_date date,
                        is_completed boolean,
                        uid varchar (36),
                        linked_docs_group_id bigint,
                        name varchar(512),
                        income_number varchar(64),
                        income_number_date date,

                        foreign key (master_id) references users(id),
                        foreign key (creator_id) references users(id),
                        foreign key (changer_id) references users(id),
                        foreign key (company_id) references companies(id),
                        foreign key (department_id) references departments(id),
                        foreign key (cagent_id) references cagents(id),
                        foreign key (linked_docs_group_id) references linked_docs_groups(id),
                        foreign key (status_id) references sprav_status_dock (id) ON DELETE SET NULL
);

alter table linked_docs add column invoicein_id bigint;

alter table linked_docs add constraint invoicein_id_fkey foreign key (invoicein_id) references invoicein (id);

create table settings_invoicein (
                                  id                          bigserial primary key not null,
                                  master_id                   bigint not null,
                                  company_id                  bigint not null,
                                  user_id                     bigint  UNIQUE not null,
                                  cagent_id                   bigint,
                                  department_id               bigint,
                                  autocreate                  boolean,
                                  status_id_on_complete       bigint,
                                  auto_add                    boolean,
                                  auto_price                  boolean,
                                  name                        varchar(512),
                                  foreign key (master_id) references users(id),
                                  foreign key (department_id) references departments(id),
                                  foreign key (cagent_id) references cagents(id),
                                  foreign key (user_id) references users(id),
                                  foreign key (status_id_on_complete) references sprav_status_dock(id),
                                  foreign key (company_id) references companies(id)
);

create table invoicein_product (
                                 id bigserial primary key not null,
                                 master_id bigint not null,
                                 company_id bigint not null,
                                 product_id bigint not null,
                                 invoicein_id bigint not null,
                                 product_count numeric(15,3) not null,
                                 product_price numeric(12,2),
                                 product_sumprice numeric(15,2),
                                 nds_id bigint not null,

                                 foreign key (invoicein_id) references invoicein (id),
                                 foreign key (nds_id) references sprav_sys_nds (id),
                                 foreign key (product_id ) references products (id)
);

ALTER TABLE invoicein_product ADD CONSTRAINT invoicein_product_uq UNIQUE (product_id, invoicein_id);

insert into permissions (id,name,description,document_name,document_id) values
(444,'Боковая панель - отображать в списке документов','Показывать документ в списке документов на боковой панели','Счёт поставщика',32),
(445,'Создание документов по всем предприятиям','Возможность создавать новые документы "Счёт поставщика" по всем предприятиям','Счёт поставщика',32),
(446,'Создание документов своего предприятия','Возможность создавать новые документы "Счёт поставщика" своего предприятия','Счёт поставщика',32),
(447,'Создание документов своих отделений','Возможность создавать новые документы "Счёт поставщика" по своим отделениям','Счёт поставщика',32),
(448,'Удаление документов по всем предприятиям','Возможность удалить документ "Счёт поставщика" в архив по всем предприятиям','Счёт поставщика',32),
(449,'Удаление документов своего предприятия','Возможность удалить документ "Счёт поставщика" своего предприятия в архив','Счёт поставщика',32),
(450,'Удаление документов своих отделений','Возможность удалить документ "Счёт поставщика" одного из своих отделений','Счёт поставщика',32),
(451,'Удаление документов созданных собой','Возможность удаления документов "Счёт поставщика", созданных собой','Счёт поставщика',32),
(452,'Просмотр документов по всем предприятиям','Прсмотр информации в документах "Счёт поставщика" по всем предприятиям','Счёт поставщика',32),
(453,'Просмотр документов своего предприятия','Прсмотр информации в документах "Счёт поставщика" своего предприятия','Счёт поставщика',32),
(454,'Просмотр документов своих отделений','Прсмотр информации в документах "Счёт поставщика" по своим отделениям','Счёт поставщика',32),
(455,'Просмотр документов созданных собой','Прсмотр информации в документах "Счёт поставщика", созданных собой','Счёт поставщика',32),
(456,'Редактирование документов по всем предприятиям','Редактирование документов "Счёт поставщика" по всем предприятиям','Счёт поставщика',32),
(457,'Редактирование документов своего предприятия','Редактирование документов "Счёт поставщика" своего предприятия','Счёт поставщика',32),
(458,'Редактирование документов своих отделений','Редактирование документов "Счёт поставщика" по своим отделениям','Счёт поставщика',32),
(459,'Редактирование документов созданных собой','Редактирование документов "Счёт поставщика", созданных собой','Счёт поставщика',32),
(460,'Проведение документов по всем предприятиям','Проведение документов "Счёт поставщика" по всем предприятиям','Счёт поставщика',32),
(461,'Проведение документов своего предприятия','Проведение документов "Счёт поставщика" своего предприятия','Счёт поставщика',32),
(462,'Проведение документов своих отделений','Проведение документов "Счёт поставщика" по своим отделениям','Счёт поставщика',32),
(463,'Проведение документов созданных собой','Проведение документов "Счёт поставщика" созданных собой','Счёт поставщика',32);

create table invoicein_files (
                               invoicein_id bigint not null,
                               file_id bigint not null,
                               foreign key (file_id) references files (id) ON DELETE CASCADE,
                               foreign key (invoicein_id ) references invoicein (id) ON DELETE CASCADE
);


create table paymentin(
                        id bigserial primary key not null,
                        master_id  bigint not null,
                        creator_id bigint not null,
                        changer_id bigint,
                        date_time_created timestamp with time zone not null,
                        date_time_changed timestamp with time zone,
                        company_id bigint not null,
                        cagent_id bigint not null,
                        status_id bigint,
                        doc_number int not null,
                        description varchar(2048),
                        summ  numeric(15,2) not null,
                        nds  numeric(15,2) not null,
                        is_deleted boolean,
                        is_completed boolean,
                        uid varchar (36),
                        linked_docs_group_id bigint,
                        income_number varchar(64),
                        income_number_date date,

                        foreign key (master_id) references users(id),
                        foreign key (creator_id) references users(id),
                        foreign key (changer_id) references users(id),
                        foreign key (company_id) references companies(id),
                        foreign key (cagent_id) references cagents(id),
                        foreign key (linked_docs_group_id) references linked_docs_groups(id),
                        foreign key (status_id) references sprav_status_dock (id) ON DELETE SET NULL
);

alter table linked_docs add column paymentin_id bigint;

alter table linked_docs add constraint paymentin_id_fkey foreign key (paymentin_id) references paymentin (id);

create table settings_paymentin (
                                  id                          bigserial primary key not null,
                                  master_id                   bigint not null,
                                  company_id                  bigint not null,
                                  user_id                     bigint  UNIQUE not null,
                                  cagent_id                   bigint,
                                  autocreate                  boolean,
                                  status_id_on_complete       bigint,
                                  foreign key (master_id) references users(id),
                                  foreign key (cagent_id) references cagents(id),
                                  foreign key (user_id) references users(id),
                                  foreign key (status_id_on_complete) references sprav_status_dock(id),
                                  foreign key (company_id) references companies(id)
);

insert into permissions (id,name,description,document_name,document_id) values
(464,'Боковая панель - отображать в списке документов','Показывать документ в списке документов на боковой панели','Входящий платеж',33),
(465,'Создание документов по всем предприятиям','Возможность создавать новые документы "Входящий платеж" по всем предприятиям','Входящий платеж',33),
(466,'Создание документов своего предприятия','Возможность создавать новые документы "Входящий платеж" своего предприятия','Входящий платеж',33),
(467,'Удаление документов по всем предприятиям','Возможность удалить документ "Входящий платеж" в архив по всем предприятиям','Входящий платеж',33),
(468,'Удаление документов своего предприятия','Возможность удалить документ "Входящий платеж" своего предприятия в архив','Входящий платеж',33),
(469,'Просмотр документов по всем предприятиям','Прсмотр информации в документах "Входящий платеж" по всем предприятиям','Входящий платеж',33),
(470,'Просмотр документов своего предприятия','Прсмотр информации в документах "Входящий платеж" своего предприятия','Входящий платеж',33),
(471,'Редактирование документов по всем предприятиям','Редактирование документов "Входящий платеж" по всем предприятиям','Входящий платеж',33),
(472,'Редактирование документов своего предприятия','Редактирование документов "Входящий платеж" своего предприятия','Входящий платеж',33),
(473,'Проведение документов по всем предприятиям','Проведение документов "Входящий платеж" по всем предприятиям','Входящий платеж',33),
(474,'Проведение документов своего предприятия','Проведение документов "Входящий платеж" своего предприятия','Входящий платеж',33);

create table paymentin_files (
                               paymentin_id bigint not null,
                               file_id bigint not null,
                               foreign key (file_id) references files (id) ON DELETE CASCADE,
                               foreign key (paymentin_id ) references paymentin (id) ON DELETE CASCADE
);


create table orderin(
                      id bigserial primary key not null,
                      master_id  bigint not null,
                      creator_id bigint not null,
                      changer_id bigint,
                      date_time_created timestamp with time zone not null,
                      date_time_changed timestamp with time zone,
                      company_id bigint not null,
                      cagent_id bigint not null,
                      status_id bigint,
                      doc_number int not null,
                      description varchar(2048),
                      summ  numeric(15,2) not null,
                      nds  numeric(15,2) not null,
                      is_deleted boolean,
                      is_completed boolean,
                      uid varchar (36),
                      linked_docs_group_id bigint,

                      foreign key (master_id) references users(id),
                      foreign key (creator_id) references users(id),
                      foreign key (changer_id) references users(id),
                      foreign key (company_id) references companies(id),
                      foreign key (cagent_id) references cagents(id),
                      foreign key (linked_docs_group_id) references linked_docs_groups(id),
                      foreign key (status_id) references sprav_status_dock (id) ON DELETE SET NULL
);

alter table linked_docs add column orderin_id bigint;

alter table linked_docs add constraint orderin_id_fkey foreign key (orderin_id) references orderin (id);

create table settings_orderin (
                                id                          bigserial primary key not null,
                                master_id                   bigint not null,
                                company_id                  bigint not null,
                                user_id                     bigint  UNIQUE not null,
                                cagent_id                   bigint,
                                autocreate                  boolean,
                                status_id_on_complete       bigint,
                                foreign key (master_id) references users(id),
                                foreign key (cagent_id) references cagents(id),
                                foreign key (user_id) references users(id),
                                foreign key (status_id_on_complete) references sprav_status_dock(id),
                                foreign key (company_id) references companies(id)
);

insert into permissions (id,name,description,document_name,document_id) values
(475,'Боковая панель - отображать в списке документов','Показывать документ в списке документов на боковой панели','Приходный ордер',35),
(476,'Создание документов по всем предприятиям','Возможность создавать новые документы "Приходный ордер" по всем предприятиям','Приходный ордер',35),
(477,'Создание документов своего предприятия','Возможность создавать новые документы "Приходный ордер" своего предприятия','Приходный ордер',35),
(478,'Удаление документов по всем предприятиям','Возможность удалить документ "Приходный ордер" в архив по всем предприятиям','Приходный ордер',35),
(479,'Удаление документов своего предприятия','Возможность удалить документ "Приходный ордер" своего предприятия в архив','Приходный ордер',35),
(480,'Просмотр документов по всем предприятиям','Прсмотр информации в документах "Приходный ордер" по всем предприятиям','Приходный ордер',35),
(481,'Просмотр документов своего предприятия','Прсмотр информации в документах "Приходный ордер" своего предприятия','Приходный ордер',35),
(482,'Редактирование документов по всем предприятиям','Редактирование документов "Приходный ордер" по всем предприятиям','Приходный ордер',35),
(483,'Редактирование документов своего предприятия','Редактирование документов "Приходный ордер" своего предприятия','Приходный ордер',35),
(484,'Проведение документов по всем предприятиям','Проведение документов "Приходный ордер" по всем предприятиям','Приходный ордер',35),
(485,'Проведение документов своего предприятия','Проведение документов "Приходный ордер" своего предприятия','Приходный ордер',35);

create table orderin_files (
                             orderin_id bigint not null,
                             file_id bigint not null,
                             foreign key (file_id) references files (id) ON DELETE CASCADE,
                             foreign key (orderin_id ) references orderin (id) ON DELETE CASCADE
);

create table vatinvoiceout(
                            id bigserial primary key not null,
                            master_id  bigint not null,
                            creator_id bigint not null,
                            changer_id bigint,
                            date_time_created timestamp with time zone not null,
                            date_time_changed timestamp with time zone,
                            company_id bigint not null,
                            cagent_id bigint not null,
                            cagent2_id bigint,
                            status_id bigint,
                            doc_number int not null,
                            description varchar(2048),
                            parent_tablename  varchar (16) not null, --orderin, paymentin, shipment
                            orderin_id bigint,
                            paymentin_id bigint,
                            shipment_id bigint,
                            gov_id varchar(20), -- идент. номер госконтракта
                            is_deleted boolean,
                            is_completed boolean,
                            uid varchar (36),
                            linked_docs_group_id bigint,
                            paydoc_number varchar(64),
                            paydoc_date date,
                            foreign key (master_id) references users(id),
                            foreign key (creator_id) references users(id),
                            foreign key (changer_id) references users(id),
                            foreign key (company_id) references companies(id),
                            foreign key (cagent_id) references cagents(id),
                            foreign key (cagent2_id) references cagents(id),
                            foreign key (orderin_id) references orderin(id),
                            foreign key (paymentin_id) references paymentin(id),
                            foreign key (shipment_id) references shipment(id),
                            foreign key (linked_docs_group_id) references linked_docs_groups(id),
                            foreign key (status_id) references sprav_status_dock (id) ON DELETE SET NULL
);

alter table linked_docs add column vatinvoiceout_id bigint;

alter table linked_docs add constraint vatinvoiceout_id_fkey foreign key (vatinvoiceout_id) references vatinvoiceout (id);


create table settings_vatinvoiceout (
                                      id                          bigserial primary key not null,
                                      master_id                   bigint not null,
                                      company_id                  bigint not null,
                                      user_id                     bigint  UNIQUE not null,
                                      cagent_id                   bigint,
                                      cagent2_id                  bigint,
                                      autocreate                  boolean,
                                      status_id_on_complete       bigint,
                                      foreign key (master_id) references users(id),
                                      foreign key (cagent_id) references cagents(id),
                                      foreign key (user_id) references users(id),
                                      foreign key (status_id_on_complete) references sprav_status_dock(id),
                                      foreign key (company_id) references companies(id)
);

insert into permissions (id,name,description,document_name,document_id) values
(486,'Боковая панель - отображать в списке документов','Показывать документ в списке документов на боковой панели','Счёт-фактура выданный',37),
(487,'Создание документов по всем предприятиям','Возможность создавать новые документы "Счёт-фактура выданный" по всем предприятиям','Счёт-фактура выданный',37),
(488,'Создание документов своего предприятия','Возможность создавать новые документы "Счёт-фактура выданный" своего предприятия','Счёт-фактура выданный',37),
(489,'Удаление документов по всем предприятиям','Возможность удалить документ "Счёт-фактура выданный" в архив по всем предприятиям','Счёт-фактура выданный',37),
(490,'Удаление документов своего предприятия','Возможность удалить документ "Счёт-фактура выданный" своего предприятия в архив','Счёт-фактура выданный',37),
(491,'Просмотр документов по всем предприятиям','Прсмотр информации в документах "Счёт-фактура выданный" по всем предприятиям','Счёт-фактура выданный',37),
(492,'Просмотр документов своего предприятия','Прсмотр информации в документах "Счёт-фактура выданный" своего предприятия','Счёт-фактура выданный',37),
(493,'Редактирование документов по всем предприятиям','Редактирование документов "Счёт-фактура выданный" по всем предприятиям','Счёт-фактура выданный',37),
(494,'Редактирование документов своего предприятия','Редактирование документов "Счёт-фактура выданный" своего предприятия','Счёт-фактура выданный',37),
(495,'Проведение документов по всем предприятиям','Проведение документов "Счёт-фактура выданный" по всем предприятиям','Счёт-фактура выданный',37),
(496,'Проведение документов своего предприятия','Проведение документов "Счёт-фактура выданный" своего предприятия','Счёт-фактура выданный',37);

create table vatinvoiceout_files (
                                   vatinvoiceout_id bigint not null,
                                   file_id bigint not null,
                                   foreign key (file_id) references files (id) ON DELETE CASCADE,
                                   foreign key (vatinvoiceout_id ) references vatinvoiceout (id) ON DELETE CASCADE
);

-- Справочник "Статьи расходов"
create table sprav_expenditure_items(
                                      id bigserial primary key not null,
                                      master_id  bigint not null,
                                      company_id bigint not null,
                                      creator_id bigint,
                                      changer_id bigint,
                                      date_time_created timestamp with time zone not null,
                                      date_time_changed timestamp with time zone,
                                      type varchar(30) not null, --return (возврат),  purchases (закупки товаров), taxes (налоги и сборы), moving (перемещение меж. своими счетами или кассами), other_opex (другие операционные)
                                      is_deleted boolean,
                                      is_completed boolean,
                                      foreign key (master_id) references users(id),
                                      foreign key (creator_id) references users(id),
                                      foreign key (changer_id) references users(id),
                                      foreign key (company_id) references companies(id)
                                    );


insert into documents (id, name, page_name, show, table_name, doc_name_ru) values (40,'Статьи расходов','expenditure',1,'sprav_expenditure_items','Статьи расходов');

insert into permissions (id,name,description,document_name,document_id) values
(497,'Боковая панель - отображать в списке документов','Показывать документ в списке документов на боковой панели','Статьи расходов',40),
(498,'Создание документов по всем предприятиям','Возможность создавать новые документы "Статьи расходов" по всем предприятиям','Статьи расходов',40),
(499,'Создание документов своего предприятия','Возможность создавать новые документы "Статьи расходов" своего предприятия','Статьи расходов',40),
(500,'Удаление документов по всем предприятиям','Возможность удалить документ "Статьи расходов" в архив по всем предприятиям','Статьи расходов',40),
(501,'Удаление документов своего предприятия','Возможность удалить документ "Статьи расходов" своего предприятия в архив','Статьи расходов',40),
(502,'Просмотр документов по всем предприятиям','Прсмотр информации в документах "Статьи расходов" по всем предприятиям','Статьи расходов',40),
(503,'Просмотр документов своего предприятия','Прсмотр информации в документах "Статьи расходов" своего предприятия','Статьи расходов',40),
(504,'Редактирование документов по всем предприятиям','Редактирование документов "Статьи расходов" по всем предприятиям','Статьи расходов',40),
(505,'Редактирование документов своего предприятия','Редактирование документов "Статьи расходов" своего предприятия','Статьи расходов',40);

alter table sprav_expenditure_items add column name varchar(60) not null;

alter table paymentin add column payment_account_id bigint;
alter table paymentin add constraint payment_account_id_fkey foreign key (payment_account_id) references companies_payment_accounts (id);


create table paymentout(
                         id bigserial primary key not null,
                         master_id  bigint not null,
                         creator_id bigint not null,
                         changer_id bigint,
                         date_time_created timestamp with time zone not null,
                         date_time_changed timestamp with time zone,
                         company_id bigint not null,
                         cagent_id bigint not null,
                         status_id bigint,
                         doc_number int not null,
                         description varchar(2048),
                         summ  numeric(15,2) not null,
                         nds  numeric(15,2) not null,
                         payment_account_id bigint not null,
                         expenditure_id bigint not null,
                         is_deleted boolean,
                         is_completed boolean,
                         uid varchar (36),
                         linked_docs_group_id bigint,
                         income_number varchar(64),
                         income_number_date date,

                         foreign key (master_id) references users(id),
                         foreign key (creator_id) references users(id),
                         foreign key (changer_id) references users(id),
                         foreign key (company_id) references companies(id),
                         foreign key (payment_account_id) references companies_payment_accounts(id),
                         foreign key (expenditure_id) references sprav_expenditure_items(id),
                         foreign key (cagent_id) references cagents(id),
                         foreign key (linked_docs_group_id) references linked_docs_groups(id),
                         foreign key (status_id) references sprav_status_dock (id) ON DELETE SET NULL
);

alter table linked_docs add column paymentout_id bigint;

alter table linked_docs add constraint paymentout_id_fkey foreign key (paymentout_id) references paymentout (id);

create table settings_paymentout (
                                   id                          bigserial primary key not null,
                                   master_id                   bigint not null,
                                   company_id                  bigint not null,
                                   user_id                     bigint  UNIQUE not null,
                                   cagent_id                   bigint,
                                   autocreate                  boolean,
                                   status_id_on_complete       bigint,
                                   foreign key (master_id) references users(id),
                                   foreign key (cagent_id) references cagents(id),
                                   foreign key (user_id) references users(id),
                                   foreign key (status_id_on_complete) references sprav_status_dock(id),
                                   foreign key (company_id) references companies(id)
);

insert into permissions (id,name,description,document_name,document_id) values
(506,'Боковая панель - отображать в списке документов','Показывать документ в списке документов на боковой панели','Исходящий платеж',34),
(507,'Создание документов по всем предприятиям','Возможность создавать новые документы "Исходящий платеж" по всем предприятиям','Исходящий платеж',34),
(508,'Создание документов своего предприятия','Возможность создавать новые документы "Исходящий платеж" своего предприятия','Исходящий платеж',34),
(509,'Удаление документов по всем предприятиям','Возможность удалить документ "Исходящий платеж" в архив по всем предприятиям','Исходящий платеж',34),
(510,'Удаление документов своего предприятия','Возможность удалить документ "Исходящий платеж" своего предприятия в архив','Исходящий платеж',34),
(511,'Просмотр документов по всем предприятиям','Прсмотр информации в документах "Исходящий платеж" по всем предприятиям','Исходящий платеж',34),
(512,'Просмотр документов своего предприятия','Прсмотр информации в документах "Исходящий платеж" своего предприятия','Исходящий платеж',34),
(513,'Редактирование документов по всем предприятиям','Редактирование документов "Исходящий платеж" по всем предприятиям','Исходящий платеж',34),
(514,'Редактирование документов своего предприятия','Редактирование документов "Исходящий платеж" своего предприятия','Исходящий платеж',34),
(515,'Проведение документов по всем предприятиям','Проведение документов "Исходящий платеж" по всем предприятиям','Исходящий платеж',34),
(516,'Проведение документов своего предприятия','Проведение документов "Исходящий платеж" своего предприятия','Исходящий платеж',34);

create table paymentout_files (
                                paymentout_id bigint not null,
                                file_id bigint not null,
                                foreign key (file_id) references files (id) ON DELETE CASCADE,
                                foreign key (paymentout_id ) references paymentout (id) ON DELETE CASCADE
);


create table orderout(
                       id bigserial primary key not null,
                       master_id  bigint not null,
                       creator_id bigint not null,
                       changer_id bigint,
                       date_time_created timestamp with time zone not null,
                       date_time_changed timestamp with time zone,
                       company_id bigint not null,
                       cagent_id bigint not null,
                       status_id bigint,
                       doc_number int not null,
                       description varchar(2048),
                       summ  numeric(15,2) not null,
                       nds  numeric(15,2) not null,
                       expenditure_id bigint not null,
                       is_deleted boolean,
                       is_completed boolean,
                       uid varchar (36),
                       linked_docs_group_id bigint,

                       foreign key (master_id) references users(id),
                       foreign key (creator_id) references users(id),
                       foreign key (changer_id) references users(id),
                       foreign key (expenditure_id) references sprav_expenditure_items(id),
                       foreign key (company_id) references companies(id),
                       foreign key (cagent_id) references cagents(id),
                       foreign key (linked_docs_group_id) references linked_docs_groups(id),
                       foreign key (status_id) references sprav_status_dock (id) ON DELETE SET NULL
);

alter table linked_docs add column orderout_id bigint;

alter table linked_docs add constraint orderout_id_fkey foreign key (orderout_id) references orderout (id);

create table settings_orderout (
                                 id                          bigserial primary key not null,
                                 master_id                   bigint not null,
                                 company_id                  bigint not null,
                                 user_id                     bigint  UNIQUE not null,
                                 cagent_id                   bigint,
                                 autocreate                  boolean,
                                 status_id_on_complete       bigint,
                                 foreign key (master_id) references users(id),
                                 foreign key (cagent_id) references cagents(id),
                                 foreign key (user_id) references users(id),
                                 foreign key (status_id_on_complete) references sprav_status_dock(id),
                                 foreign key (company_id) references companies(id)
);

insert into permissions (id,name,description,document_name,document_id) values
(517,'Боковая панель - отображать в списке документов','Показывать документ в списке документов на боковой панели','Расходный ордер',36),
(518,'Создание документов по всем предприятиям','Возможность создавать новые документы "Расходный ордер" по всем предприятиям','Расходный ордер',36),
(519,'Создание документов своего предприятия','Возможность создавать новые документы "Расходный ордер" своего предприятия','Расходный ордер',36),
(520,'Удаление документов по всем предприятиям','Возможность удалить документ "Расходный ордер" в архив по всем предприятиям','Расходный ордер',36),
(521,'Удаление документов своего предприятия','Возможность удалить документ "Расходный ордер" своего предприятия в архив','Расходный ордер',36),
(522,'Просмотр документов по всем предприятиям','Прсмотр информации в документах "Расходный ордер" по всем предприятиям','Расходный ордер',36),
(523,'Просмотр документов своего предприятия','Прсмотр информации в документах "Расходный ордер" своего предприятия','Расходный ордер',36),
(524,'Редактирование документов по всем предприятиям','Редактирование документов "Расходный ордер" по всем предприятиям','Расходный ордер',36),
(525,'Редактирование документов своего предприятия','Редактирование документов "Расходный ордер" своего предприятия','Расходный ордер',36),
(526,'Проведение документов по всем предприятиям','Проведение документов "Расходный ордер" по всем предприятиям','Расходный ордер',36),
(527,'Проведение документов своего предприятия','Проведение документов "Расходный ордер" своего предприятия','Расходный ордер',36);

create table orderout_files (
                              orderout_id bigint not null,
                              file_id bigint not null,
                              foreign key (file_id) references files (id) ON DELETE CASCADE,
                              foreign key (orderout_id ) references orderout (id) ON DELETE CASCADE
);


create table vatinvoicein(
                           id bigserial primary key not null,
                           master_id  bigint not null,
                           creator_id bigint not null,
                           changer_id bigint,
                           date_time_created timestamp with time zone not null,
                           date_time_changed timestamp with time zone,
                           company_id bigint not null,
                           cagent_id bigint not null,
                           status_id bigint,
                           doc_number int not null,
                           description varchar(2048),
                           parent_tablename  varchar (16) not null, --orderout, paymentout, acceptance
                           orderout_id bigint,
                           paymentout_id bigint,
                           acceptance_id bigint,
                           is_deleted boolean,
                           is_completed boolean,
                           uid varchar (36),
                           linked_docs_group_id bigint,
                           paydoc_number varchar(64),
                           paydoc_date date,
                           foreign key (master_id) references users(id),
                           foreign key (creator_id) references users(id),
                           foreign key (changer_id) references users(id),
                           foreign key (company_id) references companies(id),
                           foreign key (cagent_id) references cagents(id),
                           foreign key (orderout_id) references orderout(id),
                           foreign key (paymentout_id) references paymentout(id),
                           foreign key (acceptance_id) references acceptance(id),
                           foreign key (linked_docs_group_id) references linked_docs_groups(id),
                           foreign key (status_id) references sprav_status_dock (id) ON DELETE SET NULL
);

alter table linked_docs add column vatinvoicein_id bigint;

alter table linked_docs add constraint vatinvoicein_id_fkey foreign key (vatinvoicein_id) references vatinvoicein (id);


create table settings_vatinvoicein (
                                     id                          bigserial primary key not null,
                                     master_id                   bigint not null,
                                     company_id                  bigint not null,
                                     user_id                     bigint  UNIQUE not null,
                                     status_id_on_complete       bigint,
                                     foreign key (master_id) references users(id),
                                     foreign key (user_id) references users(id),
                                     foreign key (status_id_on_complete) references sprav_status_dock(id),
                                     foreign key (company_id) references companies(id)
);

insert into permissions (id,name,description,document_name,document_id) values
(528,'Боковая панель - отображать в списке документов','Показывать документ в списке документов на боковой панели','Счёт-фактура полученный',38),
(529,'Создание документов по всем предприятиям','Возможность создавать новые документы "Счёт-фактура полученный" по всем предприятиям','Счёт-фактура полученный',38),
(530,'Создание документов своего предприятия','Возможность создавать новые документы "Счёт-фактура полученный" своего предприятия','Счёт-фактура полученный',38),
(531,'Удаление документов по всем предприятиям','Возможность удалить документ "Счёт-фактура полученный" в архив по всем предприятиям','Счёт-фактура полученный',38),
(532,'Удаление документов своего предприятия','Возможность удалить документ "Счёт-фактура полученный" своего предприятия в архив','Счёт-фактура полученный',38),
(533,'Просмотр документов по всем предприятиям','Прсмотр информации в документах "Счёт-фактура полученный" по всем предприятиям','Счёт-фактура полученный',38),
(534,'Просмотр документов своего предприятия','Прсмотр информации в документах "Счёт-фактура полученный" своего предприятия','Счёт-фактура полученный',38),
(535,'Редактирование документов по всем предприятиям','Редактирование документов "Счёт-фактура полученный" по всем предприятиям','Счёт-фактура полученный',38),
(536,'Редактирование документов своего предприятия','Редактирование документов "Счёт-фактура полученный" своего предприятия','Счёт-фактура полученный',38),
(537,'Проведение документов по всем предприятиям','Проведение документов "Счёт-фактура полученный" по всем предприятиям','Счёт-фактура полученный',38),
(538,'Проведение документов своего предприятия','Проведение документов "Счёт-фактура полученный" своего предприятия','Счёт-фактура полученный',38);

create table vatinvoicein_files (
                                  vatinvoicein_id bigint not null,
                                  file_id bigint not null,
                                  foreign key (file_id) references files (id) ON DELETE CASCADE,
                                  foreign key (vatinvoicein_id ) references vatinvoicein (id) ON DELETE CASCADE
);

create table sprav_boxoffice (
                               id                          bigserial primary key not null,
                               master_id                   bigint not null,
                               company_id                  bigint not null,
                               creator_id                  bigint,
                               changer_id                  bigint,
                               date_time_created timestamp with time zone not null,
                               date_time_changed timestamp with time zone,
                               name                        varchar (64) not null,
                               description                 varchar(2048),
                               is_main                     boolean,
                               is_deleted                  boolean,
                               foreign key (master_id) references users(id),
                               foreign key (creator_id) references users(id),
                               foreign key (changer_id) references users(id),
                               foreign key (company_id) references companies(id)
);

insert into sprav_boxoffice(master_id,company_id,date_time_created,name,description,is_main)
--values (4,1,now(),'Главная','Главная касса предприятия',true);

alter table paymentout alter column cagent_id drop not null;

alter table paymentout add column moving_type varchar (10);
alter table paymentout add column boxoffice_id bigint;
alter table paymentout add constraint boxoffice_id_fkey foreign key (boxoffice_id) references sprav_boxoffice (id);
alter table paymentout add column payment_account_to_id bigint;
alter table paymentout add constraint payment_account_to_id_fkey foreign key (payment_account_to_id) references companies_payment_accounts (id);

alter table orderout alter  column cagent_id drop not null;
alter table orderout add    column moving_type varchar (10);
alter table orderout add    column boxoffice_id bigint;
alter table orderout add    constraint boxoffice_id_fkey foreign key (boxoffice_id) references sprav_boxoffice (id);
alter table orderout add    column boxoffice_to_id bigint;
alter table orderout add    constraint boxoffice_to_id_fkey foreign key (boxoffice_to_id) references sprav_boxoffice (id);
alter table orderout add    column payment_account_to_id bigint;
alter table orderout add    constraint payment_account_to_id_fkey foreign key (payment_account_to_id) references companies_payment_accounts (id);

alter table orderin alter column cagent_id drop not null;
alter table orderin add   column internal boolean;
alter table orderin add   column boxoffice_id bigint;
alter table orderin add   constraint boxoffice_id_fkey foreign key (boxoffice_id) references sprav_boxoffice (id);
alter table orderin alter column boxoffice_id set not null;

alter table paymentin alter column cagent_id drop not null;
alter table paymentin add   column internal boolean;

insert into documents (id, name, page_name, show, table_name, doc_name_ru) values (41,'Корректировка','correction',1,'correction','Корректировка');

create table correction (
                               id                           bigserial primary key not null,
                               master_id                    bigint not null,
                               company_id                   bigint not null,
                               creator_id                   bigint,
                               changer_id                   bigint,
                               date_time_created            timestamp with time zone not null,
                               date_time_changed            timestamp with time zone,
                               type                         varchar(10) not null, --boxoffice - коррекция кассы, cagent - коррекция баланса с контрагентом, account - коррекция расчётного счёта
                               summ                         numeric(15,2) not null,
                               boxoffice_id                 bigint,
                               payment_account_id           bigint,
                               cagent_id                    bigint,
                               description                  varchar(2048),
                               is_completed                 boolean,
                               is_deleted                   boolean,
                               uid                          varchar (36) not null,
                               linked_docs_group_id         bigint,
                               foreign key (master_id)  references users(id),
                               foreign key (creator_id) references users(id),
                               foreign key (changer_id) references users(id),
                               foreign key (company_id) references companies(id),
                               foreign key (boxoffice_id) references sprav_boxoffice(id),
                               foreign key (payment_account_id) references companies_payment_accounts(id),
                               foreign key (cagent_id) references cagents(id)
);


insert into permissions (id,name,description,document_name,document_id) values
(539,'Боковая панель - отображать в списке документов','Показывать документ в списке документов на боковой панели','Корректировка',41),
(540,'Создание документов по всем предприятиям','Возможность создавать новые документы "Корректировка" по всем предприятиям','Корректировка',41),
(541,'Создание документов своего предприятия','Возможность создавать новые документы "Корректировка" своего предприятия','Корректировка',41),
(542,'Удаление документов по всем предприятиям','Возможность удалить документ "Корректировка" в архив по всем предприятиям','Корректировка',41),
(543,'Удаление документов своего предприятия','Возможность удалить документ "Корректировка" своего предприятия в архив','Корректировка',41),
(544,'Просмотр документов по всем предприятиям','Прсмотр информации в документах "Корректировка" по всем предприятиям','Корректировка',41),
(545,'Просмотр документов своего предприятия','Прсмотр информации в документах "Корректировка" своего предприятия','Корректировка',41),
(546,'Редактирование документов по всем предприятиям','Редактирование документов "Корректировка" по всем предприятиям','Корректировка',41),
(547,'Редактирование документов своего предприятия','Редактирование документов "Корректировка" своего предприятия','Корректировка',41),
(548,'Проведение документов по всем предприятиям','Проведение документов "Корректировка" по всем предприятиям','Корректировка',41),
(549,'Проведение документов своего предприятия','Проведение документов "Корректировка" своего предприятия','Корректировка',41);



create table correction_files (
                                  correction_id bigint not null,
                                  file_id bigint not null,
                                  foreign key (file_id) references files (id) ON DELETE CASCADE,
                                  foreign key (correction_id ) references correction (id) ON DELETE CASCADE
);

alter table departments add column boxoffice_id bigint;
alter table departments add constraint boxoffice_id_fkey foreign key (boxoffice_id) references sprav_boxoffice (id);
alter table departments add column payment_account_id bigint;
alter table departments add constraint payment_account_id_fkey foreign key (payment_account_id) references companies_payment_accounts (id);

alter table departments drop column is_archive;

insert into documents (id, name, page_name, show, table_name, doc_name_ru) values (42,'Кассы предприятия','boxoffice',1,'sprav_boxoffice','Кассы предприятия');

insert into permissions (id,name,description,document_name,document_id) values
(550,'Боковая панель - отображать в списке документов','Показывать документ в списке документов на боковой панели','Кассы предприятия',42),
(551,'Создание документов по всем предприятиям','Возможность создавать новые документы "Кассы предприятия" по всем предприятиям','Кассы предприятия',42),
(552,'Создание документов своего предприятия','Возможность создавать новые документы "Кассы предприятия" своего предприятия','Кассы предприятия',42),
(553,'Удаление документов по всем предприятиям','Возможность удалить документ "Кассы предприятия" в архив по всем предприятиям','Кассы предприятия',42),
(554,'Удаление документов своего предприятия','Возможность удалить документ "Кассы предприятия" своего предприятия в архив','Кассы предприятия',42),
(555,'Просмотр документов по всем предприятиям','Прсмотр информации в документах "Кассы предприятия" по всем предприятиям','Кассы предприятия',42),
(556,'Просмотр документов своего предприятия','Прсмотр информации в документах "Кассы предприятия" своего предприятия','Кассы предприятия',42),
(557,'Редактирование документов по всем предприятиям','Редактирование документов "Кассы предприятия" по всем предприятиям','Кассы предприятия',42),
(558,'Редактирование документов своего предприятия','Редактирование документов "Кассы предприятия" своего предприятия','Кассы предприятия',42);


CREATE INDEX acceptance_id_index ON acceptance_product USING btree (acceptance_id);
CREATE INDEX acceptance_master_id_index ON acceptance USING btree (master_id);
CREATE INDEX acceptance_company_id_index ON acceptance USING btree (company_id);
CREATE INDEX acceptance_cagent_id_index ON acceptance USING btree (cagent_id);

CREATE INDEX shipment_id_index ON shipment_product USING btree (shipment_id);
CREATE INDEX shipment_master_id_index ON shipment USING btree (master_id);
CREATE INDEX shipment_company_id_index ON shipment USING btree (company_id);
CREATE INDEX shipment_cagent_id_index ON shipment USING btree (cagent_id);

CREATE INDEX return_id_index ON return_product USING btree (return_id);
CREATE INDEX return_master_id_index ON return USING btree (master_id);
CREATE INDEX return_company_id_index ON return USING btree (company_id);
CREATE INDEX return_cagent_id_index ON return USING btree (cagent_id);

CREATE INDEX returnsup_id_index ON returnsup_product USING btree (returnsup_id);
CREATE INDEX returnsup_master_id_index ON returnsup USING btree (master_id);
CREATE INDEX returnsup_company_id_index ON returnsup USING btree (company_id);
CREATE INDEX returnsup_cagent_id_index ON returnsup USING btree (cagent_id);

CREATE INDEX paymentin_master_id_index ON paymentin USING btree (master_id);
CREATE INDEX paymentin_company_id_index ON paymentin USING btree (company_id);
CREATE INDEX paymentin_agent_id_index ON paymentin USING btree (cagent_id);

CREATE INDEX paymentout_master_id_index ON paymentout USING btree (master_id);
CREATE INDEX paymentout_company_id_index ON paymentout USING btree (company_id);
CREATE INDEX paymentout_cagent_id_index ON paymentout USING btree (cagent_id);

CREATE INDEX orderin_master_id_index ON orderin USING btree (master_id);
CREATE INDEX orderin_company_id_index ON orderin USING btree (company_id);
CREATE INDEX orderin_cagent_id_index ON orderin USING btree (cagent_id);

CREATE INDEX orderout_master_id_index ON orderout USING btree (master_id);
CREATE INDEX orderout_company_id_index ON orderout USING btree (company_id);
CREATE INDEX orderout_cagent_id_index ON orderout USING btree (cagent_id);

CREATE INDEX orderout_boxoffice_id_index ON orderout USING btree (boxoffice_id);
CREATE INDEX orderin_boxoffice_id_index ON orderin USING btree (boxoffice_id);
CREATE INDEX paymentout_payment_account_id_index ON paymentout USING btree (payment_account_id);
CREATE INDEX paymentin_payment_account_id_index ON paymentin USING btree (payment_account_id);

alter table correction add column doc_number int not null;

alter table correction add column status_id bigint;
alter table correction add constraint status_id_fkey foreign key (status_id) references sprav_status_dock (id);

create table settings_correction (
                                   id                          bigserial primary key not null,
                                   master_id                   bigint not null,
                                   company_id                  bigint not null,
                                   user_id                     bigint  UNIQUE not null,
                                   status_id_on_complete       bigint,
                                   foreign key (master_id) references users(id),
                                   foreign key (user_id) references users(id),
                                   foreign key (status_id_on_complete) references sprav_status_dock(id),
                                   foreign key (company_id) references companies(id)
);

alter table shifts add column uid varchar (36);

insert into documents (id, name, page_name, show, table_name, doc_name_ru) values (43,'Кассовая смена','shifts',1,'shifts','Кассовые смены');

insert into permissions (id,name,description,document_name,document_id) values
(559,'Боковая панель - отображать в списке документов','Показывать документ в списке документов на боковой панели','Кассовые смены',43),
(560,'Просмотр документов по всем предприятиям','Прсмотр информации в документах "Кассовые смены" по всем предприятиям','Кассовые смены',43),
(561,'Просмотр документов своего предприятия','Прсмотр информации в документах "Кассовые смены" своего предприятия','Кассовые смены',43);




alter table receipts add column uid varchar (36);

insert into documents (id, name, page_name, show, table_name, doc_name_ru) values (44,'Кассовый чек','receipts',1,'receipts','Кассовые чеки');

insert into permissions (id,name,description,document_name,document_id) values
(562,'Боковая панель - отображать в списке документов','Показывать документ в списке документов на боковой панели','Кассовые чеки',44),
(563,'Просмотр документов по всем предприятиям','Прсмотр информации в документах "Кассовые чеки" по всем предприятиям','Кассовые чеки',44),
(564,'Просмотр документов своего предприятия','Прсмотр информации в документах "Кассовые чеки" своего предприятия','Кассовые чеки',44);

alter table shifts    add column linked_docs_group_id bigint;
alter table receipts  add column linked_docs_group_id bigint;
alter table shifts    add column acquiring_bank_id bigint; -- id банка-эквайера по электронным платежам
alter table receipts  add column acquiring_bank_id bigint; -- id банка-эквайера по электронным платежам
alter table receipts  add column parent_tablename varchar (16);--retail_sales, return, shipment - из данных документов могут создаваться чеки
alter table receipts  add column parent_doc_id int; -- id в таблице documents
alter table receipts  add column return_id bigint; -- заполняется если чек создан из возврата покупателя

alter table shifts    add constraint linked_docs_group_id_fkey foreign key (linked_docs_group_id) references linked_docs_groups (id);
alter table receipts  add constraint linked_docs_group_id_fkey foreign key (linked_docs_group_id) references linked_docs_groups (id);
alter table shifts    add constraint acquiring_bank_id_fkey foreign key (acquiring_bank_id) references cagents (id);
alter table receipts  add constraint acquiring_bank_id_fkey foreign key (acquiring_bank_id) references cagents (id);
alter table receipts  add constraint parent_doc_id_fkey foreign key (parent_doc_id) references documents (id);
alter table receipts  add constraint return_id_fkey foreign key (return_id) references return (id);

CREATE INDEX shifts_master_id_index ON shifts USING btree (master_id);
CREATE INDEX shifts_company_id_index ON shifts USING btree (company_id);
CREATE INDEX receipts_master_id_index ON receipts USING btree (master_id);
CREATE INDEX receipts_company_id_index ON receipts USING btree (company_id);

CREATE INDEX users_master_id_index ON users USING btree (master_id);
CREATE INDEX users_company_id_index ON users USING btree (company_id);

CREATE INDEX receipts_shift_id_index ON receipts USING btree (shift_id);

insert into permissions (id,name,description,document_name,document_id) values
(565,'Просмотр документов своих отделений','Прсмотр информации в документах "Кассовые чеки" по своим отделениям','Кассовые чеки',44);

insert into permissions (id,name,description,document_name,document_id) values
(566,'Просмотр документов своих отделений','Прсмотр информации в документах "Кассовые смены" по своим отделениям','Кассовые смены',43);

alter table receipts  drop constraint parent_doc_id_products_history_quantity_checkfkey;

alter table receipts  add constraint parent_doc_id_fkey foreign key (parent_doc_id) references documents (id);


alter table orderin add   column moving_type varchar (10);
alter table paymentin add column moving_type varchar (10);

alter table orderin add column payment_account_from_id bigint;
alter table orderin add column boxoffice_from_id bigint;
alter table orderin add column kassa_from_id bigint;

alter table orderin add constraint payment_account_from_id_fkey foreign key (payment_account_from_id) references companies_payment_accounts (id);
alter table orderin add constraint boxoffice_from_id_fkey foreign key (boxoffice_from_id) references sprav_boxoffice (id);
alter table orderin add constraint kassa_from_id_fkey foreign key (kassa_from_id) references kassa (id);

alter table paymentin add column payment_account_from_id bigint;
alter table paymentin add column boxoffice_from_id bigint;

alter table paymentin add constraint payment_account_from_id_fkey foreign key (payment_account_from_id) references companies_payment_accounts (id);
alter table paymentin add constraint boxoffice_from_id_fkey foreign key (boxoffice_from_id) references sprav_boxoffice (id);

alter table orderout add column kassa_to_id bigint;
alter table orderout add constraint kassa_to_id_fkey foreign key (kassa_to_id) references kassa (id);



create table history_cagent_summ (
                              id                                  bigserial primary key not null,
                              master_id                           bigint not null,
                              company_id                          bigint not null,
                              date_time_created                   timestamp with time zone not null,
                              object_id                           bigint not null, -- = cagent_id
                              doc_table_name                      varchar (16) not null, -- table name of document
                              doc_id                              bigint not null,	-- document id in table doc_table_name
                              summ_before                         numeric(15,2) not null,
                              summ_change                         numeric(15,2) not null,
                              summ_result                         numeric(15,2) not null,
                              foreign key (master_id)  references users(id),
                              foreign key (company_id) references companies(id),
                              foreign key (object_id)  references cagents(id)
);

create table history_payment_account_summ (
                              id                                  bigserial primary key not null,
                              master_id                           bigint not null,
                              company_id                          bigint not null,
                              date_time_created                   timestamp with time zone not null,
                              object_id                           bigint not null, -- = payment_account_id
                              doc_table_name                      varchar (16) not null, -- table name of document
                              doc_id                              bigint not null,	-- document id in table doc_table_name
                              summ_before                         numeric(15,2) not null,
                              summ_change                         numeric(15,2) not null,
                              summ_result                         numeric(15,2) not null,
                              foreign key (master_id)  references users(id),
                              foreign key (company_id) references companies(id),
                              foreign key (object_id)  references cagents(id)
);

create table history_boxoffice_summ (
                              id                                  bigserial primary key not null,
                              master_id                           bigint not null,
                              company_id                          bigint not null,
                              date_time_created                   timestamp with time zone not null,
                              object_id                           bigint not null, -- = boxoffice_id
                              doc_table_name                      varchar (16) not null, -- table name of document
                              doc_id                              bigint not null,	-- document id in table doc_table_name
                              summ_before                         numeric(15,2) not null,
                              summ_change                         numeric(15,2) not null,
                              summ_result                         numeric(15,2) not null,
                              foreign key (master_id)  references users(id),
                              foreign key (company_id) references companies(id),
                              foreign key (object_id)  references cagents(id)
);

create table history_kassa_summ (
                              id                                  bigserial primary key not null,
                              master_id                           bigint not null,
                              company_id                          bigint not null,
                              date_time_created                   timestamp with time zone not null,
                              object_id                           bigint not null, -- = kassa_id
                              doc_table_name                      varchar (16) not null, -- table name of document
                              doc_id                              bigint not null,	-- document id in table doc_table_name
                              summ_before                         numeric(15,2) not null,
                              summ_change                         numeric(15,2) not null,
                              summ_result                         numeric(15,2) not null,
                              foreign key (master_id)  references users(id),
                              foreign key (company_id) references companies(id),
                              foreign key (object_id)  references cagents(id)
);


alter table history_payment_account_summ add constraint summ_before_check CHECK (summ_before >= 0);
alter table history_payment_account_summ add constraint summ_result_check CHECK (summ_result >= 0);
alter table history_boxoffice_summ add constraint summ_before_check CHECK (summ_before >= 0);
alter table history_boxoffice_summ add constraint summ_result_check CHECK (summ_result >= 0);
alter table history_kassa_summ add constraint summ_before_check CHECK (summ_before >= 0);
alter table history_kassa_summ add constraint summ_result_check CHECK (summ_result >= 0);

CREATE INDEX history_payment_account_summ_master_id_index ON history_payment_account_summ USING btree (master_id);
CREATE INDEX history_payment_account_summ_company_id_index ON history_payment_account_summ USING btree (company_id);
CREATE INDEX history_boxoffice_summ_master_id_index ON history_boxoffice_summ USING btree (master_id);
CREATE INDEX history_boxoffice_summ_company_id_index ON history_boxoffice_summ USING btree (company_id);
CREATE INDEX history_kassa_summ_master_id_index ON history_kassa_summ USING btree (master_id);
CREATE INDEX history_kassa_summ_company_id_index ON history_kassa_summ USING btree (company_id);
CREATE INDEX history_cagent_summ_master_id_index ON history_cagent_summ USING btree (master_id);
CREATE INDEX history_cagent_summ_company_id_index ON history_cagent_summ USING btree (company_id);

-- Выемка
insert into documents (id, name, page_name, show, table_name, doc_name_ru) values (45,'Выемка','withdrawal',1,'withdrawal','Выемка');
create table withdrawal(
                         id bigserial primary key not null,
                         master_id  bigint not null,
                         creator_id bigint not null, -- кассир
                         date_time_created timestamp with time zone not null, -- время операции
                         company_id bigint not null, -- предприятие кассы
                         department_id bigint not null, -- отделение в котором установлена касса
                         kassa_id bigint not null, -- касса ККМ
                         boxoffice_id bigint, -- касса ККМ
                         status_id bigint, -- статуса нет у документа, но колонка нужна для работы связанности документов
                         doc_number int not null,
                         description varchar(2048),  -- примечание кассира к операции
                         summ numeric(15,2) not null, --сумма операции
                         is_delivered boolean,  -- деньги доставлены до кассы предприятия (false = "зависшие деньги" - между кассой ККМ и кассой предприятия)
                         is_completed boolean,       -- проведено - всегда, т.к. выемка не редактируется, не проводится и не удаляется, создается уже проведенной
                         uid varchar (36),
                         linked_docs_group_id bigint,
                         foreign key (master_id) references users(id),
                         foreign key (creator_id) references users(id),
                         foreign key (company_id) references companies(id),
                         foreign key (department_id) references departments(id),
                         foreign key (kassa_id) references kassa(id),
                         foreign key (boxoffice_id) references sprav_boxoffice(id),
                         foreign key (linked_docs_group_id) references linked_docs_groups(id));
alter table linked_docs add column withdrawal_id bigint;
alter table linked_docs add constraint withdrawal_id_fkey foreign key (withdrawal_id) references withdrawal (id);
insert into permissions (id,name,description,document_name,document_id) values
(567,'Боковая панель - отображать в списке документов','Показывать документ в списке документов на боковой панели','Выемка',45),
(568,'Создание документов по всем предприятиям','Возможность создавать новые документы "Выемка" по всем предприятиям','Выемка',45),
(569,'Создание документов своего предприятия','Возможность создавать новые документы "Выемка" своего предприятия','Выемка',45),
(570,'Создание документов своих отделений','Возможность создавать новые документы "Выемка" по своим отделениям','Выемка',45),
(571,'Просмотр документов по всем предприятиям','Прсмотр информации в документах "Выемка" по всем предприятиям','Выемка',45),
(572,'Просмотр документов своего предприятия','Прсмотр информации в документах "Выемка" своего предприятия','Выемка',45),
(573,'Просмотр документов своих отделений','Прсмотр информации в документах "Выемка" по своим отделениям','Выемка',45),
(574,'Просмотр документов созданных собой','Прсмотр информации в документах "Выемка", созданных собой','Выемка',45);

alter table orderin add column withdrawal_id bigint;
alter table orderin add constraint withdrawal_id_fkey foreign key (withdrawal_id) references withdrawal (id);
alter table orderin add column paymentout_id bigint;
alter table orderin add constraint paymentout_id_fkey foreign key (paymentout_id) references paymentout (id);
alter table orderin add column orderout_id bigint;
alter table orderin add constraint orderout_id_fkey foreign key (orderout_id) references orderout (id);

alter table paymentin add column paymentout_id bigint;
alter table paymentin add constraint paymentout_id_fkey foreign key (paymentout_id) references paymentout (id);
alter table paymentin add column orderout_id bigint;
alter table paymentin add constraint orderout_id_fkey foreign key (orderout_id) references orderout (id);

alter table paymentout add column is_delivered boolean;
alter table orderout add column is_delivered boolean;

-- Внесение
insert into documents (id, name, page_name, show, table_name, doc_name_ru) values (46,'Внесение','depositing',1,'depositing','Внесение');
create table depositing(
                         id bigserial primary key not null,
                         master_id  bigint not null,
                         creator_id bigint not null, -- кассир
                         date_time_created timestamp with time zone not null, -- время операции
                         company_id bigint not null, -- предприятие кассы
                         department_id bigint not null, -- отделение в котором установлена касса
                         kassa_id bigint not null, -- касса ККМ
                         boxoffice_id bigint, -- касса предприятия
                         status_id bigint, -- статуса нет у документа, но колонка нужна для работы связанности документов
                         doc_number int not null,
                         description varchar(2048),  -- примечание кассира к операции
                         summ numeric(15,2) not null, --сумма операции
                         orderout_id bigint not null,  -- id расходного ордера, деньги по которому вносятся
                         is_completed boolean,       -- проведено - всегда, т.к. Внесение не редактируется, не проводится и не удаляется, создается уже проведенной
                         uid varchar (36),
                         linked_docs_group_id bigint,
                         foreign key (master_id) references users(id),
                         foreign key (creator_id) references users(id),
                         foreign key (company_id) references companies(id),
                         foreign key (department_id) references departments(id),
                         foreign key (orderout_id) references orderout(id),
                         foreign key (kassa_id) references kassa(id),
                         foreign key (boxoffice_id) references sprav_boxoffice(id),
                         foreign key (linked_docs_group_id) references linked_docs_groups(id));
alter table linked_docs add column depositing_id bigint;
alter table linked_docs add constraint depositing_id_fkey foreign key (depositing_id) references depositing (id);
insert into permissions (id,name,description,document_name,document_id) values
(575,'Боковая панель - отображать в списке документов','Показывать документ в списке документов на боковой панели','Внесение',46),
(576,'Создание документов по всем предприятиям','Возможность создавать новые документы "Внесение" по всем предприятиям','Внесение',46),
(577,'Создание документов своего предприятия','Возможность создавать новые документы "Внесение" своего предприятия','Внесение',46),
(578,'Создание документов своих отделений','Возможность создавать новые документы "Внесение" по своим отделениям','Внесение',46),
(579,'Просмотр документов по всем предприятиям','Прсмотр информации в документах "Внесение" по всем предприятиям','Внесение',46),
(580,'Просмотр документов своего предприятия','Прсмотр информации в документах "Внесение" своего предприятия','Внесение',46),
(581,'Просмотр документов своих отделений','Прсмотр информации в документах "Внесение" по своим отделениям','Внесение',46),
(582,'Просмотр документов созданных собой','Прсмотр информации в документах "Внесение", созданных собой','Внесение',46);


alter table orderout add column kassa_department_id bigint; -- отделение, где находится касса ККМ, в которую будет внесение
alter table orderout add constraint kassa_department_id_fkey foreign key (kassa_department_id) references departments (id);

-- уникальность на то, что в приходном документе (вход. платеж, приходный ордер, внесение) только 1 уже проведенный расходный документ (чтобы убрать возможность создавать несколько проводок одного и того же исходящего внутреннего платежа, тем самым порождая деньги из воздуха)
CREATE UNIQUE INDEX paymentin_paymentout_unique_index ON paymentin (paymentout_id) WHERE is_completed;
CREATE UNIQUE INDEX paymentin_orderout_unique_index ON paymentin (orderout_id) WHERE is_completed;
CREATE UNIQUE INDEX orderin_orderout_unique_index ON orderin (orderout_id) WHERE is_completed;
CREATE UNIQUE INDEX orderin_paymentout_unique_index ON orderin (paymentout_id) WHERE is_completed;
CREATE UNIQUE INDEX orderin_withdrawal_unique_index ON orderin (withdrawal_id) WHERE is_completed;
CREATE UNIQUE INDEX depositing_orderout_unique_index ON depositing (orderout_id) WHERE is_completed;


-- Взаиморасчёты
insert into documents (id, name, page_name, show, table_name, doc_name_ru) values (47,'Взаиморасчёты','mutualpayment',1,'','Взаиморасчёты');
insert into permissions (id,name,description,document_name,document_id) values
(583,'Боковая панель - отображать в списке документов','Показывать документ в списке документов на боковой панели','Взаиморасчёты',47),
(584,'Просмотр документов по всем предприятиям','Прсмотр информации в документах "Взаиморасчёты" по всем предприятиям','Взаиморасчёты',47),
(585,'Просмотр документов своего предприятия','Прсмотр информации в документах "Взаиморасчёты" своего предприятия','Взаиморасчёты',47);

alter table history_payment_account_summ add column doc_number varchar(32);
alter table history_payment_account_summ add column doc_status_id bigint;
alter table history_payment_account_summ add constraint doc_status_id_fkey foreign key (doc_status_id) references sprav_status_dock (id);
alter table history_boxoffice_summ add column doc_number varchar(32);
alter table history_boxoffice_summ add column doc_status_id bigint;
alter table history_boxoffice_summ add constraint doc_status_id_fkey foreign key (doc_status_id) references sprav_status_dock (id);
alter table history_cagent_summ add column doc_number varchar(32);
alter table history_cagent_summ add column doc_status_id bigint;
alter table history_cagent_summ add constraint doc_status_id_fkey foreign key (doc_status_id) references sprav_status_dock (id);

alter table history_payment_account_summ add column doc_page_name varchar(32);
alter table history_boxoffice_summ add column doc_page_name varchar(32);
alter table history_cagent_summ add column doc_page_name varchar(32);
update history_payment_account_summ set doc_page_name = doc_table_name;
update history_boxoffice_summ set doc_page_name = doc_table_name;
update history_cagent_summ set doc_page_name = doc_table_name;
alter table history_payment_account_summ alter column doc_page_name set not null;
alter table history_boxoffice_summ alter column doc_page_name set not null;
alter table history_cagent_summ alter column doc_page_name set not null;


-- Движение денежных средств
insert into documents (id, name, page_name, show, table_name, doc_name_ru) values (48,'Движение денежных средств','moneyflow',1,'','Движение денежных средств');
insert into permissions (id,name,description,document_name,document_id) values
(586,'Боковая панель - отображать в списке документов','Показывать документ в списке документов на боковой панели','Движение денежных средств',48),
(587,'Просмотр документов по всем предприятиям','Прсмотр информации в документах "Движение денежных средств" по всем предприятиям','Движение денежных средств',48),
(588,'Просмотр документов своего предприятия','Прсмотр информации в документах "Движение денежных средств" своего предприятия','Движение денежных средств',48);

alter table history_kassa_summ add column doc_number varchar(32);
alter table history_kassa_summ add column doc_status_id bigint;
alter table history_kassa_summ add constraint doc_status_id_fkey foreign key (doc_status_id) references sprav_status_dock (id);
alter table history_kassa_summ add column doc_page_name varchar(32);
update history_kassa_summ set doc_page_name = doc_table_name;
alter table history_kassa_summ alter column doc_page_name set not null;


-- Прибыли и убытки
insert into documents (id, name, page_name, show, table_name, doc_name_ru) values (49,'Прибыли и убытки','profitloss',1,'','Прибыли и убытки');
insert into permissions (id,name,description,document_name,document_id) values
(589,'Боковая панель - отображать в списке документов','Показывать документ в списке документов на боковой панели','Прибыли и убытки',49),
(590,'Просмотр документов по всем предприятиям','Прсмотр информации в документах "Прибыли и убытки" по всем предприятиям','Прибыли и убытки',49),
(591,'Просмотр документов своего предприятия','Прсмотр информации в документах "Прибыли и убытки" своего предприятия','Прибыли и убытки',49);

CREATE INDEX permissions_id_index ON permissions USING btree (id);
CREATE INDEX usergroup_permissions_usergroup_id_index ON usergroup_permissions USING btree (usergroup_id);
CREATE INDEX usergroup_permissions_permission_id_index ON usergroup_permissions USING btree (permission_id);
CREATE INDEX users_username_index ON users USING btree (username);
CREATE INDEX users_password_index ON users USING btree (password);

drop table perm_permgroup;
drop table permgroup;


insert into permissions (id,name,description,document_name,document_id) values
(592,'Отчёт "Приход и расход" - просмотр по всем предприятиям','Возможность построения отчёта по приходу и расходу по всем предприятиям','Стартовая страница',26),
(593,'Отчёт "Приход и расход" - просмотр по своему предприятию','Возможность построения отчёта по приходу и расходу по своему предприятию','Стартовая страница',26);

insert into permissions (id,name,description,document_name,document_id) values
(594,'Отчёт "Деньги" - просмотр по всем предприятиям','Возможность построения отчёта по приходу и расходу по всем предприятиям','Стартовая страница',26),
(595,'Отчёт "Деньги" - просмотр по своему предприятию','Возможность построения отчёта по приходу и расходу по своему предприятию','Стартовая страница',26),
(596,'Отчёт "Мы должны" - просмотр по всем предприятиям','Возможность построения отчёта по приходу и расходу по всем предприятиям','Стартовая страница',26),
(597,'Отчёт "Мы должны" - просмотр по своему предприятию','Возможность построения отчёта по приходу и расходу по своему предприятию','Стартовая страница',26),
(598,'Отчёт "Нам должны" - просмотр по всем предприятиям','Возможность построения отчёта по приходу и расходу по всем предприятиям','Стартовая страница',26),
(599,'Отчёт "Нам должны" - просмотр по своему предприятию','Возможность построения отчёта по приходу и расходу по своему предприятию','Стартовая страница',26),
(600,'Отчёт "Новые заказы" - просмотр по всем предприятиям','Возможность построения отчёта по приходу и расходу по всем предприятиям','Стартовая страница',26),
(601,'Отчёт "Новые заказы" - просмотр по своему предприятию','Возможность построения отчёта по приходу и расходу по своему предприятию','Стартовая страница',26),
(602,'Отчёт "Просроченные заказы" - просмотр по всем предприятиям','Возможность построения отчёта по приходу и расходу по всем предприятиям','Стартовая страница',26),
(603,'Отчёт "Просроченные заказы" - просмотр по своему предприятию','Возможность построения отчёта по приходу и расходу по своему предприятию','Стартовая страница',26),
(604,'Отчёт "Просроченные счета" - просмотр по всем предприятиям','Возможность построения отчёта по приходу и расходу по всем предприятиям','Стартовая страница',26),
(605,'Отчёт "Просроченные счета" - просмотр по своему предприятию','Возможность построения отчёта по приходу и расходу по своему предприятию','Стартовая страница',26);

insert into permissions (id,name,description,document_name,document_id) values
(606,'Отчёт "Товарные остатки" - просмотр по всем предприятиям','Возможность построения отчёта "Товарные остатки" по всем предприятиям','Стартовая страница',26),
(607,'Отчёт "Товарные остатки" - просмотр по своему предприятию','Возможность построения отчёта "Товарные остатки" по своему предприятию','Стартовая страница',26),
(608,'Отчёт "Товарные остатки" - просмотр по своим отделениям','Возможность построения отчёта "Товарные остатки" по своим отделениям','Стартовая страница',26);

insert into permissions (id,name,description,document_name,document_id) values
(609,'Отчёт "Операционные расходы" - просмотр по всем предприятиям','Возможность построения отчёта по операционным расходам по всем предприятиям','Стартовая страница',26),
(610,'Отчёт "Операционные расходы" - просмотр по своему предприятию','Возможность построения отчёта по операционным расходам по своему предприятию','Стартовая страница',26);

alter table kassa add column is_virtual boolean; --виртуальная касса
alter table kassa add column allow_acquiring boolean; --прием безнала на данной кассе
alter table kassa add column acquiring_bank_id bigint; -- банк-эквайер
alter table kassa add column acquiring_precent numeric(4,2); -- процент банку за услугу эквайринга
alter table kassa add constraint acquiring_bank_id_fkey foreign key (acquiring_bank_id) references cagents (id);

alter table kassa add column acquiring_service_id bigint; --  id услуги банка-эквайера
alter table kassa add constraint acquiring_service_id_fkey foreign key (acquiring_service_id) references products (id);

alter table acceptance alter column department_id drop not null;

alter table kassa add column payment_account_id bigint; --  id расчетного счета
alter table kassa add constraint payment_account_id_fkey foreign key (payment_account_id) references companies_payment_accounts (id);
alter table kassa add column expenditure_id bigint; --  id статьи расходов
alter table kassa add constraint expenditure_id_fkey foreign key (expenditure_id) references sprav_expenditure_items (id);

alter table shifts add column acqu_acceptance_id bigint;
alter table shifts add constraint acqu_acceptance_id_fkey foreign key (acqu_acceptance_id) references acceptance (id);
alter table shifts add column acqu_paymentin_id bigint;
alter table shifts add constraint acqu_paymentin_id_fkey foreign key (acqu_paymentin_id) references paymentin (id);
alter table shifts add column acqu_paymentout_id bigint;
alter table shifts add constraint acqu_paymentout_id_fkey foreign key (acqu_paymentout_id) references paymentout (id);
alter table shifts add column acqu_correction_id bigint;
alter table shifts add constraint acqu_correction_id_fkey foreign key (acqu_correction_id) references correction (id);

alter table linked_docs add column shifts_id bigint;
alter table linked_docs add constraint shifts_id_fkey foreign key (shifts_id) references shifts (id);
alter table linked_docs add column correction_id bigint;
alter table linked_docs add constraint correction_id_fkey foreign key (correction_id) references correction (id);

alter table history_payment_account_summ drop constraint history_payment_account_summ_object_id_fkey;
alter table history_kassa_summ drop constraint history_kassa_summ_object_id_fkey;
alter table history_boxoffice_summ drop constraint history_boxoffice_summ_object_id_fkey;
alter table history_payment_account_summ add constraint history_payment_account_summ_object_id_fkey foreign key (object_id) references companies_payment_accounts (id);
alter table history_kassa_summ add constraint history_kassa_summ_object_id_fkey foreign key (object_id) references kassa (id);
alter table history_boxoffice_summ add constraint history_boxoffice_summ_object_id_fkey foreign key (object_id) references sprav_boxoffice (id);

alter table shifts add column status_id bigint;
alter table shifts add constraint shift_status_id_fkey foreign key (status_id) references sprav_status_dock (id);
alter table shifts add column doc_number bigint;
alter table shifts add column is_completed boolean;

create table kassa_files (
                               kassa_id bigint not null,
                               file_id bigint not null,
                               foreign key (file_id) references files (id) ON DELETE CASCADE,
                               foreign key (kassa_id ) references kassa (id) ON DELETE CASCADE
);
 update shifts set doc_number=shift_number;
 update shifts set is_completed=true where closer_id is not null;



--********************************************************************************************************************************************
--********************************************************************************************************************************************
--********************************************************************************************************************************************

CREATE INDEX files_name ON files USING btree (name);
CREATE INDEX files_master_id ON files USING btree (master_id);
CREATE INDEX files_company_id ON files USING btree (company_id);

create table template_types (
                           id int primary key not null,
                           template_type varchar(64) not null,
                           name_ru varchar(64) not null
);

insert into template_types (id,name_ru,template_type) values
(1,'Товарный чек','product_receipt'),
(2,'Акт','act'),
(3,'Счёт покупателю','invoiceout'),
(4,'Счёт покупателю с печатью и подписью','invoiceout_stamp_sign'),
(5,'Транспортная накладная','transport_invoice'),
(6,'Расходная накладная','expenditure_invoice'),
(7,'ТОРГ-12','torg12'),
(8,'УПД (с прослеживаемостью)','upd_with'),
(9,'УПД (без прослеживаемости)','upd_without');

create table template_docs (
                             id               bigserial primary key not null,
                             master_id        bigint not null,
                             company_id       bigint not null,
                             template_type_id int not null,                  -- id типа шаблона из таблицы template_types
                             file_id          bigint not null,                  -- id файла, содержащего шаблон
                             document_id      int not null,                     -- документ, в котором будет находиться шаблон
                             is_show          boolean not null,                 -- отображать шаблон в списке шаблонов
                             output_order     int not null,                     -- порядок вывода шаблона в списке шаблонов
                             foreign key (master_id)        references users(id),
                             foreign key (company_id)       references companies(id),
                             foreign key (document_id)      references documents(id),
                             foreign key (file_id)          references files(id),
                             foreign key (template_type_id) references template_types(id)
);


ALTER TABLE template_docs ADD CONSTRAINT company_document_template_uq UNIQUE (company_id, template_type_id, document_id) ;


ALTER TABLE sprav_sys_nds ADD COLUMN value int;
ALTER TABLE sprav_sys_nds ADD COLUMN multiplier numeric(6,4);
update sprav_sys_nds set value=20, multiplier=1.2 where name='20%';
update sprav_sys_nds set value=10, multiplier=1.1 where name='10%';
update sprav_sys_nds set value=0, multiplier=1 where name='0%';
update sprav_sys_nds set value=0, multiplier=1 where name='Без НДС';

--********************************************************************************************************************************************
--********************************************************************************************************************************************
--********************************************************************************************************************************************



alter table history_cagent_summ           add column is_completed boolean;
alter table history_payment_account_summ  add column is_completed boolean;
alter table history_boxoffice_summ        add column is_completed boolean;
alter table history_kassa_summ            add column is_completed boolean;

update history_cagent_summ           set is_completed = true;
update history_payment_account_summ  set is_completed = true;
update history_boxoffice_summ        set is_completed = true;
update history_kassa_summ            set is_completed = true;

alter table history_cagent_summ           alter column is_completed set not null;
alter table history_payment_account_summ  alter column is_completed set not null;
alter table history_boxoffice_summ        alter column is_completed set not null;
alter table history_kassa_summ            alter column is_completed set not null;

alter table history_cagent_summ           add column summ_in  numeric(15,2);
alter table history_payment_account_summ  add column summ_in  numeric(15,2);
alter table history_boxoffice_summ        add column summ_in  numeric(15,2);
alter table history_kassa_summ            add column summ_in  numeric(15,2);
alter table history_cagent_summ           add column summ_out  numeric(15,2);
alter table history_payment_account_summ  add column summ_out  numeric(15,2);
alter table history_boxoffice_summ        add column summ_out  numeric(15,2);
alter table history_kassa_summ            add column summ_out  numeric(15,2);

update history_cagent_summ set summ_in = (CASE WHEN summ_change>0 THEN summ_change ELSE 0.00 END);
update history_cagent_summ set summ_out = (CASE WHEN summ_change<0 THEN summ_change ELSE 0.00 END);
update history_payment_account_summ set summ_in = (CASE WHEN summ_change>0 THEN summ_change ELSE 0.00 END);
update history_payment_account_summ set summ_out = (CASE WHEN summ_change<0 THEN summ_change ELSE 0.00 END);
update history_boxoffice_summ set summ_in = (CASE WHEN summ_change>0 THEN summ_change ELSE 0.00 END);
update history_boxoffice_summ set summ_out = (CASE WHEN summ_change<0 THEN summ_change ELSE 0.00 END);
update history_kassa_summ set summ_in = (CASE WHEN summ_change>0 THEN summ_change ELSE 0.00 END);
update history_kassa_summ set summ_out = (CASE WHEN summ_change<0 THEN summ_change ELSE 0.00 END);

update history_cagent_summ set summ_out = 0-summ_in where doc_table_name='retail_sales';
delete from history_cagent_summ where summ_out = 0 and summ_in = 0 and doc_table_name='retail_sales';

alter table history_cagent_summ           alter column summ_in set not null;
alter table history_payment_account_summ  alter column summ_in set not null;
alter table history_boxoffice_summ        alter column summ_in set not null;
alter table history_kassa_summ            alter column summ_in set not null;
alter table history_cagent_summ           alter column summ_out set not null;
alter table history_payment_account_summ  alter column summ_out set not null;
alter table history_boxoffice_summ        alter column summ_out set not null;
alter table history_kassa_summ            alter column summ_out set not null;

ALTER TABLE history_cagent_summ           ADD CONSTRAINT history_cagent_uq UNIQUE (company_id, doc_table_name, doc_id) ;
ALTER TABLE history_payment_account_summ  ADD CONSTRAINT history_payment_account_uq UNIQUE (company_id, doc_table_name, doc_id) ;
ALTER TABLE history_boxoffice_summ        ADD CONSTRAINT history_boxoffice_uq UNIQUE (company_id, doc_table_name, doc_id) ;
ALTER TABLE history_kassa_summ            ADD CONSTRAINT history_kassa_uq UNIQUE (company_id, doc_table_name, doc_id) ;

update history_cagent_summ set summ_out=summ_out*(-1) where summ_out<0;
update history_payment_account_summ set summ_out=summ_out*(-1) where summ_out<0;
update history_boxoffice_summ set summ_out=summ_out*(-1) where summ_out<0;
update history_kassa_summ set summ_out=summ_out*(-1) where summ_out<0;

alter table history_cagent_summ           drop column summ_before;
alter table history_payment_account_summ  drop column summ_before;
alter table history_boxoffice_summ        drop column summ_before;
alter table history_kassa_summ            drop column summ_before;
alter table history_cagent_summ           drop column summ_result;
alter table history_payment_account_summ  drop column summ_result;
alter table history_boxoffice_summ        drop column summ_result;
alter table history_kassa_summ            drop column summ_result;
alter table history_cagent_summ           drop column summ_change;
alter table history_payment_account_summ  drop column summ_change;
alter table history_boxoffice_summ        drop column summ_change;
alter table history_kassa_summ            drop column summ_change;

alter table product_quantity add column avg_netcost_price numeric(15,2);
-- alter table products_history add column is_completed boolean;

create table product_history(
                              id bigserial primary key not null,
                              master_id  bigint not null,
                              company_id bigint not null,
                              department_id bigint not null,
                              date_time_created timestamp with time zone not null,
                              doc_type_id int not null,
                              doc_id bigint not null,
                              product_id bigint not null,
                              change numeric(19,3) not null,
                              price numeric(18,2) not null,
                              netcost numeric(18,2) not null,
                              is_completed boolean not null,
                              foreign key (master_id) references users(id),
                              foreign key (product_id) references products(id),
                              foreign key (doc_type_id) references documents(id),
                              foreign key (company_id) references companies(id),
                              foreign key (department_id) references departments(id)
);

ALTER TABLE product_history ADD CONSTRAINT product_history_uq UNIQUE (doc_type_id, doc_id, product_id, department_id);

insert into product_history
(master_id,company_id,department_id,date_time_created,doc_type_id,doc_id,product_id,change,price,netcost,is_completed)
select master_id,company_id,department_id,date_time_created,doc_type_id,doc_id,product_id,change,last_operation_price,avg_netcost_price,true
from products_history;

-- delete from products_history  where doc_type_id=30 and doc_id=19 and product_id=49 and id !=
-- (select id from products_history where doc_type_id=30 and doc_id=17 and product_id=49 order by id desc limit 1)

ALTER TABLE companies add column st_netcost_policy varchar(4); -- политика учета себестоимости. all - по всеу предприятию, each - по каждому отделению в отдельности
update companies set st_netcost_policy = 'each' where id>0;
ALTER TABLE companies alter st_netcost_policy set not null;

alter table product_quantity add column date_time_created  timestamp with time zone;
update product_quantity set date_time_created = now();
ALTER TABLE product_quantity alter date_time_created set not null;

insert into permissions (id,name,description,document_name,document_id) values
(611,'Проведение документов по всем предприятиям','Проведение документов "Приёмка" по всем предприятиям','Приёмка',15),
(612,'Проведение документов своего предприятия','Проведение документов "Приёмка" своего предприятия','Приёмка',15),
(613,'Проведение документов своих отделений','Проведение документов "Приёмка" по своим отделениям','Приёмка',15),
(614,'Проведение документов созданных собой','Проведение документов "Приёмка" созданных собой','Приёмка',15);


CREATE INDEX product_history_master_id ON product_history USING btree (master_id);
CREATE INDEX product_history_company_id ON product_history USING btree (company_id);
CREATE INDEX product_history_product_id ON product_history USING btree (product_id);


insert into permissions (id,name,description,document_name,document_id) values
(615,'Проведение документов по всем предприятиям','Проведение документов "Возврат поставщику" по всем предприятиям','Возврат поставщику',29),
(616,'Проведение документов своего предприятия','Проведение документов "Возврат поставщику" своего предприятия','Возврат поставщику',29),
(617,'Проведение документов своих отделений','Проведение документов "Возврат поставщику" по своим отделениям','Возврат поставщику',29),
(618,'Проведение документов созданных собой','Проведение документов "Возврат поставщику" созданных собой','Возврат поставщику',29);

insert into permissions (id,name,description,document_name,document_id) values
(619,'Проведение документов по всем предприятиям','Проведение документов "Возврат покупателя" по всем предприятиям','Возврат покупателя',28),
(620,'Проведение документов своего предприятия','Проведение документов "Возврат покупателя" своего предприятия','Возврат покупателя',28),
(621,'Проведение документов своих отделений','Проведение документов "Возврат покупателя" по своим отделениям','Возврат покупателя',28),
(622,'Проведение документов созданных собой','Проведение документов "Возврат покупателя" созданных собой','Возврат покупателя',28);

insert into permissions (id,name,description,document_name,document_id) values
(623,'Проведение документов по всем предприятиям','Проведение документов "Списание" по всем предприятиям','Списание',17),
(624,'Проведение документов своего предприятия','Проведение документов "Списание" своего предприятия','Списание',17),
(625,'Проведение документов своих отделений','Проведение документов "Списание" по своим отделениям','Списание',17),
(626,'Проведение документов созданных собой','Проведение документов "Списание" созданных собой','Списание',17);

insert into permissions (id,name,description,document_name,document_id) values
(627,'Проведение документов по всем предприятиям','Проведение документов "Оприходование" по всем предприятиям','Оприходование',16),
(628,'Проведение документов своего предприятия','Проведение документов "Оприходование" своего предприятия','Оприходование',16),
(629,'Проведение документов своих отделений','Проведение документов "Оприходование" по своим отделениям','Оприходование',16),
(630,'Проведение документов созданных собой','Проведение документов "Оприходование" созданных собой','Оприходование',16);

insert into permissions (id,name,description,document_name,document_id) values
(631,'Проведение документов по всем предприятиям','Проведение документов "Инвентаризация" по всем предприятиям','Инвентаризация',27),
(632,'Проведение документов своего предприятия','Проведение документов "Инвентаризация" своего предприятия','Инвентаризация',27),
(633,'Проведение документов своих отделений','Проведение документов "Инвентаризация" по своим отделениям','Инвентаризация',27),
(634,'Проведение документов созданных собой','Проведение документов "Инвентаризация" созданных собой','Инвентаризация',27);


-- Справочник "Налоги"
create table sprav_taxes(
                          id bigserial primary key not null,
                          master_id  bigint not null,
                          company_id bigint not null,
                          creator_id bigint,
                          changer_id bigint,
                          date_time_created timestamp with time zone not null,
                          date_time_changed timestamp with time zone,
                          name varchar(30) not null,
                          description varchar(130),
                          is_active boolean not null,
                          value int not null,
                          multiplier numeric(3,2),
                          output_order int not null,
                          is_deleted boolean,
                          name_api_atol varchar(10),
                          foreign key (master_id)  references users(id),
                          foreign key (creator_id) references users(id),
                          foreign key (changer_id) references users(id),
                          foreign key (company_id) references companies(id)
);

alter table documents add column doc_name_en varchar (40);
insert into documents (id, name, page_name, show, table_name, doc_name_ru, doc_name_en) values (50,'Налоги','taxes',1,'sprav_taxes','Налоги','Taxes');
insert into permissions (id,name,description,document_name,document_id) values
(635,'Боковая панель - отображать в списке документов','Показывать документ в списке документов на боковой панели','Налоги',50),
(636,'Создание документов по всем предприятиям','Возможность создавать новые документы "Налоги" по всем предприятиям','Налоги',50),
(637,'Создание документов своего предприятия','Возможность создавать новые документы "Налоги" своего предприятия','Налоги',50),
(638,'Удаление документов по всем предприятиям','Возможность удалить документ "Налоги" в архив по всем предприятиям','Налоги',50),
(639,'Удаление документов своего предприятия','Возможность удалить документ "Налоги" своего предприятия в архив','Налоги',50),
(640,'Просмотр документов по всем предприятиям','Прсмотр информации в документах "Налоги" по всем предприятиям','Налоги',50),
(641,'Просмотр документов своего предприятия','Прсмотр информации в документах "Налоги" своего предприятия','Налоги',50),
(642,'Редактирование документов по всем предприятиям','Редактирование документов "Налоги" по всем предприятиям','Налоги',50),
(643,'Редактирование документов своего предприятия','Редактирование документов "Налоги" своего предприятия','Налоги',50);

alter table retail_sales_product drop constraint retail_sales_product_nds_id_fkey;
update retail_sales_product t set nds_id=(select st.id from sprav_taxes st where st.company_id=t.company_id and st.name='Без НДС');
alter table retail_sales_product add constraint retail_sales_product_tax_id_fkey foreign key (nds_id) references sprav_taxes (id);

alter table return_product drop constraint return_product_nds_id_fkey;
update return_product t set nds_id=(select st.id from sprav_taxes st where st.company_id=t.company_id and st.name='Без НДС');
alter table return_product add constraint return_product_tax_id_fkey foreign key (nds_id) references sprav_taxes (id);

alter table returnsup_product drop constraint returnsup_product_nds_id_fkey;
update returnsup_product t set nds_id=(select st.id from sprav_taxes st where st.company_id=t.company_id and st.name='Без НДС');
alter table returnsup_product add constraint returnsup_product_tax_id_fkey foreign key (nds_id) references sprav_taxes (id);

alter table shipment_product drop constraint shipment_product_nds_id_fkey;
update shipment_product t set nds_id=(select st.id from sprav_taxes st where st.company_id=t.company_id and st.name='Без НДС');
alter table shipment_product add constraint shipment_product_tax_id_fkey foreign key (nds_id) references sprav_taxes (id);

alter table invoiceout_product drop constraint invoiceout_product_nds_id_fkey;
update invoiceout_product t set nds_id=(select st.id from sprav_taxes st where st.company_id=t.company_id and st.name='Без НДС');
alter table invoiceout_product add constraint invoiceout_product_tax_id_fkey foreign key (nds_id) references sprav_taxes (id);

alter table ordersup_product drop constraint ordersup_product_nds_id_fkey;
update ordersup_product t set nds_id=(select st.id from sprav_taxes st where st.company_id=t.company_id and st.name='Без НДС');
alter table ordersup_product add constraint ordersup_product_tax_id_fkey foreign key (nds_id) references sprav_taxes (id);

alter table invoicein_product drop constraint invoicein_product_nds_id_fkey;
update invoicein_product t set nds_id=(select st.id from sprav_taxes st where st.company_id=t.company_id and st.name='Без НДС');
alter table invoicein_product add constraint invoicein_product_tax_id_fkey foreign key (nds_id) references sprav_taxes (id);

alter table acceptance_product drop constraint acceptance_nds_id_fkey;
update acceptance_product t set nds_id=(select st.id from sprav_taxes st where st.company_id=(select company_id from products where id=t.product_id) and st.name='Без НДС');
alter table acceptance_product add constraint acceptance_product_tax_id_fkey foreign key (nds_id) references sprav_taxes (id);

alter table customers_orders_product drop constraint customers_orders_product_nds_id_fkey;
update customers_orders_product t set nds_id=(select st.id from sprav_taxes st where st.company_id=t.company_id and st.name='Без НДС');
alter table customers_orders_product add constraint customers_orders_product_tax_id_fkey foreign key (nds_id) references sprav_taxes (id);

alter table products drop constraint nds_id_fkey;
update products t set nds_id=(select st.id from sprav_taxes st where st.company_id=t.company_id and st.name='Без НДС');
alter table products add constraint products_tax_id_fkey foreign key (nds_id) references sprav_taxes (id);

drop table sprav_sys_nds;

alter table product_groups add column is_deleted boolean;
alter table sprav_sys_edizm add column is_deleted boolean;
alter table sprav_type_prices add column is_deleted boolean;

update permissions set name='Просмотр документов по всем предприятиям' where id=29;
update permissions set name='Просмотр документов своего предприятия' where id=30;
update permissions set description='Просмотр документов по всем предприятиям' where id=29;
update permissions set description='Просмотр документов своего предприятия' where id=30;
update permissions set name='Редактирование документов по всем предприятиям' where id=34;
update permissions set name='Редактирование документов своего предприятия' where id=33;
update permissions set description='Редактирование документов по всем предприятиям' where id=34;
update permissions set description='Редактирование документов своего предприятия' where id=33;

alter table users add column is_deleted boolean;
alter table usergroup add column is_deleted boolean;
update documents set name='Роли', doc_name_ru='Роли' where id=6;


create table sprav_sys_locales(
  id                int not null,
  name              varchar(64) not null,
  code              varchar(8) not null
);

insert into sprav_sys_locales (id, name, code) values
(1,'English (Australia)','en-au'),
(2,'English (Canada)','en-ca'),
(3,'English (United States)','en-us'),
(4,'English (United Kingdom)','en-gb'),
(5,'English (Ireland)','en-ie'),
(6,'English (Israel)','en-il'),
(7,'English (India)','en-in'),
(8,'English (New Zealand)','en-nz'),
(9,'English (Singapore)','en-sg'),
(10,'Russian','ru');

alter table sprav_sys_locales add constraint sprav_sys_locales_id_uq unique (id);

create table sprav_sys_languages
(
  id                int         not null,
  name              varchar(64) not null,
  suffix            varchar(2)  not null,
  default_locale_id int         not null,
  foreign key (default_locale_id) references sprav_sys_locales (id)
);

insert into sprav_sys_languages (id, name, suffix, default_locale_id) values
(1,'English','en',4),
(2,'Русский','ru',10);

alter table sprav_sys_languages add constraint sprav_sys_languages_id_uq unique (id);


create table user_settings(
                            id bigserial primary key not null,
                            master_id  bigint not null,
                            user_id  bigint not null,
                            time_zone_id int not null,
                            language_id int not null,
                            locale_id int not null,
                            foreign key (master_id)  references users(id),
                            foreign key (time_zone_id) references sprav_sys_timezones(id),
                            foreign key (language_id) references sprav_sys_languages(id),
                            foreign key (locale_id) references sprav_sys_locales(id),
                            foreign key (user_id) references users(id)
);

ALTER TABLE sprav_sys_timezones RENAME COLUMN name_rus TO name_ru;
ALTER TABLE sprav_sys_timezones ADD COLUMN name_en varchar(128);
update sprav_sys_timezones set name_en=canonical_id;

alter table user_settings add constraint user_uq UNIQUE (user_id);

alter table products add column is_deleted boolean;
alter table products drop column is_archive;

create table _dictionary
(
  key               varchar(32)  PRIMARY KEY not null,
  tr_en             text,
  tr_ru             text
);
insert into _dictionary (key, tr_ru, tr_en) values
('yes',          'Да','Yes'),
('no',           'Нет','No'),
('completed',    'Проведён','Completed'),
('number',       '№', 'No'),
('role',         'Роль','Role'),
('user',         'Пользователь','User'),
('department',   'Отделение','Department'),
('company',      'Предприятие','Company'),
('file',         'Файл','File'),
('productgroup', 'Группа товаров','Product group'),
('unit',         'Единица измерения', 'Unit'),
('boxoffice',    'Касса предприятия','Cash room'),
('cparty',       'Контрагент','Counterparty'),
('tax',          'Налог','Tax'),
('status',       'Статус документа','Document status'),
('expenditure',  'Статья расходов','Expenditure'),
('pricetype',    'Тип цены','Price type'),
('paymentin',    'Входящий платёж','Payment in'),
('paymentout',   'Исходящий платёж','Payment out'),
('orderin',      'Приходный ордер','Order in'),
('orderout',     'Расходный ордер','Order out'),
('correction',   'Корректировка','Correction'),
('profitloss',   'Прибыли и убытки','P&L report'),
('mut_payments', 'Взаиморасчёты','Mutual payments'),
('moneyflow',    'Движение денежных средств','Moneyflow'),
('inventory',    'Инвентаризация','Inventory'),
('writeoff',     'Списание','Writeoff'),
('posting',      'Оприходование','Posting'),
('moving',       'Перемещение','Moving'),
('retailsale',   'Розничная продажа','Retail sale'),
('shift',        'Кассовая смена','Cashier''s shift'),
('receipt',      'Кассовый чек','Cashier''s check'),
('kassa',        'Касса онлайн','Sales register'),
('withdrawal',   'Выемка','Withdrawal'),
('depositing',   'Внесение','Depositing'),
('tr',           'Итог смены','Trade result'),
('return',       'Возврат покупателя','Buyers'' return'),
('c_order',      'Заказ покупателя','Customer''s order'),
('shipment',     'Отгрузка','Shipment'),
('invoiceout',   'Счёт покупателю','Invoice to customer'),
('v_invoiceout', 'Счёт-фактура выданный','Issued VAT invoice'),
('returnsup',    'Возврат поставщику','Return to supplier'),
('ordersup',     'Заказ поставщику','Order to supplier'),
('acceptance',   'Приёмка','Acceptance'),
('invoicein',    'Счёт поставщика','Suppliers'' invoice'),
('v_invoicein',  'Счёт-фактура полученный','Received VAT invoice'),
('retailsales',  'Розничная продажа','Retail sale'),
('customersorders','Заказ покупателя','Customer''s order'),
('vatinvoiceout','Счёт-фактура выданный','Issued VAT invoice'),
('vatinvoicein', 'Счёт-фактура полученный','Received VAT invoice');

insert into _dictionary (key, tr_ru, tr_en) values ('open_in_new_window', 'Открыть документ в новом окне', 'Open document in new window');

update sprav_sys_barcode set name = 'QR-code' where id=5;
update sprav_sys_barcode set description = 'modules.tip.bar_ean13' where id=1;
update sprav_sys_barcode set description = 'modules.tip.bar_ean8' where id=2;
update sprav_sys_barcode set description = 'modules.tip.bar_code128' where id=3;
update sprav_sys_barcode set description = 'modules.tip.bar_pdf417' where id=4;
update sprav_sys_barcode set description = 'modules.tip.bar_qr' where id=5;

alter table sprav_sys_writeoff rename column name to name_ru;
alter table sprav_sys_writeoff rename column description to description_ru;
alter table sprav_sys_writeoff add column name_en varchar(128);
alter table sprav_sys_writeoff add column description_en varchar(256);

update sprav_sys_writeoff set name_en='General running costs',description_en='For example, paper for office equipment was issued to an accountant' where id=1;
update sprav_sys_writeoff set name_en='Selling costs',description_en='For example, a container was issued for packaging finished products' where id=2;
update sprav_sys_writeoff set name_en='Shortfalls and losses from damage to valuables',description_en='For example, writing off missing materials' where id=3;
update sprav_sys_writeoff set name_en='Overhead costs',description_en='For example, rags and gloves were released to the cleaner serving the workshop' where id=4;
update sprav_sys_writeoff set name_en='Primary production',description_en='For example, raw materials for the production of products are released' where id=5;
update sprav_sys_writeoff set name_en='Auxiliary production',description_en='For example, materials are released to the repair shop' where id=6;
update sprav_sys_writeoff set name_en='Other expenses',description_en='For example, writing off worn-out tools or equipment' where id=7;

insert into _dictionary (key, tr_ru, tr_en) values
('income',          'Приход','Income'),
('expense',         'Расход','Expense');

insert into _dictionary (key, tr_ru, tr_en) values
('selected',    'Выбранные','Selected'),
('all',         'Все','All');


insert into _dictionary (key, tr_ru, tr_en) values
('overdue_invcs',     'Просроченные счета', 'Overdue invoices'),
('overdue_ordrs',     'Просроченные заказы','Overdue orders'),
('new_orders',        'Новые заказы',       'New orders'),
('money',             'Деньги',             'Money'),
('your_debt',         'Вы должны',          'Your debt'),
('you_owed',          'Вам должны',         'You are owed');

ALTER TABLE template_docs drop CONSTRAINT company_document_template_uq;
ALTER TABLE template_docs drop column template_type_id;
ALTER TABLE template_docs add column name varchar(200);


ALTER TABLE template_docs alter column name set not null ;
ALTER TABLE template_docs add column user_id bigint not null;
ALTER TABLE template_docs add constraint user_id_fkey foreign key (user_id) references users (id);


ALTER TABLE permissions drop column document_name;
ALTER TABLE permissions drop column description;
ALTER TABLE permissions add column name_ru  varchar(250);
ALTER TABLE permissions add column name_en  varchar(250);
update permissions set name_ru = name;
ALTER TABLE permissions add column output_order int;
delete from usergroup_permissions where permission_id in (select id from permissions where name like 'Меню - %');
delete from permissions where name like 'Меню - %';
delete from usergroup_permissions where permission_id in (select id from permissions where name like 'Информация по%');
delete from permissions where name like 'Информация по%';
delete from usergroup_permissions where permission_id in (select id from permissions where document_id = 7);
delete from permissions where document_id = 7;
delete from usergroup_permissions where permission_id in (select id from permissions where document_id = 8);
delete from permissions where document_id = 8;
delete from documents where id in(7,8);
ALTER TABLE permissions drop column name;
delete from usergroup_permissions where permission_id = 35;
delete from permissions where id = 35;

update permissions set name_ru = 'Редактирование своего предприятия' where document_id = 3 and name_ru = 'Редактирование своего';
update permissions set name_ru = 'Редактирование всех предприятий' where document_id = 3 and name_ru = 'Редактирование всех';
update permissions set name_ru = 'Просмотр своего предприятия' where document_id = 3 and name_ru = 'Просмотр своего';
update permissions set name_ru = 'Просмотр всех предприятий' where document_id = 3 and name_ru = 'Просмотр всех';

update permissions set name_ru = 'Редактирование документов своего предприятия' where id=15;
update permissions set name_ru = 'Редактирование документов всех предприятий' where id=16;
update permissions set name_ru = 'Просмотр документов своего предприятия' where id=13;
update permissions set name_ru = 'Просмотр документов всех предприятий' where id=14;

update permissions set name_ru = 'Редактирование документов своего предприятия' where id=26;
update permissions set name_ru = 'Редактирование документов всех предприятий' where id=27;
update permissions set name_ru = 'Просмотр документов своего предприятия' where id=24;
update permissions set name_ru = 'Просмотр документов всех предприятий' where id=25;

update permissions set name_ru = 'Просмотр документов всех предприятий' where name_ru='Просмотр документов по всем предприятиям';
update permissions set name_ru = 'Редактирование документов всех предприятий' where name_ru='Редактирование документов по всем предприятиям';
update permissions set name_ru = 'Удаление документов всех предприятий' where name_ru='Удаление документов по всем предприятиям';
update permissions set name_ru = 'Проведение документов всех предприятий' where name_ru='Проведение документов по всем предприятиям';

update permissions set name_ru = 'Создание документов по всем предприятиям' where name_ru='Создание';
update permissions set name_ru = 'Удаление документов всех предприятий' where name_ru='Удаление';
update permissions set name_ru = 'Отображать в списке документов на боковой панели' where name_ru='Боковая панель - отображать в списке документов';
update permissions set output_order = 1000;

update permissions set output_order = 10  where name_ru = 'Отображать в списке документов на боковой панели';

update permissions set output_order = 20  where name_ru = 'Создание документов по всем предприятиям';
update permissions set output_order = 30  where name_ru = 'Создание документов своего предприятия';
update permissions set output_order = 40  where name_ru = 'Создание документов своих отделений';

update permissions set output_order = 50  where name_ru = 'Просмотр документов всех предприятий';
update permissions set output_order = 60  where name_ru = 'Просмотр документов своего предприятия';
update permissions set output_order = 70  where name_ru = 'Просмотр документов своих отделений';
update permissions set output_order = 80  where name_ru = 'Просмотр документов созданных собой';

update permissions set output_order = 90  where name_ru = 'Редактирование документов всех предприятий';
update permissions set output_order = 100 where name_ru = 'Редактирование документов своего предприятия';
update permissions set output_order = 110 where name_ru = 'Редактирование документов своих отделений';
update permissions set output_order = 120 where name_ru = 'Редактирование документов созданных собой';

update permissions set output_order = 130 where name_ru = 'Удаление документов всех предприятий';
update permissions set output_order = 140 where name_ru = 'Удаление документов своего предприятия';
update permissions set output_order = 150 where name_ru = 'Удаление документов своих отделений';
update permissions set output_order = 160 where name_ru = 'Удаление документов созданных собой';

update permissions set output_order = 170 where name_ru = 'Проведение документов всех предприятий';
update permissions set output_order = 180 where name_ru = 'Проведение документов своего предприятия';
update permissions set output_order = 190 where name_ru = 'Проведение документов своих отделений';
update permissions set output_order = 200 where name_ru = 'Проведение документов созданных собой';

update permissions set output_order = 11  where name_ru = 'Отображать';

update permissions set output_order = 50  where name_ru = 'Просмотр всех предприятий';
update permissions set output_order = 60  where name_ru = 'Просмотр своего предприятия';
update permissions set output_order = 70  where name_ru = 'Редактирование всех предприятий';
update permissions set output_order = 80  where name_ru = 'Редактирование своего предприятия';

update permissions set output_order = 210 where name_ru = 'Просмотр цен по всем предприятиям';
update permissions set output_order = 220 where name_ru = 'Просмотр цен своего предприятия';
update permissions set output_order = 230 where name_ru = 'Установка цен по всем предприятиям';
update permissions set output_order = 240 where name_ru = 'Установка цен своего предприятия';

update permissions set output_order = 250 where name_ru = 'Корзина - Восстановление файлов по всем предприятиям';
update permissions set output_order = 260 where name_ru = 'Корзина - Восстановление файлов своего предприятия';
update permissions set output_order = 270 where name_ru = 'Корзина - Очистка корзины по всем предприятиям';
update permissions set output_order = 280 where name_ru = 'Корзина - Очистка корзины своего предприятия';
update permissions set output_order = 290 where name_ru = 'Корзина - Удаление из корзины файлов по всем предприятиям';
update permissions set output_order = 300 where name_ru = 'Корзина - Удаление из корзины файлов своего предприятия';

update permissions set output_order = 310 where name_ru = 'Просмотр остатков по всем предприятиям';
update permissions set output_order = 320 where name_ru = 'Просмотр остатков своего предприятия';
update permissions set output_order = 330 where name_ru = 'Просмотр остатков своих отделений';
update permissions set output_order = 340 where name_ru = 'Установка остатков по всем предприятиям';
update permissions set output_order = 350 where name_ru = 'Установка остатков своего предприятия';
update permissions set output_order = 360 where name_ru = 'Установка остатков своих отделений';

update permissions set output_order = 400 where name_ru = 'Отчёт "Просроченные заказы" - просмотр по всем предприятиям';
update permissions set output_order = 410 where name_ru = 'Отчёт "Просроченные заказы" - просмотр по своему предприятию';
update permissions set output_order = 420 where name_ru = 'Отчёт "Просроченные счета" - просмотр по всем предприятиям';
update permissions set output_order = 430 where name_ru = 'Отчёт "Просроченные счета" - просмотр по своему предприятию';
update permissions set output_order = 440 where name_ru = 'Отчёт "Новые заказы" - просмотр по всем предприятиям';
update permissions set output_order = 450 where name_ru = 'Отчёт "Новые заказы" - просмотр по своему предприятию';
update permissions set output_order = 460 where name_ru = 'Отчёт "Деньги" - просмотр по всем предприятиям';
update permissions set output_order = 470 where name_ru = 'Отчёт "Деньги" - просмотр по своему предприятию';
update permissions set output_order = 480 where name_ru = 'Отчёт "Мы должны" - просмотр по всем предприятиям';
update permissions set output_order = 490 where name_ru = 'Отчёт "Мы должны" - просмотр по своему предприятию';
update permissions set output_order = 500 where name_ru = 'Отчёт "Нам должны" - просмотр по всем предприятиям';
update permissions set output_order = 510 where name_ru = 'Отчёт "Нам должны" - просмотр по своему предприятию';
update permissions set output_order = 520 where name_ru = 'Отчёт "Объёмы" - просмотр по всем предприятиям';
update permissions set output_order = 530 where name_ru = 'Отчёт "Объёмы" - просмотр по своему предприятию';
update permissions set output_order = 540 where name_ru = 'Отчёт "Объёмы" - просмотр по своим отделениям';
update permissions set output_order = 548 where name_ru = 'Отчёт "Приход и расход" - просмотр по всем предприятиям';
update permissions set output_order = 550 where name_ru = 'Отчёт "Приход и расход" - просмотр по своему предприятию';
update permissions set output_order = 560 where name_ru = 'Отчёт "Товарные остатки" - просмотр по всем предприятиям';
update permissions set output_order = 570 where name_ru = 'Отчёт "Товарные остатки" - просмотр по своему предприятию';
update permissions set output_order = 580 where name_ru = 'Отчёт "Товарные остатки" - просмотр по своим отделениям';
update permissions set output_order = 590 where name_ru = 'Отчёт "Операционные расходы" - просмотр по всем предприятиям';
update permissions set output_order = 600 where name_ru = 'Отчёт "Операционные расходы" - просмотр по своему предприятию';

update permissions set output_order = 650 where name_ru = 'Категории - Создание для всех предприятий';
update permissions set output_order = 660 where name_ru = 'Категории - Создание для своего предприятия';
update permissions set output_order = 670 where name_ru = 'Категории - Редактирование у всех предприятий';
update permissions set output_order = 680 where name_ru = 'Категории - Редактирование у своего предприятия';
update permissions set output_order = 690 where name_ru = 'Категории - Удаление у всех предприятий';
update permissions set output_order = 700 where name_ru = 'Категории - Удаление у своего предприятия';

delete from permissions where document_id=1;
delete from documents_menu where document_id=1;
delete from documents where id = 1;

delete from permissions where document_id=2;
delete from documents_menu where document_id=2;
delete from documents where id = 2;

update documents set doc_name_en = 'Mutual payments' where doc_name_ru = 'Взаиморасчёты';
update documents set doc_name_en = 'Depositings' where doc_name_ru = 'Внесение';
update documents set doc_name_en = 'Customers returns' where doc_name_ru = 'Возврат покупателя';
update documents set doc_name_en = 'Returns to suppliers' where doc_name_ru = 'Возврат поставщику';
update documents set doc_name_en = 'Incoming payments' where doc_name_ru = 'Входящий платеж';
update documents set doc_name_en = 'Withdrawals' where doc_name_ru = 'Выемка';
update documents set doc_name_en = 'Products groups' where doc_name_ru = 'Группы товаров';
update documents set doc_name_en = 'Money flows' where doc_name_ru = 'Движение денежных средств';
update documents set doc_name_en = 'Units of measure' where doc_name_ru = 'Единицы измерения';
update documents set doc_name_en = 'Customers orders' where doc_name_ru = 'Заказ покупателя';
update documents set doc_name_en = 'Orders to suppliers' where doc_name_ru = 'Заказ поставщику';
update documents set doc_name_en = 'Inventories' where doc_name_ru = 'Инвентаризация';
update documents set doc_name_en = 'Outgoing payments' where doc_name_ru = 'Исходящий платеж';
update documents set doc_name_en = 'Cashier''s shifts' where doc_name_ru = 'Кассовые смены';
update documents set doc_name_en = 'Сashier''s checks' where doc_name_ru = 'Кассовые чеки';
update documents set doc_name_en = 'Sales registers' where doc_name_ru = 'Кассы онлайн';
update documents set doc_name_en = 'Сash rooms' where doc_name_ru = 'Кассы предприятия';
update documents set doc_name_en = 'Counterparties' where doc_name_ru = 'Контрагенты';
update documents set doc_name_en = 'Corrections' where doc_name_ru = 'Корректировка';
update documents set doc_name_en = 'Taxes' where doc_name_ru = 'Налоги';
update documents set doc_name_en = 'Postings' where doc_name_ru = 'Оприходование';
update documents set doc_name_en = 'Shipments' where doc_name_ru = 'Отгрузка';
update documents set doc_name_en = 'Departments' where doc_name_ru = 'Отделения';
update documents set doc_name_en = 'Movings' where doc_name_ru = 'Перемещение';
update documents set doc_name_en = 'Users' where doc_name_ru = 'Пользователи';
update documents set doc_name_en = 'Companies' where doc_name_ru = 'Предприятия';
update documents set doc_name_en = 'P&L report' where doc_name_ru = 'Прибыли и убытки';
update documents set doc_name_en = 'Acceptances' where doc_name_ru = 'Приёмка';
update documents set doc_name_en = 'Credit slips' where doc_name_ru = 'Приходный ордер';
update documents set doc_name_en = 'Debit slips' where doc_name_ru = 'Расходный ордер';
update documents set doc_name_en = 'Retail sales' where doc_name_ru = 'Розничная продажа';
update documents set doc_name_en = 'Roles' where doc_name_ru = 'Роли';
update documents set doc_name_en = 'Sites' where doc_name_ru = 'Сайты';
update documents set doc_name_en = 'Writeoffs' where doc_name_ru = 'Списание';
update documents set doc_name_en = 'Dashboard' where doc_name_ru = 'Стартовая страница';
update documents set doc_name_en = 'Document statuses' where doc_name_ru = 'Статусы документов';
update documents set doc_name_en = 'Expenditures' where doc_name_ru = 'Статьи расходов';
update documents set doc_name_en = 'Issued VAT invoices' where doc_name_ru = 'Счет-фактура выданный';
update documents set doc_name_en = 'Received VAT invoices' where doc_name_ru = 'Счет-фактура полученный';
update documents set doc_name_en = 'Invoices to customers' where doc_name_ru = 'Счет покупателю';
update documents set doc_name_en = 'Suppliers'' invoices' where doc_name_ru = 'Счет поставщика';
update documents set doc_name_en = 'Price types' where doc_name_ru = 'Типы цен';
update documents set doc_name_en = 'In-stock balance' where doc_name_ru = 'Товарные остатки';
update documents set doc_name_en = 'Products' where doc_name_ru = 'Товары и услуги';
update documents set doc_name_en = 'Files' where doc_name_ru = 'Файлы';
update documents set doc_name_en = 'Prices' where doc_name_ru = 'Цены';

update permissions set name_en = 'Display in the list of documents in the sidebar' where name_ru = 'Отображать в списке документов на боковой панели';

update permissions set name_en = 'Creation of documents for all companies' where name_ru = 'Создание документов по всем предприятиям';
update permissions set name_en = 'Create your company documents' where name_ru = 'Создание документов своего предприятия';
update permissions set name_en = 'Create documents of your departments' where name_ru = 'Создание документов своих отделений';

update permissions set name_en = 'View documents of all companies' where name_ru = 'Просмотр документов всех предприятий';
update permissions set name_en = 'View your company documents' where name_ru = 'Просмотр документов своего предприятия';
update permissions set name_en = 'View documents of your departments' where name_ru = 'Просмотр документов своих отделений';
update permissions set name_en = 'View documents created by yourself' where name_ru = 'Просмотр документов созданных собой';

update permissions set name_en = 'Editing documents of all companies' where name_ru = 'Редактирование документов всех предприятий';
update permissions set name_en = 'Editing your company documents' where name_ru = 'Редактирование документов своего предприятия';
update permissions set name_en = 'Editing documents of your departments' where name_ru = 'Редактирование документов своих отделений';
update permissions set name_en = 'Editing documents created by yourself' where name_ru = 'Редактирование документов созданных собой';

update permissions set name_en = 'Deleting documents of all companies' where name_ru = 'Удаление документов всех предприятий';
update permissions set name_en = 'Deleting your company documents' where name_ru = 'Удаление документов своего предприятия';
update permissions set name_en = 'Deleting documents of your departments' where name_ru = 'Удаление документов своих отделений';
update permissions set name_en = 'Deleting documents created by yourself' where name_ru = 'Удаление документов созданных собой';

update permissions set name_en = 'Completion documents of all companies' where name_ru = 'Проведение документов всех предприятий';
update permissions set name_en = 'Completion your company documents' where name_ru = 'Проведение документов своего предприятия';
update permissions set name_en = 'Completion documents of your departments' where name_ru = 'Проведение документов своих отделений';
update permissions set name_en = 'Completion documents created by yourself' where name_ru = 'Проведение документов созданных собой';

update permissions set name_en = 'Display' where name_ru = 'Отображать';

update permissions set name_en = 'View all companies' where name_ru = 'Просмотр всех предприятий';
update permissions set name_en = 'View your company' where name_ru = 'Просмотр своего предприятия';
update permissions set name_en = 'Editing all companies' where name_ru = 'Редактирование всех предприятий';
update permissions set name_en = 'Editing your company' where name_ru = 'Редактирование своего предприятия';

update permissions set name_en = 'View prices for all companies' where name_ru = 'Просмотр цен по всем предприятиям';
update permissions set name_en = 'View your company prices' where name_ru = 'Просмотр цен своего предприятия';
update permissions set name_en = 'Setting prices for all companies' where name_ru = 'Установка цен по всем предприятиям';
update permissions set name_en = 'Setting your company prices' where name_ru = 'Установка цен своего предприятия';

update permissions set name_en = 'Recycle Bin - File recovery of all companies' where name_ru = 'Корзина - Восстановление файлов по всем предприятиям';
update permissions set name_en = 'Recycle Bin - File recovery of your company' where name_ru = 'Корзина - Восстановление файлов своего предприятия';
update permissions set name_en = 'Recycle Bin - Empty of all companies' where name_ru = 'Корзина - Очистка корзины по всем предприятиям';
update permissions set name_en = 'Recycle Bin - Empty of your company' where name_ru = 'Корзина - Очистка корзины своего предприятия';
update permissions set name_en = 'Recycle Bin - Deletion of all companies' where name_ru = 'Корзина - Удаление из корзины файлов по всем предприятиям';
update permissions set name_en = 'Recycle Bin - Deletion of your company' where name_ru = 'Корзина - Удаление из корзины файлов своего предприятия';

update permissions set name_en = 'View in-stock balances of all companies' where name_ru = 'Просмотр остатков по всем предприятиям';
update permissions set name_en = 'View in-stock balances of your company' where name_ru = 'Просмотр остатков своего предприятия';
update permissions set name_en = 'View in-stock balances of your departments' where name_ru = 'Просмотр остатков своих отделений';
update permissions set name_en = 'Setting in-stock balances of all companies' where name_ru = 'Установка остатков по всем предприятиям';
update permissions set name_en = 'Setting in-stock balances of your company' where name_ru = 'Установка остатков своего предприятия';
update permissions set name_en = 'Setting in-stock balances of your departments' where name_ru = 'Установка остатков своих отделений';

update permissions set name_en = 'Report "Overdue orders" - view of all companies' where name_ru = 'Отчёт "Просроченные заказы" - просмотр по всем предприятиям';
update permissions set name_en = 'Report "Overdue orders" - view of your company' where name_ru = 'Отчёт "Просроченные заказы" - просмотр по своему предприятию';
update permissions set name_en = 'Report "Overdue invoices" - view of all companies' where name_ru = 'Отчёт "Просроченные счета" - просмотр по всем предприятиям';
update permissions set name_en = 'Report "Overdue invoices" - view of your company' where name_ru = 'Отчёт "Просроченные счета" - просмотр по своему предприятию';
update permissions set name_en = 'Report "New orders" - view of all companies' where name_ru = 'Отчёт "Новые заказы" - просмотр по всем предприятиям';
update permissions set name_en = 'Report "New orders" - view of your company' where name_ru = 'Отчёт "Новые заказы" - просмотр по своему предприятию';
update permissions set name_en = 'Report "Money" - view of all companies' where name_ru = 'Отчёт "Деньги" - просмотр по всем предприятиям';
update permissions set name_en = 'Report "Money" - view of your company' where name_ru = 'Отчёт "Деньги" - просмотр по своему предприятию';
update permissions set name_en = 'Report "Your debt" - view of all companies' where name_ru = 'Отчёт "Мы должны" - просмотр по всем предприятиям';
update permissions set name_en = 'Report "Your debt" - view of your company' where name_ru = 'Отчёт "Мы должны" - просмотр по своему предприятию';
update permissions set name_en = 'Report "You are owed" - view of all companies' where name_ru = 'Отчёт "Нам должны" - просмотр по всем предприятиям';
update permissions set name_en = 'Report "You are owed" - view of your company' where name_ru = 'Отчёт "Нам должны" - просмотр по своему предприятию';
update permissions set name_en = 'Report "Sales volumes" - view of all companies' where name_ru = 'Отчёт "Объёмы" - просмотр по всем предприятиям';
update permissions set name_en = 'Report "Sales volumes" - view of your company' where name_ru = 'Отчёт "Объёмы" - просмотр по своему предприятию';
update permissions set name_en = 'Report "Sales volumes" - view of your departments' where name_ru = 'Отчёт "Объёмы" - просмотр по своим отделениям';
update permissions set name_en = 'Report "Income and expense" - view of all companies' where name_ru = 'Отчёт "Приход и расход" - просмотр по всем предприятиям';
update permissions set name_en = 'Report "Income and expense" - view of your company' where name_ru = 'Отчёт "Приход и расход" - просмотр по своему предприятию';
update permissions set name_en = 'Report "In-stock balance" - view of all companies' where name_ru = 'Отчёт "Товарные остатки" - просмотр по всем предприятиям';
update permissions set name_en = 'Report "In-stock balance" - view of your company' where name_ru = 'Отчёт "Товарные остатки" - просмотр по своему предприятию';
update permissions set name_en = 'Report "In-stock balance" - view of your departments' where name_ru = 'Отчёт "Товарные остатки" - просмотр по своим отделениям';
update permissions set name_en = 'Report "Operating expenses" - view of all companies' where name_ru = 'Отчёт "Операционные расходы" - просмотр по всем предприятиям';
update permissions set name_en = 'Report "Operating expenses" - view of your company' where name_ru = 'Отчёт "Операционные расходы" - просмотр по своему предприятию';

update permissions set name_en = 'Categories - Creation for all companies' where name_ru = 'Категории - Создание для всех предприятий';
update permissions set name_en = 'Categories - Creation for your company' where name_ru = 'Категории - Создание для своего предприятия';
update permissions set name_en = 'Categories - Editing for all companies' where name_ru = 'Категории - Редактирование у всех предприятий';
update permissions set name_en = 'Categories - Editing for your company' where name_ru = 'Категории - Редактирование у своего предприятия';
update permissions set name_en = 'Categories - Deleting for all companies' where name_ru = 'Категории - Удаление у всех предприятий';
update permissions set name_en = 'Categories - Deleting for your company' where name_ru = 'Категории - Удаление у своего предприятия';


-- Справочник "Валюты"
create table sprav_currencies(
                                      id bigserial primary key not null,
                                      master_id  bigint not null,
                                      company_id bigint not null,
                                      creator_id bigint not null,
                                      changer_id bigint,
                                      date_time_created timestamp with time zone not null,
                                      date_time_changed timestamp with time zone,
                                      name_short varchar(6) not null,
                                      name_full  varchar(200) not null,
                                      code_lit   varchar(3) not null,
                                      code_num   varchar(3) not null,
                                      is_deleted boolean,
                                      is_default boolean,
                                      foreign key (master_id) references users(id),
                                      foreign key (creator_id) references users(id),
                                      foreign key (changer_id) references users(id),
                                      foreign key (company_id) references companies(id)
);


insert into documents (id, name, page_name, show, table_name, doc_name_ru, doc_name_en) values (51,'Валюты','currencies',1,'sprav_currencies','Валюты', 'Currencies');

insert into permissions (id,name_ru,name_en,document_id,output_order) values
(644,'Отображать в списке документов на боковой панели','Display in the list of documents in the sidebar',51,10),
(645,'Создание документов по всем предприятиям','Creation of documents for all companies',51,20),
(646,'Создание документов своего предприятия','Create your company documents',51,30),
(647,'Удаление документов всех предприятий','Deleting documents of all companies',51,130),
(648,'Удаление документов своего предприятия','Deleting your company documents',51,140),
(649,'Просмотр документов всех предприятий','View documents of all companies',51,50),
(650,'Просмотр документов своего предприятия','View your company documents',51,60),
(651,'Редактирование документов всех предприятий','Editing documents of all companies',51,90),
(652,'Редактирование документов своего предприятия','Editing your company documents',51,100);


alter table users add column activation_code varchar(36);
alter table users add column repair_pass_code varchar(36);

alter table cagents add column region varchar(120);
alter table cagents add column city varchar(120);
alter table cagents add column jr_region varchar(120);
alter table cagents add column jr_city varchar(120);

alter table companies add column region varchar(120);
alter table companies add column city varchar(120);
alter table companies add column jr_region varchar(120);
alter table companies add column jr_city varchar(120);

alter table customers_orders add column region varchar(120);
alter table customers_orders add column city varchar(120);

alter table sprav_sys_countries add column name_es varchar(128);
alter table sprav_sys_countries add column name_pt varchar(128);
alter table sprav_sys_countries add column name_fr varchar(128);
alter table sprav_sys_countries add column name_it varchar(128);
alter table sprav_sys_countries add column organization varchar(128);

update sprav_sys_countries set organization = 'world';

alter table companies drop column currency_id;

alter table companies add column type varchar(10);        -- entity or individual
alter table cagents add column type varchar(10);        -- entity or individual

alter table cagents alter column opf_id drop not null;
update cagents set type='entity';
update companies set type='entity';

insert into _dictionary (key, tr_ru, tr_en) values
('acc_short',   'р/с',                'acc'),
('cash_room',   'Касса',              'Сash room');

alter table sprav_sys_locales add column date_format varchar(10);

update sprav_sys_locales set date_format='DD/MM/YYYY' where id=1;
update sprav_sys_locales set date_format='YYYY-MM-DD' where id=2;
update sprav_sys_locales set date_format='MM/DD/YYYY' where id=3;
update sprav_sys_locales set date_format='DD/MM/YYYY' where id=4;
update sprav_sys_locales set date_format='DD/MM/YYYY' where id=5;
update sprav_sys_locales set date_format='DD/MM/YYYY' where id=6;
update sprav_sys_locales set date_format='DD/MM/YYYY' where id=7;
update sprav_sys_locales set date_format='DD/MM/YYYY' where id=8;
update sprav_sys_locales set date_format='DD/MM/YYYY' where id=9;
update sprav_sys_locales set date_format='DD.MM.YYYY' where id=10;


alter table sprav_expenditure_items add column is_default boolean;


insert into _dictionary (key, tr_ru, tr_en) values
('st_new',              'Новый документ',         'New document'),
('st_cancel',           'Отмена',                 'Cancelled'),
('st_send',             'Товары отправлены',      'Shipped'),
('st_ret_compl',        'Возврат произведён',     'Refund made'),
('st_assembly',         'Сборка',                 'Order-picking'),
('st_wait_pay',         'Ожидание оплаты',        'Waiting for payment'),
('st_wait_receive',     'Ожидание поступления',   'Waiting for receiving'),
('st_paym_made',        'Платёж произведён',      'Payment made'),
('st_new_order',        'Новый заказ',            'New order'),
('st_assembl_ord',      'Сборка заказа',          'Order-picking'),
('st_await_iss',        'Ждёт выдачи',            'Awaiting issuance'),
('st_issd_buyer',       'Выдан покупателю',       'Given to the buyer'),
('st_wait_prices',      'Ожидание цен',           'Waiting for pricing'),
('st_wait_invoice',     'Ожидание счёта',         'Waiting for the invoice'),
('st_ord_delvrd',       'Заказ доставлен',        'Order delivered'),
('st_in_process',       'В процессе',             'In process'),
('st_completed',        'Завершено',              'Completed'),
('st_payment_send',     'Отправлен',              'Sent'),
('st_money_accptd',     'Деньги приняты',         'Cash accepted'),
('st_money_issued',     'Деньги выданы',          'Given out'),
('st_invc_issued',      'Счёт выставлен',         'Invoice issued'),
('st_invc_paid',        'Счёт оплачен',           'Invoice paid'),
('st_printed',          'Напечатан',              'Printed');


alter table sprav_sys_ppr rename column name to name_ru;
alter table sprav_sys_ppr add column name_en varchar(128);

update sprav_sys_ppr set name_en = 'Commodity' where id = 1;
update sprav_sys_ppr set name_en = 'Service' where id = 4;






create table version(
    value      varchar(10) not null,
    date       varchar(10) not null
);
insert into version (value, date) values ('1.000-0','02/06/2022');
create table settings_general(
    show_registration_link    boolean not null,
    allow_registration        boolean not null,
    show_forgot_link          boolean not null,
    allow_recover_password    boolean not null
);
insert into settings_general (show_registration_link, allow_registration, show_forgot_link, allow_recover_password) values (true,true,true,true);
-- update settings_general set show_registration_link = true, allow_registration = true, show_forgot_link = true, allow_recover_password = true;
alter table settings_general add column show_in_signin text;

------------------------------------------------  end of 1.0.0-0   -----------------------------------------------------
-----------------------------------------------  begin of 1.0.1-0   ----------------------------------------------------
alter table companies_payment_accounts add column creator_id bigint;
alter table companies_payment_accounts add column changer_id bigint;
alter table companies_payment_accounts add column date_time_created timestamp with time zone;
alter table companies_payment_accounts add column date_time_changed timestamp with time zone;
alter table companies_payment_accounts add column description varchar(2048);
alter table companies_payment_accounts add column intermediatery varchar(2048);
alter table companies_payment_accounts add column swift varchar(11);
alter table companies_payment_accounts add column iban varchar(34);
alter table companies_payment_accounts add column is_main boolean;
alter table companies_payment_accounts add column is_deleted boolean;
alter table companies_payment_accounts add constraint companies_payment_accounts_creator_id_fkey foreign key (creator_id) references users (id);
alter table companies_payment_accounts add constraint companies_payment_accounts_changer_id_fkey foreign key (changer_id) references users (id);
insert into documents (id, name, page_name, show, table_name, doc_name_ru, doc_name_en) values (52,'Расчётные счета','accounts',1,'companies_payment_accounts','Расчётные счета', 'Bank accounts');
insert into permissions (id,name_ru,name_en,document_id,output_order) values
(653,'Отображать в списке документов на боковой панели','Display in the list of documents in the sidebar',52,10),
(654,'Создание документов по всем предприятиям','Creation of documents for all companies',52,20),
(655,'Создание документов своего предприятия','Create your company documents',52,30),
(656,'Удаление документов всех предприятий','Deleting documents of all companies',52,130),
(657,'Удаление документов своего предприятия','Deleting your company documents',52,140),
(658,'Просмотр документов всех предприятий','View documents of all companies',52,50),
(659,'Просмотр документов своего предприятия','View your company documents',52,60),
(660,'Редактирование документов всех предприятий','Editing documents of all companies',52,90),
(661,'Редактирование документов своего предприятия','Editing your company documents',52,100);
alter table cagents_payment_accounts add column intermediatery varchar(2048);
alter table cagents_payment_accounts add column swift varchar(11);
alter table cagents_payment_accounts add column iban varchar(34);
alter table template_docs drop constraint template_docs_file_id_fkey;
alter table template_docs add constraint template_docs_file_id_fkey foreign key (file_id) references files (id) on delete cascade;

alter table companies add column legal_form varchar(240);
alter table cagents add column legal_form varchar(240);
update companies set legal_form = '';
update cagents set legal_form = '';

create table plans(
                    id                 serial primary key not null,
                    name_en            varchar(200) not null,
                    name_ru            varchar(200) not null,
                    version            int not null,
                    daily_price        numeric(10,10) not null,
                    is_default         boolean not null,
                    is_nolimits        boolean not null, -- used for standalone servers. If 'TRUE' - system will not check the limits
                    is_archive         boolean not null,
                    date_time_created  timestamp with time zone not null,
                    date_time_archived timestamp with time zone,
                    output_order       int not null,
                    n_companies        int not null,
                    n_departments      int not null,
                    n_users            int not null,
                    n_products         int not null,
                    n_counterparties   int not null,
                    n_megabytes        int not null
);
alter table plans add constraint plans_name_en_version_uq unique (name_en, version) ;
alter table plans add constraint plans_name_ru_version_uq unique (name_ru, version) ;

create table plans_add_options(
                    id                 bigserial primary key not null,
                    user_id            bigint not null,
                    n_companies        int not null,
                    companies_ppu      numeric(10,10),
                    n_departments      int not null,
                    departments_ppu    numeric(10,10),
                    n_users            int not null,
                    users_ppu          numeric(10,10),
                    n_products         int not null,
                    products_ppu       numeric(10,10),
                    n_counterparties   int not null,
                    counterparties_ppu numeric(10,10),
                    n_megabytes        int not null,
                    megabytes_ppu      numeric(10,10),
                    foreign key (user_id) references users(id)
);
alter table plans_add_options add constraint user_id_uq unique (user_id) ;

insert into plans (
                    name_en,
                    name_ru,
                    version,
                    daily_price,
                    is_default,
                    is_nolimits,
                    is_archive,
                    date_time_created,
                    output_order,
                    n_companies,
                    n_departments,
                    n_users,
                    n_products,
                    n_counterparties,
                    n_megabytes) values
                    ('No limits','Безлимитный', 1, 0, true, true,  false, now(), 100, 0, 0, 0, 0, 0, 0),
                    ('Free',     'Бесплатный',  1, 0, true, false, false, now(), 200, 1, 1, 1, 100, 100, 50);

alter table users add column plan_id int;
alter table users add constraint plan_id_fkey foreign key (plan_id) references plans (id);
update users set plan_id = 1 where id = master_id;

alter table settings_general add column plan_default_id int;
alter table settings_general add constraint plan_default_id_fkey foreign key (plan_default_id) references plans (id);
update settings_general set plan_default_id = 2;
alter table settings_general alter plan_default_id set not null;

update version set value = '1.0.1-0', date = '27-06-2022';
------------------------------------------------  end of 1.0.1   -----------------------------------------------------
-----------------------------------------------  begin of 1.0.2   ----------------------------------------------------

insert into _dictionary (key, tr_ru, tr_en) values
('um_uncountable',          'Неисчислимое',               'Uncountable'),
('um_kilogramm',            'Килограмм',                  'Kilogramm'),
('um_gramm',                'Грамм',                      'Gramm'),
('um_ton',                  'Тонна',                      'Ton'),
('um_meter',                'Метр',                       'Meter'),
('um_centimeter',           'Сантиметр',                  'Centimeter'),
('um_litre',                'Литр',                       'Litre'),
('um_cubic_meter',          'Кубический метр',            'Cubic meter'),
('um_square_meter',         'Квадратный метр',            'Square meter'),
('um_kilogramm_s',          'кг',                         'kg'),
('um_gramm_s',              'г',                          'g'),
('um_ton_s',                'т',                          't'),
('um_meter_s',              'м',                          'm'),
('um_centimeter_s',         'см',                         'cm'),
('um_litre_s',              'л',                          'L'),
('um_cubic_meter_s',        'м3',                         'm3'),
('um_square_meter_s',       'м2',                         'm2'),

('curr_us_dollar',          'Американский доллар',        'US Dollar'),
('curr_euro',               'Евро',                       'Euro'),
('curr_canadian_dollar',    'Канадский доллар',           'Canadian Dollar'),
('curr_australian_dollar',  'Австралийский доллар',       'Australian Dollar'),
('curr_new_zealand_dollar', 'Новозеландский доллар',      'New Zealand Dollar'),
('curr_russian_rouble',     'Российский рубль',           'Russian Rouble'),
('curr_pound_sterling',     'Фунт стерлингов',            'Pound Sterling'),

('tax_no_tax',              'Без НДС',                    'No taxes'),
('tax_tax',                 'НДС',                        'Vat'),

('main_bank_acc',           'Мой банк',                   'Main Bank account'),
('main_cash_room',          'Касса предприятия',          'Main Cash room'),
('my_company',              'Мое предприятие',            'My company'),
('my_department',           'Мое отделение',              'My department'),
('role_admins',             'Администраторы',             'Administrators'),

('catg_suppliers',          'Поставщики',                 'Suppliers'),
('catg_customers',          'Покупатели',                 'Customers'),
('catg_employees',          'Сотрудники',                 'Employees'),
('catg_banks',              'Банки',                      'Banks'),
('catg_transport',          'Транспорт',                  'Transport'),
('catg_rent',               'Аренда',                     'Rent'),
('catg_tax_srvcs',          'Налоговые',                  'Tax services'),

('basic_price',             'Базовая цена',               'Basic price'),

('exp_rent',                'Аренда',                     'Rent'),
('exp_return',              'Возвраты',                   'Return'),
('exp_salary',              'Зарплата',                   'Salary'),
('exp_banking_srvcs',       'Банк. обслуживание',         'Banking services'),
('exp_taxes',               'Налоги',                     'Taxes'),
('exp_pay_goods_srvcs',     'Оплата за товары и услуги',  'Payment for goods and services'),
('exp_pay_wh_company',      'Внутренние платежи',         'Payments within the company');
insert into _dictionary (key, tr_ru, tr_en) values
('um_piece',                'Штука',                      'Piece'),
('um_piece_s',              'шт',                         'pcs');
update version set value = '1.0.2', date = '02-07-2022';
------------------------------------------------  end of 1.0.2  ------------------------------------------------------
-----------------------------------------------  begin of 1.0.3   ----------------------------------------------------
insert into _dictionary (key, tr_ru, tr_en) values
('f_ctg_images',                'Картинки',                      'Images'),
('f_ctg_goods',                 'Товары',                        'Goods'),
('f_ctg_docs',                  'Документы',                     'Docs'),
('f_with_stamp_sign',           'с печатью и подписями',         'with stamp and signatures');
insert into _dictionary (key, tr_ru, tr_en) values
('signature',                   'Подпись',                       'Signature'),
('logo',                        'Логотип',                       'Logo');
insert into _dictionary (key, tr_ru, tr_en) values
('f_ctg_templates',             'Шаблоны',                       'Templates');
insert into _dictionary (key, tr_ru, tr_en) values
('stamp',                       'Печать',                        'Stamp');
update version set value = '1.0.3', date = '08-07-2022';
------------------------------------------------  end of 1.0.3  ------------------------------------------------------
-----------------------------------------------  begin of 1.0.4   ----------------------------------------------------
update documents set doc_name_en = 'Products and Services' where doc_name_ru = 'Товары и услуги';
alter table user_settings add column time_format varchar(2);
update user_settings set time_format = '12'; -- can be 12 or 24
alter table user_settings alter time_format set not null;

ALTER TABLE acceptance ADD COLUMN create_time_holder TIMESTAMP with time zone;
UPDATE acceptance SET create_time_holder = acceptance_date::TIMESTAMP;
ALTER TABLE acceptance ALTER COLUMN acceptance_date TYPE TIMESTAMP with time zone USING create_time_holder;
ALTER TABLE acceptance DROP COLUMN create_time_holder;

ALTER TABLE return ADD COLUMN create_time_holder TIMESTAMP with time zone;
UPDATE return SET create_time_holder = date_return::TIMESTAMP;
ALTER TABLE return ALTER COLUMN date_return TYPE TIMESTAMP with time zone USING create_time_holder;
ALTER TABLE return DROP COLUMN create_time_holder;

ALTER TABLE returnsup ADD COLUMN create_time_holder TIMESTAMP with time zone;
UPDATE returnsup SET create_time_holder = date_return::TIMESTAMP;
ALTER TABLE returnsup ALTER COLUMN date_return TYPE TIMESTAMP with time zone USING create_time_holder;
ALTER TABLE returnsup DROP COLUMN create_time_holder;

ALTER TABLE ordersup ADD COLUMN create_time_holder TIMESTAMP with time zone;
UPDATE ordersup SET create_time_holder = ordersup_date::TIMESTAMP;
ALTER TABLE ordersup ALTER COLUMN ordersup_date TYPE TIMESTAMP with time zone USING create_time_holder;
ALTER TABLE ordersup DROP COLUMN create_time_holder;

ALTER TABLE invoicein ADD COLUMN create_time_holder TIMESTAMP with time zone;
UPDATE invoicein SET create_time_holder = invoicein_date::TIMESTAMP;
ALTER TABLE invoicein ALTER COLUMN invoicein_date TYPE TIMESTAMP with time zone USING create_time_holder;
ALTER TABLE invoicein DROP COLUMN create_time_holder;

ALTER TABLE invoiceout ADD COLUMN create_time_holder TIMESTAMP with time zone;
UPDATE invoiceout SET create_time_holder = invoiceout_date::TIMESTAMP;
ALTER TABLE invoiceout ALTER COLUMN invoiceout_date TYPE TIMESTAMP with time zone USING create_time_holder;
ALTER TABLE invoiceout DROP COLUMN create_time_holder;

ALTER TABLE customers_orders ADD COLUMN create_time_holder TIMESTAMP with time zone;
UPDATE customers_orders SET create_time_holder = shipment_date::TIMESTAMP;
ALTER TABLE customers_orders ALTER COLUMN shipment_date TYPE TIMESTAMP with time zone USING create_time_holder;
ALTER TABLE customers_orders DROP COLUMN create_time_holder;

ALTER TABLE shipment ADD COLUMN create_time_holder TIMESTAMP with time zone;
UPDATE shipment SET create_time_holder = shipment_date::TIMESTAMP;
ALTER TABLE shipment ALTER COLUMN shipment_date TYPE TIMESTAMP with time zone USING create_time_holder;
ALTER TABLE shipment DROP COLUMN create_time_holder;

ALTER TABLE writeoff ADD COLUMN create_time_holder TIMESTAMP with time zone;
UPDATE writeoff SET create_time_holder = writeoff_date::TIMESTAMP;
ALTER TABLE writeoff ALTER COLUMN writeoff_date TYPE TIMESTAMP with time zone USING create_time_holder;
ALTER TABLE writeoff DROP COLUMN create_time_holder;

ALTER TABLE posting ADD COLUMN create_time_holder TIMESTAMP with time zone;
UPDATE posting SET create_time_holder = posting_date::TIMESTAMP;
ALTER TABLE posting ALTER COLUMN posting_date TYPE TIMESTAMP with time zone USING create_time_holder;
ALTER TABLE posting DROP COLUMN create_time_holder;

update version set value = '1.0.4', date = '27-08-2022';
------------------------------------------------  end of 1.0.4  ------------------------------------------------------
-----------------------------------------------  start of 1.0.5  -----------------------------------------------------
update documents set doc_name_en = 'Purchase order' where doc_name_ru = 'Заказ поставщику';
update _dictionary set tr_en = 'Purchase order' where key = 'ordersup';

update version set value = '1.0.4-1', date = '28-08-2022';
------------------------------------------------  end of 1.0.4  ------------------------------------------------------
-----------------------------------------------  start of 1.1.0  -----------------------------------------------------
alter table sprav_taxes add column woo_id int;

alter table companies add column is_store boolean;
alter table companies add column store_site_address varchar(128); -- e.g. http://localhost/DokioShop
alter table companies add column store_key varchar(128);    -- consumer key
alter table companies add column store_secret varchar(128); -- consumer secret
alter table companies add column store_type varchar(8); -- e.g. woo
alter table companies add column store_api_version varchar(16); -- e.g. v3
alter table companies add column crm_secret_key varchar(36); -- smth like UUID
alter table companies add column store_price_type_regular bigint; -- id of regular price
alter table companies add column store_price_type_sale bigint; -- id of sale price
alter table companies add constraint store_price_type_regular_id_fkey foreign key (store_price_type_regular) references sprav_type_prices (id);
alter table companies add constraint store_price_type_sale_id_fkey foreign key (store_price_type_sale) references sprav_type_prices (id);

alter table sprav_taxes add constraint woo_id_uq unique (company_id, woo_id) ;
alter table sprav_taxes alter column value type numeric(4,2);
alter table sprav_taxes alter column multiplier type numeric(5,4);

alter table product_categories add column date_time_sync timestamp with time zone;
alter table product_categories add column slug varchar(120);
alter table product_categories add column description varchar(250);
alter table product_categories add column display varchar(16);--Options: default, products, subcategories and both. Default is default.
alter table product_categories add column image_id bigint; -- id of category image file
alter table product_categories add constraint image_id_fkey foreign key (image_id) references files (id) ON DELETE SET NULL;
alter table files add column alt varchar(120);
alter table product_categories add column woo_id int;
alter table product_categories add constraint product_categories_woo_id_uq unique (company_id, woo_id) ;
alter table product_categories add constraint product_categories_slug_uq unique (company_id, slug);-- all company categories need to have unique slug names
alter table product_categories add constraint product_categories_name_uq unique (parent_id, name); -- one parent category can't contains two or more subcategories with the same names
CREATE UNIQUE INDEX product_categories_name_nn_uq ON product_categories (name, company_id) WHERE parent_id IS NULL;
alter table product_categories add column is_store_category boolean;
alter table products add column type	varchar(8); --Product type. Options: simple, grouped, external and variable. Default is simple.
alter table products add column slug varchar(120); --Product slug.
alter table products add column featured boolean; --Featured product. Default is false.
alter table products add column short_description varchar(2048);
alter table products alter column description TYPE varchar(16384);
alter table products add column virtual boolean; --If the product is virtual. Default is false.
alter table products add column downloadable	boolean; 	--If the product is downloadable. Default is false.
alter table products add column download_limit	integer; --Number of times downloadable files can be downloaded after purchase. Default is -1.
alter table products add column download_expiry	 integer; --Number of days until access to downloadable files expires. Default is -1.
alter table products add column external_url	varchar(255); --Product external URL. Only for external products.
alter table products add column button_text	 varchar(60); --Product external button text. Only for external products.
alter table products add column tax_status	varchar(8); --	Tax status. Options: taxable, shipping and none. Default is taxable.
alter table products add column manage_stock	boolean; --	Stock management at product level. Default is false.
alter table products add column stock_status varchar(10);	--Controls the stock status of the product. Options: instock, outofstock, onbackorder. Default is instock.
alter table products add column backorders	varchar(6); --If managing stock, this controls if backorders are allowed. Options: no, notify and yes. Default is no.
alter table products add column sold_individually	 boolean; --	Allow one item to be bought in a single order. Default is false.
alter table products add column height	numeric(10,3); --	Product height.
alter table products add column width	numeric(10,3); --	Product width.
alter table products add column length	numeric(10,3); --	Product length.
alter table products add column shipping_class	varchar(120); --	Shipping class slug.
alter table products add column reviews_allowed	 boolean; -- Allow reviews. Default is true.
alter table products add column parent_id	 bigint; --	Product parent ID.
alter table products add constraint parent_id_fkey foreign key (parent_id) references products (id);
alter table products add column purchase_note	 varchar(1000); -- Optional note to send the customer after purchase.
alter table products add column menu_order	int; -- Menu order, used to custom sort products.
alter table products add column date_on_sale_from_gmt  timestamp with time zone;
alter table products add column date_on_sale_to_gmt  timestamp with time zone;

-- DELETE FROM product_productcategories WHERE ctid NOT IN (SELECT max(ctid) FROM product_productcategories GROUP BY category_id, product_id); --use this if the next row won't be run perfect (there is duplicates)
alter table product_productcategories add constraint product_productcategories_uq unique (category_id, product_id);

create table product_upsell(
                    master_id          bigint,
                    product_id         bigint,
                    child_id           bigint,
                    foreign key (master_id)        references users(id),
                    foreign key (product_id)       references products(id),
                    foreign key (child_id)         references products(id)
);
create table product_crosssell(
                             master_id          bigint,
                             product_id         bigint,
                             child_id           bigint,
                             foreign key (master_id)        references users(id),
                             foreign key (product_id)       references products(id),
                             foreign key (child_id)         references products(id)
);
alter table product_upsell add constraint product_upsell_uq unique (child_id, product_id);
alter table product_crosssell add constraint product_crosssell_uq unique (child_id, product_id);
create table product_grouped(
                                master_id          bigint,
                                product_id         bigint,
                                child_id           bigint,
                                foreign key (master_id)        references users(id),
                                foreign key (product_id)       references products(id),
                                foreign key (child_id)         references products(id)
);
alter table product_grouped add constraint product_grouped_uq unique (child_id, product_id);

alter table products add column low_stock_threshold	numeric(12,3); --	Low stock threshold

create table product_downloadable_files(
                                product_id          bigint,
                                file_id             bigint,
                                output_order        int,
                                foreign key (file_id)          references files(id),
                                foreign key (product_id)       references products(id)
);
alter table product_downloadable_files add constraint product_downloadable_files_uq unique (file_id, product_id);

create table product_attributes(
                            id                  bigserial primary key not null,
                            master_id           bigint not null,
                            company_id          bigint not null,
                            creator_id          bigint not null,
                            changer_id          bigint,
                            date_time_created   timestamp with time zone not null,
                            date_time_changed   timestamp with time zone,
                            woo_id              int, -- Attribute name.MANDATORY
                            name                varchar(120),
                            slug                varchar(120), -- An alphanumeric identifier for the resource unique to its type.
                            type                varchar(16), -- Type of attribute. By default only 'select' is supported.
                            order_by            varchar(16), -- Default sort order. Options: menu_order, name, name_num and id. Default is menu_order.
                            has_archives        boolean, -- Enable/Disable attribute archives. Default is false.
                            is_deleted boolean,
                            foreign key (master_id) references users(id),
                            foreign key (creator_id) references users(id),
                            foreign key (changer_id) references users(id),
                            foreign key (company_id) references companies(id)
);
create table product_attribute_terms(
                             id                 bigserial primary key not null,
                             woo_id             int,
                             master_id          bigint,
                             attribute_id       bigint, -- Id of a parent attribute
                             name               varchar(120), -- Term name.
                             slug               varchar(120), -- An alphanumeric identifier for the resource unique to its type.
                             description        varchar(1000), -- HTML description of the resource.
                             menu_order         int,    -- Menu order, used to custom sort the resource.
                             foreign key (master_id) references users(id),
                             foreign key (attribute_id) references product_attributes(id) on delete cascade
);

create table product_custom_attributes(
                              id                 bigserial primary key not null,
                              master_id          bigint,
                              product_id         bigint,
                              name               varchar(120),
                              terms              varchar(1000), -- String with terms divided by | e.g. Red | Blue | Yellow
                              visible            boolean, -- Define if the attribute is visible on the "Additional information" tab in the product's page.
                              foreign key (master_id) references users(id),
                              foreign key (product_id) references products(id)
);
alter table product_custom_attributes add constraint product_custom_attributes_name_uq unique (name, product_id);

insert into _dictionary (key, tr_ru, tr_en) values
('color',    'Цвет','Color'),
('size',     'Размер','Size');

insert into documents (id, name, page_name, show, table_name, doc_name_ru, doc_name_en) values (53,'Атрибуты товаров','productattributes',1,'product_attributes','Атрибуты товаров', 'Product attributes');

insert into permissions (id,name_ru,name_en,document_id,output_order) values
(662,'Отображать в списке документов на боковой панели','Display in the list of documents in the sidebar',53,10),
(663,'Создание документов по всем предприятиям','Creation of documents for all companies',53,20),
(664,'Создание документов своего предприятия','Create your company documents',53,30),
(665,'Удаление документов всех предприятий','Deleting documents of all companies',53,130),
(666,'Удаление документов своего предприятия','Deleting your company documents',53,140),
(667,'Просмотр документов всех предприятий','View documents of all companies',53,50),
(668,'Просмотр документов своего предприятия','View your company documents',53,60),
(669,'Редактирование документов всех предприятий','Editing documents of all companies',53,90),
(670,'Редактирование документов своего предприятия','Editing your company documents',53,100);


alter table product_attributes add constraint product_attributes_slug_uq unique (company_id, slug);-- all company product attributes need to have unique slug names
alter table product_attribute_terms add constraint product_attribute_terms_slug_uq unique (attribute_id, slug);-- product attribute need to have unique terms slug names
alter table product_attributes add constraint product_attributes_name_uq unique (company_id, name);--product attribute name must be unique
alter table product_attribute_terms add constraint product_attribute_terms_name_uq unique (attribute_id, name);-- product attribute need to have unique terms names
alter table product_attribute_terms alter column attribute_id set not null;


create table product_productattributes(
                                        master_id     bigint not null,
                                        product_id    bigint not null,
                                        attribute_id  bigint not null,
                                        position      int not null, -- 	Attribute position
                                        visible       boolean, -- Define if the attribute is visible on the "Additional information" tab in the product's page. Default is false.
                                        variation	    boolean, -- Define if the attribute can be used as variation. Default is false.
                                        foreign key (master_id) references users(id),
                                        foreign key (product_id) references products(id),
                                        foreign key (attribute_id) references product_attributes(id)
);
alter table product_productattributes add constraint product_productattribute_uq unique (product_id, attribute_id);--product attribute in the product card must be unique
create table product_terms(
                                        master_id     bigint not null,
                                        product_id    bigint not null,
                                        term_id       bigint not null,
                                        foreign key (master_id) references users(id),
                                        foreign key (product_id) references products(id),
                                        foreign key (term_id) references product_attribute_terms(id)
);

alter table product_terms add constraint product_term_uq unique (product_id, term_id);--product term in the product card must be unique

alter table product_terms add column product_attribute_id bigint not null;
alter table product_terms add constraint attribute_id_fkey foreign key (product_attribute_id) references product_attributes (id);


alter table product_terms drop constraint product_terms_term_id_fkey, add constraint product_terms_term_id_fkey foreign key (term_id) references product_attribute_terms(id) on delete cascade;
alter table products add column woo_id int;
alter table products add column date_time_syncwoo timestamp with time zone;
--DELETE FROM product_files WHERE ctid NOT IN (SELECT max(ctid) FROM product_files GROUP BY file_id, product_id); run it, if next row won't be executed
alter table product_files add constraint product_file_uq unique (file_id, product_id);
alter table products add column need_to_syncwoo boolean; -- the product is need to be synchronized because its category turned into store type category
alter table customers_orders add column woo_gmt_date varchar(19);
alter table customers_orders add column woo_id int;
alter table companies add column nds_included boolean; -- used with nds_payer as default values for Customers orders fields "Tax" and "Tax included"
alter table companies add column store_orders_department_id bigint;
alter table companies add constraint store_orders_department_id_fkey foreign key (store_orders_department_id) references departments (id);
alter table companies add column store_if_customer_not_found varchar(11); -- create_new or use_default
alter table companies add column store_default_customer_id bigint;
alter table companies add constraint store_default_customer_id_fkey foreign key (store_default_customer_id) references cagents (id);
alter table cagents add column woo_id integer;
alter table companies add column store_default_creator_id bigint;
alter table companies add constraint store_default_creator_id_fkey foreign key (store_default_creator_id) references users (id);
alter table companies add column store_days_for_esd int;
alter table products add constraint product_woo_id_uq unique (company_id, woo_id);
create table company_store_departments(
                                      master_id          bigint not null,
                                      company_id         bigint not null,
                                      department_id      bigint not null,
                                      menu_order         int,    -- Menu order, used to custom sort the departments
                                      foreign key (master_id) references users(id),
                                      foreign key (department_id) references departments(id),
                                      foreign key (company_id) references companies(id)
);
alter table company_store_departments add constraint company_store_department_uq unique (company_id, department_id);
alter table companies add column store_auto_reserve boolean; -- auto reserve product after getting internet store order
alter table products add column outofstock_aftersale boolean; -- auto set product as out-of-stock after it has been sold
update version set value = '1.1.0', date = '28-12-2022';
------------------------------------------------  end of 1.1.0  ------------------------------------------------------
------------------------------------------------  start of 1.1.1  ------------------------------------------------------
alter table template_docs add column type varchar(8); -- the type of template/ Can be: "document", "label"
alter table template_docs add column num_labels_in_row int; -- quantity of labels in the each row
update template_docs set type='document';
alter table template_docs alter column type set not null;
alter table products add column label_description varchar(2048);
insert into _dictionary (key, tr_ru, tr_en) values
('pricetag',             'Ценник',                       'Price tag');
alter table products add column description_html varchar(16384);
alter table products add column short_description_html varchar(3000);
alter table products add column description_type varchar(6); -- editor / custom
alter table products add column short_description_type varchar(6); -- editor / custom
alter table file_filecategories alter column category_id TYPE bigint USING category_id::bigint;
alter table sprav_sys_edizm add column is_default boolean;
create table customers_orders_files (
                                customers_orders_id bigint not null,
                                file_id bigint not null,
                                foreign key (file_id) references files (id) ON DELETE CASCADE,
                                foreign key (customers_orders_id ) references customers_orders (id) ON DELETE CASCADE
);
alter table plans add column n_stores int;
update plans set n_stores=1 where name_en='No limits';
update plans set n_stores=0 where name_en='Free';
alter table plans alter column n_stores set not null;
alter table plans_add_options add column n_stores int not null;
alter table plans_add_options add column stores_ppu numeric(10,3);
alter table plans_add_options alter column companies_ppu TYPE numeric (10,3);
alter table plans_add_options alter column departments_ppu TYPE numeric (10,3);
alter table plans_add_options alter column users_ppu TYPE numeric (10,3);
alter table plans_add_options alter column products_ppu TYPE numeric (10,3);
alter table plans_add_options alter column counterparties_ppu TYPE numeric (10,3);
alter table plans_add_options alter column megabytes_ppu TYPE numeric (10,3);
alter table companies add column store_ip varchar(21);
update companies set crm_secret_key = null where crm_secret_key='';
alter table companies add constraint crm_secret_key_uq unique (crm_secret_key);
-- CREATE UNIQUE INDEX crm_secret_key_uq ON companies (crm_secret_key) WHERE crm_secret_key IS NOT NULL;
CREATE INDEX crm_secret_key_idx ON companies(crm_secret_key);
update version set value = '1.1.1', date = '13-01-2023';
------------------------------------------------  end of 1.1.1  ------------------------------------------------------
----------------------------------------------  start of 1.2.0  ------------------------------------------------------
drop table if exists sites_html;
drop table if exists sites_routes;
drop table if exists sites;
delete from usergroup_permissions where permission_id in (select id from permissions where document_id=20);
delete from permissions where document_id=20;
delete from documents where id=20;

create table stores (id bigserial primary key not null,
                     master_id  bigint not null,
                     company_id bigint not null,
                     creator_id bigint not null,
                     changer_id bigint,
                     date_time_created timestamp with time zone not null,
                     date_time_changed timestamp with time zone,
                     name varchar(250) not null,
                     lang_code  varchar(2) not null,
                     store_type varchar(8) not null, -- e.g. woo
                     store_api_version varchar(16) not null, -- e.g. v3
                     crm_secret_key varchar(36), -- smth like UUID
                     store_price_type_regular bigint not null, -- id of regular price
                     store_price_type_sale bigint, -- id of sale price
                     store_ip varchar(21) not null,
                     is_deleted boolean,
                     store_auto_reserve boolean,
                     store_days_for_esd int not null,
                     store_default_creator_id bigint not null,
                     store_default_customer_id bigint,
                     store_if_customer_not_found varchar(11) not null,
                     store_orders_department_id bigint not null,
                     foreign key (master_id) references users(id),
                     foreign key (creator_id) references users(id),
                     foreign key (changer_id) references users(id),
                     foreign key (company_id) references companies(id),
                     foreign key (store_default_creator_id) references users(id),
                     foreign key (store_default_customer_id) references cagents(id),
                     foreign key (store_orders_department_id) references departments(id)
);
alter table stores add constraint store_price_type_regular_id_fkey foreign key (store_price_type_regular) references sprav_type_prices (id);
alter table stores add constraint store_price_type_sale_id_fkey foreign key (store_price_type_sale) references sprav_type_prices (id);
alter table stores add constraint store_crm_secret_key_uq unique (crm_secret_key);

insert into documents (id, name, page_name, show, table_name, doc_name_ru, doc_name_en) values (54,'Интернет-магазины','stores',1,'stores','Интернет-магазины', 'Online stores');

insert into permissions (id,name_ru,name_en,document_id,output_order) values
(671,'Отображать в списке документов на боковой панели','Display in the list of documents in the sidebar',54,10),
(672,'Создание документов по всем предприятиям','Creation of documents for all companies',54,20),
(673,'Создание документов своего предприятия','Create your company documents',54,30),
(674,'Удаление документов всех предприятий','Deleting documents of all companies',54,130),
(675,'Удаление документов своего предприятия','Deleting your company documents',54,140),
(676,'Просмотр документов всех предприятий','View documents of all companies',54,50),
(677,'Просмотр документов своего предприятия','View your company documents',54,60),
(678,'Редактирование документов всех предприятий','Editing documents of all companies',54,90),
(679,'Редактирование документов своего предприятия','Editing your company documents',54,100);

create table store_departments(
                                master_id          bigint not null,
                                company_id         bigint not null,
                                store_id           bigint not null,
                                department_id      bigint not null,
                                menu_order         int,    -- Menu order, used to custom sort the departments
                                foreign key (master_id) references users(id),
                                foreign key (department_id) references departments(id),
                                foreign key (store_id) references stores(id),
                                foreign key (company_id) references companies(id)
);
alter table store_departments add constraint store_department_uq unique (store_id, department_id);
create table stores_products (
                                master_id          bigint not null,
                                company_id         bigint not null,
                                store_id           bigint not null,
                                product_id         bigint not null,
                                woo_id             int,
                                need_to_syncwoo    boolean,
                                date_time_syncwoo  timestamp with time zone,
                                foreign key (master_id) references users(id),
                                foreign key (company_id) references companies(id),
                                foreign key (product_id) references products(id),
                                foreign key (store_id) references stores(id)
);
alter table stores_products add constraint stores_products_uq unique (store_id, product_id);
alter table companies add column store_default_lang_code  varchar(2);-- e.g. RU or EN
update companies set store_default_lang_code='EN';
create table store_translate_categories(
                                master_id          bigint not null,
                                company_id         bigint not null,
                                lang_code          varchar(2) not null,
                                category_id        bigint not null,
                                name               varchar(512),
                                slug               varchar(120),
                                description        varchar(250),
                                foreign key (master_id) references users(id),
                                foreign key (company_id) references companies(id),
                                foreign key (category_id) references product_categories(id) on delete cascade
);
alter table store_translate_categories add constraint category_lang_uq unique (category_id, lang_code);
alter table companies alter column store_default_lang_code set not null;
create table stores_productcategories (
                                master_id          bigint not null,
                                company_id         bigint not null,
                                store_id           bigint not null,
                                category_id        bigint not null,
                                woo_id             int,
                                foreign key (master_id) references users(id),
                                foreign key (company_id) references companies(id),
                                foreign key (category_id) references product_categories(id) on delete cascade,
                                foreign key (store_id) references stores(id)
);
alter table stores_productcategories add constraint stores_categories_uq unique (store_id, category_id);

create table store_translate_attributes(
                                master_id          bigint not null,
                                company_id         bigint not null,
                                lang_code          varchar(2) not null,
                                attribute_id       bigint not null,
                                name               varchar(512),
                                slug               varchar(120),
                                foreign key (master_id) references users(id),
                                foreign key (company_id) references companies(id),
                                foreign key (attribute_id) references product_attributes(id) on delete cascade
);
alter table store_translate_attributes add constraint attribute_lang_uq unique (attribute_id, lang_code);
create table stores_attributes (
                                master_id          bigint not null,
                                company_id         bigint not null,
                                store_id           bigint not null,
                                attribute_id       bigint not null,
                                woo_id             int,
                                foreign key (master_id) references users(id),
                                foreign key (company_id) references companies(id),
                                foreign key (attribute_id) references product_attributes(id) on delete cascade,
                                foreign key (store_id) references stores(id)
);
alter table stores_attributes add constraint stores_attributes_uq unique (store_id, attribute_id);
create table store_translate_terms(
                                master_id          bigint not null,
                                company_id         bigint not null,
                                lang_code          varchar(2) not null,
                                term_id            bigint not null,
                                name               varchar(512),
                                slug               varchar(120),
                                description        varchar(250),
                                foreign key (master_id) references users(id),
                                foreign key (company_id) references companies(id),
                                foreign key (term_id) references product_attribute_terms(id) on delete cascade
);
alter table store_translate_terms add constraint term_lang_uq unique (term_id, lang_code);
create table stores_terms (
                                master_id          bigint not null,
                                company_id         bigint not null,
                                store_id           bigint not null,
                                term_id            bigint not null,
                                woo_id             int,
                                foreign key (master_id) references users(id),
                                foreign key (company_id) references companies(id),
                                foreign key (term_id) references product_attribute_terms(id) on delete cascade,
                                foreign key (store_id) references stores(id)
);
alter table stores_terms add constraint stores_terms_uq unique (store_id, term_id);
create table store_translate_products(
                                    master_id          bigint not null,
                                    company_id         bigint not null,
                                    lang_code          varchar(2) not null,
                                    product_id         bigint not null,
                                    name               varchar(512),
                                    slug               varchar(512),
                                    description        varchar(100000),
                                    description_html   varchar(100000),
                                    short_description  varchar(100000),
                                    short_description_html varchar(100000),
                                    foreign key (master_id) references users(id),
                                    foreign key (company_id) references companies(id),
                                    foreign key (product_id) references products(id) on delete cascade
);
alter table store_translate_products add constraint product_lang_uq unique (product_id, lang_code);
alter table products alter column description type varchar(100000);
alter table products alter column description_html type varchar(100000);
alter table products alter column short_description type varchar(100000);
alter table products alter column short_description_html type varchar(100000);
alter table products alter column slug type varchar(512);

alter table customers_orders add column store_id bigint;
alter table customers_orders add constraint customers_orders_store_id_fkey foreign key (store_id) references stores (id);
CREATE UNIQUE INDEX store_translate_attributes_slug_uq ON store_translate_attributes (company_id, lang_code, slug) WHERE slug !='';-- attribute need to have unique translated slugs for each language
drop index product_categories_name_nn_uq;
CREATE UNIQUE INDEX product_categories_name_nn_uq ON product_categories (name, company_id) WHERE parent_id IS NULL;
alter table product_categories drop constraint product_categories_slug_uq;
CREATE UNIQUE INDEX product_categories_slug_uq ON product_categories (company_id, slug) WHERE slug !='';
alter table product_attributes drop constraint product_attributes_name_uq;
alter table store_translate_terms add column attribute_id int not null ;
alter table store_translate_terms add constraint store_translate_terms_attribute_id_fkey foreign key (attribute_id) references product_attributes (id) on delete cascade;
CREATE UNIQUE INDEX store_translate_terms_name_uq ON store_translate_terms (attribute_id, lang_code, name) WHERE name !='';
CREATE UNIQUE INDEX store_translate_terms_slug_uq ON store_translate_terms (attribute_id, lang_code, slug) WHERE slug !='';
alter table plans alter column  daily_price type numeric (20,10);
drop table company_store_departments;
insert into _dictionary (key, tr_en, tr_ru) values ('sale_price', 'Sale price', 'Скидочная цена');

alter table products drop column woo_id;
alter table products drop column date_time_syncwoo;
alter table products drop column need_to_syncwoo;
alter table companies drop column is_store;
alter table companies drop column store_site_address;
alter table companies drop column store_key;
alter table companies drop column store_secret;
alter table companies drop column store_type;
alter table companies drop column store_api_version;
alter table companies drop column crm_secret_key;
alter table companies drop column store_price_type_regular;
alter table companies drop column store_price_type_sale;
alter table product_categories drop column woo_id;
alter table product_attributes drop column woo_id;
alter table product_attribute_terms drop column woo_id;
update version set value = '1.2.0', date = '13-03-2023';
------------------------------------------------  end of 1.2.0  ------------------------------------------------------
insert into _dictionary (key, tr_ru, tr_en) values
('payroll_taxes',               'Налоги на зарплату',                       'Payroll taxes'),
('accounting_srvcs',            'Бухгалтерские услуги',                     'Accounting services');
update _dictionary set tr_ru = 'Подоходные налоги', tr_en = 'Income taxes' where key = 'exp_taxes';

CREATE UNIQUE INDEX products_sku_uq ON products (article, company_id) WHERE article !=''; -- because in WooCommerce article must be unique

insert into _dictionary (key, tr_ru, tr_en) values
('catg_accounting',              'Бухгалтерские услуги',                      'Accounting services'),
('cagent_director_y',            'Директор (вы)',                             'CEO (you)'),
('cagent_accntnts',              'Бухгалтер',                                 'Accountant'),
('cagent_supplier',              'Поставщик',                                 'Supplier'),
('cagent_customer',              'Покупатель',                                'Customer'),
('cagent_bank',                  'Банк',                                      'Bank'),
('cagent_taxoffce',              'Налоговая служба',                          'Tax office'),
('cagent_carrier',               'Перевозчик',                                'Carrier'),
('cagent_landlord',              'Арендодатель',                              'Landlord'),
('p_catg_myprods',               'Мои товары',                                'My products'),
('p_catg_srvcs',                 'Мои услуги',                                'My services'),
('p_catg_in_srvcs',              'Принимаемые',                               'Receiving'),
('prod_work_empl',               'Работа и услуги от сотрудника',             'Work and services from an employee'),
('prod_transp',                  'Транспортные услуги',                       'Transport service'),
('prod_rent',                    'Услуги аренды',                             'Rent service'),
('prod_prolltax',                'Обязательства по налогам на зарплату',      'Payroll taxes obligations'),
('prod_incomtax',                'Обязательства по налогу на прибыль',        'Income taxes obligations'),
('prod_banking',                 'Банковские услуги',                         'Banking service'),
('prod_my_prod',                 'Мой товар',                                 'My example product'),
('prod_my_srvc',                 'Моя услуга',                                'My example service'),
('prod_accounting',              'Бухгалтерские услуги',                      'Accounting service');




update version set value = '1.2.1', date = '05-04-2023';
------------------------------------------------  end of 1.2.1  ------------------------------------------------------































































WITH
  credit as (
    selectgetProductHistoryTableReport
        (select coalesce(sum(acp.product_sumprice),0) from acceptance_product acp where acp.acceptance_id in
          (select ac.id from acceptance ac where ac.master_id=4 and ac.company_id=1 and coalesce(ac.is_completed,false)=true and ac.cagent_id=1))
        +
        (select coalesce(sum(rcp.product_sumprice),0) from return_product rcp where rcp.return_id in
          (select rc.id from return rc where rc.master_id=4 and rc.company_id=1 and coalesce(rc.is_completed,false)=true and rc.cagent_id=1))
        +
        (select coalesce(sum(pi.summ),0) from paymentin pi where pi.master_id=4 and pi.company_id=1 and pi.cagent_id=1 and coalesce(pi.is_completed,false)=true)
        +
        (select coalesce(sum(oi.summ),0) from orderin oi where oi.master_id=4 and oi.company_id=1 and oi.cagent_id=1 and coalesce(oi.is_completed,false)=true)
  ),
  debet as (
    select
        (select coalesce(sum(shp.product_sumprice),0) from shipment_product shp where shp.shipment_id in
          (select sh.id from shipment sh where sh.master_id=4 and sh.company_id=1 and coalesce(sh.is_completed,false)=true and sh.cagent_id=1))
        +
        (select coalesce(sum(rsp.product_sumprice),0) from returnsup_product rsp where rsp.returnsup_id in
          (select rs.id from returnsup rs where rs.master_id=4 and rs.company_id=1 and coalesce(rs.is_completed,false)=true and rs.cagent_id=1))
        +
        (select coalesce(sum(po.summ),0) from paymentout po where po.master_id=4 and po.company_id=1 and po.cagent_id=1 and coalesce(po.is_completed,false)=true)
        +
        (select coalesce(sum(oo.summ),0) from orderout oo where oo.master_id=4 and oo.company_id=1 and oo.cagent_id=1 and coalesce(oo.is_completed,false)=true)
  )
select
  (select * from credit) as credit,
  (select * from debet) as debet,
  ((select * from credit)-(select * from debet)) as balance;


WITH
  income as (
    select
      (select coalesce(sum(oi.summ),0) from orderin oi where oi.master_id=4 and oi.company_id=1 and boxoffice_id=1 and coalesce(oi.is_completed,false)=true)
  ),
  outcome as (
    select
      (select coalesce(sum(oo.summ),0) from orderout oo where oo.master_id=4 and oo.company_id=1 and boxoffice_id=1 and coalesce(oo.is_completed,false)=true)
  )
select
  (select * from income) as income,
  (select * from outcome) as outcome,
  ((select * from income)-(select * from outcome)) as balance;

WITH
  income as (
    select
      (select coalesce(sum(pi.summ),0) from paymentin pi where pi.master_id=4 and pi.company_id=1 and payment_account_id=4 and coalesce(pi.is_completed,false)=true)
  ),
  outcome as(
    select
      (select coalesce(sum(po.summ),0) from paymentout po where po.master_id=4 and po.company_id=1 and payment_account_id=4 and coalesce(po.is_completed,false)=true)
  )
select
  (select * from income) as income,
  (select * from outcome) as outcome,
  ((select * from income)-(select * from outcome)) as balance;


WITH
  income as (
    select
      (select coalesce(sum(pi.summ),0) from paymentin pi where pi.master_id=4 and pi.company_id=1 and payment_account_id=4 and coalesce(pi.is_completed,false)=true)
  ),
  outcome as(
    select
      (select coalesce(sum(po.summ),0) from paymentout po where po.master_id=4 and po.company_id=1 and payment_account_id=4 and coalesce(po.is_completed,false)=true)
  ),
  correction as(
    select
      (select coalesce(sum(co.summ),0) from correction co where co.master_id=4 and co.company_id=1 and payment_account_id=4 and coalesce(co.is_completed,false)=true)
  )
select
  (select * from income) as income,
  (select * from outcome) as outcome,
  ((select * from income)-(select * from outcome)+(select * from correction)) as balance;






delete from user_roles where user_id=141;
delete from users where id=141;



