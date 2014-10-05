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
package org.apache.sis.metadata;

import java.util.Arrays;
import java.util.Collections;
import javax.measure.unit.SI;
import org.opengis.metadata.citation.Role;
import org.opengis.metadata.citation.PresentationForm;
import org.opengis.metadata.identification.TopicCategory;
import org.opengis.util.InternationalString;
import org.apache.sis.util.collection.TableColumn;
import org.apache.sis.util.collection.TreeTableFormat;
import org.apache.sis.util.iso.SimpleInternationalString;
import org.apache.sis.metadata.iso.content.DefaultBand;
import org.apache.sis.metadata.iso.content.DefaultImageDescription;
import org.apache.sis.metadata.iso.citation.DefaultCitation;
import org.apache.sis.metadata.iso.citation.DefaultIndividual;
import org.apache.sis.metadata.iso.citation.DefaultResponsibility;
import org.apache.sis.metadata.iso.content.DefaultAttributeGroup;
import org.apache.sis.metadata.iso.identification.DefaultKeywords;
import org.apache.sis.metadata.iso.identification.DefaultDataIdentification;
import org.apache.sis.metadata.iso.lineage.DefaultProcessing;
import org.apache.sis.test.DependsOn;
import org.apache.sis.test.TestCase;
import org.junit.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.apache.sis.test.Assert.*;


/**
 * Tests the {@link TreeTableFormat} applied to the formatting of metadata tree.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @since   0.3 (derived from geotk-3.00)
 * @version 0.5
 * @module
 */
@DependsOn(TreeTableViewTest.class)
public final strictfp class TreeTableFormatTest extends TestCase {
    /**
     * The formatter to use.
     */
    private final TreeTableFormat format;

    /**
     * Creates a new test case.
     */
    public TreeTableFormatTest() {
        format = new TreeTableFormat(null, null);
        format.setColumns(TableColumn.NAME, TableColumn.VALUE);
    }

    /**
     * Creates a band for the given minimum and maximum wavelengths, in centimetres.
     */
    private static DefaultBand createBand(final double min, final double max) {
        final DefaultBand band = new DefaultBand();
        band.setMinValue(min);
        band.setMaxValue(max);
        band.setUnits(SI.CENTIMETRE);
        return band;
    }

    /**
     * Creates the citation to use for testing purpose.
     */
    private static DefaultCitation createCitation() {
        final DefaultCitation citation = new DefaultCitation();
        final InternationalString title = new SimpleInternationalString("Undercurrent");
        citation.setTitle(title);
        citation.setISBN("9782505004509");
        citation.setPresentationForms(asList(
                PresentationForm.DOCUMENT_HARDCOPY,
                PresentationForm.IMAGE_HARDCOPY));
        citation.setAlternateTitles(asList(
                new SimpleInternationalString("Alt A"),
                new SimpleInternationalString("Alt B")));
        citation.setCitedResponsibleParties(asList(
                new DefaultResponsibility(Role.AUTHOR, null, new DefaultIndividual("Testsuya Toyoda", null, null)),
                new DefaultResponsibility(null, null, new DefaultIndividual("A japanese author", null, null))));
        return citation;
    }

    /**
     * Tests the formatting of a {@link DefaultCitation} object.
     */
    @Test
    public void testCitation() {
        final DefaultCitation citation = createCitation();
        final String text = format.format(citation.asTreeTable());
        assertMultilinesEquals(
            "Citation\n" +
            "  ├─Title……………………………………………………………………… Undercurrent\n" +
            "  ├─Alternate title (1 of 2)…………………… Alt A\n" +
            "  ├─Alternate title (2 of 2)…………………… Alt B\n" +
            "  ├─Identifier\n" +
            "  │   ├─Code……………………………………………………………… 9782505004509\n" +
            "  │   ├─Authority\n" +
            "  │   │   └─Title………………………………………………… ISBN\n" +
            "  │   └─Code space……………………………………………… ISBN\n"+
            "  ├─Cited responsible party (1 of 2)\n" +
            "  │   ├─Party\n" +
            "  │   │   └─Name…………………………………………………… Testsuya Toyoda\n" +
            "  │   └─Role……………………………………………………………… Author\n" +
            "  ├─Cited responsible party (2 of 2)\n" +
            "  │   └─Party\n" +
            "  │       └─Name…………………………………………………… A japanese author\n" +
            "  ├─Presentation form (1 of 2)……………… Document hardcopy\n" +
            "  ├─Presentation form (2 of 2)……………… Image hardcopy\n" +
            "  └─ISBN………………………………………………………………………… 9782505004509\n", text);
    }

    /**
     * Tests the formatting of a {@link DefaultProcessing} object.
     */
    @Test
    public void testProcessing() {
        final DefaultCitation   titled = new DefaultCitation("Some specification");
        final DefaultCitation    coded = new DefaultCitation();
        final DefaultCitation untitled = new DefaultCitation();
        titled  .setPresentationForms(singleton(PresentationForm.DOCUMENT_HARDCOPY));
        coded   .setPresentationForms(singleton(PresentationForm.IMAGE_HARDCOPY));
        untitled.setCitedResponsibleParties(singleton(new DefaultResponsibility(Role.AUTHOR, null, null)));
        final DefaultProcessing processing = new DefaultProcessing();
        processing.setDocumentations(asList(titled, coded, untitled));
        final String text = format.format(processing.asTreeTable());
        assertMultilinesEquals(
            "Processing\n" +
            "  ├─Documentation (1 of 3)\n" +
            "  │   ├─Title……………………………………………… Some specification\n" +
            "  │   └─Presentation form……………… Document hardcopy\n" +
            "  ├─Documentation (2 of 3)\n" +
            "  │   └─Presentation form……………… Image hardcopy\n" +
            "  └─Documentation (3 of 3)\n" +
            "      └─Cited responsible party\n" +
            "          └─Role……………………………………… Author\n", text);
    }

    /**
     * Tests the formatting of a {@link DefaultImageDescription} object.
     */
    @Test
    public void testImageDescription() {
        final DefaultImageDescription image = new DefaultImageDescription();
        image.setAttributeGroups(Arrays.asList(
            new DefaultAttributeGroup(null, createBand(0.25, 0.26)),
            new DefaultAttributeGroup(null, createBand(0.28, 0.29))
        ));
        final String text = format.format(image.asTreeTable());
        assertMultilinesEquals(
            "Image description\n" +
            "  ├─Attribute group (1 of 2)\n" +
            "  │   └─Attribute\n" +
            "  │       ├─Max value………………… 0.26\n" +
            "  │       ├─Min value………………… 0.25\n" +
            "  │       └─Units…………………………… cm\n" +
            "  └─Attribute group (2 of 2)\n" +
            "      └─Attribute\n" +
            "          ├─Max value………………… 0.29\n" +
            "          ├─Min value………………… 0.28\n" +
            "          └─Units…………………………… cm\n", text);
    }

    /**
     * Tests the formatting of a {@link DefaultDataIdentification} object with custom code list elements
     * Note that adding enumeration values is normally not allowed, so a future version of this test may
     * need to change the code type.
     */
    @Test
    public void testTreeWithCustomElements() {
        final DefaultKeywords keywords = new DefaultKeywords();
        keywords.setKeywords(Arrays.asList(
                new SimpleInternationalString("Apple"),
                new SimpleInternationalString("Orange"),
                new SimpleInternationalString("Kiwi")));

        final DefaultDataIdentification identification = new DefaultDataIdentification();
        identification.setDescriptiveKeywords(Collections.singleton(keywords));
        identification.setTopicCategories(Arrays.asList(
                TopicCategory.HEALTH,
                TopicCategory.valueOf("OCEANS"), // Existing category
                TopicCategory.valueOf("test"))); // Custom category

        final String text = format.format(identification.asTreeTable());
        assertMultilinesEquals(
            "Data identification\n" +
            "  ├─Descriptive keywords\n" +
            "  │   ├─Keyword (1 of 3)…………… Apple\n" +
            "  │   ├─Keyword (2 of 3)…………… Orange\n" +
            "  │   └─Keyword (3 of 3)…………… Kiwi\n" +
            "  ├─Topic category (1 of 3)…… Health\n" +
            "  ├─Topic category (2 of 3)…… Oceans\n" +
            "  └─Topic category (3 of 3)…… Test\n",
            text);
    }
}
