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

package demo.machinelearning;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MachineLearningService extends AbstractVerticle {

  private final Logger logger = LoggerFactory.getLogger(MachineLearningService.class);

  @Override
  public void start() {
    Router router = Router.router(vertx);
    router.post().handler(BodyHandler.create());
    router.post("/regression").handler(this::analyze);

    vertx.createHttpServer()
      .requestHandler(router)
      .listen(3002);
  }

  private void analyze(RoutingContext ctx) {
    JsonArray data = ctx.getBodyAsJson().getJsonArray("data");

    SimpleRegression regression = new SimpleRegression();
    for (int i = 0; i < data.size(); i++) {
      regression.addData((double) i, data.getDouble(i));
    }

    JsonObject response = new JsonObject()
      .put("slope", regression.getSlope())
      .put("confidenceInterval", regression.getSlopeConfidenceInterval());

    ctx.response()
      .putHeader("Content-Type", "application/json")
      .end(response.encode());
  }

  public static void main(String[] args) {
    Vertx
      .vertx()
      .deployVerticle(new MachineLearningService());
  }
}
