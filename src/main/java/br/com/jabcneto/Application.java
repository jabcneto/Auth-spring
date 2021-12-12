package br.com.jabcneto;

import br.com.jabcneto.entity.Role;
import br.com.jabcneto.entity.User;
import br.com.jabcneto.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    CommandLineRunner run(UserService userService){
        return args -> {
            userService.saveRole(new Role("ROLE_USER"));
            userService.saveRole(new Role("ROLE_ADMIN"));

            userService.saveUser(new User("John Doe", "john", "1234"));
            userService.saveUser(new User("John Doe", "john2", "1234"));
            userService.addRoleToUser("john", "ROLE_USER");
            userService.addRoleToUser("john2", "ROLE_ADMIN");
        };
    }

}
