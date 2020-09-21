package com.sixhands;

import com.sixhands.domain.User;
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
    @Value("${6hands.send-mail}")
    public void setsSendMailStatic(String require){ sSendMail = require; }
    private static String sSendMail;

    public static boolean isSendingMail(){ return !StringUtils.isEmpty(sSendMail) && sSendMail.equalsIgnoreCase("true"); }

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
            user.setEmail("test@sixhands.dev");
            user.setRole("ROLE_USER");
            user.setPassword(encoder.encode("123"));
            userRepo.save(user);
            System.out.println("ADDED TEST USER");
        }
    }
}