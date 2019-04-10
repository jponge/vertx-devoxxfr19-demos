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


import io.reactivex.Completable;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.RxHelper;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.handler.StaticHandler;
import io.vertx.reactivex.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.reactivex.kafka.client.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class Dashboard extends AbstractVerticle {

  private final Logger logger = LoggerFactory.getLogger(Dashboard.class);

  private KafkaConsumer<String, JsonObject> valuesConsumer;
  private KafkaConsumer<String, JsonObject> throughputConsumer;
  private KafkaConsumer<String, JsonObject> alertConsumer;

  @Override
  public Completable rxStart() {
    Router router = configureRouter();

    valuesConsumer = KafkaConsumer.create(vertx, KafkaConfig.consumer("dashboard"));
    configureValueUpdater();

    throughputConsumer = KafkaConsumer.create(vertx, KafkaConfig.consumer("throughput"));
    configureThroughputUpdater();

    alertConsumer = KafkaConsumer.create(vertx, KafkaConfig.consumer("alerts"));
    configureAlertUpdater();

    return vertx.createHttpServer()
      .requestHandler(router)
      .rxListen(8000)
      .ignoreElement();
  }

  private Router configureRouter() {
    Router router = Router.router(vertx);

    router.get("/").handler(rc -> rc.reroute("/static/index.html"));
    router.get("/static/*").handler(StaticHandler.create());

    PermittedOptions permissions = new PermittedOptions()
      .setAddressRegex("client.updates\\..+");

    BridgeOptions options = new BridgeOptions()
      .addOutboundPermitted(permissions);

    SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
    sockJSHandler.bridge(options);

    router.route("/eventbus/*").handler(sockJSHandler);
    return router;
  }

  private void configureValueUpdater() {
    EventBus eventBus = vertx.eventBus();
    valuesConsumer
      .rxSubscribe("ingestion.measure")
      .andThen(valuesConsumer.toFlowable())
      .subscribe(record ->
        eventBus.publish("client.updates.measures", record.value()));
  }

  private void configureThroughputUpdater() {
    EventBus eventBus = vertx.eventBus();
    throughputConsumer
      .rxSubscribe("ingestion.measure")
      .andThen(throughputConsumer.toFlowable())
      .buffer(5, TimeUnit.SECONDS, RxHelper.scheduler(vertx))
      .subscribe(records ->
        eventBus.publish("client.updates.throughput", ((double)records.size()) / 5.0d));
  }

  private void configureAlertUpdater() {
    EventBus eventBus = vertx.eventBus();
    alertConsumer
      .rxSubscribe("alerts.pressure-drop")
      .andThen(alertConsumer.toFlowable())
      .subscribe(record ->
        eventBus.publish("client.updates.alert", record.value()));
  }

  public static void main(String[] args) {
    Vertx
      .vertx()
      .deployVerticle(new Dashboard());
  }
}
