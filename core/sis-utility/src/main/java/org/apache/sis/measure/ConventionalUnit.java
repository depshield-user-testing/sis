/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sis.measure;

import java.util.Map;
import javax.measure.Unit;
import javax.measure.Quantity;
import javax.measure.Dimension;
import javax.measure.UnitConverter;
import javax.measure.UnconvertibleException;
import javax.measure.IncommensurableException;
import org.apache.sis.util.resources.Errors;
import org.apache.sis.util.ArgumentChecks;
import org.apache.sis.math.Fraction;


/**
 * A unit of measure which is related to a base or derived unit through a conversion formula.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @since   0.8
 * @version 0.8
 * @module
 */
final class ConventionalUnit<Q extends Quantity<Q>> extends AbstractUnit<Q> {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = 6963634855104019466L;

    /**
     * The base, derived or alternate units to which this {@code ConventionalUnit} is related.
     * This is called "preferred unit" in GML.
     */
    final SystemUnit<Q> target;

    /**
     * The conversion from this unit to the {@linkplain #target} unit.
     */
    final UnitConverter toTarget;

    /**
     * Creates a new unit having the given symbol and EPSG code.
     *
     * @param  target     the base or derived units to which this {@code ConventionalUnit} is related.
     * @param  toTarget   the conversion from this unit to the {@code target} unit.
     * @param  symbol     the unit symbol, or {@code null} if this unit has no specific symbol.
     * @param  scope   {@link UnitRegistry#SI}, {@link UnitRegistry#ACCEPTED}, other constants or 0 if unknown.
     * @param  epsg       the EPSG code,   or 0 if this unit has no EPSG code.
     */
    ConventionalUnit(final SystemUnit<Q> target, final UnitConverter toTarget, final String symbol, final byte scope, final short epsg) {
        super(symbol, scope, epsg);
        this.target   = target;
        this.toTarget = toTarget;
    }

    /**
     * Creates a new unit with default name and symbol for the given converter.
     */
    static <Q extends Quantity<Q>> AbstractUnit<Q> create(final SystemUnit<Q> target, final UnitConverter toTarget) {
        if (toTarget.isIdentity()) {
            return target;
        }
        // TODO: check for existing unit.
        return new ConventionalUnit<>(target, toTarget, null, (byte) 0, (short) 0);
    }

    /**
     * Returns the dimension of this unit.
     * Two units {@code u1} and {@code u2} are {@linkplain #isCompatible(Unit) compatible}
     * if and only if {@code u1.getDimension().equals(u2.getDimension())}.
     *
     * @return the dimension of this unit.
     *
     * @see #isCompatible(Unit)
     */
    @Override
    public Dimension getDimension() {
        return target.dimension;
    }

    /**
     * Returns the unscaled system unit from which this unit is derived.
     */
    @Override
    public SystemUnit<Q> getSystemUnit() {
        return target;
    }

    /**
     * Returns the base units and their exponent whose product is the system unit,
     * or {@code null} if the system unit is a base unit (not a product of existing units).
     *
     * @return the base units and their exponent making up the system unit.
     */
    @Override
    public Map<SystemUnit<?>, Integer> getBaseUnits() {
        return target.getBaseUnits();
    }

    /**
     * Returns the base units used by Apache SIS implementations.
     * Contrarily to {@link #getBaseUnits()}, this method never returns {@code null}.
     */
    @Override
    final Map<SystemUnit<?>, Fraction> getBaseSystemUnits() {
        return target.getBaseSystemUnits();
    }

    /**
     * Casts this unit to a parameterized unit of specified nature or throw a {@code ClassCastException}
     * if the dimension of the specified quantity and this unit's dimension do not match.
     *
     * @param  <T>   the type of the quantity measured by the unit.
     * @param  type  the quantity class identifying the nature of the unit.
     * @return this unit parameterized with the specified type.
     * @throws ClassCastException if the dimension of this unit is different from the specified quantity dimension.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Quantity<T>> Unit<T> asType(final Class<T> type) throws ClassCastException {
        final Unit<T> alternate = target.asType(type);
        if (target.equals(alternate)) {
            return (Unit<T>) this;
        }
        return alternate.transform(toTarget);
    }

    /**
     * Returns a converter of numeric values from this unit to another unit of same type.
     *
     * @param  that  the unit of same type to which to convert the numeric values.
     * @return the converter from this unit to {@code that} unit.
     * @throws UnconvertibleException if the converter can not be constructed.
     */
    @Override
    public UnitConverter getConverterTo(final Unit<Q> that) throws UnconvertibleException {
        if (that == this) {
            return LinearConverter.IDENTITY;
        }
        ArgumentChecks.ensureNonNull("that", that);
        UnitConverter c = toTarget;
        if (target != that) {                           // Optimization for a common case.
            final Unit<Q> step = that.getSystemUnit();
            if (target != step && !target.equalsIgnoreMetadata(step)) {
                // Should never occur unless parameterized type has been compromised.
                throw new UnconvertibleException(incompatible(that));
            }
            c = step.getConverterTo(that).concatenate(c);
        }
        return c;
    }

    /**
     * Returns a converter from this unit to the specified unit of unknown type.
     * This method can be used when the quantity type of the specified unit is unknown at compile-time
     * or when dimensional analysis allows for conversion between units of different type.
     *
     * @param  that  the unit to which to convert the numeric values.
     * @return the converter from this unit to {@code that} unit.
     * @throws IncommensurableException if this unit is not {@linkplain #isCompatible(Unit) compatible} with {@code that} unit.
     *
     * @see #isCompatible(Unit)
     */
    @Override
    public UnitConverter getConverterToAny(final Unit<?> that) throws IncommensurableException {
        if (that == this) {
            return LinearConverter.IDENTITY;
        }
        ArgumentChecks.ensureNonNull("that", that);
        UnitConverter c = toTarget;
        if (target != that) {                           // Optimization for a common case.
            final Unit<?> step = that.getSystemUnit();
            if (target != step && !target.isCompatible(step)) {
                throw new IncommensurableException(incompatible(that));
            }
            c = step.getConverterToAny(that).concatenate(c);
        }
        return c;
    }

    /**
     * Unsupported operation for conventional units, as required by JSR-363 specification.
     *
     * @param  symbol  the new symbol for the alternate unit.
     * @return the alternate unit.
     * @throws UnsupportedOperationException always thrown since this unit is not an unscaled standard unit.
     *
     * @see SystemUnit#alternate(String)
     */
    @Override
    public Unit<Q> alternate(final String symbol) {
        throw new UnsupportedOperationException(Errors.format(Errors.Keys.NonSystemUnit_1, this));
    }

    /**
     * Ensures that the scale of measurement of this units is a ratio scale.
     */
    private void ensureRatioScale() {
        if (!toTarget.isLinear()) {
            throw new IllegalStateException(Errors.format(Errors.Keys.NonRatioUnit_1, this));
        }
    }

    /**
     * Returns the product of this unit with the one specified.
     *
     * @param  multiplier  the unit multiplier.
     * @return {@code this} × {@code multiplier}
     */
    @Override
    public Unit<?> multiply(final Unit<?> multiplier) {
        ensureRatioScale();
        return target.multiply(multiplier).transform(toTarget);
    }

    /**
     * Returns the quotient of this unit with the one specified.
     *
     * @param  divisor  the unit divisor.
     * @return {@code this} ÷ {@code divisor}
     */
    @Override
    public Unit<?> divide(final Unit<?> divisor) {
        ensureRatioScale();
        return target.divide(divisor).transform(toTarget);
    }

    /**
     * Returns a unit equals to this unit raised to an exponent.
     *
     * @param  n  the exponent.
     * @return the result of raising this unit to the exponent.
     */
    @Override
    public Unit<?> pow(final int n) {
        ensureRatioScale();
        final Unit<?> result = target.pow(n);
        return (result == target) ? this : result.transform(LinearConverter.pow(toTarget, n, false));
    }

    /**
     * Returns a unit equals to the given root of this unit.
     *
     * @param  n  the root's order.
     * @return the result of taking the given root of this unit.
     * @throws ArithmeticException if {@code n == 0}.
     */
    @Override
    public Unit<?> root(final int n) {
        ensureRatioScale();
        final Unit<?> result = target.root(n);
        return (result == target) ? this : result.transform(LinearConverter.pow(toTarget, n, true));
    }

    /**
     * Returns the unit derived from this unit using the specified converter.
     *
     * @param  operation  the converter from the transformed unit to this unit.
     * @return the unit after the specified transformation.
     */
    @Override
    public Unit<Q> transform(final UnitConverter operation) {
        ArgumentChecks.ensureNonNull("operation", operation);
        return create(target, toTarget.concatenate(operation));
    }

    /**
     * Compares this unit with the given object for equality.
     *
     * @param  other  the other object to compares with this unit, or {@code null}.
     * @return {@code true} if the given object is equals to this unit.
     */
    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (super.equals(other)) {
            final ConventionalUnit<?> that = (ConventionalUnit<?>) other;
            return target.equals(that.target) && toTarget.equals(that.toTarget);
        }
        return false;
    }

    /**
     * Returns a hash code value for this unit.
     */
    @Override
    public int hashCode() {
        return super.hashCode() + 37 * (target.hashCode() + 31 * toTarget.hashCode());
    }
}