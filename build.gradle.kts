plugins {
    val springBootVersion = "3.3.4"
    val kotlinVersion = "2.2.20"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    kotlin("plugin.jpa") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    application
    id("org.springframework.boot") version springBootVersion
    id("io.spring.dependency-management") version "1.1.0"
}

application {
    mainClass = "HousekeeperApplicationKt"
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    val jwtVersion = "0.12.3"

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-security")

    implementation("commons-validator:commons-validator:1.7")

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    //open-api (ex swagger)
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.1.0")

    implementation("org.apache.poi:poi:5.2.3")
    implementation("org.apache.poi:poi-ooxml:5.2.3")

    //postgres
    runtimeOnly("org.postgresql:postgresql")
    implementation("com.vladmihalcea:hibernate-types-60:2.21.1")

    implementation("io.jsonwebtoken:jjwt-api:${jwtVersion}")
    implementation("io.jsonwebtoken:jjwt-impl:${jwtVersion}")
    implementation("io.jsonwebtoken:jjwt-jackson:${jwtVersion}")

    // https://mvnrepository.com/artifact/org.apache.pdfbox/pdfbox
    implementation("org.apache.pdfbox:pdfbox:3.0.6")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.8.0")
    testImplementation("io.mockk:mockk:1.13.3")

}

dependencyManagement {
    val springCloudVersion = "2023.0.3"
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${springCloudVersion}")
    }
}

//tasks.withType<KotlinJvmCompile>().configureEach {
//    compilerOptions {
//        jvmTarget.set(JvmTarget.JVM_17)
//        freeCompilerArgs.add("-Xjsr305=strict")
//    }
//}
//
//
//tasks.jar {
//    enabled = false
//}
tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}