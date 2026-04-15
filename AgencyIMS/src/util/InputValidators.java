package util;

import java.time.LocalDate;

public final class InputValidators {

    private InputValidators() {
    }

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static boolean isPositiveInteger(String value) {
        if (isBlank(value)) {
            return false;
        }
        try {
            return Integer.parseInt(value.trim()) > 0;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    public static boolean hasValidDateRange(LocalDate issueDate, LocalDate expiryDate) {
        return issueDate != null && expiryDate != null && !expiryDate.isBefore(issueDate);
    }
}
