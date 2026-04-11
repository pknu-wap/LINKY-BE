plugins {
    java
    id("org.springframework.boot") version "4.0.5"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.project"
version = "0.0.1-SNAPSHOT"
description = "LINKY-BE_PROJECT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    runtimeOnly("com.mysql:mysql-connector-j")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("org.springframework.boot:spring-boot-starter-security") // 인증
    implementation("io.jsonwebtoken:jjwt:0.9.1") // JWT
    implementation("org.projectlombok:lombok") // getter/setter 자동
    annotationProcessor("org.projectlombok:lombok")

    implementation("org.jsoup:jsoup:1.15.3") // (3주차용 미리 추가해도 OK)
    implementation("org.json:json:20230227")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
