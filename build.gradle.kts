// 프로젝트에서 사용할 Gradle 플러그인을 선언한다.
plugins {
    java
    id("org.springframework.boot") version "4.0.5"
    id("io.spring.dependency-management") version "1.1.7"
}

// 빌드 산출물의 그룹 ID를 설정한다.
group = "com.project"

// 빌드 산출물의 버전을 설정한다.
version = "0.0.1-SNAPSHOT"

// 프로젝트 설명 값을 설정한다.
description = "LINKY-BE_PROJECT"

// Java 컴파일에 사용할 도구 버전을 설정한다.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

// 의존성을 받을 Maven 저장소를 설정한다.
repositories {
    mavenCentral()
}

// 애플리케이션 실행과 테스트에 필요한 라이브러리를 선언한다.
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    runtimeOnly("com.mysql:mysql-connector-j")
    testRuntimeOnly("com.h2database:h2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    implementation("org.jsoup:jsoup:1.17.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.1")

    // JWT (Java 17 호환 버전)
    implementation("org.json:json:20230227")
}

// JUnit Platform으로 테스트를 실행하도록 설정한다.
tasks.withType<Test> {
    useJUnitPlatform()
}
