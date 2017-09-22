package cc.redberry.rings.poly.univar;

import cc.redberry.rings.poly.FactorDecomposition;
import cc.redberry.rings.poly.Util;

import java.util.ArrayList;

import static cc.redberry.rings.poly.univar.ModularComposition.*;
import static cc.redberry.rings.poly.univar.UnivariateGCD.PolynomialGCD;
import static cc.redberry.rings.poly.univar.UnivariatePolynomialArithmetic.polyMultiplyMod;


/**
 * Distinct-degree factorization of univariate polynomials over finite fields.
 *
 * @since 1.0
 */
public final class DistinctDegreeFactorization {
    private DistinctDegreeFactorization() {}

    /**
     * Performs distinct-degree factorization for square-free polynomial {@code poly} using plain incremental exponents
     * algorithm.
     *
     * <p> In the case of not square-free input, the algorithm works, but the resulting d.d.f. may be incomplete.
     *
     * @param poly the polynomial
     * @return distinct-degree decomposition of {@code poly}
     */
    public static FactorDecomposition<UnivariatePolynomialZp64> DistinctDegreeFactorizationPlain(UnivariatePolynomialZp64 poly) {
        if (poly.isConstant())
            return FactorDecomposition.constantFactor(poly);

        long factor = poly.lc();
        UnivariatePolynomialZp64 base = poly.clone().monic();
        UnivariatePolynomialZp64 polyModulus = base.clone();

        if (base.degree <= 1)
            return FactorDecomposition.singleFactor(base.createConstant(factor), base);

        if (base.isMonomial())
            return FactorDecomposition.singleFactor(base.createConstant(factor), base);

        UnivariateDivision.InverseModMonomial<UnivariatePolynomialZp64> invMod = UnivariateDivision.fastDivisionPreConditioning(polyModulus);
        UnivariatePolynomialZp64 exponent = poly.createMonomial(1);
        FactorDecomposition<UnivariatePolynomialZp64> result = FactorDecomposition.constantFactor(poly.createOne());
        int i = 0;
        while (!base.isConstant()) {
            ++i;
            exponent = UnivariatePolynomialArithmetic.polyPowMod(exponent, poly.ring.modulus, polyModulus, invMod, false);
            UnivariatePolynomialZp64 tmpExponent = exponent.clone();
            tmpExponent.ensureCapacity(1);
            tmpExponent.data[1] = base.subtract(tmpExponent.data[1], 1);
            tmpExponent.fixDegree();
            UnivariatePolynomialZp64 gcd = PolynomialGCD(tmpExponent, base);
            if (!gcd.isConstant())
                result.addFactor(gcd.monic(), i);


            base = UnivariateDivision.quotient(base, gcd, false); //can safely destroy reused base
            if (base.degree < 2 * (i + 1)) {// <- early termination
                if (!base.isConstant())
                    result.addFactor(base.monic(), base.degree);
                break;
            }
        }
        return result.setConstantFactor(poly.createConstant(factor));
    }

    /**
     * Performs distinct-degree factorization for square-free polynomial {@code poly} using plain incremental exponents
     * algorithm with precomputed exponents.
     *
     * <p> In the case of not square-free input, the algorithm works, but the resulting d.d.f. may be incomplete.
     *
     * @param poly the polynomial
     * @return distinct-degree decomposition of {@code poly}
     */
    public static FactorDecomposition<UnivariatePolynomialZp64> DistinctDegreeFactorizationPrecomputedExponents(UnivariatePolynomialZp64 poly) {
        if (poly.isConstant())
            return FactorDecomposition.constantFactor(poly);

        long factor = poly.lc();
        UnivariatePolynomialZp64 base = poly.clone().monic();
        UnivariatePolynomialZp64 polyModulus = base.clone();

        if (base.degree <= 1)
            return FactorDecomposition.singleFactor(base.createConstant(factor), base);

        if (base.isMonomial())
            return FactorDecomposition.singleFactor(base.createConstant(factor), base);

        UnivariateDivision.InverseModMonomial<UnivariatePolynomialZp64> invMod = UnivariateDivision.fastDivisionPreConditioning(polyModulus);
        UnivariatePolynomialZp64 exponent = poly.createMonomial(1);
        FactorDecomposition<UnivariatePolynomialZp64> result = FactorDecomposition.constantFactor(poly.createOne());

        ArrayList<UnivariatePolynomialZp64> xPowers = xPowers(polyModulus, invMod);
        int i = 0;
        while (!base.isConstant()) {
            ++i;
            exponent = powModulusMod(exponent, polyModulus, invMod, xPowers);
            UnivariatePolynomialZp64 tmpExponent = exponent.clone();
            tmpExponent.ensureCapacity(1);
            tmpExponent.data[1] = poly.subtract(tmpExponent.data[1], 1);
            tmpExponent.fixDegree();
            UnivariatePolynomialZp64 gcd = PolynomialGCD(tmpExponent, base);
            if (!gcd.isConstant())
                result.addFactor(gcd.monic(), i);

            base = UnivariateDivision.quotient(base, gcd, false); //can safely destroy reused base
            if (base.degree < 2 * (i + 1)) {// <- early termination
                if (!base.isConstant())
                    result.addFactor(base.monic(), base.degree);
                break;
            }
        }
        return result.setConstantFactor(poly.createConstant(factor));
    }

    /** Shoup's parameter */
    private static final double SHOUP_BETA = 0.5;

    /** Baby step / giant step components for d.d.f. in Shoup's algorithm */
    private static <Poly extends IUnivariatePolynomial<Poly>> BabyGiantSteps<Poly> generateBabyGiantSteps(Poly poly) {
        int n = poly.degree();
        int l = (int) Math.ceil(Math.pow(1.0 * n, SHOUP_BETA));
        int m = (int) Math.ceil(1.0 * n / 2 / l);

        UnivariateDivision.InverseModMonomial<Poly> invMod = UnivariateDivision.fastDivisionPreConditioning(poly);
        ArrayList<Poly> xPowers = xPowers(poly, invMod);

        //baby steps
        ArrayList<Poly> babySteps = new ArrayList<>();
        babySteps.add(poly.createMonomial(1)); // <- add x
        Poly xPower = xPowers.get(1); // x^p mod poly
        babySteps.add(xPower); // <- add x^p mod poly
        for (int i = 0; i <= l - 2; ++i)
            babySteps.add(xPower = powModulusMod(xPower, poly, invMod, xPowers));

        // <- xPower = x^(p^l) mod poly

        //giant steps
        ArrayList<Poly> giantSteps = new ArrayList<>();
        giantSteps.add(poly.createMonomial(1)); // <- add x
        giantSteps.add(xPower);
        Poly xPowerBig = xPower;
        int tBrentKung = (int) Math.sqrt(poly.degree());
        ArrayList<Poly> hPowers = polyPowers(xPowerBig, poly, invMod, tBrentKung);
        for (int i = 0; i < m - 1; ++i)
            giantSteps.add(xPowerBig = compositionBrentKung(xPowerBig, hPowers, poly, invMod, tBrentKung));

        return new BabyGiantSteps<>(l, m, babySteps, giantSteps, invMod);
    }

    /** Shoup's main gcd loop */
    private static <T extends IUnivariatePolynomial<T>> void DistinctDegreeFactorizationShoup(T poly,
                                                                                              BabyGiantSteps<T> steps,
                                                                                              FactorDecomposition<T> result) {
        //generate each I_j
        ArrayList<T> iBases = new ArrayList<>();
        for (int j = 0; j <= steps.m; ++j) {
            T iBase = poly.createOne();
            for (int i = 0; i <= steps.l - 1; ++i) {
                T tmp = steps.giantSteps.get(j).clone().subtract(steps.babySteps.get(i));
                iBase = polyMultiplyMod(iBase, tmp, poly, steps.invMod, false);
            }
            iBases.add(iBase);
        }

        T current = poly.clone();
        for (int j = 1; j <= steps.m; ++j) {
            T gcd = UnivariateGCD.PolynomialGCD(current, iBases.get(j));
            if (gcd.isConstant())
                continue;
            current = UnivariateDivision.quotient(current, gcd, false);
            for (int i = steps.l - 1; i >= 0; --i) {
                T tmp = UnivariateGCD.PolynomialGCD(gcd, steps.giantSteps.get(j).clone().subtract(steps.babySteps.get(i)));
                if (!tmp.isConstant())
                    result.addFactor(tmp.clone().monic(), steps.l * j - i);

                gcd = UnivariateDivision.quotient(gcd, tmp, false);
            }
        }
        if (!current.isOne())
            result.addFactor(current.monic(), current.degree());
    }

    /**
     * Performs distinct-degree factorization for square-free polynomial {@code poly} using Victor Shoup's baby step /
     * giant step algorithm.
     *
     * <p> In the case of not square-free input, the algorithm works, but the resulting d.d.f. may be incomplete.
     *
     * @param poly the polynomial
     * @return distinct-degree decomposition of {@code poly}
     */
    public static <Poly extends IUnivariatePolynomial<Poly>> FactorDecomposition<Poly> DistinctDegreeFactorizationShoup(Poly poly) {
        Util.ensureOverFiniteField(poly);
        Poly factor = poly.lcAsPoly();
        poly = poly.clone().monic();
        FactorDecomposition<Poly> result = FactorDecomposition.constantFactor(factor);
        DistinctDegreeFactorizationShoup(poly, generateBabyGiantSteps(poly), result);
        return result;
    }

    /** baby/giant steps for Shoup's d.d.f. algorithm */
    private static final class BabyGiantSteps<Poly extends IUnivariatePolynomial<Poly>> {
        final int l, m;
        final ArrayList<Poly> babySteps;
        final ArrayList<Poly> giantSteps;
        final UnivariateDivision.InverseModMonomial<Poly> invMod;

        public BabyGiantSteps(int l, int m, ArrayList<Poly> babySteps, ArrayList<Poly> giantSteps, UnivariateDivision.InverseModMonomial<Poly> invMod) {
            this.l = l;
            this.m = m;
            this.babySteps = babySteps;
            this.giantSteps = giantSteps;
            this.invMod = invMod;
        }
    }

    /** when to switch to Shoup's algorithm */
    private static final int DEGREE_SWITCH_TO_SHOUP = 256;

    /**
     * Performs distinct-degree factorization for square-free polynomial {@code poly}.
     *
     * <p> In the case of not square-free input, the algorithm works, but the resulting d.d.f. may be incomplete.
     *
     * @param poly the polynomial
     * @return distinct-degree decomposition of {@code poly}
     */
    public static FactorDecomposition<UnivariatePolynomialZp64> DistinctDegreeFactorization(UnivariatePolynomialZp64 poly) {
        if (poly.degree < DEGREE_SWITCH_TO_SHOUP)
            return DistinctDegreeFactorizationPrecomputedExponents(poly);
        else
            return DistinctDegreeFactorizationShoup(poly);
    }

    /**
     * Performs distinct-degree factorization for square-free polynomial {@code poly}.
     *
     * <p> In the case of not square-free input, the algorithm works, but the resulting d.d.f. may be incomplete.
     *
     * @param poly the polynomial
     * @return distinct-degree decomposition of {@code poly}
     */
    @SuppressWarnings("unchecked")
    public static <Poly extends IUnivariatePolynomial<Poly>> FactorDecomposition<Poly> DistinctDegreeFactorization(Poly poly) {
        Util.ensureOverFiniteField(poly);
        if (poly instanceof UnivariatePolynomialZp64)
            return (FactorDecomposition<Poly>) DistinctDegreeFactorization((UnivariatePolynomialZp64) poly);
        else
            return DistinctDegreeFactorizationShoup(poly);
    }


    /**
     * Performs square-free factorization followed by distinct-degree factorization modulo {@code modulus}.
     *
     * @param poly the polynomial
     * @return square-free and distinct-degree decomposition of {@code poly} modulo {@code modulus}
     */
    static FactorDecomposition<UnivariatePolynomialZp64> DistinctDegreeFactorizationComplete(UnivariatePolynomialZp64 poly) {
        FactorDecomposition<UnivariatePolynomialZp64> squareFree = UnivariateSquareFreeFactorization.SquareFreeFactorization(poly);
        long overallFactor = squareFree.constantFactor.lc();

        FactorDecomposition<UnivariatePolynomialZp64> result = FactorDecomposition.constantFactor(poly.createOne());
        for (int i = squareFree.size() - 1; i >= 0; --i) {
            FactorDecomposition<UnivariatePolynomialZp64> dd = DistinctDegreeFactorization(squareFree.get(i));
            int nFactors = dd.size();
            for (int j = nFactors - 1; j >= 0; --j)
                result.addFactor(dd.get(j), squareFree.getExponent(i));
            overallFactor = poly.multiply(overallFactor, dd.constantFactor.lc());
        }

        return result.setConstantFactor(poly.createConstant(overallFactor));
    }
}