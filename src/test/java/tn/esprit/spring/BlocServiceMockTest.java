package tn.esprit.spring;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.spring.DAO.Entities.Bloc;
import tn.esprit.spring.DAO.Repositories.BlocRepository;
import tn.esprit.spring.DAO.Repositories.ChambreRepository;
import tn.esprit.spring.DAO.Repositories.FoyerRepository;
import tn.esprit.spring.Services.Bloc.BlocService;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BlocServiceMockTest {

    @Mock
    BlocRepository blocRepository;

    @Mock
    ChambreRepository chambreRepository;

    @Mock
    FoyerRepository foyerRepository;

    @InjectMocks
    BlocService blocService;

    @Order(1)
    @Test
    void testAddOrUpdate() {
        Bloc bloc = new Bloc();
        bloc.setNomBloc("TestBloc");

        when(blocRepository.save(bloc)).thenReturn(bloc);

        Bloc result = blocService.addOrUpdate(bloc);

        assertNotNull(result);
        assertEquals("TestBloc", result.getNomBloc());
        verify(blocRepository, times(1)).save(bloc);
    }

    @Order(2)
    @Test
    void testFindById() {
        Bloc bloc = new Bloc();
        bloc.setIdBloc(1L);

        when(blocRepository.findById(1L)).thenReturn(Optional.of(bloc));

        Bloc result = blocService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getIdBloc());
        verify(blocRepository, times(1)).findById(1L);
    }

    @Order(3)
    @Test
    void testDeleteById() {
        Bloc bloc = new Bloc();
        bloc.setIdBloc(1L);

        when(blocRepository.findById(1L)).thenReturn(Optional.of(bloc));

        blocService.deleteById(1L);

        verify(chambreRepository, times(1)).deleteAll(bloc.getChambres());
        verify(blocRepository, times(1)).delete(bloc);
    }
}
