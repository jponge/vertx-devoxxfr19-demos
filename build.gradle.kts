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

plugins {
  id("io.vertx.vertx-plugin") version "1.0.1"
}

repositories {
  jcenter()
}

dependencies {
  implementation("ch.qos.logback:logback-classic:1.2.3")
  implementation("org.apache.commons:commons-math3:3.6.1")

  implementation("io.vertx:vertx-rx-java2")

  implementation("io.vertx:vertx-web")
  implementation("io.vertx:vertx-web-client")

  implementation("io.vertx:vertx-mongo-client")
  implementation("io.vertx:vertx-kafka-client")

  testCompile("org.junit.jupiter:junit-jupiter-api:5.4.2")
  testCompile("org.assertj:assertj-core:3.11.1")
  testRuntime("org.junit.jupiter:junit-jupiter-engine:5.4.2")
}

vertx {
  mainVerticle = "demo.MainVerticle"
  vertxVersion = "3.7.0"
}

tasks.withType<Test> {
  useJUnitPlatform()
}
