package de.ipb_halle.lbac.material.common.bean;

/**
 *
 * @author swittche
 */
public class CASValidator implements IndexValidatorInterface {

    @Override
    public boolean validate(String casNumber) throws Exception {
        casNumber = casNumber.trim();

        checkPattern(casNumber);

        casNumber = casNumber.replaceAll("-", "");
        int checkDigit = Integer.parseInt(casNumber.substring(casNumber.length() - 1));
        String casNumberWithoutCheckDigit = casNumber.substring(0, casNumber.length() - 1);

        validateChecksum(casNumberWithoutCheckDigit, checkDigit);

        return true;

    }

    private void validateChecksum(String casNumberWithoutCheckDigit, int checkDigit) throws Exception, NumberFormatException {
        double result = 0;

        for (int i = 0; i < casNumberWithoutCheckDigit.length(); i++) {
            int numberAtIndex = Integer.parseInt(String.valueOf(((casNumberWithoutCheckDigit.charAt(i)))));
            result += (casNumberWithoutCheckDigit.length() - i) * numberAtIndex;
        }

        if (result % 10 != checkDigit) {
            throw new Exception("Checknumber is not valid");
        }
    }

    private void checkPattern(String casNumber) throws Exception {
        if (!casNumber.matches("\\d{2,7}-\\d{2}-\\d{1}")) {
            throw new Exception("Numberpattern is not valid");
        }
    }
}
