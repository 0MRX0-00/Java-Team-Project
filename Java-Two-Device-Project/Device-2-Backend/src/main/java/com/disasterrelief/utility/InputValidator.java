package com.disasterrelief.utility;

import com.disasterrelief.exception.ValidationException;

import java.util.*;

/**
 * InputValidator performs input hygiene and manifest parsing using Java String split, trim, format.
 */
public class InputValidator {

    private static final Set<String> ALLOWED_CARGO_ITEMS = new HashSet<>(
            Arrays.asList("FOOD", "WATER", "PLASMA", "MEDICINE", "TENT", "BLANKET", "GENERATOR")
    );

    private InputValidator() {}

    /**
     * Parsed item representation from manifest strings.
     */
    public static class ManifestItem {
        private final String itemName;
        private final int quantity;

        public ManifestItem(String itemName, int quantity) {
            this.itemName = itemName;
            this.quantity = quantity;
        }

        public String getItemName() { return itemName; }
        public int getQuantity() { return quantity; }

        @Override
        public String toString() {
            return String.format("%-12s : %,6d units", itemName, quantity);
        }
    }

    /**
     * Parses and strictly validates a cargo manifest string such as "FOOD,100,WATER,200,PLASMA,20".
     * Uses String.split(",") and String.trim().
     */
    public static List<ManifestItem> parseAndValidateManifest(String rawManifest) {
        if (rawManifest == null || rawManifest.trim().isEmpty()) {
            throw new ValidationException("Cargo manifest string cannot be empty");
        }

        String[] tokens = rawManifest.split(",");
        if (tokens.length % 2 != 0) {
            throw new ValidationException(String.format(
                    "Invalid cargo manifest format. Expected key-value pairs separated by commas (found %d tokens)",
                    tokens.length
            ));
        }

        List<ManifestItem> items = new ArrayList<>();
        Set<String> seenItems = new HashSet<>();

        for (int i = 0; i < tokens.length; i += 2) {
            String itemKey = tokens[i].trim().toUpperCase();
            String qtyStr = tokens[i + 1].trim();

            if (itemKey.isEmpty()) {
                throw new ValidationException(String.format("Missing item name at token index %d", i));
            }

            if (!itemKey.matches("^[A-Z_]+$")) {
                throw new ValidationException(String.format("Invalid cargo item identifier '%s'. Must contain alphabetic characters only", itemKey));
            }

            if (seenItems.contains(itemKey)) {
                throw new ValidationException(String.format("Duplicate cargo item detected in manifest: '%s'", itemKey));
            }

            int quantity;
            try {
                quantity = Integer.parseInt(qtyStr);
            } catch (NumberFormatException e) {
                throw new ValidationException(String.format("Non-numeric quantity '%s' for cargo item '%s'", qtyStr, itemKey));
            }

            if (quantity <= 0) {
                throw new ValidationException(String.format("Quantity for cargo item '%s' must be positive (received: %d)", itemKey, quantity));
            }

            seenItems.add(itemKey);
            items.add(new ManifestItem(itemKey, quantity));
        }

        return items;
    }

    /**
     * Generates a formatted manifest string from parsed items using String.format().
     */
    public static String formatManifestTable(List<ManifestItem> items) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("+-----------------+------------+%n"));
        sb.append(String.format("| %-15s | %-10s |%n", "SUPPLY ITEM", "QUANTITY"));
        sb.append(String.format("+-----------------+------------+%n"));
        for (ManifestItem item : items) {
            sb.append(String.format("| %-15s | %10d |%n", item.getItemName(), item.getQuantity()));
        }
        sb.append(String.format("+-----------------+------------+%n"));
        return sb.toString();
    }

    /**
     * Trims and cleans free-text field notes.
     */
    public static String cleanText(String text) {
        if (text == null) {
            return "";
        }
        return text.trim().replaceAll("\\s+", " ");
    }
}

