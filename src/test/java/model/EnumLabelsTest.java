package model;

import com.fixmygadget.model.Feature;
import com.fixmygadget.model.GadgetType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class EnumLabelsTest {

    @Test
    void gadgetTypeLabelsAreHumanReadable() {
        assertEquals("Smartphone", GadgetType.SMARTPHONE.getLabel());
        assertEquals("Tablet", GadgetType.TABLET.getLabel());
        assertEquals("Watch", GadgetType.WATCH.getLabel());
    }

    @Test
    void gadgetTypeToStringReturnsLabel() {
        for (GadgetType type : GadgetType.values()) {
            assertEquals(type.getLabel(), type.toString());
        }
    }

    @Test
    void featureLabelsAreHumanReadable() {
        assertEquals("Camera", Feature.CAMERA.getLabel());
        assertEquals("GPS", Feature.GPS.getLabel());
        assertEquals("Heart rate monitor", Feature.HEART_RATE.getLabel());
    }

    @Test
    void featureToStringReturnsLabel() {
        for (Feature feature : Feature.values()) {
            assertEquals(feature.getLabel(), feature.toString());
        }
    }

    // The database stores enum names (schema.sql); these must stay in sync with the enums.
    @Test
    void enumNamesMatchDatabaseCodes() {
        assertSame(GadgetType.SMARTPHONE, GadgetType.valueOf("SMARTPHONE"));
        assertSame(GadgetType.TABLET, GadgetType.valueOf("TABLET"));
        assertSame(GadgetType.WATCH, GadgetType.valueOf("WATCH"));
        assertSame(Feature.CAMERA, Feature.valueOf("CAMERA"));
        assertSame(Feature.GPS, Feature.valueOf("GPS"));
        assertSame(Feature.HEART_RATE, Feature.valueOf("HEART_RATE"));
    }
}
