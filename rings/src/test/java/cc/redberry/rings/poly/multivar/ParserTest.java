package cc.redberry.rings.poly.multivar;

import cc.redberry.rings.IntegersZp64;
import cc.redberry.rings.Rational;
import cc.redberry.rings.Rings;
import cc.redberry.rings.bigint.BigInteger;
import cc.redberry.rings.poly.FiniteField;
import cc.redberry.rings.poly.univar.UnivariatePolynomial;
import cc.redberry.rings.poly.univar.UnivariatePolynomialZ64;
import cc.redberry.rings.poly.univar.UnivariatePolynomialZp64;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

import static cc.redberry.rings.Rings.Q;
import static cc.redberry.rings.poly.multivar.MonomialOrder.GREVLEX;

/**
 * @since 1.0
 */
public class ParserTest extends AMultivariateTest {
    @Test
    public void test1() throws Exception {
        Q.parse("+12");
    }

    @Test
    public void test2() throws Exception {
        System.out.println(Parser.parse("(2/3)*a*b^2 - (1/3)*a^3*b^2", Q, Q, MonomialOrder.LEX));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void test3() throws Exception {
        String[] strs = {
                "b+(1+x^2)*b^2+(x)*b^3+(1+x+x^2)*b^4",
                "(1+x)*b^4+(1+x)*b^8",
                "(x)*b^3+(1+x^2)*b^4+(x+x^2)*b^5",
                "(x+x^2)*b^2+(x^2)*b^3+(x+x^2)*b^4+(1+x^2)*b^5+(1+x^2)*b^6",
                "1+(1+x^2)*b+(x+x^2)*b^3+(x^2)*b^4+(x)*b^6+(1+x+x^2)*b^7",
                "(1+x+x^2)*b^7",
                "(1+x)*b^5",
                "(1+x)*b^4+(1+x)*b^8",
                "(1+x^2)*b^4+b^5+(x)*b^6+(1+x)*b^7",
                "(x)*b^3+(1+x^2)*b^4+(x+x^2)*b^5",
                "(x)*b^2+(1+x^2)*b^3+(1+x+x^2)*b^5+b^6+(1+x^2)*b^7+(x+x^2)*b^8",
                "(x+x^2)*b^2+(x^2)*b^3+(x+x^2)*b^4+(1+x^2)*b^5+(1+x^2)*b^6",
                "(1+x+x^2)*b^7",
                "1+(1+x^2)*b+(x+x^2)*b^3+(x^2)*b^4+(x)*b^6+(1+x+x^2)*b^7",
                "(1+x)+(x)*b+b^3+(1+x^2)*b^4",
                "(1+x+x^2)+b",
                "(x)*b^3+(1+x^2)*b^4+(x+x^2)*b^5",
                "(1+x+x^2)*b^7",
                "(1+x)*b^5",
                "(1+x)*b^4+(1+x)*b^8",
                "(1+x^2)*b^4+b^5+(x)*b^6+(1+x)*b^7",
                "(x)*b^3+(1+x^2)*b^4+(x+x^2)*b^5",
                "(x)*b^2+(1+x^2)*b^3+(1+x+x^2)*b^5+b^6+(1+x^2)*b^7+(x+x^2)*b^8",
                "(1+x+x^2)*b^7",
                "(1+x)*b^4+(1+x)*b^8",
        };
        FiniteField<UnivariatePolynomialZp64> minorDomain = new FiniteField<>(UnivariatePolynomialZ64.create(1, 0, 1, 1).modulus(2));
        FiniteField<UnivariatePolynomial<UnivariatePolynomialZp64>> domain = new FiniteField<>(UnivariatePolynomial.parse("(1+x^2)+(x^2)*x+(x+x^2)*x^2+x^3", minorDomain));
        String[] vars = {"a", "b", "c"};
        MultivariatePolynomial<UnivariatePolynomialZp64> arr[] = Arrays.stream(strs)
                .map(s -> MultivariatePolynomial.parse(s, domain, vars))
                .toArray(MultivariatePolynomial[]::new);
        for (int i = 0; i < arr.length; i++) {
            Assert.assertEquals(strs[i], arr[i].toString(vars));
            Assert.assertEquals(arr[i], arr[i].parsePoly(arr[i].toString()));
        }
    }

    @Test
    public void test4() throws Exception {
        FiniteField<UnivariatePolynomialZp64> domain = Rings.GF(2, 3);
        System.out.println(Parser.parse("2", domain, domain, MonomialOrder.LEX, "x", "y", "z"));
    }

    @Test
    public void test5() throws Exception {
        System.out.println(MultivariatePolynomialZp64.parse("12312341231412423142342343125234234321423 + x", new IntegersZp64(3), MonomialOrder.LEX));
    }

    @Test
    public void test6() throws Exception {
        String[] vars = {"u0", "u1", "u2", "u3"};
        MultivariatePolynomial<Rational<BigInteger>>
                a = MultivariatePolynomial.parse("u3*u3 + u2*u2 + u1*u1 + u0*u0 + u1*u1 + u2*u2 + u3*u3 - u0", Q, GREVLEX, vars),
                b = MultivariatePolynomial.parse("u3*0 + u2*0 + u1*u3 + u0*u2 + u1*u1 + u2*u0 + u3*u1 - u2", Q, GREVLEX, vars);

        System.out.println(a.toString(vars));
        System.out.println(b.toString(vars));
    }
}