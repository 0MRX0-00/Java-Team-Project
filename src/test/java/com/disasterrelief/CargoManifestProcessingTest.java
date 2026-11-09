package com.disasterrelief;

import com.disasterrelief.exception.ValidationException;
import com.disasterrelief.utility.InputValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Cargo Manifest String Processing & Parsing Tests")
class CargoManifestProcessingTest {

    @Test
    @DisplayName("Should successfully parse and format standard manifest")
    void testStandardManifestParsing() {
        String input = "FOOD,100,WATER,200,PLASMA,20";
        List<InputValidator.ManifestItem> items = InputValidator.parseAndValidateManifest(input);

        assertNotNull(items);
        assertEquals(3, items.size());

        assertEquals("FOOD", items.get(0).getItemName());
        assertEquals(100, items.get(0).getQuantity());

        assertEquals("WATER", items.get(1).getItemName());
        assertEquals(200, items.get(1).getQuantity());

        assertEquals("PLASMA", items.get(2).getItemName());
        assertEquals(20, items.get(2).getQuantity());

        // Test String.format() table output
        String formattedTable = InputValidator.formatManifestTable(items);
        assertNotNull(formattedTable);
        assertTrue(formattedTable.contains("FOOD"));
        assertTrue(formattedTable.contains("100"));
        assertTrue(formattedTable.contains("SUPPLY ITEM"));
    }

    @Test
    @DisplayName("Should trim whitespace in keys and quantities")
    void testWhitespaceTrimming() {
        String input = "  FOOD ,  150  , WATER,   350  ";
        List<InputValidator.ManifestItem> items = InputValidator.parseAndValidateManifest(input);

        assertEquals(2, items.size());
        assertEquals("FOOD", items.get(0).getItemName());
        assertEquals(150, items.get(0).getQuantity());
        assertEquals("WATER", items.get(1).getItemName());
        assertEquals(350, items.get(1).getQuantity());
    }

    @Test
    @DisplayName("Should reject manifest with duplicate item names")
    void testDuplicateItemRejection() {
        String duplicateManifest = "FOOD,100,WATER,200,FOOD,50";
        ValidationException ex = assertThrows(ValidationException.class, () ->
                InputValidator.parseAndValidateManifest(duplicateManifest));
        assertTrue(ex.getMessage().contains("Duplicate cargo item detected"));
    }

    @Test
    @DisplayName("Should reject negative or zero quantities")
    void testNegativeOrZeroQuantityRejection() {
        assertThrows(ValidationException.class, () ->
                InputValidator.parseAndValidateManifest("FOOD,-50,WATER,100"));
        assertThrows(ValidationException.class, () ->
                InputValidator.parseAndValidateManifest("FOOD,0,WATER,100"));
    }

    @Test
    @DisplayName("Should reject odd number of tokens (missing value)")
    void testMissingFieldRejection() {
        assertThrows(ValidationException.class, () ->
                InputValidator.parseAndValidateManifest("FOOD,100,WATER"));
    }

    @Test
    @DisplayName("Should reject non-numeric quantities")
    void testNonNumericQuantityRejection() {
        assertThrows(ValidationException.class, () ->
                InputValidator.parseAndValidateManifest("FOOD,HUNDRED,WATER,200"));
    }

    @Test
    @DisplayName("Should test text hygiene with cleanText()")
    void testCleanText() {
        String messy = "   Commander   Rajesh     Kumar   ";
        assertEquals("Commander Rajesh Kumar", InputValidator.cleanText(messy));
    }
}

