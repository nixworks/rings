package cc.r2.core.poly.univar;

import cc.r2.core.poly.FiniteField;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public class EqualDegreeFactorizationTest {

    @Test
    public void test1() throws Exception {
        int modulus = 6101;
        lUnivariatePolynomialZp a = lUnivariatePolynomialZ.create(5224, 5225, 5225, 5225, 1).modulus(modulus);
        for (int i = 0; i < 10; i++)
            Assert.assertEquals(4, EqualDegreeFactorization.CantorZassenhaus(a, 1).size());
    }

    @Test
    public void test2() throws Exception {
        int modulus = 13;
        lUnivariatePolynomialZp poly = lUnivariatePolynomialZ.create(9, 0, 1).modulus(modulus);
        Assert.assertEquals(2, EqualDegreeFactorization.CantorZassenhaus(poly, 1).size());
    }

    @Ignore
    @Test
    public void test3a() throws Exception {
        lUnivariatePolynomialZp irreducible = lUnivariatePolynomialZ.create(1, 1, 1, 1, 1).modulus(2);
        FiniteField<lUnivariatePolynomialZp> domain = new FiniteField<>(irreducible);
        UnivariatePolynomial<lUnivariatePolynomialZp> poly = UnivariatePolynomial.parse(domain, "(1+x+x^2)+(1+x+x^2)*x+(1+x+x^3)*x^4+x^6");

        DivisionWithRemainder.InverseModMonomial<UnivariatePolynomial<lUnivariatePolynomialZp>> invMod = DivisionWithRemainder.fastDivisionPreConditioning(poly);


        int d = 3, pPower = 4;
        int
                c0 = 0, n0 = 0,
                c1 = 0, n1 = 0,
                c2 = 0, n2 = 0,
                i = 0;
        for (; i < 10_000; ++i) {
            UnivariatePolynomial<lUnivariatePolynomialZp> randomPoly = EqualDegreeFactorization.randomMonicPoly(poly);
            UnivariatePolynomial<lUnivariatePolynomialZp> gcd0 = UnivariateGCD.PolynomialGCD(randomPoly, poly);
            if (!gcd0.isConstant() && !gcd0.equals(poly)) ++n0;
            else ++c0;

            UnivariatePolynomial<lUnivariatePolynomialZp> splitting = EqualDegreeFactorization.tracePolyGF2(randomPoly, pPower * d, poly, invMod);
            UnivariatePolynomial<lUnivariatePolynomialZp> gcd1 = UnivariateGCD.PolynomialGCD(splitting, poly);
            if (!gcd1.isConstant() && !gcd1.equals(poly)) ++n1;
            else ++c1;

            UnivariatePolynomial<lUnivariatePolynomialZp> gcd2 = UnivariateGCD.PolynomialGCD(splitting.clone().increment(), poly);
            if (!gcd2.isConstant() && !gcd2.equals(poly)) ++n2;
            else ++c2;

            Assert.assertEquals(poly, gcd1.clone().multiply(gcd2));// : randomPoly + ": " + splitting + " : " + gcd1.clone().multiply(gcd2) + " == " + poly;

            if (i % 1000 == 0) {
                System.out.println("=========");
                System.out.println(c0);
                System.out.println(n0);
                System.out.println();
                System.out.println(c1);
                System.out.println(n1);
                System.out.println();
                System.out.println(c2);
                System.out.println(n2);
                System.out.println();
            }
        }
    }
}