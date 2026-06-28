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
    // annotationProcessor 는 implementation 을 상속하지 않아 BOM 을 따로 적용해야 Lombok 버전이 해결된다.
    val springBootBom = platform("org.springframework.boot:spring-boot-dependencies:4.0.7")
    implementation(springBootBom)
    annotationProcessor(springBootBom)
    testAnnotationProcessor(springBootBom)

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Lombok (버전은 spring-boot-dependencies BOM 관리: 1.18.46)
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    runtimeOnly("com.h2database:h2")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    // Spring Boot 4 모듈 분리: MockMvc 슬라이스(@AutoConfigureMockMvc)는 webmvc-test 에 있다.
    testImplementation("org.springframework.boot:spring-boot-webmvc-test")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
