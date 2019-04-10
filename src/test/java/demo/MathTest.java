/*
 * Copyright (c) 2019 Red Hat, Inc.
 *
 *   This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * If it is not possible or desirable to put the notice in a particular
 * file, then You may include the notice in a location (such as a LICENSE
 * file in a relevant directory) where a recipient would be likely to look
 * for such a notice.
 *
 * You may add additional accurate notices of copyright ownership.
 *
 */

package demo;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Percentage.withPercentage;

class MathTest {

  @Test
  void flat_regression() {
    SimpleRegression regression = new SimpleRegression();
    regression.addData(1.0, 10.0);
    regression.addData(2.0, 10.0);
    regression.addData(3.0, 10.0);
    assertThat(regression.getSlope()).isCloseTo(0.0d, withPercentage(0.0d));
  }

  @Test
  void up_regression() {
    SimpleRegression regression = new SimpleRegression();
    regression.addData(1.0, 10.0);
    regression.addData(2.0, 11.0);
    regression.addData(3.0, 12.0);
    assertThat(regression.getSlope()).isCloseTo(1.0d, withPercentage(0.1d));
  }

  @Test
  void down_regression() {
    SimpleRegression regression = new SimpleRegression();
    regression.addData(1.0, 10.0);
    regression.addData(2.0, 8.0);
    regression.addData(3.0, 7.0);
    System.out.println(regression.getSignificance());
    assertThat(regression.getSlope()).isCloseTo(-1.5d, withPercentage(1.0d));
  }
}
