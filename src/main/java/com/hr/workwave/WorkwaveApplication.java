package com.hr.workwave;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
public class WorkwaveApplication {


	public static void main(String[] args) {
		SpringApplication.run(WorkwaveApplication.class, args);
	}
}

//	@Autowired
//	private EmailService emailService;
//
//	public WorkwaveApplication(EmailService emailService) {
//		this.emailService = emailService;
//	}

//public static void main(String[] args) {
//	ApplicationContext context = SpringApplication.run(WorkwaveApplication.class, args);

//		WorkwaveApplication app = context.getBean(WorkwaveApplication.class);
//
//		app.run();
//}
//
//	private void run() {
//		emailService.sendEmail("anymoreEmail@gmail.com", "Leave Request Cancel", "Leave Request Cancel Email");
//	}


