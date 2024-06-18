package de.ipb_halle.lbac.material.common.bean;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

/**
 *
 * @author swittche
 */
public class CASValidatorTest {
    private CASValidator validator=new CASValidator();


    @Test
    public void cas_test_invalid_numbers() throws Exception{
        Assert.assertThrows(Exception.class, ()->{validator.validate(null); });
        Assert.assertThrows(Exception.class, ()->{validator.validate(""); });
        Assert.assertThrows(Exception.class, ()->{validator.validate("64-17-6"); });
        Assert.assertThrows(Exception.class, ()->{validator.validate("64--17-5"); });
    }

    @Test
    public void cas_test_valid_numbers() throws Exception{
        validator.validate("64-17-5");
        validator.validate("78-10-4 ");
        validator.validate("78-87-5");
        validator.validate("79-11-8");
        validator.validate(" 95-38-5");
        validator.validate("101-02-0");
        validator.validate("102-60-3");
        validator.validate("106-46-7 ");
    }

}
