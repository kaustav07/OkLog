#!/usr/bin/env bash
./gradlew clean
./gradlew :oklog-shared:bintrayUpload
./gradlew :oklog-core:bintrayUpload
./gradlew :oklog-java:bintrayUpload
./gradlew :oklog3-java:bintrayUpload
./gradlew :oklog-core-android:bintrayUpload
./gradlew :oklog:bintrayUpload
./gradlew :oklog3:bintrayUpload