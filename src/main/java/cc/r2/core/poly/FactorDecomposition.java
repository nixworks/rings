package cc.r2.core.poly;

import cc.r2.core.util.ArraysUtil;
import gnu.trove.list.array.TIntArrayList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cc.r2.core.poly.PolynomialMethods.polyPow;

/**
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public final class FactorDecomposition<Poly extends IPolynomial<Poly>> implements Iterable<Poly>, java.io.Serializable {
    private static final long serialVersionUID = 1L;
    /** Holds a numerical factor */
    public final Poly constantFactor;
    /** Factors */
    public final List<Poly> factors;
    /** Either exponents or distinct-degree powers */
    public final TIntArrayList exponents;

    private FactorDecomposition(Poly constantFactor) {
        this(constantFactor, new ArrayList<>(), new TIntArrayList());
    }

    private FactorDecomposition(Poly constantFactor, List<Poly> factors, TIntArrayList exponents) {
        if (!constantFactor.isConstant())
            throw new IllegalArgumentException("Factor " + constantFactor + " is not a constant.");
        this.constantFactor = constantFactor;
        this.factors = factors;
        this.exponents = exponents;
    }

    @Override
    public Iterator<Poly> iterator() {
        return factors.iterator();
    }

    /** Returns i-th factor */
    public Poly get(int i) { return factors.get(i); }

    /** Exponent of i-th factor */
    public int getExponent(int i) { return exponents.get(i); }

    /** Number of factors */
    public int size() { return factors.size(); }

    /** The constant factor */
    public FactorDecomposition<Poly> setConstantFactor(Poly constantFactor) {
        this.constantFactor.set(constantFactor);
        return this;
    }

    /** Whether there are no non-trivial factors */
    public boolean isTrivial() { return size() == 1;}

    /** Sum all exponents */
    public int sumExponents() {
        return exponents.sum();
    }

    /** Multiply each exponent by a given factor */
    public void raiseExponents(long val) {
        for (int i = exponents.size() - 1; i >= 0; --i)
            exponents.set(i, MachineArithmetic.safeToInt(exponents.get(i) * val));
    }

    /** add another factor */
    public FactorDecomposition<Poly> addConstantFactor(Poly poly) {
        if (!poly.isConstant())
            throw new RuntimeException("not a constant");
        constantFactor.multiply(poly);
        return this;
    }

    /** add another factor */
    public FactorDecomposition<Poly> addFactor(Poly poly, int exponent) {
        if (poly.isConstant()) {
            if (exponent != 1)
                throw new IllegalArgumentException("exponent != 1");
            return addConstantFactor(poly);
        }

        factors.add(poly);
        exponents.add(exponent);
        return this;
    }

    /** add another factor */
    public FactorDecomposition<Poly> addAll(FactorDecomposition<Poly> other) {
        factors.addAll(other.factors);
        exponents.addAll(other.exponents);
        constantFactor.multiply(other.constantFactor);
        return this;
    }

    /** Canonical form of factor list */
    @SuppressWarnings("unchecked")
    public FactorDecomposition<Poly> canonicalForm() {
        if (factors.size() == 0)
            return this;
        Poly[] fTmp = factors.toArray(factors.get(0).createArray(factors.size()));
        int[] eTmp = exponents.toArray();
        for (int i = fTmp.length - 1; i >= 0; --i) {
            Poly poly = fTmp[i];
            if (poly.isMonomial() && eTmp[i] != 1) {
                poly = PolynomialMethods.polyPow(poly, eTmp[i], false);
                assert poly.isMonomial();
            }
            if (poly.signumOfLC() < 0) {
                poly.negate();
                if (eTmp[i] % 2 == 1)
                    constantFactor.negate();
            }
        }

        ArraysUtil.quickSort(fTmp, eTmp);
        for (int i = 0; i < fTmp.length; i++) {
            factors.set(i, fTmp[i]);
            exponents.set(i, eTmp[i]);
        }
        return this;
    }

    public int signum() {
        int signum = constantFactor.signumOfLC();
        for (int i = 0; i < factors.size(); i++)
            signum *= exponents.get(i) % 2 == 0 ? 1 : factors.get(i).signumOfLC();
        return signum;
    }

    public FactorDecomposition<Poly> withoutConstantFactor() {
        return new FactorDecomposition<>(constantFactor.createOne(), factors, exponents);
    }

    public FactorDecomposition<Poly> monic() {
        for (int i = 0; i < factors.size(); i++) {
            Poly factor = factors.get(i);
            addConstantFactor(polyPow(factor.lcAsPoly(), exponents.get(i), false));
            factor = factor.monic();
            assert factor != null;
        }
        return this;
    }

    public FactorDecomposition<Poly> primitive() {
        for (int i = 0; i < factors.size(); i++) {
            Poly factor = factors.get(i);
            Poly content = factor.contentAsPoly();
            addConstantFactor(polyPow(content, exponents.get(i), false));
            factor = factor.divideByLC(content);
            assert factor != null;
            if (factor.signumOfLC() < 0) {
                factor.negate();
                if (exponents.get(i) % 2 == 1)
                    constantFactor.negate();
            }
        }
        return this;
    }

    public FactorDecomposition<Poly> reduceConstantContent() {
        return constantFactor.isOverField() ? monic() : primitive();
    }

    /** Map polynomials using mapper */
    public <PolyT extends IPolynomial<PolyT>>
    FactorDecomposition<PolyT> map(Function<Poly, PolyT> mapper) {
        return new FactorDecomposition<>(mapper.apply(constantFactor),
                factors.stream().map(mapper).collect(Collectors.toList()),
                exponents);
    }

    public Stream<Poly> stream() {
        return Stream.concat(Stream.of(constantFactor), factors.stream());
    }

    public Stream<Poly> streamWithoutConstant() {
        return factors.stream();
    }

    public Poly[] factorsArrayWithoutLC() {
        Poly[] array = constantFactor.createArray(size());
        return factors.toArray(array);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FactorDecomposition<?> that = (FactorDecomposition<?>) o;

        if (!constantFactor.equals(that.constantFactor)) return false;
        if (factors != null ? !factors.equals(that.factors) : that.factors != null) return false;
        return exponents != null ? exponents.equals(that.exponents) : that.exponents == null;
    }

    @Override
    public int hashCode() {
        int result = factors != null ? factors.hashCode() : 0;
        result = 31 * result + (exponents != null ? exponents.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return toString(true);
    }

    protected String toString(boolean infoAsExponents) {
        return toStringFactorization(factors, exponents, constantFactor, infoAsExponents);
    }

    /** pretty print for factorization */
    private static String toStringFactorization(List factors, TIntArrayList exponents, IPolynomial factor, boolean infoAsExponents) {
        if (factors.isEmpty())
            return factor.toString();

        StringBuilder sb = new StringBuilder();
        if (!factor.isOne()) {
            sb.append(factor);
            if (factors.size() > 0)
                sb.append("*");
        }
        for (int i = 0; ; i++) {
            sb.append("(").append(factors.get(i)).append(")");
            if (infoAsExponents && exponents.get(i) != 1)
                sb.append("^").append(exponents.get(i));
            if (i == factors.size() - 1)
                return sb.toString();
            sb.append("*");
        }
    }

    /** multiply factors */
    public Poly toPolynomial() {
        return toPolynomial(false);
    }

    /** multiply DDF factors */
    public Poly toPolynomialIgnoringExponents() {
        return toPolynomial(true);
    }

    /** square-free part */
    public Poly squareFreePart() {
        return toPolynomialIgnoringExponents();
    }

    private Poly toPolynomial(boolean ignoreExps) {
        Poly r = constantFactor.clone();
        for (int i = 0; i < factors.size(); i++) {
            Poly tmp = ignoreExps ? factors.get(i) : PolynomialMethods.polyPow(factors.get(i), exponents.get(i), true);
            r = r.multiply(tmp);
        }
        return r;
    }

    @Override
    public FactorDecomposition<Poly> clone() {
        return new FactorDecomposition<>(
                constantFactor.clone(),
                factors.stream().map(Poly::clone).collect(Collectors.toList()),
                new TIntArrayList(exponents));
    }

    /** decomposition with single numeric factor */
    public static <Poly extends IPolynomial<Poly>> FactorDecomposition<Poly> empty(Poly factory) {
        return new FactorDecomposition<>(factory.createOne());
    }

    /** decomposition with single numeric factor */
    public static <Poly extends IPolynomial<Poly>> FactorDecomposition<Poly> constantFactor(Poly factor) {
        return new FactorDecomposition<>(factor);
    }

    /** decomposition with single factor */
    public static <Poly extends IPolynomial<Poly>> FactorDecomposition<Poly> singleFactor(Poly constantFactor, Poly poly) {
        FactorDecomposition<Poly> ts = new FactorDecomposition<>(constantFactor);
        ts.addFactor(poly, 1);
        return ts;
    }

    /** decomposition with single factor */
    public static <Poly extends IPolynomial<Poly>> FactorDecomposition<Poly> singleFactor(Poly poly) {
        if (poly.isConstant())
            return constantFactor(poly);
        return singleFactor(poly.createOne(), poly);
    }

    public static <Poly extends IPolynomial<Poly>> FactorDecomposition<Poly> create(List<Poly> factors) {
        return create(factors.get(0).createOne(), new ArrayList<>(factors));
    }

    public static <Poly extends IPolynomial<Poly>> FactorDecomposition<Poly> create(Poly constantFactor, List<Poly> factors) {
        return create(constantFactor, factors, new TIntArrayList(ArraysUtil.arrayOf(1, factors.size())));
    }

    public static <Poly extends IPolynomial<Poly>> FactorDecomposition<Poly> create(Poly constantFactor, List<Poly> factors, TIntArrayList exponents) {
        return new FactorDecomposition<>(constantFactor, factors, exponents);
    }
}
