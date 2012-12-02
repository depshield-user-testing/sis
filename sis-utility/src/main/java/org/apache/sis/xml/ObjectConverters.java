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
package org.apache.sis.xml;

import java.net.URI;
import java.net.URL;
import java.net.URISyntaxException;
import java.net.MalformedURLException;
import java.util.Locale;
import java.util.UUID;
import javax.measure.unit.Unit;
import org.apache.sis.measure.Units;
import org.apache.sis.util.Locales;

import static org.apache.sis.util.CharSequences.trimWhitespaces;


/**
 * Performs conversions of objects encountered during XML (un)marshalling. Each method in this
 * class is a converter and can be invoked at (un)marshalling time. The default implementation
 * is straightforward and documented in the javadoc of each method.
 *
 * <p>This class provides a way to handle the errors which may exist in some XML documents.
 * For example a URL in the document may be malformed, causing a {@link MalformedURLException}
 * to be thrown. If this error is not handled, it will cause the (un)marshalling of the entire
 * document to fail. An application may want to change this behavior by replacing URLs that
 * are known to be erroneous by fixed versions of those URLs. Example:</p>
 *
 * {@preformat java
 *     class URLFixer extends ObjectConverters {
 *         &#64;Override
 *         public URL toURL(MarshalContext context, URI uri) throws MalformedURLException {
 *             try {
 *                 return super.toURL(context, uri);
 *             } catch (MalformedURLException e) {
 *                 if (uri.equals(KNOWN_ERRONEOUS_URI) {
 *                     return FIXED_URL;
 *                 } else {
 *                     throw e;
 *                 }
 *             }
 *         }
 *     }
 * }
 *
 * See the {@link XML#CONVERTERS} javadoc for an example of registering a custom
 * {@code ObjectConverters} to a (un)marshaller.
 *
 * @author Martin Desruisseaux (Geomatys)
 * @since   0.3 (derived from geotk-3.07)
 * @version 0.3
 * @module
 */
public class ObjectConverters {
    /**
     * The default, thread-safe and immutable instance. This instance defines the
     * converters used during every (un)marshalling if no {@code ObjectConverters}
     * was explicitly set.
     */
    public static final ObjectConverters DEFAULT = new ObjectConverters();

    /**
     * Creates a default {@code ObjectConverters}. This is for subclasses only,
     * since new instances are useful only if at least one method is overridden.
     */
    protected ObjectConverters() {
    }

    /**
     * Invoked when an exception occurred in any {@code toXXX(…)} method. The default implementation
     * does nothing and return {@code false}, which will cause the (un)marshalling process of the
     * whole XML document to fail.
     *
     * <p>This method provides a single hook that subclasses can override in order to provide their
     * own error handling for every methods defined in this class, like the example documented in
     * the {@link XML#CONVERTERS} javadoc. Subclasses also have the possibility to override individual
     * {@code toXXX(…)} methods, like the example provided in this <a href="#skip-navbar_top">class
     * javadoc</a>.</p>
     *
     * @param  <T> The compile-time type of the {@code sourceType} argument.
     * @param  context Context (GML version, locale, <i>etc.</i>) of the (un)marshalling process.
     * @param  value The value that can't be converted.
     * @param  sourceType The base type of the value to convert. This is determined by the argument
     *         type of the method that caught the exception. For example the source type is always
     *         {@code URI.class} if the exception has been caught by the {@link #toURL(URI)} method.
     * @param  targetType The expected type of the converted object.
     * @param  exception The exception that occurred during the conversion attempt.
     * @return {@code true} if the (un)marshalling process should continue despite this error,
     *         or {@code false} (the default) if the exception should be propagated, thus causing
     *         the (un)marshalling to fail.
     */
    protected <T> boolean exceptionOccured(MarshalContext context, T value,
            Class<T> sourceType, Class<?> targetType, Exception exception)
    {
        return false;
    }

    /**
     * Converts the given string to a locale. The string is the language code either as the 2
     * letters or the 3 letters ISO code. It can optionally be followed by the {@code '_'}
     * character and the country code (again either as 2 or 3 letters), optionally followed
     * by {@code '_'} and the variant.
     *
     * @param  context Context (GML version, locale, <i>etc.</i>) of the (un)marshalling process.
     * @param  value The string to convert to a locale, or {@code null}.
     * @return The converted locale, or {@code null} if the given value was null or empty, or
     *         if an exception was thrown and {@code exceptionOccured(…)} returned {@code true}.
     * @throws IllegalArgumentException If the given string can not be converted to a locale.
     */
    public Locale toLocale(final MarshalContext context, String value) throws IllegalArgumentException {
        value = trimWhitespaces(value);
        if (value != null && !value.isEmpty()) try {
            return Locales.parse(value);
        } catch (IllegalArgumentException e) {
            if (!exceptionOccured(context, value, String.class, Locale.class, e)) {
                throw e;
            }
        }
        return null;
    }

    /**
     * Converts the given string to a unit. The default implementation is as below, omitting
     * the check for null value and the call to {@link #exceptionOccured exceptionOccured(…)}
     * in case of error:
     *
     * {@preformat java
     *     return Units.valueOf(value);
     * }
     *
     * @param  context Context (GML version, locale, <i>etc.</i>) of the (un)marshalling process.
     * @param  value The string to convert to a unit, or {@code null}.
     * @return The converted unit, or {@code null} if the given value was null or empty, or
     *         if an exception was thrown and {@code exceptionOccured(…)} returned {@code true}.
     * @throws IllegalArgumentException If the given string can not be converted to a unit.
     *
     * @see Units#valueOf(String)
     */
    public Unit<?> toUnit(final MarshalContext context, String value) throws IllegalArgumentException {
        value = trimWhitespaces(value);
        if (value != null && !value.isEmpty()) try {
            return Units.valueOf(value);
        } catch (IllegalArgumentException e) {
            if (!exceptionOccured(context, value, String.class, Unit.class, e)) {
                throw e;
            }
        }
        return null;
    }

    /**
     * Converts the given string to a Universal Unique Identifier. The default implementation
     * is as below, omitting the check for null value and the call to {@link #exceptionOccured
     * exceptionOccured(…)} in case of error:
     *
     * {@preformat java
     *     return UUID.fromString(value);
     * }
     *
     * @param  context Context (GML version, locale, <i>etc.</i>) of the (un)marshalling process.
     * @param  value The string to convert to a UUID, or {@code null}.
     * @return The converted UUID, or {@code null} if the given value was null or empty, or
     *         if an exception was thrown and {@code exceptionOccured(…)} returned {@code true}.
     * @throws IllegalArgumentException If the given string can not be converted to a UUID.
     *
     * @see UUID#fromString(String)
     */
    public UUID toUUID(final MarshalContext context, String value) throws IllegalArgumentException {
        value = trimWhitespaces(value);
        if (value != null && !value.isEmpty()) try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            if (!exceptionOccured(context, value, String.class, UUID.class, e)) {
                throw e;
            }
        }
        return null;
    }

    /**
     * Converts the given string to a URI. The default performs the following work
     * (omitting the check for null value and the call to {@link #exceptionOccured
     * exceptionOccured(…)} in case of error):
     *
     * {@preformat java
     *     return new URI(value);
     * }
     *
     * @param  context Context (GML version, locale, <i>etc.</i>) of the (un)marshalling process.
     * @param  value The string to convert to a URI, or {@code null}.
     * @return The converted URI, or {@code null} if the given value was null or empty, or if
     *         an exception was thrown and {@code exceptionOccured(…)} returned {@code true}.
     * @throws URISyntaxException If the given string can not be converted to a URI.
     *
     * @see URI#URI(String)
     */
    public URI toURI(final MarshalContext context, String value) throws URISyntaxException {
        value = trimWhitespaces(value);
        if (value != null && !value.isEmpty()) try {
            return new URI(value);
        } catch (URISyntaxException e) {
            if (!exceptionOccured(context, value, String.class, URI.class, e)) {
                throw e;
            }
        }
        return null;
    }

    /**
     * Converts the given URL to a URI. The default implementation is as below, omitting
     * the check for null value and the call to {@link #exceptionOccured exceptionOccured(…)}
     * in case of error:
     *
     * {@preformat java
     *     return value.toURI();
     * }
     *
     * @param  context Context (GML version, locale, <i>etc.</i>) of the (un)marshalling process.
     * @param  value The URL to convert to a URI, or {@code null}.
     * @return The converted URI, or {@code null} if the given value was null or if an
     *         exception was thrown and {@code exceptionOccured(…)} returned {@code true}.
     * @throws URISyntaxException If the given URL can not be converted to a URI.
     *
     * @see URL#toURI()
     */
    public URI toURI(final MarshalContext context, final URL value) throws URISyntaxException {
        if (value != null) try {
            return value.toURI();
        } catch (URISyntaxException e) {
            if (!exceptionOccured(context, value, URL.class, URI.class, e)) {
                throw e;
            }
        }
        return null;
    }

    /**
     * Converts the given URI to a URL. The default implementation is as below, omitting
     * the check for null value and the call to {@link #exceptionOccured exceptionOccured(…)}
     * in case of error:
     *
     * {@preformat java
     *     return value.toURL();
     * }
     *
     * @param  context Context (GML version, locale, <i>etc.</i>) of the (un)marshalling process.
     * @param  value The URI to convert to a URL, or {@code null}.
     * @return The converted URL, or {@code null} if the given value was null or if an
     *         exception was thrown and {@code exceptionOccured(…)} returned {@code true}.
     * @throws MalformedURLException If the given URI can not be converted to a URL.
     *
     * @see URI#toURL()
     */
    public URL toURL(final MarshalContext context, final URI value) throws MalformedURLException {
        if (value != null) try {
            return value.toURL();
        } catch (MalformedURLException | IllegalArgumentException e) {
            if (!exceptionOccured(context, value, URI.class, URL.class, e)) {
                throw e;
            }
        }
        return null;
    }

    /**
     * Converts the given string to a {@code NilReason}. The default implementation is as below,
     * omitting the check for null value and the call to {@link #exceptionOccured exceptionOccured(…)}
     * in case of error:
     *
     * {@preformat java
     *     return NilReason.valueOf(value);
     * }
     *
     * @param  context Context (GML version, locale, <i>etc.</i>) of the (un)marshalling process.
     * @param  value The string to convert to a nil reason, or {@code null}.
     * @return The converted nil reason, or {@code null} if the given value was null or empty, or
     *         if an exception was thrown and {@code exceptionOccured(…)} returned {@code true}.
     * @throws URISyntaxException If the given string can not be converted to a nil reason.
     *
     * @see NilReason#valueOf(String)
     */
    public NilReason toNilReason(final MarshalContext context, String value) throws URISyntaxException {
        value = trimWhitespaces(value);
        if (value != null && !value.isEmpty()) try {
            return NilReason.valueOf(value);
        } catch (URISyntaxException e) {
            if (!exceptionOccured(context, value, String.class, URI.class, e)) {
                throw e;
            }
        }
        return null;
    }
}
