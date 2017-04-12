package cc.r2.core.poly.multivar;

import cc.r2.core.number.BigInteger;
import cc.r2.core.test.AbstractTest;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public class RandomMultivariatePolynomialTest extends AbstractTest{
    @Test
    public void name() throws Exception {
        System.out.println(RandomMultivariatePolynomial.randomPolynomial(5,5,5, BigInteger.valueOf(123),MultivariatePolynomial.LEX, getRandom()));
    }
}