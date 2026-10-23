package com.example.amportoes;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.*;

class AmPortoesApplicationTest {

    @Test
    void mainDeveIniciarAplicacaoSpringBoot() {
        try (var springApplication = mockStatic(SpringApplication.class)) {
            AmPortoesApplication.main(new String[]{});

            springApplication.verify(() ->
                    SpringApplication.run(AmPortoesApplication.class, new String[]{})
            );
        }
    }
}