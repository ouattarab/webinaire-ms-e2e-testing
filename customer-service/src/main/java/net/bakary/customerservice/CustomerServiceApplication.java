package net.bakary.customerservice;

import net.bakary.customerservice.entities.Customer;
import net.bakary.customerservice.repository.CustomerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CustomerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomerServiceApplication.class, args);
    }
    @Bean
    CommandLineRunner commandLineRunner(CustomerRepository customerRepository) {
        return args -> {
            customerRepository.save(Customer.builder().firstName("Ouattara")
                    .lastName("Mohamed").email("mohamed@gmail.com").build());

            customerRepository.save(Customer.builder().firstName("Ouattara")
                    .lastName("Bakary").email("jobdebakary@gmail.com").build());

            customerRepository.save(Customer.builder().firstName("Ouattara")
                    .lastName("Tata").email("tata@gmail.com").build());
        };



    }

}
