package util;

import com.fixmygadget.util.InputRules;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InputRulesTest {

    @ParameterizedTest
    @ValueSource(strings = {"Ivan", "Іван", "O'Brien", "O\u2019Brien", "Anna-Maria", "Mary Ann", "  Ivan  ", "Ł"})
    void acceptsValidNames(String name) {
        assertTrue(InputRules.isValidName(name));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "-Ivan", "'Ivan", "Ivan1", "Iv@n", "123", "Ivan_Petrenko", "Ivan.", "Ivan,Petro"})
    void rejectsInvalidNames(String name) {
        assertFalse(InputRules.isValidName(name));
    }

    @ParameterizedTest
    @ValueSource(strings = {"+380501112233", "+380000000000", " +380501112233 "})
    void acceptsValidPhones(String phone) {
        assertTrue(InputRules.isValidPhone(phone));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "0501112233", "380501112233", "+38050111223", "+3805011122334",
            "+380abcdefghi", "+381501112233", "+380 50 111 22 33", "+380-50-111-22-33",
            "+380\u0660\u0661\u0662\u0663\u0664\u0665\u0666\u0667\u0668"})
    void rejectsInvalidPhones(String phone) {
        assertFalse(InputRules.isValidPhone(phone));
    }

    @Test
    void phoneConstantsAreConsistent() {
        assertEquals("+380", InputRules.PHONE_PREFIX);
        assertEquals(InputRules.PHONE_PREFIX.length() + 9, InputRules.PHONE_LENGTH);
    }
}
