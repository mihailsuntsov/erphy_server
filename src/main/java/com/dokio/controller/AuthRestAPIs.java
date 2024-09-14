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
package com.dokio.controller;

import java.sql.Timestamp;
import java.util.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.validation.Valid;

import com.dokio.message.request.CompaniesForm;
import com.dokio.message.request.DepartmentForm;
import com.dokio.message.request.Sprav.StoresForm;
import com.dokio.message.request.additional.DepartmentPartsForm;
import com.dokio.message.response.Settings.SettingsGeneralJSON;
import com.dokio.message.response.additional.BaseFiles;
import com.dokio.model.*;
import com.dokio.repository.*;
import com.dokio.security.CryptoService;
import com.dokio.security.services.UserDetailsServiceImpl;
import com.dokio.service.StorageService;
import com.dokio.util.CommonUtilites;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import com.dokio.message.request.LoginForm;
import com.dokio.message.request.SignUpForm;
import com.dokio.message.response.JwtResponse;
import com.dokio.message.response.ResponseMessage;
import com.dokio.security.jwt.JwtProvider;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/public")
@Repository
public class AuthRestAPIs {

	Logger logger = Logger.getLogger("AuthRestAPIs");
    @Autowired
    private EntityManagerFactory emf;
	@Autowired
	AuthenticationManager authenticationManager;
	@Autowired
	UserRepository userRepository;
	@Autowired
	RoleRepository roleRepository;
	@Autowired
	PasswordEncoder encoder;
	@Autowired
	JwtProvider jwtProvider;
	@Autowired
	UserDetailsServiceImpl userDetailsService;
	@Autowired
	CompanyRepositoryJPA companyRepositoryJPA;
	@Autowired
	DepartmentRepositoryJPA departmentRepositoryJPA;
    @Autowired
    UserGroupRepositoryJPA userGroupRepository;
	@Autowired
	UserRepositoryJPA userRepositoryJPA;
	@Autowired
    SpravCurrenciesRepository currenciesRepository;
	@Autowired
    CagentRepositoryJPA cagentRepository;
	@Autowired
	ProductsRepositoryJPA productsRepository;
    @Autowired
    SpravSysEdizmJPA spravSysEdizm;
    @Autowired
    SpravBoxofficeRepositoryJPA boxofficeRepository;
    @Autowired
    SpravExpenditureRepositoryJPA expenditureRepository;
    @Autowired
    TypePricesRepositoryJPA typePricesRepository;
    @Autowired
    SpravTaxesRepository taxesRepository;
    @Autowired
    MailRepository mailRepository;
    @Autowired
	SpravStatusDocRepository statusDocRepository;
	@Autowired
	CompaniesPaymentAccountsRepositoryJPA paymentAccountsRepository;
	@Autowired
	CommonUtilites cu;
	@Autowired
	FileRepositoryJPA fileRepository;
	@Autowired
	DocumentsRepositoryJPA documentsRepository;
	@Autowired
	SpravProductAttributeRepository spravProductAttributes;
	@Autowired
	SpravJobtitleRepositoryJPA spravJobtitleRepository;
	@Autowired
	SubscriptionRepositoryJPA subscriptionRepository;
    @Autowired
    StoreRepository storeRepository;
	@Autowired
	StorageService storageService;
	@Autowired
	private CryptoService cryptoService;

	@PostMapping("/signin")
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginForm loginRequest) {
		logger.info("Processing post request for path /signin: " + loginRequest.toString());

		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        //Тут, если логин или пароль неверны, код далее не выполняется, и authenticationManager возвращает: {status: 401, error: "Unauthorized", message: "Error -> Unauthorized"}

        Integer userStatus = userRepository.findByUsername(loginRequest.getUsername()).get().getStatus_account();

        if(userStatus == 2) { // статус = Активный
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtProvider.generateJwtToken(authentication);
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return ResponseEntity.ok(new JwtResponse(jwt, userDetails.getUsername(), userDetails.getAuthorities()));
        } else
		    return new ResponseEntity<>(//иначе отправляем то же самое, когда логин-пароль не верны
		  "{\n" +
				"\"status\": 401,\n" +
				"\"error\": \"Unauthorized\",\n" +
				"\"message\": \""+(userStatus == 1?"Error -> Not activated":"Error -> Unauthorized")+"\"\n" +
				"}\n", HttpStatus.UNAUTHORIZED);
	}

	@SuppressWarnings("Duplicates")
	@PostMapping("/signup")
	public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpForm signUpRequest) {
		logger.info("Processing post request for path /signup: " + signUpRequest.toString());
		try {
			// checking that system allow to register new users:
			SettingsGeneralJSON settingsGeneral = cu.getSettingsGeneral(true);
			if (!settingsGeneral.isAllowRegistration())
				return new ResponseEntity<>("Registration of new users is not available at the moment", HttpStatus.INTERNAL_SERVER_ERROR);






			EntityManager emgr = emf.createEntityManager();
			if (userRepository.existsByUsername(signUpRequest.getUsername())) {
				return new ResponseEntity<>(new ResponseMessage("login_registered"),
						HttpStatus.NOT_ACCEPTABLE);
			}

			if (userRepository.existsByEmail(signUpRequest.getEmail())) {
				return new ResponseEntity<>(new ResponseMessage("email_registered"),
						HttpStatus.NOT_ACCEPTABLE);
			}
			// Если такого логина и емайла нет
			// Создание аккаунта для нового пользователя
			User user = new User(signUpRequest.getName(), signUpRequest.getUsername(), signUpRequest.getEmail(),
					encoder.encode(signUpRequest.getPassword()));

			int cntUsers = cu.getCntRegisteredUsers();

//			Set<String> strRoles = new HashSet<>();
//			strRoles.add("admin"); // это "системная" роль, для спринга. Ею наделяются все пользователи. Все их реальные права регулируются Докио
			Set<Role> roles = new HashSet<>();
			Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN).orElseThrow(() -> new RuntimeException("Fail! -> Cause: User Role not find."));
			roles.add(adminRole);
			user.setDate_time_created(new Timestamp(System.currentTimeMillis()));
			//добавили юзеру сет ролей и сохранили его
			user.setRoles(roles);
			user.setStatus_account(cntUsers==0?2:1); //Status = 1: email need to be verified. 2: email verified.
			// Since the first user may not have a correctly configured mail server (because it vas installed a few minutes ago),
			// he do not need to verify his email
			user.setActivationCode(cntUsers>0?UUID.randomUUID().toString():null); // Код активации, высылаемый на e-mail
			user.setPlanId(settingsGeneral.getPlanDefaultId());
			user.setPlanPrice(settingsGeneral.getPlanPrice());
			user.setFreeTrialDays(settingsGeneral.getFreeTrialDays());
			Long createdUserId = userRepository.save(user).getId();// и сохранили его
			user.setMaster(userDetailsService.getUserById(createdUserId));// в качестве мастера устанавливаем его же
			userRepository.save(user);// сохраняем чтобы записался master id

			// уcтановим пользователю часовой пояс (timeZone 21 = Etc/GMT+0), язык и локаль
			userRepositoryJPA.setUserSettings(createdUserId, 21, userRepositoryJPA.getLangIdBySuffix(signUpRequest.getLanguage()), signUpRequest.getLanguage().equals("ru") ? 10 : 4, "24");

			userRepository.save(user);// сохраняем чтобы применился язык
			String cryptoPassword = cryptoService.getCryptoPasswordFromDatabase(createdUserId);
//			if(Objects.isNull(cryptoPassword)) throw new Exception("Can't create crypto password!");
			Map<String, String> map = cu.translateForUser(createdUserId, new String[]{"'my_company'", "'my_department'", "'role_admins'", "'default_store_name'", "'dep_part'"});
			// set plan options with current prices to master user
			subscriptionRepository.createMasterUserPlanOptions(createdUserId);
			// создадим пользователю предприятие
			CompaniesForm company = new CompaniesForm();
			company.setName(map.get("my_company"));
			company.setSt_prefix_barcode_packed(20);
			company.setSt_prefix_barcode_pieced(21);
			company.setSt_netcost_policy("all");
			company.setStore_default_lang_code(signUpRequest.getLanguage());
			Long companyId = companyRepositoryJPA.insertCompanyFast(company, createdUserId);
			// типы цен
			List<Long> prices = typePricesRepository.insertPriceTypesFast(createdUserId, createdUserId, companyId);
			// касса предприятия (денежная комната)
			Long bo = boxofficeRepository.insertBoxofficesFast(createdUserId, createdUserId, companyId);
			// расчетный счет предприятия
			Long ac = paymentAccountsRepository.insertPaymentAccountsFast(createdUserId, createdUserId, companyId);
			// создадим отделение
			DepartmentForm department = new DepartmentForm();
			department.setName(map.get("my_department"));
			department.setPrice_id(prices.get(0));
			department.setBoxoffice_id(bo);
			department.setPayment_account_id(ac);
			Long departmentId = departmentRepositoryJPA.insertDepartmentFast(department, companyId, createdUserId);
			departmentRepositoryJPA.insertDepartmentPartFast(map.get("dep_part")+" 1", departmentId, createdUserId);
//			DepartmentPartsForm departmentPartsForm = new DepartmentPartsForm();
			Companies userCompany = emgr.find(Companies.class, companyId);
			Set<Long> userDepartmentsIds = new HashSet<>(Arrays.asList(departmentId));
			Set<Departments> userDepartments = departmentRepositoryJPA.getDepartmentsSetBySetOfDepartmentsId(userDepartmentsIds);
			user.setCompany(userCompany);
			user.setDepartments(userDepartments);
			user.setFio_name(signUpRequest.getName());
			userRepository.saveAndFlush(user);
			// создадим Роль (группу пользователей)
			Long usergroupId = userGroupRepository.insertUsergroupFast(map.get("role_admins"), createdUserId, createdUserId);
			Set<Long> permissions = getAdminPermissions();
			userGroupRepository.setPermissionsToUserGroup(permissions, usergroupId);
			// зададим пользователю набор валют
			currenciesRepository.insertCurrenciesFast(createdUserId, createdUserId, companyId);
			// базовые категоии контрагентов и сами контрагенты
			cagentRepository.insertCagentCategoriesFast(createdUserId, createdUserId, companyId, cryptoPassword);
			// базовые категоии файлов + базовые файлы (шаблоны)
			Long templateCategoryId = fileRepository.insertFileCategoriesFast(createdUserId, createdUserId, companyId);
			// now need to put base files into this category in accordance of user language
			List<BaseFiles> baseFilesList = fileRepository.insertBaseFilesFast(createdUserId, createdUserId, companyId, templateCategoryId);
			// forming print menu for documents
			if (!Objects.isNull(baseFilesList))
				documentsRepository.createPrintMenus(baseFilesList, createdUserId, createdUserId, companyId);
			// единицы имерения
			spravSysEdizm.insertEdizmFast(createdUserId, createdUserId, companyId);
			// налоги
			taxesRepository.insertTaxesFast(createdUserId, createdUserId, companyId);
			// store connection
			StoresForm store = new StoresForm();
            store.setName(map.get("default_store_name"));
            store.setLang_code(signUpRequest.getLanguage());
            store.setCompany_id(companyId);
            store.setStore_ip("127.0.0.1");
            store.setCrm_secret_key(cntUsers==0?storageService.getSecretKey():"");// first user will get the key from a file, that was created at the end of ERPHY installation
            store.setStore_if_customer_not_found("create_new");
            store.setStore_price_type_regular(prices.get(0));
            store.setStore_price_type_sale(prices.get(1));
            store.setStore_orders_department_id(departmentId);
            store.setStoreDepartments(new ArrayList<>(Arrays.asList(departmentId)));
            store.setStore_default_creator_id(createdUserId);
            store.setStore_days_for_esd(1);
            store.setStore_auto_reserve(false);
            store.setIs_let_sync(true);

//            Long storeId=storeRepository.insertStoreFast(store,createdUserId,createdUserId);

			// базовые категоии товаров и сами товары
			productsRepository.insertProductCategoriesFast(createdUserId, createdUserId, companyId);
			// траты
			expenditureRepository.insertExpendituresFast(createdUserId, createdUserId, companyId);
			// статусы документов
			statusDocRepository.insertStatusesFast(createdUserId, createdUserId, companyId);
			// базовые аттрибуты товаров (размер, цвет)
//			spravProductAttributes.insertProductAttributeFast(createdUserId, createdUserId, companyId, storeId);
			// Должности / Job titles
			spravJobtitleRepository.createJobtitlesFast(createdUserId, createdUserId, companyId);;
			// Занести пользователя в контрагенты
//			if(settingsGeneral.isSaas() && !Objects.isNull(settingsGeneral.getBilling_cagents_category_id()))
//				userRepositoryJPA.setUserAsCagent(createdUserId, signUpRequest.getName(), signUpRequest.getEmail(), settingsGeneral, cryptoPassword);
			// отправили письмо для подтверждения e-mail (кроме самого первого зарегистрированного - ему подтверждение не надо, т.к. почта не настроена и он может не получить емайл)
			// sending email message for email validation (but the first registred user - he do not need to be validated by email as the email server may be not set up correctly)
			if(cntUsers>0)
				mailRepository.activateAccount(signUpRequest.getEmail(), user.getActivationCode(),signUpRequest.getLanguage());

			// if it is not a SaaS mode - after the registration of first user need to disallow other registrations
			if(!settingsGeneral.isSaas()&& cntUsers==0)
				cu.setLetToRegisterNewUsers(false);
			// if it is a SaaS mode and need to create support user
			if(settingsGeneral.isSaas() && settingsGeneral.isCreate_support_user())
				createUserOfSupportFast(createdUserId, userCompany, userDepartments, usergroupId, settingsGeneral.getStores_alert_email(), signUpRequest.getEmail(), "en",adminRole);

			return new ResponseEntity<>(String.valueOf(createdUserId), HttpStatus.OK);
		} catch (Exception e){
			e.printStackTrace();logger.error("Controller registerUser error", e);
			return new ResponseEntity<>("User registration error", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

//	String getCryptoPassword(Long createdUserId){
//		try{
//			return cryptoService.getCryptoPasswordFromDatabase(createdUserId);
//		} catch (Exception e) {
//			logger.error("Exception in method getCryptoPassword. MasterId = "+createdUserId, e);
//			e.printStackTrace();
//			return null;
//		}
//	}

	public Set<Long> getAdminPermissions(){
		return new HashSet<>(Arrays.asList(
				183L,185L,189L,191L,187L,612L,704L,706L,709L,713L,717L,721L,653L,655L,659L,661L,657L,724L,725L,726L,/*559L,561L,*/ 18L,  /*3L,*/  5L,  7L,
				539L,541L,545L,547L,543L,549L,126L,130L,134L,136L,132L,138L,140L,142L,475L,477L,481L,483L,479L,485L,644L,646L,650L,652L,648L,
				279L,281L,288L,292L,284L,401L,324L,601L,603L,605L,595L,597L,599L,326L,593L,607L,610L,517L,519L,523L,525L,521L,527L, 17L, 11L, 13L, 15L, 12L,
				268L,272L,276L,278L,274L,701L,702L,703L,497L,499L,503L,505L,501L,143L,147L,151L,153L,149L,178L,182L,180L,155L,734L,157L,159L,231L,236L,233L,
				464L,466L,470L,472L,468L,474L,328L,330L,337L,341L,333L,632L,404L,406L,413L,417L,409L,421L,/*486L,488L,492L,494L,490L,496L,*/692L,694L,698L,700L,696L,
				586L,588L,376L,378L,385L,389L,381L,393L,583L,585L,671L,673L,677L,679L,675L,506L,508L,512L,514L,510L,516L,589L,591L,199L,201L,208L,212L,204L,628L,
				 90L, 93L, 94L, 96L, 98L,238L,240L,243L,662L,664L,668L,670L,666L,160L,164L,168L,170L,166L,172L,174L,186L,424L,426L,433L,437L,429L,441L,
				/*528L,530L,534L,536L,532L,538L,*/683L,685L,689L,691L,687L,360L,362L,369L,373L,365L,616L, 28L, 31L, 29L, 34L, 32L,252L,254L,261L,265L,257L,397L,
				680L,681L,682L,444L,446L,453L,457L,449L,461L,635L,637L,641L,643L,639L,117L,120L,123L,125L,121L, 19L, 22L, 23L, 24L, 26L,
				215L,217L,224L,228L,220L,624L,550L,552L,556L,558L,554L,344L,346L,353L,357L,349L,620L,176L

// Old list with "... all companies"
//				6L,8L, 3L, 4L, 18L, 19L, 25L, 27L, 22L, 23L, 26L, 17L, 14L, 16L, 11L, 12L, 28L, 29L, 34L, 31L, 32L, 90L, 93L, 94L, 95L, 97L, 117L,
//				120L, 131L, 122L, 124L, 126L, 129L, 131L, 133L, 135L, 137L, 138L, 139L, 140L, 141L, 142L, 143L, 146L, 148L, 150L, 152L, 154L, 156L, 158L, 177L, 179L, 181L, 160L, 163L, 165L, 167L, 169L, 171L, 173L, 175L,
//				183L, 188L, 190L, 184L, 186L, 611L, 199L, 200L, 203L, 207L, 211L, 627L, 215L, 216L, 219L, 223L, 227L, 623L, 231L, 232L, 235L, 252L, 253L, 256L, 260L, 264L, 396L, 268L, 271L, 273L, 275L, 277L, 279L, 280L,
//				283L, 287L, 291L, 400L, 309L, 312L, 316L, 320L, 296L, 299L, 302L, 305L, 324L, 325L, 592L, 594L, 596L, 598L, 600L, 602L, 604L, 606L, 609L, 328L, 329L, 332L, 336L, 340L, 631L, 344L, 345L, 348L,
//				352L, 356L, 619L, 360L, 361L, 364L, 368L, 372L, 615L, 376L, 377L, 380L, 384L, 388L, 392L, 404L, 405L, 408L, 412L, 416L, 420L, 424L, 425L, 428L, 432L, 436L, 440L, 444L, 445L, 448L, 452L, 456L, 460L, 464L,
//				465L, 467L, 469L, 471L, 473L, 475L, 476L, 478L, 480L, 482L, 484L, 486L, 487L, 489L, 491L, 493L, 495L, 497L, 498L, 500L, 502L, 504L, 506L, 507L, 509L, 511L, 513L, 515L, 517L, 518L, 520L, 522L, 524L, 526L,
//				528L, 529L, 531L, 533L, 535L, 537L, 539L, 540L, 542L, 544L, 546L, 548L, 550L, 551L, 553L, 555L, 557L, 560L, 563L, 568L, 571L, 576L, 579L, 583L, 584L, 586L, 587L, 589L, 590L, 635L,
//				636L, 638L, 640L, 642L, 238L, 239L, 242L, 112L, 644L, 645L, 647L, 649L, 651L, 653L, 654L, 656L, 658L, 660L, 662L, 663L, 665L, 667L, 669L, 671L, 672L, 674L, 676L, 678L, 680L, 681L, 682L, 683L, 684L, 686L,
//				688L, 690L, 683L, 684L, 686L, 688L, 690L, 692L, 693L, 695L, 697L, 699L, 701L, 702L, 703L, 704L, 705L, 708L, 712L, 716L, 720L, 724L, 725L, 726L

		));
	}
// 308 559 562 295 575 567 - All Retail sales "Display in the list of documents in the sidebar"

	private void createUserOfSupportFast(Long mId, Companies company, Set<Departments> userDepartments, Long usergroupId, String adminEmail, String createdUserEmail, String language, Role adminRole) throws Exception {
		try {
			String supportUserName = "support_"+mId.toString();
			String userPassword= UUID.randomUUID().toString();
			User user = new User("Support user", supportUserName, "support_user_of@account_with_id."+mId, encoder.encode(userPassword));
			Set<Role> roles = new HashSet<>();
			roles.add(adminRole);
			user.setDate_time_created(new Timestamp(System.currentTimeMillis()));
			//добавили юзеру сет ролей и сохранили его
			user.setRoles(roles);
			user.setStatus_account(2); //2: email verified.
			user.setMaster(userRepository.findById(mId).orElseThrow(
					() -> new UsernameNotFoundException("User Not Found with -> id : " + mId.toString())));
			user.setCompany(company);
			user.setDepartments(userDepartments);
			Long createdUserId = userRepository.save(user).getId();// и сохранили его
			userGroupRepository.addUserToUserGroup(createdUserId,usergroupId);
			Map<String, String> map = cu.translateForUser(mId, new String[]{"'acc_tech_support'"});
			user.setName(map.get("acc_tech_support"));
			user.setFio_name(map.get("acc_tech_support"));
			user.setMaster(userDetailsService.getUserById(mId));
			userRepository.save(user);// сохраняем чтобы записался masterId и новое имя
			// уcтановим пользователю часовой пояс (timeZone), язык и локаль
			userRepositoryJPA.setUserSettings(createdUserId, 24, userRepositoryJPA.getLangIdBySuffix(language), language.equals("ru") ? 10 : 4, "24");
			userRepository.save(user);// сохраняем чтобы применился язык
			mailRepository.newSaasUserRegistered(adminEmail, createdUserEmail, supportUserName, userPassword, language);
		} catch (Exception e) {
			logger.error("Exception in method createUserOfSupportFast. MasterId = "+mId, e);
			e.printStackTrace();
			throw new Exception();
		}
	}
}