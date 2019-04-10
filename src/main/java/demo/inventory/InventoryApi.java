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

package demo.inventory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InventoryApi extends AbstractVerticle {

  private final Logger logger = LoggerFactory.getLogger(InventoryApi.class);

  @Override
  public void start() {
    Router router = Router.router(vertx);

    router.get("/:tyreId").handler(ctx -> {
      String tyreId = ctx.pathParam("tyreId");
      JsonObject response = new JsonObject()
        .put("tyreId", tyreId)
        .put("brand", brandFor(tyreId));
      ctx.response()
        .putHeader("Content-Type", "application/json")
        .end(response.encode());
    });

    vertx.createHttpServer()
      .requestHandler(router)
      .listen(3001);
  }

  private String brandFor(String tyreId) {
    return tyreId.hashCode() % 2 == 0 ? "Miecholin" : "Brie Jay Stone";
  }

  public static void main(String[] args) {
    Vertx
      .vertx()
      .deployVerticle(new InventoryApi());
  }
}
