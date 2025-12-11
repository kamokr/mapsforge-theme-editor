import java.io.FileOutputStream

plugins {
    id("java")
    id("application")
    idea
}

group = "kamokr"
version = "0.1"
val mapsforgeVersion = "0.25.0"
//val mapsforgeVersion = "0.26.1"

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

    implementation("com.github.mapsforge.mapsforge:mapsforge-core:${mapsforgeVersion}@jar")
    implementation("com.github.mapsforge.mapsforge:mapsforge-map:${mapsforgeVersion}@jar")
    implementation("com.github.mapsforge.mapsforge:mapsforge-map-reader:${mapsforgeVersion}@jar")
    implementation("com.github.mapsforge.mapsforge:mapsforge-themes:${mapsforgeVersion}@jar")

    implementation("com.github.mapsforge.mapsforge:mapsforge-map-awt:${mapsforgeVersion}@jar")
    implementation("guru.nidi.com.kitfox:svgSalamander:1.1.3")
    implementation("net.sf.kxml:kxml2:2.3.0")

    implementation("com.github.mapsforge.mapsforge:mapsforge-poi:${mapsforgeVersion}@jar")
    implementation("com.github.mapsforge.mapsforge:mapsforge-poi-awt:${mapsforgeVersion}@jar")
    implementation("org.xerial:sqlite-jdbc:3.43.0.0")
    
//    implementation("com.github.kamokr.mapsforge:mapsforge-core:kamokr-SNAPSHOT")
//    implementation("com.github.kamokr.mapsforge:mapsforge-map:kamokr-SNAPSHOT")
//    implementation("com.github.kamokr.mapsforge:mapsforge-map-reader:kamokr-SNAPSHOT")
//    implementation("com.github.kamokr.mapsforge:mapsforge-themes:kamokr-SNAPSHOT")
//    implementation("com.github.kamokr.mapsforge:mapsforge-map-awt:kamokr-SNAPSHOT")
//    implementation("com.github.kamokr.mapsforge:mapsforge-poi:kamokr-SNAPSHOT")
//    implementation("com.github.kamokr.mapsforge:mapsforge-poi-awt:kamokr-SNAPSHOT")

    implementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.1")
    implementation("com.sun.xml.bind:jaxb-xjc:4.0.4")
    implementation("org.eclipse.persistence:org.eclipse.persistence.moxy:4.0.8")

    implementation("org.jdom:jdom2:2.0.6.1")
    implementation("org.apache.ws.xmlschema:xmlschema-core:2.3.1")

    implementation("com.formdev:flatlaf:3.4")
}

tasks.register<Jar>("fatJar") {
    archiveBaseName.set("mapsforge-theme-editor")
    archiveClassifier.set("")
    archiveVersion.set("")

    manifest {
        attributes["Main-Class"] = "kamokr.mapsforge.theme.editor.Main"
    }

    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    }) {
        exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.register("listDeps") {
    doLast {
        configurations.runtimeClasspath.get().forEach { println(it.name) }
    }
}

// Task to preprocess XML RenderTheme schema and add editor specific attributes ========================================
val projectSchemaFile = file("src/main/resources/renderTheme.editor.xsd")
val schemaFile = file("build/tmp/renderTheme.xsd")
val schemaFileUrl = "https://raw.githubusercontent.com/kamokr/mapsforge/kamokr/resources/renderTheme.xsd"

tasks.register("downloadOriginalSchema") {
    // make sure the output directory exists
    schemaFile.parentFile.mkdirs()
    uri(schemaFileUrl).toURL().openStream().use {
        it.copyTo(FileOutputStream(schemaFile))
    }
}

tasks.register<XSLTTransformTask>("preprocessSchema") {
    dependsOn("downloadOriginalSchema")
    source = schemaFile
    stylesheet = file("add-editor-attributes.xslt")
    output = projectSchemaFile
}

// Custom task for XSLT transformation
abstract class XSLTTransformTask : DefaultTask() {
    @get:InputFile
    abstract val source: RegularFileProperty

    @get:InputFile
    abstract val stylesheet: RegularFileProperty

    @get:OutputFile
    abstract val output: RegularFileProperty

    @TaskAction
    fun transform() {
        val factory = javax.xml.transform.TransformerFactory.newInstance()
        val transformer = factory.newTransformer(javax.xml.transform.stream.StreamSource(stylesheet.get().asFile))

        // Configure transformer to avoid extra whitespace
        transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes")
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4")
        transformer.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "no")

        transformer.transform(
            javax.xml.transform.stream.StreamSource(source.get().asFile),
            javax.xml.transform.stream.StreamResult(output.get().asFile)
        )
    }
}

tasks.named("processResources") {
    dependsOn("preprocessSchema")
}

tasks.test {
    useJUnitPlatform()
}