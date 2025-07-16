package tn.esprit.spring;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tn.esprit.spring.DAO.Entities.Bloc;
import tn.esprit.spring.Services.Bloc.BlocService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BlocServiceTest {

    @Autowired
    BlocService blocService;

    @Order(1)
    @Test
    void testAddOrUpdate() {
        Bloc bloc = new Bloc();
        bloc.setNomBloc("IntegrationTestBloc");

        Bloc result = blocService.addOrUpdate(bloc);

        assertNotNull(result);
        assertEquals("IntegrationTestBloc", result.getNomBloc());
    }

    @Order(2)
    @Test
    void testFindAll() {
        List<Bloc> blocs = blocService.findAll();

        assertNotNull(blocs);
        assertTrue(blocs.size() > 0);
    }

    @Order(3)
    @Test
    void testFindById() {
        Bloc bloc = blocService.findById(1L);

        assertNotNull(bloc);
        assertEquals(1L, bloc.getIdBloc());
    }

    @Order(4)
    @Test
    void testDeleteById() {
        blocService.deleteById(1L);

        assertThrows(Exception.class, () -> blocService.findById(1L));
    }
}
