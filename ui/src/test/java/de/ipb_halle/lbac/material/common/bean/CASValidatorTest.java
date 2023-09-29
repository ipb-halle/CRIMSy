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
    public void cas_test_1() throws Exception{
        Assert.assertThrows(Exception.class, ()->{validator.validate("");});
    }
    
    @Test
    public void cas_test_2() throws Exception{
        validator.validate("64-17-5");
    }
    
}
