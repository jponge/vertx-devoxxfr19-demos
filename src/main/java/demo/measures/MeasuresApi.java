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

package demo.measures;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneOffset;

import static java.time.ZonedDateTime.now;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;

public class MeasuresApi extends AbstractVerticle {

  private final Logger logger = LoggerFactory.getLogger(MeasuresApi.class);

  private MongoClient mongoClient;

  @Override
  public void start() {
    JsonObject config = new JsonObject()
      .put("db_name", "ingested")
      .put("useObjectId", true);
    mongoClient = MongoClient.createShared(vertx, config);

    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    router.post("/ingest").handler(this::ingest);
    router.get("/all/:tyreId").handler(this::allMeasures);
    router.get("/last/:n/:tyreId").handler(this::lastMeasures);

    vertx.createHttpServer()
      .requestHandler(router)
      .listen(3000, ar -> {
        if (ar.succeeded()) {
          logger.info("Ingester listening on port 3000");
        } else {
          logger.error("Woops", ar.cause());
        }
      });
  }

  private void ingest(RoutingContext ctx) {
    JsonObject body = ctx.getBodyAsJson();
    if (!body.containsKey("tyreId") || !body.containsKey("pressure")) {
      ctx.response().setStatusCode(400).end();
      return;
    }

    JsonObject document = new JsonObject()
      .put("tyreId", body.getString("tyreId"))
      .put("pressure", body.getDouble("pressure"))
      .put("ingestionDate", new JsonObject()
        .put("$date", now(ZoneOffset.UTC).format(ISO_INSTANT)));

    mongoClient.insert("measures", document, ar -> {
      if (ar.succeeded()) {
        JsonObject response = new JsonObject()
          .put("eventId", ar.result());
        ctx.response()
          .putHeader("Content-Type", "application/json")
          .end(response.encode());
      } else {
        logger.error("Woops", ar.cause());
        ctx.response().setStatusCode(500).end();
      }
    });
  }

  private void allMeasures(RoutingContext ctx) {
    String tyreId = ctx.pathParam("tyreId");

    JsonObject query = new JsonObject()
      .put("tyreId", tyreId);
    FindOptions options = new FindOptions()
      .setSort(new JsonObject().put("_id", -1));

    fetchAndRespond(ctx, tyreId, query, options);
  }

  private void lastMeasures(RoutingContext ctx) {
    String tyreId = ctx.pathParam("tyreId");
    int count = Integer.parseInt(ctx.pathParam("n"));

    JsonObject query = new JsonObject()
      .put("tyreId", tyreId);
    FindOptions options = new FindOptions()
      .setLimit(count)
      .setSort(new JsonObject().put("_id", -1));

    fetchAndRespond(ctx, tyreId, query, options);
  }

  private void fetchAndRespond(RoutingContext ctx, String tyreId, JsonObject query, FindOptions options) {
    mongoClient.findWithOptions("measures", query, options, ar -> {
      if (ar.succeeded()) {
        JsonObject response = new JsonObject()
          .put("tyreId", tyreId)
          .put("measures", new JsonArray(ar.result()));
        ctx.response()
          .putHeader("Content-Type", "application/json")
          .end(response.encode());
      } else {
        logger.error("Woops", ar.cause());
        ctx.response().setStatusCode(500).end();
      }
    });
  }

  public static void main(String[] args) {
    Vertx
      .vertx()
      .deployVerticle(new MeasuresApi());
  }
}
