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
package com.dokio.controller;

import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.validation.Valid;

import com.dokio.message.request.CompaniesForm;
import com.dokio.message.request.DepartmentForm;
import com.dokio.model.*;
import com.dokio.repository.*;
import com.dokio.security.services.UserDetailsServiceImpl;
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
import org.springframework.web.bind.annotation.*;

import com.dokio.message.request.LoginForm;
import com.dokio.message.request.SignUpForm;
import com.dokio.message.response.JwtResponse;
import com.dokio.message.response.ResponseMessage;
import com.dokio.security.jwt.JwtProvider;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/public")
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
		Long createdUserId=userRepository.save(user).getId();// и сохранили его
		// создадим пользователю предприятие
		CompaniesForm company = new CompaniesForm();
		company.setName("My company");
		Long companyId = companyRepositoryJPA.insertCompanyFast(company,createdUserId);
        // типы цен
        Long price = typePricesRepository.insertPriceTypesFast(createdUserId,companyId);
        // кассы предприятия (денежные комнаты)
        Long bo = boxofficeRepository.insertBoxofficesFast(createdUserId,companyId);
		// создадим пользователю отделение
		DepartmentForm department = new DepartmentForm();
		department.setName("My department");
        department.setPrice_id(price);
        department.setBoxoffice_id(bo);
		Long departmentId = departmentRepositoryJPA.insertDepartmentFast(department,companyId,createdUserId);
        Companies userCompany = emgr.find(Companies.class, companyId);
        Set<Long> userDepartmentsIds = new HashSet<>(Arrays.asList(departmentId));
        Set<Departments> userDepartments = departmentRepositoryJPA.getDepartmentsSetBySetOfDepartmentsId(userDepartmentsIds);
		user.setCompany(userCompany);
		user.setDepartments(userDepartments);
		user.setMaster(user);
        userRepository.saveAndFlush(user);
		// создадим пользователю Роль (группу пользователей)
		Long usergroupId = userGroupRepository.insertUsergroupFast("Administrators",companyId,createdUserId);
		Set<Long> permissions = new HashSet<>(Arrays.asList(6L,8L, 3L, 4L, 18L, 19L, 25L, 27L, 22L, 23L, 26L, 17L, 14L, 16L, 11L, 12L, 28L, 29L, 34L, 31L, 32L, 90L, 93L, 94L, 95L, 97L, 108L, 111L, 113L, 113L, 115L, 117L,
			120L, 131L, 122L, 124L, 126L, 129L, 131L, 133L, 135L, 137L, 138L, 139L, 140L, 141L, 142L, 143L, 146L, 148L, 150L, 152L, 154L, 156L, 158L, 177L, 179L, 181L, 160L, 163L, 165L, 167L, 169L, 171L, 173L, 175L,
			183L, 188L, 190L, 184L, 186L, 611L, 199L, 200L, 203L, 207L, 211L, 627L, 215L, 216L, 219L, 223L, 227L, 623L, 231L, 232L, 235L, 252L, 253L, 256L, 260L, 264L, 396L, 268L, 271L, 273L, 275L, 277L, 279L, 280L,
			283L, 287L, 291L, 400L, 308L, 309L, 312L, 316L, 320L, 295L, 296L, 299L, 302L, 305L, 324L, 325L, 592L, 594L, 596L, 598L, 600L, 602L, 604L, 606L, 609L, 328L, 329L, 332L, 336L, 340L, 631L, 344L, 345L, 348L,
			352L, 356L, 619L, 360L, 361L, 364L, 368L, 372L, 615L, 376L, 377L, 380L, 384L, 388L, 392L, 404L, 405L, 408L, 412L, 416L, 420L, 424L, 425L, 428L, 432L, 436L, 440L, 444L, 445L, 448L, 452L, 456L, 460L, 464L,
			465L, 467L, 469L, 471L, 473L, 475L, 476L, 478L, 480L, 482L, 484L, 486L, 487L, 489L, 491L, 493L, 495L, 497L, 498L, 500L, 502L, 504L, 506L, 507L, 509L, 511L, 513L, 515L, 517L, 518L, 520L, 522L, 524L, 526L,
			528L, 529L, 531L, 533L, 535L, 537L, 539L, 540L, 542L, 544L, 546L, 548L, 550L, 551L, 553L, 555L, 557L, 559L, 560L, 562L, 563L, 567L, 568L, 571L, 575L, 576L, 579L, 583L, 584L, 586L, 587L, 589L, 590L, 635L,
			636L, 638L, 640L, 642L, 238L, 239L, 242L, 112L, 644L, 645L, 647L, 649L, 651L));
		userGroupRepository.setPermissionsToUserGroup(permissions,usergroupId);
        // уcтановим пользователю часовой пояс (timeZone), язык и локаль
        userRepositoryJPA.setUserSettings(createdUserId,24, userRepositoryJPA.getLangIdBySuffix(signUpRequest.getLanguage()), 4);
        // зададим пользователю набор валют
        currenciesRepository.insertCurrenciesFast(createdUserId,companyId);
        // базовые категоии контрагентов
        cagentRepository.insertCagentCategoriesFast(createdUserId,companyId);
        // единицы имерения
        spravSysEdizm.insertEdizmFast(createdUserId,companyId);
        // налоги
        taxesRepository.insertTaxesFast(createdUserId,companyId);
        // траты
        expenditureRepository.insertExpendituresFast(createdUserId,companyId);
        // отправили письмо для подтверждения e-mail
        mailRepository.activateAccount(signUpRequest.getEmail(),user.getActivationCode());

		return new ResponseEntity<>(String.valueOf(createdUserId), HttpStatus.OK);
	}
}