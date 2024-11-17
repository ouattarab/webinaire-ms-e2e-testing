package net.bakary.customerservice.repository;

import net.bakary.customerservice.entities.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.assertj.core.api.Assertions;

import java.util.Optional;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE) // Utiliser la base de données MySQL configurée
public class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    public void setUp(){
        customerRepository.save(Customer.builder().firstName("Ouattara")
                .lastName("Mohamed").email("mohamed@gmail.com").build());

        customerRepository.save(Customer.builder().firstName("Ouattara")
                .lastName("Bakary").email("jobdebakary@gmail.com").build());

        customerRepository.save(Customer.builder().firstName("Ouattara")
                .lastName("Tata").email("tata@gmail.com").build());
    }

    /** @Test
    public void shouldFindCustomerByFirstEmail(){
        String givenEmail = "jobdebakary@gmail.com";
        Optional<Customer> result = customerRepository.findByEmail(givenEmail);

        Assertions.assertThat(result).isPresent();
        Assertions.assertThat(result.get().getEmail()).isEqualTo(givenEmail);
    } */
}
