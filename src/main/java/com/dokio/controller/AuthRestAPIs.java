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

import java.util.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.validation.Valid;

import com.dokio.message.request.CompaniesForm;
import com.dokio.message.request.DepartmentForm;
import com.dokio.message.response.Settings.SettingsGeneralJSON;
import com.dokio.message.response.additional.BaseFiles;
import com.dokio.model.*;
import com.dokio.repository.*;
import com.dokio.security.services.UserDetailsServiceImpl;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
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

		Set<String> strRoles= new HashSet<>();
		strRoles.add("admin"); // это "системная" роль, для спринга. Ею наделяются все пользователи. Все их реальные права регулируются Докио
		Set<Role> roles = new HashSet<>();
        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN).orElseThrow(() -> new RuntimeException("Fail! -> Cause: User Role not find."));
        roles.add(adminRole);

		//добавили юзеру сет ролей и сохранили его
		user.setRoles(roles);
		user.setStatus_account(1); //Статус 1 - e-mail не верифицирован
        user.setActivationCode(UUID.randomUUID().toString()); // Код активации, высылаемый на e-mail
		SettingsGeneralJSON settingsGeneral = cu.getSettingsGeneral(); // чтобы узнать тарифный план по умолчанию
		user.setPlanId(settingsGeneral.getPlanDefaultId());
		user.setPlanPrice(settingsGeneral.getPlanPrice());
		user.setFreeTrialDays(settingsGeneral.getFreeTrialDays());
		Long createdUserId=userRepository.save(user).getId();// и сохранили его
		user.setMaster(userDetailsService.getUserById(createdUserId));// в качестве мастера устанавливаем его же
		userRepository.save(user);// сохраняем чтобы записался master id
		// уcтановим пользователю часовой пояс (timeZone), язык и локаль
		userRepositoryJPA.setUserSettings(createdUserId,24, userRepositoryJPA.getLangIdBySuffix(signUpRequest.getLanguage()), signUpRequest.getLanguage().equals("ru")?10:4, "24");
		userRepository.save(user);// сохраняем чтобы применился язык
		Map<String, String> map = cu.translateForUser(createdUserId, new String[]{"'my_company'","'my_department'","'role_admins'"});
		// set plan options with current prices to master user
		userRepositoryJPA.createMasterUserPlanOptions(createdUserId, settingsGeneral.getPlanDefaultId());
		// создадим пользователю предприятие
		CompaniesForm company = new CompaniesForm();
		company.setName(map.get("my_company"));
		company.setSt_prefix_barcode_packed(20);
		company.setSt_prefix_barcode_pieced(21);
		company.setSt_netcost_policy("all");
		company.setStore_default_lang_code(signUpRequest.getLanguage());
		Long companyId = companyRepositoryJPA.insertCompanyFast(company,createdUserId);
        // типы цен
        Long price = typePricesRepository.insertPriceTypesFast(createdUserId,companyId);
        // касса предприятия (денежная комната)
        Long bo = boxofficeRepository.insertBoxofficesFast(createdUserId,companyId);
		// расчетный счет предприятия
		Long ac = paymentAccountsRepository.insertPaymentAccountsFast(createdUserId,companyId);
		// создадим пользователю отделение
		DepartmentForm department = new DepartmentForm();
		department.setName(map.get("my_department"));
        department.setPrice_id(price);
        department.setBoxoffice_id(bo);
		department.setPayment_account_id(ac);
		Long departmentId = departmentRepositoryJPA.insertDepartmentFast(department,companyId,createdUserId);
        Companies userCompany = emgr.find(Companies.class, companyId);
        Set<Long> userDepartmentsIds = new HashSet<>(Arrays.asList(departmentId));
        Set<Departments> userDepartments = departmentRepositoryJPA.getDepartmentsSetBySetOfDepartmentsId(userDepartmentsIds);
		user.setCompany(userCompany);
		user.setDepartments(userDepartments);
        userRepository.saveAndFlush(user);
		// создадим пользователю Роль (группу пользователей)
		Long usergroupId = userGroupRepository.insertUsergroupFast(map.get("role_admins"),companyId,createdUserId);
		Set<Long> permissions = getAdminPermissions();
		userGroupRepository.setPermissionsToUserGroup(permissions,usergroupId);
        // зададим пользователю набор валют
        currenciesRepository.insertCurrenciesFast(createdUserId,createdUserId,companyId);
        // базовые категоии контрагентов и сами контрагенты
        cagentRepository.insertCagentCategoriesFast(createdUserId,createdUserId,companyId);
		// базовые категоии файлов + базовые файлы (шаблоны)
		Long templateCategoryId = fileRepository.insertFileCategoriesFast(createdUserId,createdUserId,companyId);
		// now need to put base files into this category in accordance of user language
		List<BaseFiles> baseFilesList = fileRepository.insertBaseFilesFast(createdUserId, createdUserId, companyId, templateCategoryId);
		// forming print menu for documents
		if(!Objects.isNull(baseFilesList)) documentsRepository.createPrintMenus(baseFilesList,createdUserId,createdUserId,companyId);
        // единицы имерения
        spravSysEdizm.insertEdizmFast(createdUserId,createdUserId,companyId);
		// налоги
		taxesRepository.insertTaxesFast(createdUserId,createdUserId,companyId);
		// базовые категоии товаров и сами товары
		productsRepository.insertProductCategoriesFast(createdUserId,createdUserId,companyId);
        // траты
        expenditureRepository.insertExpendituresFast(createdUserId,createdUserId,companyId);
        // статусы документов
		statusDocRepository.insertStatusesFast(createdUserId,createdUserId,companyId);
		// базовые аттрибуты товаров (размер, цвет)
		spravProductAttributes.insertProductAttributeFast(createdUserId,createdUserId,companyId);
        // отправили письмо для подтверждения e-mail
        mailRepository.activateAccount(signUpRequest.getEmail(),user.getActivationCode());

		return new ResponseEntity<>(String.valueOf(createdUserId), HttpStatus.OK);
	}

	public Set<Long> getAdminPermissions(){
		return new HashSet<>(Arrays.asList(6L,8L, 3L, 4L, 18L, 19L, 25L, 27L, 22L, 23L, 26L, 17L, 14L, 16L, 11L, 12L, 28L, 29L, 34L, 31L, 32L, 90L, 93L, 94L, 95L, 97L, 117L,
				120L, 131L, 122L, 124L, 126L, 129L, 131L, 133L, 135L, 137L, 138L, 139L, 140L, 141L, 142L, 143L, 146L, 148L, 150L, 152L, 154L, 156L, 158L, 177L, 179L, 181L, 160L, 163L, 165L, 167L, 169L, 171L, 173L, 175L,
				183L, 188L, 190L, 184L, 186L, 611L, 199L, 200L, 203L, 207L, 211L, 627L, 215L, 216L, 219L, 223L, 227L, 623L, 231L, 232L, 235L, 252L, 253L, 256L, 260L, 264L, 396L, 268L, 271L, 273L, 275L, 277L, 279L, 280L,
				283L, 287L, 291L, 400L, 309L, 312L, 316L, 320L, 296L, 299L, 302L, 305L, 324L, 325L, 592L, 594L, 596L, 598L, 600L, 602L, 604L, 606L, 609L, 328L, 329L, 332L, 336L, 340L, 631L, 344L, 345L, 348L,
				352L, 356L, 619L, 360L, 361L, 364L, 368L, 372L, 615L, 376L, 377L, 380L, 384L, 388L, 392L, 404L, 405L, 408L, 412L, 416L, 420L, 424L, 425L, 428L, 432L, 436L, 440L, 444L, 445L, 448L, 452L, 456L, 460L, 464L,
				465L, 467L, 469L, 471L, 473L, 475L, 476L, 478L, 480L, 482L, 484L, 486L, 487L, 489L, 491L, 493L, 495L, 497L, 498L, 500L, 502L, 504L, 506L, 507L, 509L, 511L, 513L, 515L, 517L, 518L, 520L, 522L, 524L, 526L,
				528L, 529L, 531L, 533L, 535L, 537L, 539L, 540L, 542L, 544L, 546L, 548L, 550L, 551L, 553L, 555L, 557L, 560L, 563L, 568L, 571L, 576L, 579L, 583L, 584L, 586L, 587L, 589L, 590L, 635L,
				636L, 638L, 640L, 642L, 238L, 239L, 242L, 112L, 644L, 645L, 647L, 649L, 651L, 653L, 654L, 656L, 658L, 660L, 662L, 663L, 665L, 667L, 669L, 671L, 672L, 674L, 676L, 678L, 680L, 681L, 682L));
	}
// 308 559 562 295 575 567 - All Retail sales "Display in the list of documents in the sidebar"
}