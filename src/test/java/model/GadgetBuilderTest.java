package model;

import com.fixmygadget.model.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GadgetBuilderTest {

    private GadgetBuilder valid() {
        return new GadgetBuilder()
                .name("Pixel 8")
                .manufacturer("Google")
                .price(new BigDecimal("699.99"))
                .type(GadgetType.SMARTPHONE)
                .screen(1080, 2400);
    }

    @Test
    void buildsGadgetWithAllFields() {
        Gadget g = valid().id(7).feature(Feature.CAMERA).feature(Feature.GPS).build();

        assertEquals(7, g.getId());
        assertEquals("Pixel 8", g.getName());
        assertEquals("Google", g.getManufacturer());
        assertEquals(new BigDecimal("699.99"), g.getPrice());
        assertEquals(GadgetType.SMARTPHONE, g.getType());
        assertEquals(1080, g.getScreenWidth());
        assertEquals(2400, g.getScreenHeight());
        assertEquals(2, g.getFeatures().size());
        assertTrue(g.hasFeature(Feature.CAMERA));
        assertTrue(g.hasFeature(Feature.GPS));
    }

    @Test
    void idIsNullByDefault() {
        assertNull(valid().build().getId());
    }

    @Test
    void duplicateFeaturesAreStoredOnce() {
        Gadget g = valid().feature(Feature.GPS).feature(Feature.GPS).build();
        assertEquals(1, g.getFeatures().size());
    }

    @Test
    void methodsReturnSameBuilderForChaining() {
        GadgetBuilder b = new GadgetBuilder();
        assertSame(b, b.name("x"));
        assertSame(b, b.manufacturer("x"));
        assertSame(b, b.price(BigDecimal.ONE));
        assertSame(b, b.type(GadgetType.WATCH));
        assertSame(b, b.screen(1, 1));
        assertSame(b, b.feature(Feature.GPS));
        assertSame(b, b.id(1));
    }

    @Test
    void missingNameFails() {
        assertThrows(IllegalStateException.class, () -> valid().name(null).build());
    }

    @Test
    void missingManufacturerFails() {
        assertThrows(IllegalStateException.class, () -> valid().manufacturer(null).build());
    }

    @Test
    void missingPriceFails() {
        assertThrows(IllegalStateException.class, () -> valid().price(null).build());
    }

    @Test
    void missingTypeFails() {
        assertThrows(IllegalStateException.class, () -> valid().type(null).build());
    }

    @Test
    void emptyBuilderFails() {
        assertThrows(IllegalStateException.class, () -> new GadgetBuilder().build());
    }

    @Test
    void builderCanBeUsedViaBuilderInterface() {
        Builder<Gadget> b = valid();
        assertEquals("Pixel 8", b.build().getName());
    }
}
