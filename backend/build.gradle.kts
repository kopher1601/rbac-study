plugins {
    java
    id("org.springframework.boot") version "4.0.7"
}

group = "com.study"
version = "0.0.1-SNAPSHOT"
description = "RBAC / ABAC / ReBAC 학습용 인가 백엔드"

java {
    // Gradle 자체를 Java 21 로 실행하므로 toolchain 프로비저닝 없이 실행 JDK 로 컴파일한다.
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

// Spring Boot 의 BOM 을 platform 으로 가져와 버전 관리(io.spring.dependency-management 플러그인 불필요)
dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:4.0.7"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    runtimeOnly("com.h2database:h2")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
