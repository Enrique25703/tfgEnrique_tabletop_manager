package org.example.tfgenrique;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "TFG_DB_PASSWORD", matches = ".+")
class TfgEnriqueApplicationTests {

    /** Requires a MySQL schema initialized from sql/CopiaSeguridadBaseDatos.sql. */
    @Test
    void contextLoads() {
    }

}
