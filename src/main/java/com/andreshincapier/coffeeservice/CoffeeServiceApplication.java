package com.andreshincapier.coffeeservice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@SpringBootApplication
public class CoffeeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoffeeServiceApplication.class, args);
    }
}

@RestController
@RequestMapping("/coffees")
class CoffeeController {
    private final CoffeeService service;


    CoffeeController(CoffeeService service) {
        this.service = service;
    }

    @GetMapping
    public Flux<Coffee> all() {
        return service.getAllCoffees();
    }

    @GetMapping("/{id}")
    public Mono<Coffee> byId(@PathVariable String id) {
        return service.getCoffeeById(id);
    }

    @GetMapping(value = "/{id}/orders", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<CoffeeOrder> orders(@PathVariable String id) {
        return service.getOrders(id);
    }
}

@Service
class CoffeeService {
    private final CoffeeRepository repository;

    CoffeeService(CoffeeRepository repository) {
        this.repository = repository;
    }

    Flux<Coffee> getAllCoffees() {
        return repository.findAll();
    }

    Mono<Coffee> getCoffeeById(String id) {
        return repository.findById(id);
    }

    Flux<CoffeeOrder> getOrders(String coffeeId) {
        return Flux.<CoffeeOrder>generate(sink -> sink.next(new CoffeeOrder(coffeeId, Instant.now())))
                .delayElements(Duration.ofSeconds(NumberUtils.INTEGER_ONE));
    }
}

@Component
class DataLoader {
    private final CoffeeRepository repository;

    DataLoader(CoffeeRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    private void load() {
        repository.deleteAll().thenMany(
                        Flux.just("Americano", "Esmeralda", "Kadis coffee", "Café Olé", "Delta", "Java")
                                .map(name -> new Coffee(UUID.randomUUID().toString(), name))
                                .flatMap(repository::save))
                .thenMany(repository.findAll())
                .subscribe(System.out::println);
    }
}

interface CoffeeRepository extends ReactiveCrudRepository<Coffee, String> {
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class CoffeeOrder {
    private String coffeeId;
    private Instant dateOrdered;
}

@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
class Coffee {
    @Id
    private String id;
    private String name;
}