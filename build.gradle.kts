import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
	java
	id("org.springframework.boot") version "4.0.0"
	id("io.spring.dependency-management") version "1.1.7"
	id("checkstyle")
	id("com.diffplug.spotless") version "6.23.0"
}

group = "io.rafaalberto"
version = "1.0.0-SNAPSHOT"
description = "Event-driven transaction processor using Kafka, clean architecture and atomic operations."

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

val testcontainersVersion = "1.19.8"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-kafka")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    runtimeOnly("org.postgresql:postgresql")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.testcontainers:junit-jupiter:$testcontainersVersion")
    testImplementation("org.testcontainers:postgresql:$testcontainersVersion")
    testImplementation("org.testcontainers:kafka:$testcontainersVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

}

checkstyle {
	toolVersion = "10.12.4"
	configFile = file("config/checkstyle/checkstyle.xml")
}

spotless {
	java {
		googleJavaFormat("1.17.0")
		removeUnusedImports()
		trimTrailingWhitespace()
		endWithNewline()
		target("src/**/*.java")
	}
}

tasks.withType<Checkstyle>().configureEach {
	reports {
		xml.required = false
		html.required = true
		html.outputLocation = layout.buildDirectory.file("reports/checkstyle/checkstyle.html")
	}
}

tasks.named("checkstyleMain") {
	mustRunAfter("spotlessCheck")
}

tasks.named("checkstyleTest") {
	mustRunAfter("spotlessCheck")
}

tasks.withType<Test>().configureEach {
	useJUnitPlatform()

	testLogging {
		events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
		exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.SHORT
		showStandardStreams = false
	}
}

tasks.test {
    exclude("**/integration/**")
    exclude("**/acceptance/**")
    exclude("**/*IntegrationTest.java")
    exclude("**/*AcceptanceTest.java")
}

tasks.register<Test>("integrationTest") {
    description = "Runs integration tests"
    group = "verification"

    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath

    include("**/integration/**")
    include("**/*IntegrationTest.java")

    shouldRunAfter(tasks.test)
}

tasks.register<Test>("acceptanceTest") {
    description = "Runs acceptance tests"
    group = "verification"

    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath

    include("**/acceptance/**")
    include("**/*AcceptanceTest.java")

    shouldRunAfter(tasks.named("integrationTest"))
}

tasks.named("check") {
	dependsOn("spotlessCheck")
	dependsOn(tasks.named("checkstyleMain"), tasks.named("checkstyleTest"))
	dependsOn("integrationTest")
    dependsOn("acceptanceTest")
}
