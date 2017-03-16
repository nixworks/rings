package cc.r2.core.poly2;

/**
 * @author Stanislav Poslavsky
 * @since 1.0
 */
interface MutablePolynomial<T extends MutablePolynomial> {

    /**
     * Returns {@code true} if this is zero
     *
     * @return whether {@code this} is zero
     */
    boolean isZero();

    /**
     * Returns {@code true} if this is one
     *
     * @return whether {@code this} is one
     */
    boolean isOne();

    /**
     * Returns {@code true} if this polynomial is monic
     *
     * @return whether {@code this} is monic
     */
    boolean isMonic();

    /**
     * Returns {@code true} if this polynomial has only constant term
     *
     * @return whether {@code this} is constant
     */
    boolean isConstant();

    /**
     * Returns {@code true} if this polynomial has only one monomial term
     *
     * @return whether {@code this} has the form {@code c*x^i} (one term)
     */
    boolean isMonomial();

    /** fill content with zeroes */
    T toZero();

    /**
     * Sets the content of this to {@code oth}
     *
     * @param oth the polynomial
     * @return this := oth
     */
    T set(T oth);

    /**
     * Returns the quotient {@code this / x^offset}, it is polynomial with coefficient list formed by shifting coefficients
     * of {@code this} to the left by {@code offset}.
     *
     * @param offset shift amount
     * @return the quotient {@code this / x^offset}
     */
    T shiftLeft(int offset);

    /**
     * Multiplies {@code this} by the {@code x^offset}.
     *
     * @param offset monomial exponent
     * @return {@code this * x^offset}
     */
    T shiftRight(int offset);

    /**
     * Returns the remainder {@code this rem x^(newDegree + 1)}, it is polynomial with the coefficient list truncated
     * to the new degree {@code newDegree}.
     *
     * @param newDegree new degree
     * @return remainder {@code this rem x^(newDegree + 1)}
     */
    T truncate(int newDegree);

    /**
     * Reverses the coefficients of this
     *
     * @return reversed polynomial
     */
    T reverse();

    /**
     * Reduces poly to its primitive part
     *
     * @return primitive part (poly will be modified)
     */
    T primitivePart();

    /**
     * Adds 1 to this
     *
     * @return {@code this + 1}
     */
    T increment();

    /**
     * Subtracts 1 from this
     *
     * @return {@code this - 1}
     */
    T decrement();

    /**
     * Returns 0 (new instance)
     *
     * @return new instance of 0
     */
    T createZero();

    /**
     * Returns 1 (new instance)
     *
     * @return new instance of 1
     */
    T createOne();


    /**
     * Adds {@code oth} to {@code this}.
     *
     * @param oth the polynomial
     * @return {@code this + oth}
     */
    T add(T oth);

    /**
     * Subtracts {@code oth} from {@code this}.
     *
     * @param oth the polynomial
     * @return {@code this - oth}
     */
    T subtract(T oth);


    /**
     * Negates this and returns
     *
     * @return this negated
     */
    T negate();

    /**
     * Sets this to {@code this * oth }
     *
     * @param oth the polynomial
     * @return {@code this * oth }
     */
    T multiply(T oth);

    /**
     * Square of {@code this}
     *
     * @return {@code this * this}
     */
    T square();

    /**
     * Returns the formal derivative of this poly
     *
     * @return the formal derivative
     */
    T derivative();

    /**
     * Deep copy of this
     *
     * @return deep copy of this
     */
    T clone();
}