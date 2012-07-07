/**
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

package org.apache.sis.core;

import junit.framework.TestCase;

//JDK imports
import java.awt.geom.Rectangle2D;

/**
 * Tests methods from the {@link LatLonPointRadius} class.
 * 
 * @author rlaidlaw
 */
public class TestLatLonPointRadius extends TestCase
{
  private static final double EPSILON = 0.000001;

  /**
   * Tests the LatLonPoint constructor.
   */
  public void testCreateLatLonPointRadius()
  {
    LatLonPointRadius region = new LatLonPointRadius(new LatLon(0.0, 0.0), 100.0);
    assertNotNull(region);
  }

  /**
   * Tests the getCircularRegionApproximation() method.
   */
  public void testGetCircularRegionApproximation()
  {
    LatLonPointRadius pr1 = new LatLonPointRadius(new LatLon(0.0, 0.0), 25000.0);
    LatLon pts1[] = pr1.getCircularRegionApproximation(10);
    assertEquals(5, pts1.length);
    assertEquals(-90.0, pts1[0].getLat(), EPSILON);
    assertEquals(-180.0, pts1[0].getLon(), EPSILON);
    assertEquals(90.0, pts1[1].getLat(), EPSILON);
    assertEquals(-180.0, pts1[1].getLon(), EPSILON);
    assertEquals(90.0, pts1[2].getLat(), EPSILON);
    assertEquals(180.0, pts1[2].getLon(), EPSILON);
    assertEquals(-90.0, pts1[3].getLat(), EPSILON);
    assertEquals(180.0, pts1[3].getLon(), EPSILON);
    assertEquals(-90.0, pts1[4].getLat(), EPSILON);
    assertEquals(-180.0, pts1[4].getLon(), EPSILON);
    
    LatLonPointRadius pr2 = new LatLonPointRadius(new LatLon(0.0, 0.0), 1000.0);
    LatLon pts2[] = pr2.getCircularRegionApproximation(6);
    assertEquals(7, pts2.length);
  }

  /**
   * Tests the getRectangularRegionApproximation() method.
   */
  public void testGetRectangularRegionApproximation() 
  {
    LatLonPointRadius pr1 = new LatLonPointRadius(new LatLon(0.0, 0.0), 25000.0);
    Rectangle2D r1 = pr1.getRectangularRegionApproximation(10);
    assertEquals(0.0, r1.getX(), EPSILON);
    assertEquals(0.0, r1.getY(), EPSILON);
    assertEquals(360.0, r1.getWidth(), EPSILON);
    assertEquals(180.0, r1.getHeight(), EPSILON);
  }
}
