package cc.r2.core.poly2;


import cc.r2.core.poly2.DivisionWithRemainder.InverseModMonomial;

import java.util.ArrayList;

import static cc.r2.core.poly2.PolynomialArithmetics.polyMod;

/**
 * Polynomial composition.
 *
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public final class ModularComposition {
    private ModularComposition() {}


    /**
     * Returns {@code x^{i*modulus} mod polyModulus} for i in {@code [0...degree]}, where {@code degree} is {@code polyModulus} degree.
     *
     * @param polyModulus the monic modulus
     * @param invMod      pre-conditioned modulus ({@link DivisionWithRemainder#fastDivisionPreConditioning(IMutablePolynomial)} )})
     * @return {@code x^{i*modulus} mod polyModulus} for i in {@code [0...degree]}, where {@code degree} is {@code polyModulus} degree
     * @see DivisionWithRemainder#fastDivisionPreConditioning(IMutablePolynomial)
     */
    public static ArrayList<MutablePolynomialMod> xPowers(MutablePolynomialMod polyModulus, InverseModMonomial<MutablePolynomialMod> invMod) {
        return polyPowers(PolynomialArithmetics.createMonomialMod(polyModulus.modulus, polyModulus, invMod), polyModulus, invMod, polyModulus.degree);
    }

    /**
     * Returns {@code x^{i*modulus} mod polyModulus} for i in {@code [0...degree]}, where {@code degree} is {@code polyModulus} degree.
     *
     * @param polyModulus the monic modulus
     * @param invMod      pre-conditioned modulus ({@link DivisionWithRemainder#fastDivisionPreConditioning(IMutablePolynomial)} )})
     * @return {@code x^{i*modulus} mod polyModulus} for i in {@code [0...degree]}, where {@code degree} is {@code polyModulus} degree
     * @see DivisionWithRemainder#fastDivisionPreConditioning(IMutablePolynomial)
     */
    public static ArrayList<bMutablePolynomialMod> xPowers(bMutablePolynomialMod polyModulus, InverseModMonomial<bMutablePolynomialMod> invMod) {
        return polyPowers(PolynomialArithmetics.createMonomialMod(polyModulus.modulus, polyModulus, invMod), polyModulus, invMod, polyModulus.degree);
    }


    /**
     * Returns {@code x^{i*modulus} mod polyModulus} for i in {@code [0...degree]}, where {@code degree} is {@code polyModulus} degree.
     *
     * @param polyModulus the monic modulus
     * @param invMod      pre-conditioned modulus ({@link DivisionWithRemainder#fastDivisionPreConditioning(IMutablePolynomial)} )})
     * @return {@code x^{i*modulus} mod polyModulus} for i in {@code [0...degree]}, where {@code degree} is {@code polyModulus} degree
     * @see DivisionWithRemainder#fastDivisionPreConditioning(IMutablePolynomial)
     */
    @SuppressWarnings("unchecked")
    public static <T extends IMutablePolynomialZp<T>> ArrayList<T> xPowers(T polyModulus, InverseModMonomial<T> invMod) {
        if (polyModulus instanceof MutablePolynomialMod) {
            MutablePolynomialMod pm = (MutablePolynomialMod) polyModulus;
            InverseModMonomial<MutablePolynomialMod> im = (InverseModMonomial<MutablePolynomialMod>) invMod;
            return (ArrayList<T>) polyPowers(PolynomialArithmetics.createMonomialMod(pm.modulus, pm, im), pm, im, pm.degree);
        } else {
            bMutablePolynomialMod pm = (bMutablePolynomialMod) polyModulus;
            InverseModMonomial<bMutablePolynomialMod> im = (InverseModMonomial<bMutablePolynomialMod>) invMod;
            return (ArrayList<T>) polyPowers(PolynomialArithmetics.createMonomialMod(pm.modulus, pm, im), pm, im, pm.degree);
        }
    }

    /**
     * Returns {@code poly^{i} mod polyModulus} for i in {@code [0...nIterations]}
     *
     * @param poly        the polynomial
     * @param polyModulus the monic polynomial modulus
     * @param invMod      pre-conditioned modulus ({@link DivisionWithRemainder#fastDivisionPreConditioning(IMutablePolynomial)} )})
     * @return {@code poly^{i} mod polyModulus} for i in {@code [0...nIterations]}
     * @see DivisionWithRemainder#fastDivisionPreConditioning(IMutablePolynomial)
     */
    public static <T extends IMutablePolynomialZp<T>> ArrayList<T> polyPowers(T poly, T polyModulus, InverseModMonomial<T> invMod, int nIterations) {
        ArrayList<T> exponents = new ArrayList<>();
        polyPowers(polyMod(poly, polyModulus, invMod, true), polyModulus, invMod, nIterations, exponents);
        return exponents;
    }

    /** writes poly^{i} mod polyModulus for i in [0...nIterations] to exponents */
    private static <T extends IMutablePolynomialZp<T>> void polyPowers(T polyReduced, T polyModulus, InverseModMonomial<T> invMod, int nIterations, ArrayList<T> exponents) {
        exponents.add(polyReduced.createOne());
        // polyReduced must be reduced!
        T base = polyReduced.clone();//polyMod(poly, polyModulus, invMod, true);
        exponents.add(base);
        T prev = base;
        for (int i = 0; i < nIterations; i++)
            exponents.add(prev = polyMod(prev.clone().multiply(base), polyModulus, invMod, false));
    }

    /**
     * Returns {@code poly^modulus mod polyModulus} using precomputed monomial powers {@code x^{i*modulus} mod polyModulus} for i in {@code [0...degree(poly)]}
     *
     * @param poly        the polynomial
     * @param polyModulus the monic polynomial modulus
     * @param invMod      pre-conditioned modulus ({@link DivisionWithRemainder#fastDivisionPreConditioning(IMutablePolynomial)} )})
     * @param xPowers     precomputed monomial powers {@code x^{i*modulus} mod polyModulus} for i in {@code [0...degree(poly)]}
     * @return {@code poly^modulus mod polyModulus}
     * @see #xPowers(MutablePolynomialMod, InverseModMonomial)
     * @see DivisionWithRemainder#fastDivisionPreConditioning(IMutablePolynomial)
     **/
    public static MutablePolynomialMod powModulusMod(MutablePolynomialMod poly,
                                                     MutablePolynomialMod polyModulus,
                                                     InverseModMonomial<MutablePolynomialMod> invMod,
                                                     ArrayList<MutablePolynomialMod> xPowers) {
        poly = polyMod(poly, polyModulus, invMod, true);
        return powModulusMod0(poly, polyModulus, invMod, xPowers);
    }

    /** doesn't do poly mod polyModulus first */
    private static MutablePolynomialMod powModulusMod0(MutablePolynomialMod poly,
                                                       MutablePolynomialMod polyModulus,
                                                       InverseModMonomial<MutablePolynomialMod> invMod,
                                                       ArrayList<MutablePolynomialMod> xPowers) {
        MutablePolynomialMod res = poly.createZero();
        for (int i = poly.degree; i >= 0; --i) {
            if (poly.data[i] == 0)
                continue;
            res.addMul(xPowers.get(i), poly.data[i]);
        }
        return polyMod(res, polyModulus, invMod, false);
    }

    /**
     * Returns {@code poly^modulus mod polyModulus} using precomputed monomial powers {@code x^{i*modulus} mod polyModulus} for i in {@code [0...degree(poly)]}
     *
     * @param poly        the polynomial
     * @param polyModulus the monic polynomial modulus
     * @param invMod      pre-conditioned modulus ({@link DivisionWithRemainder#fastDivisionPreConditioning(IMutablePolynomial)} )})
     * @param xPowers     precomputed monomial powers {@code x^{i*modulus} mod polyModulus} for i in {@code [0...degree(poly)]}
     * @return {@code poly^modulus mod polyModulus}
     * @see #xPowers(MutablePolynomialMod, InverseModMonomial)
     * @see DivisionWithRemainder#fastDivisionPreConditioning(IMutablePolynomial)
     **/
    public static bMutablePolynomialMod powModulusMod(bMutablePolynomialMod poly,
                                                      bMutablePolynomialMod polyModulus,
                                                      InverseModMonomial<bMutablePolynomialMod> invMod,
                                                      ArrayList<bMutablePolynomialMod> xPowers) {
        poly = polyMod(poly, polyModulus, invMod, true);
        return powModulusMod0(poly, polyModulus, invMod, xPowers);
    }

    /** doesn't do poly mod polyModulus first */
    private static bMutablePolynomialMod powModulusMod0(bMutablePolynomialMod poly,
                                                        bMutablePolynomialMod polyModulus,
                                                        InverseModMonomial<bMutablePolynomialMod> invMod,
                                                        ArrayList<bMutablePolynomialMod> xPowers) {
        bMutablePolynomialMod res = poly.createZero();
        for (int i = poly.degree; i >= 0; --i) {
            if (poly.data[i].isZero())
                continue;
            res.addMul(xPowers.get(i), poly.data[i]);
        }
        return polyMod(res, polyModulus, invMod, false);
    }

    /**
     * Returns {@code poly^modulus mod polyModulus} using precomputed monomial powers {@code x^{i*modulus} mod polyModulus} for i in {@code [0...degree(poly)]}
     *
     * @param poly        the polynomial
     * @param polyModulus the monic polynomial modulus
     * @param invMod      pre-conditioned modulus ({@link DivisionWithRemainder#fastDivisionPreConditioning(IMutablePolynomial)} )})
     * @param xPowers     precomputed monomial powers {@code x^{i*modulus} mod polyModulus} for i in {@code [0...degree(poly)]}
     * @return {@code poly^modulus mod polyModulus}
     * @see #xPowers(MutablePolynomialMod, InverseModMonomial)
     * @see DivisionWithRemainder#fastDivisionPreConditioning(IMutablePolynomial)
     **/
    @SuppressWarnings("unchecked")
    public static <T extends IMutablePolynomialZp<T>> T powModulusMod(T poly,
                                                                      T polyModulus,
                                                                      InverseModMonomial<T> invMod,
                                                                      ArrayList<T> xPowers) {
        if (poly instanceof MutablePolynomialMod)
            return (T) powModulusMod((MutablePolynomialMod) poly, (MutablePolynomialMod) polyModulus,
                    (InverseModMonomial<MutablePolynomialMod>) invMod, (ArrayList<MutablePolynomialMod>) xPowers);
        else
            return (T) powModulusMod((bMutablePolynomialMod) poly, (bMutablePolynomialMod) polyModulus,
                    (InverseModMonomial<bMutablePolynomialMod>) invMod, (ArrayList<bMutablePolynomialMod>) xPowers);
    }


    @SuppressWarnings("unchecked")
    private static <T extends IMutablePolynomialZp<T>> T powModulusMod0(T poly,
                                                                        T polyModulus,
                                                                        InverseModMonomial<T> invMod,
                                                                        ArrayList<T> xPowers) {
        if (poly instanceof MutablePolynomialMod)
            return (T) powModulusMod0((MutablePolynomialMod) poly, (MutablePolynomialMod) polyModulus, (InverseModMonomial<MutablePolynomialMod>) invMod, (ArrayList<MutablePolynomialMod>) xPowers);
        else
            return (T) powModulusMod0((bMutablePolynomialMod) poly, (bMutablePolynomialMod) polyModulus, (InverseModMonomial<bMutablePolynomialMod>) invMod, (ArrayList<bMutablePolynomialMod>) xPowers);
    }

    /**
     * Returns modular composition {@code poly(point) mod polyModulus } calculated using Brent & Kung algorithm for modular composition.
     *
     * @param poly        the polynomial
     * @param pointPowers precomputed powers of evaluation point {@code point^{i} mod polyModulus}
     * @param polyModulus the monic polynomial modulus
     * @param invMod      pre-conditioned modulus ({@link DivisionWithRemainder#fastDivisionPreConditioning(IMutablePolynomial)} )})
     * @param tBrentKung  Brent-Kung splitting parameter (optimal choice is ~sqrt(main.degree))
     * @return modular composition {@code poly(point) mod polyModulus }
     * @see #polyPowers(IMutablePolynomialZp, IMutablePolynomialZp, InverseModMonomial, int)
     * @see DivisionWithRemainder#fastDivisionPreConditioning(IMutablePolynomial)
     */
    public static <T extends IMutablePolynomialZp<T>> T compositionBrentKung(
            T poly,
            ArrayList<T> pointPowers,
            T polyModulus,
            InverseModMonomial<T> invMod,
            int tBrentKung) {
        if (poly.isConstant())
            return poly;
        ArrayList<T> gj = new ArrayList<>();
        int degree = poly.degree();
        for (int i = 0; i <= degree; ) {
            int to = i + tBrentKung;
            if (to > (degree + 1))
                to = degree + 1;
            T g = poly.getRange(i, to);
            gj.add(powModulusMod0(g, polyModulus, invMod, pointPowers));
            i = to;
        }
        T pt = pointPowers.get(tBrentKung);
        T res = poly.createZero();
        for (int i = gj.size() - 1; i >= 0; --i)
            res = polyMod(res.multiply(pt).add(gj.get(i)), polyModulus, invMod, false);
        return res;
    }

    /**
     * Returns modular composition {@code poly(point) mod polyModulus } calculated using Brent & Kung algorithm for modular composition.
     *
     * @param poly        the polynomial
     * @param point       the evaluation point
     * @param polyModulus the monic polynomial modulus
     * @param invMod      pre-conditioned modulus ({@link DivisionWithRemainder#fastDivisionPreConditioning(IMutablePolynomial)} )})
     * @return modular composition {@code poly(point) mod polyModulus }
     * @see DivisionWithRemainder#fastDivisionPreConditioning(IMutablePolynomial)
     */
    public static <T extends IMutablePolynomialZp<T>> T compositionBrentKung(T poly, T point, T polyModulus, InverseModMonomial<T> invMod) {
        if (poly.isConstant())
            return poly;
        int t = safeToInt(Math.sqrt(poly.degree()));
        ArrayList<T> hPowers = polyPowers(point, polyModulus, invMod, t);
        return compositionBrentKung(poly, hPowers, polyModulus, invMod, t);
    }

    private static int safeToInt(double dbl) {
        if (dbl > Integer.MAX_VALUE || dbl < Integer.MIN_VALUE)
            throw new ArithmeticException("int overflow");
        return (int) dbl;
    }

    /**
     * Returns modular composition {@code poly(point) mod polyModulus } calculated with plain Horner scheme.
     *
     * @param poly        the polynomial
     * @param point       the evaluation point
     * @param polyModulus the monic polynomial modulus
     * @param invMod      pre-conditioned modulus ({@link DivisionWithRemainder#fastDivisionPreConditioning(IMutablePolynomial)} )})
     * @return modular composition {@code poly(point) mod polyModulus }
     * @see DivisionWithRemainder#fastDivisionPreConditioning(IMutablePolynomial)
     */
    public static MutablePolynomialMod compositionHorner(MutablePolynomialMod poly, MutablePolynomialMod point, MutablePolynomialMod polyModulus, InverseModMonomial<MutablePolynomialMod> invMod) {
        if (poly.isConstant())
            return poly;
        MutablePolynomialMod res = poly.createZero();
        for (int i = poly.degree; i >= 0; --i)
            res = polyMod(res.multiply(point).addMonomial(poly.data[i], 0), polyModulus, invMod, false);
        return res;
    }

    /**
     * Returns modular composition {@code poly(point) mod polyModulus}. Brent & Kung algorithm used
     * ({@link #compositionBrentKung(IMutablePolynomialZp, ArrayList, IMutablePolynomialZp, InverseModMonomial, int)}).
     *
     * @param poly        the polynomial
     * @param point       the evaluation point
     * @param polyModulus the monic polynomial modulus
     * @return modular composition {@code poly(point) mod polyModulus }
     * @see #polyPowers(IMutablePolynomialZp, IMutablePolynomialZp, InverseModMonomial, int)
     * @see DivisionWithRemainder#fastDivisionPreConditioning(IMutablePolynomial)
     */
    public static <T extends IMutablePolynomialZp<T>> T composition(T poly, T point, T polyModulus) {
        return compositionBrentKung(poly, point, polyModulus, DivisionWithRemainder.fastDivisionPreConditioning(point));
    }
}
