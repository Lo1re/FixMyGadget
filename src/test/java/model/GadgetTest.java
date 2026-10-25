package model;

import com.fixmygadget.model.Feature;
import com.fixmygadget.model.Gadget;
import com.fixmygadget.model.GadgetType;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GadgetTest {

    //sample obj
    private Gadget sample() {
        Gadget g = new Gadget("Watch 5", "Samsung", new BigDecimal("199.50"), GadgetType.WATCH, 450, 450);
        g.setId(3);
        g.addFeature(Feature.HEART_RATE);
        g.addFeature(Feature.GPS);
        return g;
    }


    @Test
    void copyConstructorCopiesAllFields() {
        Gadget original = sample();

        Gadget copy = new Gadget(original);

        assertNotSame(original, copy);
        assertEquals(original.getId(), copy.getId());
        assertEquals(original.getName(), copy.getName());
        assertEquals(original.getManufacturer(), copy.getManufacturer());
        assertEquals(original.getPrice(), copy.getPrice());
        assertEquals(original.getType(), copy.getType());
        assertEquals(original.getScreenWidth(), copy.getScreenWidth());
        assertEquals(original.getScreenHeight(), copy.getScreenHeight());
        assertEquals(original.getFeatures(), copy.getFeatures());
    }

    @Test
    void copyHasIndependentFeatureSet() {
        Gadget original = sample();
        Gadget copy = new Gadget(original);

        //diff sets with same values
        assertNotSame(original.getFeatures(), copy.getFeatures());

        copy.addFeature(Feature.CAMERA);
        copy.removeFeature(Feature.GPS);

        //not affect each other
        assertTrue(original.hasFeature(Feature.GPS));
        assertFalse(original.hasFeature(Feature.CAMERA));
        assertEquals(2, original.getFeatures().size());
    }

    @Test
    void changingCopyDoesNotAffectOriginal() {
        Gadget original = sample();
        Gadget copy = new Gadget(original);

        copy.setName("Other");
        copy.setPrice(BigDecimal.ONE);

        assertEquals("Watch 5", original.getName());
        assertEquals(new BigDecimal("199.50"), original.getPrice());
    }

    @Test
    void copyOfGadgetWithoutFeaturesHasNoFeatures() {
        Gadget original = new Gadget("N", "M", BigDecimal.ONE, GadgetType.TABLET, 1, 1);
        assertTrue(new Gadget(original).getFeatures().isEmpty());
    }



    @Test
    void addRemoveHasFeature() {
        Gadget g = new Gadget();
        assertFalse(g.hasFeature(Feature.CAMERA));
        g.addFeature(Feature.CAMERA);
        assertTrue(g.hasFeature(Feature.CAMERA));
        g.removeFeature(Feature.CAMERA);
        assertFalse(g.hasFeature(Feature.CAMERA));
    }



    @Test
    void writeToProducesSemicolonSeparatedLine() throws IOException {
        Gadget g = new Gadget("Pad", "Apple", new BigDecimal("500"), GadgetType.TABLET, 2048, 1536);
        g.addFeature(Feature.CAMERA);
        StringWriter out = new StringWriter();

        g.writeTo(out);

        assertEquals("Pad;Apple;500;TABLET;2048;1536;CAMERA\n", out.toString());
    }

    @Test
    void writeToWithoutFeaturesEndsWithEmptyField() throws IOException {
        Gadget g = new Gadget("Pad", "Apple", new BigDecimal("500"), GadgetType.TABLET, 2048, 1536);
        StringWriter out = new StringWriter();

        g.writeTo(out);

        assertEquals("Pad;Apple;500;TABLET;2048;1536;\n", out.toString());
    }

    @Test
    void readFromParsesLine() throws IOException {
        Gadget g = new Gadget();

        boolean ok = g.readFrom(new StringReader("Pad;Apple;500;TABLET;2048;1536;CAMERA,GPS\n"));

        assertTrue(ok);
        assertEquals("Pad", g.getName());
        assertEquals("Apple", g.getManufacturer());
        assertEquals(new BigDecimal("500"), g.getPrice());
        assertEquals(GadgetType.TABLET, g.getType());
        assertEquals(2048, g.getScreenWidth());
        assertEquals(1536, g.getScreenHeight());
        assertTrue(g.hasFeature(Feature.CAMERA));
        assertTrue(g.hasFeature(Feature.GPS));
    }

    @Test
    void readFromHandlesWindowsLineEndingAndEmptyFeatures() throws IOException {
        Gadget g = new Gadget();

        assertTrue(g.readFrom(new StringReader("Pad;Apple;500;TABLET;10;20;\r\n")));

        assertTrue(g.getFeatures().isEmpty());
        assertEquals(20, g.getScreenHeight());
    }

    @Test
    void readFromReturnsFalseAtEndOfStream() throws IOException {
        assertFalse(new Gadget().readFrom(new StringReader("")));
    }

    @Test
    void readFromReadsConsecutiveLines() throws IOException {
        StringReader reader = new StringReader("A;M;1;WATCH;1;1;\nB;N;2;TABLET;2;2;\n");
        Gadget first = new Gadget();
        Gadget second = new Gadget();

        assertTrue(first.readFrom(reader));
        assertTrue(second.readFrom(reader));
        assertFalse(new Gadget().readFrom(reader));

        assertEquals("A", first.getName());
        assertEquals("B", second.getName());
    }

    @Test
    void readFromRejectsInvalidType() {
        Gadget g = new Gadget();
        assertThrows(IllegalArgumentException.class,
                () -> g.readFrom(new StringReader("A;M;1;TOASTER;1;1;\n")));
    }

    @Test
    void writeThenReadRoundTrip() throws IOException {
        Gadget original = sample();
        StringWriter out = new StringWriter();
        original.writeTo(out);

        Gadget restored = new Gadget();
        assertTrue(restored.readFrom(new StringReader(out.toString())));

        assertEquals(original.getName(), restored.getName());
        assertEquals(original.getManufacturer(), restored.getManufacturer());
        assertEquals(original.getPrice(), restored.getPrice());
        assertEquals(original.getType(), restored.getType());
        assertEquals(original.getScreenWidth(), restored.getScreenWidth());
        assertEquals(original.getScreenHeight(), restored.getScreenHeight());
        assertEquals(original.getFeatures(), restored.getFeatures());
    }
}
