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

import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.RxHelper;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.kafka.client.consumer.KafkaConsumer;
import io.vertx.reactivex.kafka.client.consumer.KafkaConsumerRecord;
import io.vertx.reactivex.kafka.client.producer.KafkaProducer;
import io.vertx.reactivex.kafka.client.producer.KafkaProducerRecord;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class PressureAlerter extends AbstractVerticle {

  private final Logger logger = LoggerFactory.getLogger(PressureAlerter.class);

  private KafkaConsumer<String, JsonObject> consumer;
  private KafkaProducer<String, JsonObject> producer;

  @Override
  public void start() {
    producer = KafkaProducer.create(vertx, KafkaConfig.producer());

    consumer = KafkaConsumer.create(vertx, KafkaConfig.consumer("pressure-alerter"));

    consumer.rxSubscribe("ingestion.measure")
      .andThen(consumer.toFlowable())
      .groupBy(KafkaConsumerRecord::key)
      .flatMap(gf -> gf.buffer(10, TimeUnit.SECONDS, RxHelper.scheduler(vertx)))
      .subscribe(this::produceAlert);
  }

  private void produceAlert(List<KafkaConsumerRecord<String, JsonObject>> records) {
    if (records.size() == 0) {
      return;
    }

    SimpleRegression regression = new SimpleRegression();
    for (int i = 0; i < records.size(); i++) {
      regression.addData((double) i, records.get(i).value().getDouble("pressure"));
    }

    String tyreId = records.get(0).key();
    JsonObject insights = new JsonObject()
      .put("tyreId", tyreId)
      .put("alert", regression.getSlope() < -0.10d);

    KafkaProducerRecord<String, JsonObject> alertRecord = KafkaProducerRecord
      .create("alerts.pressure-drop", tyreId, insights);

    producer.write(alertRecord);
  }

  public static void main(String[] args) {
    Vertx
      .vertx()
      .deployVerticle(new PressureAlerter());
  }
}
