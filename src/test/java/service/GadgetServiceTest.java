package service;

import com.fixmygadget.dao.GadgetDao;
import com.fixmygadget.exception.NotFoundException;
import com.fixmygadget.model.Feature;
import com.fixmygadget.model.Gadget;
import com.fixmygadget.model.GadgetBuilder;
import com.fixmygadget.model.GadgetType;
import com.fixmygadget.service.GadgetService;
import com.fixmygadget.sort.GadgetSortField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GadgetServiceTest {

    @Mock private GadgetDao gadgetDao;

    private GadgetService service;

    @BeforeEach
    void setUp() {
        service = new GadgetService(gadgetDao);
    }

    private Gadget valid() {
        return new GadgetBuilder().id(1).name("Pixel").manufacturer("Google")
                .price(new BigDecimal("500")).type(GadgetType.SMARTPHONE).screen(1080, 2400).build();
    }



    @Test
    void addGadgetSavesValidGadget() {
        Gadget g = valid();

        service.addGadget(g);

        verify(gadgetDao).save(g);
    }

    @Test
    void addGadgetAllowsZeroPrice() {
        Gadget g = valid();
        g.setPrice(BigDecimal.ZERO);

        service.addGadget(g);

        verify(gadgetDao).save(g);
    }

    @Test
    void addGadgetRejectsBlankName() {
        Gadget g = valid();
        g.setName("  ");
        assertThrows(IllegalArgumentException.class, () -> service.addGadget(g));
        verifyNoInteractions(gadgetDao);
    }

    @Test
    void addGadgetRejectsNullName() {
        Gadget g = valid();
        g.setName(null);
        assertThrows(IllegalArgumentException.class, () -> service.addGadget(g));
        verifyNoInteractions(gadgetDao);
    }

    @Test
    void addGadgetRejectsBlankManufacturer() {
        Gadget g = valid();
        g.setManufacturer("");
        assertThrows(IllegalArgumentException.class, () -> service.addGadget(g));
        verifyNoInteractions(gadgetDao);
    }

    @Test
    void addGadgetRejectsNullType() {
        Gadget g = valid();
        g.setType(null);
        assertThrows(IllegalArgumentException.class, () -> service.addGadget(g));
        verifyNoInteractions(gadgetDao);
    }

    @Test
    void addGadgetRejectsNullPrice() {
        Gadget g = valid();
        g.setPrice(null);
        assertThrows(IllegalArgumentException.class, () -> service.addGadget(g));
        verifyNoInteractions(gadgetDao);
    }

    @Test
    void addGadgetRejectsNegativePrice() {
        Gadget g = valid();
        g.setPrice(new BigDecimal("-0.01"));
        assertThrows(IllegalArgumentException.class, () -> service.addGadget(g));
        verifyNoInteractions(gadgetDao);
    }

    @Test
    void addGadgetRejectsNonPositiveScreenWidth() {
        Gadget g = valid();
        g.setScreenWidth(0);
        assertThrows(IllegalArgumentException.class, () -> service.addGadget(g));
        verifyNoInteractions(gadgetDao);
    }

    @Test
    void addGadgetRejectsNonPositiveScreenHeight() {
        Gadget g = valid();
        g.setScreenHeight(-5);
        assertThrows(IllegalArgumentException.class, () -> service.addGadget(g));
        verifyNoInteractions(gadgetDao);
    }



    @Test
    void updateGadgetUpdatesExisting() {
        Gadget g = valid();
        when(gadgetDao.findById(1)).thenReturn(g);

        service.updateGadget(g);

        verify(gadgetDao).update(g);
    }

    @Test
    void updateGadgetRequiresId() {
        Gadget g = valid();
        g.setId(null);

        assertThrows(IllegalArgumentException.class, () -> service.updateGadget(g));

        verifyNoInteractions(gadgetDao);
    }

    @Test
    void updateGadgetValidatesFields() {
        Gadget g = valid();
        g.setName("");

        assertThrows(IllegalArgumentException.class, () -> service.updateGadget(g));

        verify(gadgetDao, never()).update(any());
    }

    @Test
    void updateGadgetFailsWhenNotFound() {
        Gadget g = valid();
        when(gadgetDao.findById(1)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> service.updateGadget(g));

        verify(gadgetDao, never()).update(any());
    }



    @Test
    void deleteGadgetDeletesExisting() {
        when(gadgetDao.findById(1)).thenReturn(valid());

        service.deleteGadget(1);

        verify(gadgetDao).delete(1);
    }

    @Test
    void deleteGadgetFailsWhenNotFound() {
        when(gadgetDao.findById(1)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> service.deleteGadget(1));

        verify(gadgetDao, never()).delete(any());
    }

    @Test
    void getByIdReturnsGadget() {
        Gadget g = valid();
        when(gadgetDao.findById(1)).thenReturn(g);

        assertSame(g, service.getById(1));
    }

    @Test
    void getByIdThrowsWhenMissing() {
        when(gadgetDao.findById(1)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> service.getById(1));
    }



    @Test
    void getAllDelegatesToDao() {
        List<Gadget> list = List.of(valid());
        when(gadgetDao.findAll()).thenReturn(list);

        assertSame(list, service.getAll());
    }

    @Test
    void searchMethodsDelegateToDao() {
        List<Gadget> a = List.of(valid());
        List<Gadget> b = List.of(valid());
        List<Gadget> c = List.of(valid());
        List<Gadget> d = List.of(valid());
        when(gadgetDao.findByNameAndType("Pixel", GadgetType.SMARTPHONE)).thenReturn(a);
        when(gadgetDao.findByManufacturer("Google")).thenReturn(b);
        when(gadgetDao.findByType(GadgetType.TABLET)).thenReturn(c);
        when(gadgetDao.findByScreenSize(1, 2, 3, 4)).thenReturn(d);

        assertSame(a, service.findByNameAndType("Pixel", GadgetType.SMARTPHONE));
        assertSame(b, service.findByManufacturer("Google"));
        assertSame(c, service.findByType(GadgetType.TABLET));
        assertSame(d, service.findByScreenSize(1, 2, 3, 4));
    }



    @Test
    void addFeatureAddsAndUpdates() {
        Gadget g = valid();
        when(gadgetDao.findById(1)).thenReturn(g);

        service.addFeature(1, Feature.GPS);

        assertTrue(g.hasFeature(Feature.GPS));
        verify(gadgetDao).update(g);
    }

    @Test
    void addFeatureFailsWhenGadgetNotFound() {
        when(gadgetDao.findById(1)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> service.addFeature(1, Feature.GPS));

        verify(gadgetDao, never()).update(any());
    }

    @Test
    void removeFeatureRemovesAndUpdates() {
        Gadget g = valid();
        g.addFeature(Feature.CAMERA);
        when(gadgetDao.findById(1)).thenReturn(g);

        service.removeFeature(1, Feature.CAMERA);

        assertFalse(g.hasFeature(Feature.CAMERA));
        verify(gadgetDao).update(g);
    }

    @Test
    void removeFeatureFailsWhenGadgetNotFound() {
        when(gadgetDao.findById(1)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> service.removeFeature(1, Feature.CAMERA));
    }



    @Test
    void sortByPriceAscending() {
        Gadget a = valid(); a.setId(1); a.setPrice(new BigDecimal("300"));
        Gadget b = valid(); b.setId(2); b.setPrice(new BigDecimal("100"));
        Gadget c = valid(); c.setId(3); c.setPrice(new BigDecimal("200"));
        List<Gadget> list = new ArrayList<>(List.of(a, b, c));

        List<Gadget> result = service.sort(list, GadgetSortField.PRICE, true);

        assertSame(list, result);
        assertEquals(List.of(2, 3, 1), result.stream().map(Gadget::getId).toList());
    }

    @Test
    void sortByNameDescending() {
        Gadget a = valid(); a.setId(1); a.setName("Alpha");
        Gadget b = valid(); b.setId(2); b.setName("Charlie");
        Gadget c = valid(); c.setId(3); c.setName("Bravo");
        List<Gadget> list = new ArrayList<>(List.of(a, b, c));

        service.sort(list, GadgetSortField.NAME, false);

        assertEquals(List.of(2, 3, 1), list.stream().map(Gadget::getId).toList());
    }



    @Test
    void exportCatalogWritesEveryGadget() throws IOException {
        Gadget a = new GadgetBuilder().name("A").manufacturer("M1").price(new BigDecimal("10"))
                .type(GadgetType.WATCH).screen(1, 2).build();
        Gadget b = new GadgetBuilder().name("B").manufacturer("M2").price(new BigDecimal("20"))
                .type(GadgetType.TABLET).screen(3, 4).feature(Feature.CAMERA).build();
        when(gadgetDao.findAll()).thenReturn(List.of(a, b));
        StringWriter out = new StringWriter();

        service.exportCatalog(out);

        assertEquals("A;M1;10;WATCH;1;2;\nB;M2;20;TABLET;3;4;CAMERA\n", out.toString());
    }

    @Test
    void exportCatalogOfEmptyDbWritesNothing() throws IOException {
        when(gadgetDao.findAll()).thenReturn(List.of());
        StringWriter out = new StringWriter();

        service.exportCatalog(out);

        assertEquals("", out.toString());
    }

    @Test
    void importCatalogSavesEveryLineAndReturnsCount() throws IOException {
        String data = "A;M1;10;WATCH;1;2;\nB;M2;20;TABLET;3;4;CAMERA\n";

        int count = service.importCatalog(new StringReader(data));

        assertEquals(2, count);
        ArgumentCaptor<Gadget> captor = ArgumentCaptor.forClass(Gadget.class);
        verify(gadgetDao, times(2)).save(captor.capture());
        assertEquals("A", captor.getAllValues().get(0).getName());
        assertEquals("B", captor.getAllValues().get(1).getName());
        assertTrue(captor.getAllValues().get(1).hasFeature(Feature.CAMERA));
    }

    @Test
    void importCatalogOfEmptyInputReturnsZero() throws IOException {
        assertEquals(0, service.importCatalog(new StringReader("")));
        verifyNoInteractions(gadgetDao);
    }

    @Test
    void importCatalogValidatesGadgets() {
        String data = "A;M1;-10;WATCH;1;2;\n";

        assertThrows(IllegalArgumentException.class,
                () -> service.importCatalog(new StringReader(data)));

        verify(gadgetDao, never()).save(any());
    }
}
