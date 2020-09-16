package com.sixhands;

import com.sixhands.controller.dtos.ProjectDTO;
import com.sixhands.controller.dtos.UserAndExpDTO;
import com.sixhands.domain.Project;
import com.sixhands.domain.User;
import com.sixhands.misc.GenericUtils;
import com.sixhands.repository.ProjectRepository;
import com.sixhands.repository.UserRepository;
import com.sixhands.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.thymeleaf.templateresolver.FileTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

@Transactional
@SpringBootApplication
public class SixHandsApplication implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(SixHandsApplication.class, args);
    }

    @Autowired
    private ThymeleafProperties properties;

    @Value("${spring.thymeleaf.templates_root:}")
    private String templatesRoot;

    @Bean
    public ITemplateResolver defaultTemplateResolver() {
        FileTemplateResolver resolver = new FileTemplateResolver();
        resolver.setSuffix(properties.getSuffix());
        resolver.setCharacterEncoding("UTF-8");
        resolver.setPrefix(templatesRoot);
        resolver.setTemplateMode(properties.getMode());
        resolver.setCacheable(properties.isCache());
        return resolver;
    }
    @Value("${6hands.require-verification}")
    public void setsRequireVerificationStatic(String require){ sRequireVerification = require; }
    private static String sRequireVerification;

    public static boolean requireVerification(){ return !StringUtils.isEmpty(sRequireVerification) && sRequireVerification.equalsIgnoreCase("true"); }

    @Value("${6hands.create-test-user}")
    private String createTestUser;

    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private ProjectRepository projectRepo;
    @Autowired
    private ProjectService projectService;
    @Override
    public void run(String... args) throws Exception {
        if(userRepo.count() == 0 && createTestUser.equalsIgnoreCase("true")){
            User user = new User();
            user.setFirst_name("Peter");
            user.setLast_name("Parker");
/*
test@sixhands.dev

*/
            user.setEmail("test@sixhands.dev");
            user.setRole("ROLE_USER");
            user.setPassword(encoder.encode("123"));
            userRepo.save(user);
            System.out.println("ADDED TEST USER");
        }
        if(projectRepo.count() > 0 && userRepo.count() > 0){
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < projectRepo.count(); i++) {
                Project project = GenericUtils.initializeAndUnproxy( projectRepo.findAll().get(i) );
                UserAndExpDTO[] userAndExpDTO = GenericUtils.initializeAndUnproxy( projectService.projectExpByProject(project) );
                ProjectDTO projectDTO = new ProjectDTO();
                projectDTO.setProject(project);
                projectDTO.setMembers(userAndExpDTO);
                stringBuilder.append(projectDTO.toCsvDto().toString(i == 0)).append(i==projectRepo.count()-1?"":"\n");
            }
            System.out.println("====PROJECTS START====");
            System.out.println(stringBuilder);
            System.out.println("=====PROJECTS END=====");

            stringBuilder = new StringBuilder();
            for (int i = 0; i < userRepo.count(); i++) {
                User user = GenericUtils.initializeAndUnproxy( userRepo.findAll().get(i) );
                if(i==0) stringBuilder.append( String.join(",", user.toCSV().keySet()) ).append("\n");
                stringBuilder.append( String.join(",", user.toCSV().values()) ).append(i==userRepo.count()-1?"":"\n");
            }
            System.out.println("====USERS START====");
            System.out.println(stringBuilder);
            System.out.println("=====USERS END=====");
        }
    }
}