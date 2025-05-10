package bank.testovoe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories("bank.testovoe.repository")
public class TestovoeApplication {

	public static void main(String[] args) {
		SpringApplication.run(TestovoeApplication.class, args);
	}

}
