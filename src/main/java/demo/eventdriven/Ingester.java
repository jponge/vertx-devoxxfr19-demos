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

package demo.eventdriven;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneOffset;

import static java.time.ZonedDateTime.now;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;

public class Ingester extends AbstractVerticle {

  private final Logger logger = LoggerFactory.getLogger(Ingester.class);

  private KafkaProducer<String, JsonObject> kafkaProducer;

  @Override
  public void start() {
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    router.post("/ingest").handler(this::ingest);

    kafkaProducer = KafkaProducer.create(vertx, KafkaConfig.producer());

    vertx.createHttpServer()
      .requestHandler(router)
      .listen(7000);
  }

  private void ingest(RoutingContext ctx) {
    JsonObject body = ctx.getBodyAsJson();
    String tyreId = body.getString("tyreId");

    JsonObject measure = new JsonObject()
      .put("tyreId", tyreId)
      .put("pressure", body.getDouble("pressure"))
      .put("ingestionData", now(ZoneOffset.UTC).format(ISO_INSTANT));

    KafkaProducerRecord<String, JsonObject> record = KafkaProducerRecord
      .create("ingestion.measure", tyreId, measure);

    kafkaProducer.write(record, ar -> {
      if (ar.succeeded()) {
        ctx.response().end();
      } else {
        logger.error("Woops", ar.cause());
        ctx.response().setStatusCode(500).end();
      }
    });
  }

  public static void main(String[] args) {
    Vertx
      .vertx()
      .deployVerticle(new Ingester());
  }
}
