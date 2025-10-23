plugins {
    id("java")
    id("application")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven(url="https://jitpack.io")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation("com.github.mapsforge.mapsforge:mapsforge-core:0.26.1")
    implementation("com.github.mapsforge.mapsforge:mapsforge-map:0.26.1")
    implementation("com.github.mapsforge.mapsforge:mapsforge-map-reader:0.26.1")
    implementation("com.github.mapsforge.mapsforge:mapsforge-themes:0.26.1")

    implementation("com.github.mapsforge.mapsforge:mapsforge-map-awt:0.26.1")
    implementation("guru.nidi.com.kitfox:svgSalamander:1.1.3")
    implementation("net.sf.kxml:kxml2:2.3.0")

    implementation("com.github.mapsforge.mapsforge:mapsforge-core:0.26.1")
    implementation("com.github.mapsforge.mapsforge:mapsforge-poi:0.26.1")

    implementation("com.github.mapsforge.mapsforge:mapsforge-poi-awt:0.26.1")
    implementation("org.xerial:sqlite-jdbc:3.43.0.0")

    implementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.4")
    implementation("org.eclipse.persistence:org.eclipse.persistence.moxy:4.0.8")
    implementation("com.formdev:flatlaf:3.4")
}

tasks.test {
    useJUnitPlatform()
}