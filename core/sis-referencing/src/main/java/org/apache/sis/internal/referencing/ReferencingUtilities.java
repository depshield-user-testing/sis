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
package org.apache.sis.internal.referencing;

import java.util.Collection;
import java.util.logging.Logger;
import javax.measure.unit.Unit;
import org.opengis.annotation.UML;
import org.opengis.annotation.Specification;
import org.opengis.referencing.cs.*;
import org.opengis.referencing.crs.*;
import org.opengis.referencing.datum.Datum;
import org.opengis.referencing.datum.GeodeticDatum;
import org.apache.sis.util.Static;
import org.apache.sis.util.Utilities;
import org.apache.sis.util.logging.Logging;
import org.apache.sis.util.resources.Errors;
import org.apache.sis.internal.jaxb.Context;
import org.apache.sis.referencing.CommonCRS;
import org.apache.sis.referencing.crs.DefaultGeographicCRS;
import org.apache.sis.referencing.cs.AxesConvention;
import org.apache.sis.referencing.datum.DefaultGeodeticDatum;

import static java.util.Collections.singletonMap;
import static org.opengis.referencing.IdentifiedObject.NAME_KEY;


/**
 * A set of static methods working on GeoAPI referencing objects.
 * Some of those methods may be useful, but not really rigorous.
 * This is why they do not appear in the public packages.
 *
 * <p><strong>Do not rely on this API!</strong> It may change in incompatible way in any future release.</p>
 *
 * @author  Martin Desruisseaux (IRD, Geomatys)
 * @since   0.5 (derived from geotk-2.0)
 * @version 0.5
 * @module
 */
public final class ReferencingUtilities extends Static {
    /**
     * The logger to use for messages related to the {@code sis-referencing} module.
     */
    public static final Logger LOGGER = Logging.getLogger("org.apache.sis.referencing");

    /**
     * Do not allow instantiation of this class.
     */
    private ReferencingUtilities() {
    }

    /**
     * Returns the WKT type of the given interface.
     *
     * For {@link CoordinateSystem} base type, the returned value shall be one of
     * {@code affine}, {@code Cartesian}, {@code cylindrical}, {@code ellipsoidal}, {@code linear},
     * {@code parametric}, {@code polar}, {@code spherical}, {@code temporal} or {@code vertical}.
     *
     * @param  base The abstract base interface.
     * @param  type The interface or classes for which to get the WKT type.
     * @return The WKT type for the given class or interface, or {@code null} if none.
     */
    public static String toWKTType(final Class<?> base, final Class<?> type) {
        if (type != base) {
            final UML uml = type.getAnnotation(UML.class);
            if (uml != null && uml.specification() == Specification.ISO_19111) {
                String name = uml.identifier();
                final int length = name.length() - 5; // Length without "CS_" and "CS".
                if (length >= 1 && name.startsWith("CS_") && name.endsWith("CS")) {
                    final StringBuilder buffer = new StringBuilder(length).append(name, 3, 3 + length);
                    if (!name.regionMatches(3, "Cartesian", 0, 9)) {
                        buffer.setCharAt(0, Character.toLowerCase(buffer.charAt(0)));
                    }
                    name = buffer.toString();
                    if (name.equals("time")) {
                        name = "temporal";
                    }
                    return name;
                }
            }
            for (final Class<?> c : type.getInterfaces()) {
                if (base.isAssignableFrom(c)) {
                    final String name = toWKTType(base, c);
                    if (name != null) {
                        return name;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns the unit used for all axes in the given coordinate system.
     * If not all axes use the same unit, then this method returns {@code null}.
     *
     * <p>This method is used either when the coordinate system is expected to contain exactly one axis,
     * or for operations that support only one units for all axes, for example Well Know Text version 1
     * (WKT 1) formatting.</p>
     *
     * @param cs The coordinate system for which to get the unit, or {@code null}.
     * @return The unit for all axis in the given coordinate system, or {@code null}.
     */
    public static Unit<?> getUnit(final CoordinateSystem cs) {
        Unit<?> unit = null;
        if (cs != null) {
            for (int i=cs.getDimension(); --i>=0;) {
                final CoordinateSystemAxis axis = cs.getAxis(i);
                if (axis != null) { // Paranoiac check.
                    final Unit<?> candidate = axis.getUnit();
                    if (candidate != null) {
                        if (unit == null) {
                            unit = candidate;
                        } else if (!unit.equals(candidate)) {
                            return null;
                        }
                    }
                }
            }
        }
        return unit;
    }

    /**
     * Copies all {@link SingleCRS} components from the given source to the given collection.
     * For each {@link CompoundCRS} element found in the iteration, this method replaces the
     * {@code CompoundCRS} by its {@linkplain CompoundCRS#getComponents() components}, which
     * may themselves have other {@code CompoundCRS}. Those replacements are performed recursively
     * until we obtain a flat view of CRS components.
     *
     * @param  source The collection of single or compound CRS.
     * @param  addTo  Where to add the single CRS in order to obtain a flat view of {@code source}.
     * @return {@code true} if this method found only single CRS in {@code source}, in which case {@code addTo}
     *         got the same content (assuming that {@code addTo} was empty prior this method call).
     * @throws ClassCastException if a CRS is neither a {@link SingleCRS} or a {@link CompoundCRS}.
     *
     * @see org.apache.sis.referencing.CRS#getSingleComponents(CoordinateReferenceSystem)
     */
    public static boolean getSingleComponents(final Iterable<? extends CoordinateReferenceSystem> source,
            final Collection<? super SingleCRS> addTo) throws ClassCastException
    {
        boolean sameContent = true;
        for (final CoordinateReferenceSystem candidate : source) {
            if (candidate instanceof CompoundCRS) {
                getSingleComponents(((CompoundCRS) candidate).getComponents(), addTo);
                sameContent = false;
            } else {
                // Intentional CassCastException here if the candidate is not a SingleCRS.
                addTo.add((SingleCRS) candidate);
            }
        }
        return sameContent;
    }

    /**
     * Derives a geographic CRS with (<var>longitude</var>, <var>latitude</var>) axis order
     * in decimal degrees, relative to Greenwich. If no such CRS can be obtained of created,
     * returns null.
     *
     * <p>This method is similar to the use of {@link AxesConvention#NORMALIZED} except for the following:</p>
     * <ul>
     *   <li>The datum prime meridian is set to Greenwich.</li>
     *   <li>This method does not require an Apache SIS implementation of the CRS object.</li>
     * </ul>
     *
     * @param  crs A source CRS, or {@code null}.
     * @return A two-dimensional geographic CRS with standard axes, or {@code null} if none.
     */
    public static GeographicCRS toNormalizedGeographicCRS(CoordinateReferenceSystem crs) {
        /*
         * ProjectedCRS instances always have a GeographicCRS as their base.
         * More generally, derived CRS are always derived from a base, which
         * is often (but not necessarily) geographic.
         */
        while (crs instanceof GeneralDerivedCRS) {
            crs = ((GeneralDerivedCRS) crs).getBaseCRS();
        }
        /*
         * At this point we usually have a GeographicCRS, but it could also be a GeocentricCRS.
         * In any case if the datum does not have the Greenwich prime meridian, we need to fix.
         */
        if (crs instanceof GeodeticCRS) {
            GeodeticDatum datum = ((GeodeticCRS) crs).getDatum();
            if (datum.getPrimeMeridian().getGreenwichLongitude() != 0) {
                datum = new DefaultGeodeticDatum(singletonMap(NAME_KEY, datum.getName().getCode()),
                        datum.getEllipsoid(), CommonCRS.WGS84.primeMeridian());
            } else if (crs instanceof DefaultGeographicCRS) {
                return ((DefaultGeographicCRS) crs).forConvention(AxesConvention.NORMALIZED);
            }
            final CoordinateSystem cs = CommonCRS.defaultGeographic().getCoordinateSystem();
            if (crs instanceof GeographicCRS && Utilities.equalsIgnoreMetadata(cs, crs.getCoordinateSystem())) {
                return (GeographicCRS) crs;
            }
            return new DefaultGeographicCRS(singletonMap(DefaultGeographicCRS.NAME_KEY, crs.getName().getCode()),
                    datum, (EllipsoidalCS) cs);
        }
        if (crs instanceof CompoundCRS) {
            for (final CoordinateReferenceSystem e : ((CompoundCRS) crs).getComponents()) {
                final GeographicCRS candidate = toNormalizedGeographicCRS(e);
                if (candidate != null) {
                    return candidate;
                }
            }
        }
        return null;
    }

    /**
     * Ensures that the given argument value is {@code false}. This method is invoked by private setter methods,
     * which are themselves invoked by JAXB at unmarshalling time. Invoking this method from those setter methods
     * serves two purposes:
     *
     * <ul>
     *   <li>Make sure that a singleton property is not defined twice in the XML document.</li>
     *   <li>Protect ourselves against changes in immutable objects outside unmarshalling. It should
     *       not be necessary since the setter methods shall not be public, but we are paranoiac.</li>
     *   <li>Be a central point where we can trace all setter methods, in case we want to improve
     *       warning or error messages in future SIS versions.</li>
     * </ul>
     *
     * @param  classe    The caller class, used only in case of warning message to log.
     * @param  method    The caller method, used only in case of warning message to log.
     * @param  name      The property name, used only in case of error message to format.
     * @param  isDefined Whether the property in the caller object is current defined.
     * @return {@code true} if the caller can set the property.
     * @throws IllegalStateException If {@code isDefined} is {@code true} and we are not unmarshalling an object.
     */
    public static boolean canSetProperty(final Class<?> classe, final String method,
            final String name, final boolean isDefined) throws IllegalStateException
    {
        if (!isDefined) {
            return true;
        }
        final Context context = Context.current();
        if (context != null) {
            Context.warningOccured(context, LOGGER, classe, method, Errors.class, Errors.Keys.ElementAlreadyPresent_1, name);
            return false;
        } else {
            throw new IllegalStateException(Errors.format(Errors.Keys.ElementAlreadyPresent_1, name));
        }
    }
}