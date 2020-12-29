package com.ylizma.keyclockdemo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.keycloak.adapters.AdapterDeploymentContext;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.spi.HttpFacade;
import org.keycloak.adapters.springsecurity.facade.SimpleHttpFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@SpringBootApplication
public class KeyclockDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(KeyclockDemoApplication.class, args);
    }

    @Bean
    CommandLineRunner start(ProductRepository repository) {
        return args -> {
            Product p = new Product(null, "television", 200.0);
            Product p2 = new Product(null, "computer", 5000.0);
            Product p3 = new Product(null, "laptop", 10000.0);
            repository.save(p);
            repository.save(p2);
            repository.save(p3);
        };
    }
}


@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String label;
    private Double price;
}

@Controller
class ProductController {

    final ProductRepository productRepository;
    final AdapterDeploymentContext adapter;

    public ProductController(ProductRepository productRepository, AdapterDeploymentContext adapter) {
        this.productRepository = productRepository;
        this.adapter = adapter;
    }

    @GetMapping("/")
    public String index(Model model) {
        return "index";
    }

    @GetMapping("/products")
    public String products(Model model) {
        model.addAttribute("products", productRepository.findAll());
        return "products";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) throws ServletException {
        request.logout();
        return "redirect:/";
    }

    @GetMapping("/changepassword")
    public String changePassword(HttpServletRequest request, HttpServletResponse response, RedirectAttributes attributes) throws ServletException {
        HttpFacade httpFacade = new SimpleHttpFacade(request, response);
        KeycloakDeployment keycloakDeployment = adapter.resolveDeployment(httpFacade);
        attributes.addAttribute("referrer", keycloakDeployment.getResourceName());
        return "redirect:" + keycloakDeployment.getAccountUrl() + "/password";
    }
}

@Repository
interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAll();
}
