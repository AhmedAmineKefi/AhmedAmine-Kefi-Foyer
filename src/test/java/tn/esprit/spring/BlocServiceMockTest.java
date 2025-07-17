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

import java.util.Arrays;
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

    @Test
    @Order(1)
    public void testAddOrUpdate() {
        Bloc mockBloc = new Bloc();
        mockBloc.setIdBloc(1L);
        mockBloc.setNomBloc("Test Bloc");

        when(blocRepository.save(mockBloc)).thenReturn(mockBloc);

        Bloc result = blocService.addOrUpdate(mockBloc);

        assertNotNull(result);
        assertEquals("Test Bloc", result.getNomBloc());
        verify(blocRepository, times(1)).save(mockBloc);
    }

    @Test
    @Order(2)
    public void testFindAll() {
        Bloc mockBloc1 = new Bloc();
        mockBloc1.setIdBloc(1L);
        mockBloc1.setNomBloc("Bloc 1");

        Bloc mockBloc2 = new Bloc();
        mockBloc2.setIdBloc(2L);
        mockBloc2.setNomBloc("Bloc 2");

        when(blocRepository.findAll()).thenReturn(Arrays.asList(mockBloc1, mockBloc2));

        var result = blocService.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(blocRepository, times(1)).findAll();
    }

    @Test
    @Order(3)
    public void testFindById() {
        Bloc mockBloc = new Bloc();
        mockBloc.setIdBloc(1L);
        mockBloc.setNomBloc("Test Bloc");

        when(blocRepository.findById(1L)).thenReturn(Optional.of(mockBloc));

        Bloc result = blocService.findById(1L);

        assertNotNull(result);
        assertEquals("Test Bloc", result.getNomBloc());
        verify(blocRepository, times(1)).findById(1L);
    }

    @Test
    @Order(4)
    public void testDeleteById() {
        Bloc mockBloc = new Bloc();
        mockBloc.setIdBloc(1L);

        when(blocRepository.findById(1L)).thenReturn(Optional.of(mockBloc));

        blocService.deleteById(1L);

        verify(chambreRepository, times(1)).deleteAll(mockBloc.getChambres());
        verify(blocRepository, times(1)).delete(mockBloc);
    }
}
