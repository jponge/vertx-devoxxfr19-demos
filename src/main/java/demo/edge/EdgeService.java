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

package demo.edge;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.client.HttpResponse;
import io.vertx.reactivex.ext.web.client.WebClient;
import io.vertx.reactivex.ext.web.client.predicate.ResponsePredicate;
import io.vertx.reactivex.ext.web.codec.BodyCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class EdgeService extends AbstractVerticle {

  private final Logger logger = LoggerFactory.getLogger(EdgeService.class);

  private WebClient webClient;

  @Override
  public Completable rxStart() {
    Router router = Router.router(vertx);
    router.get("/:tyreId").handler(this::fetch);

    webClient = WebClient.create(vertx);

    return vertx.createHttpServer()
      .requestHandler(router)
      .rxListen(4000)
      .ignoreElement();
  }

  private void fetch(RoutingContext ctx) {
    String tyreId = ctx.pathParam("tyreId");

    Single<JsonObject> enrichedMeasureData = webClient
      .get(3000, "localhost", "/last/5/" + tyreId)
      .as(BodyCodec.jsonObject())
      .expect(ResponsePredicate.SC_OK)
      .rxSend()
      .flatMap(this::enrichWithMachineLearning);

    Single<JsonObject> inventoryData = webClient
      .get(3001, "localhost", "/" + tyreId)
      .as(BodyCodec.jsonObject())
      .expect(ResponsePredicate.SC_OK)
      .rxSend()
      .map(HttpResponse::body);

    Single
      .zip(enrichedMeasureData, inventoryData, JsonObject::mergeIn)
      .subscribe(
        result -> success(ctx, result),
        err -> woops(ctx, err));
  }

  private Single<JsonObject> enrichWithMachineLearning(HttpResponse<JsonObject> measureResponse) {
    JsonObject lastFiveMeasures = measureResponse.body();

    if (lastFiveMeasures.getJsonArray("measures").isEmpty()) {
      return Single.error(new IllegalArgumentException("Missing measures"));
    }

    List<Double> pressures = lastFiveMeasures.getJsonArray("measures")
      .stream()
      .map(JsonObject.class::cast)
      .map(json -> json.getDouble("pressure"))
      .collect(Collectors.toList());

    JsonObject data = new JsonObject()
      .put("data", new JsonArray(pressures));

    return webClient
      .post(3002, "localhost", "/regression")
      .as(BodyCodec.jsonObject())
      .expect(ResponsePredicate.SC_OK)
      .rxSendJsonObject(data)
      .flatMap(mlResponse -> {
        JsonObject mlData = mlResponse.body();
        lastFiveMeasures
          .put("pressureDropping", mlData.getDouble("slope") < -0.1d)
          .put("regressionData", mlData);
        return Single.just(lastFiveMeasures);
      });
  }

  private void success(RoutingContext ctx, JsonObject result) {
    ctx.response()
      .putHeader("Content-Type", "application/json")
      .end(result.encode());
  }

  private void woops(RoutingContext ctx, Throwable err) {
    logger.error("Woops", err);
    ctx.response()
      .setStatusCode((err instanceof IllegalArgumentException) ? 400 : 500)
      .end();
  }
}
