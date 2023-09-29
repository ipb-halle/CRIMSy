/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.ipb_halle.lbac.material.common.bean;

/**
 *
 * @author swittche
 */
public class CASValidator implements IndexValidatorInterface {

    @Override
    public boolean validate(String casNumber) throws Exception {

        casNumber = casNumber.replace("-", "");

        if (!casNumber.matches("\\d{5}")) {
//        if (!(casNumber.length() < 2 && casNumber.length() > 7)) {
            throw new Exception("Number is not valid");
        }

        int checkDigit = Integer.parseInt(casNumber.substring(casNumber.length() - 1));
        casNumber = casNumber.substring(0, casNumber.length() - 1);
        double result = 0;

        for (int i = 1; i <= casNumber.length(); i++) {
            int digit = Integer.parseInt(casNumber.substring(casNumber.length() - i, casNumber.length() - i + 1));
            result += digit * i;
        }

        if (result % 10 != checkDigit) {
            throw new Exception("Number is not valid");
        } else {
            return true;
        }
    }
}
