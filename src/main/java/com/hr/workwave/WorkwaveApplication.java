package com.hr.workwave;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import repo.LeaveRequestRepository;

@SpringBootApplication
@RequiredArgsConstructor
public class WorkwaveApplication {

	public static void main(String[] args) {
		SpringApplication.run(WorkwaveApplication.class, args);
	}

	private final LeaveRequestRepository leaveRequestRepository;

	@RestController
	public class TestController {
		@GetMapping
				("/test")
		public String test() {
			System.out.println(leaveRequestRepository.findAll().toString());
			return "Hello, World!";
		}
	}

}
