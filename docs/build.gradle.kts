import java.util.Properties

group = rootProject.group
version = rootProject.version

plugins {
    id("ai.kotlin.jvm")
    id("ai.kotlin.dokka")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.knit)
    alias(libs.plugins.dokka.mkdocs)
}

dependencies {
    implementation(project(":agents:agents-test"))
    implementation(project(":koog-agents"))
    implementation(libs.opentelemetry.exporter.otlp)
    implementation(libs.opentelemetry.exporter.logging)

    dokka(project(":agents:agents-core"))
    dokka(project(":agents:agents-features:agents-features-debugger"))
    dokka(project(":agents:agents-features:agents-features-event-handler"))
    dokka(project(":agents:agents-features:agents-features-memory"))
    dokka(project(":agents:agents-features:agents-features-opentelemetry"))
    dokka(project(":agents:agents-features:agents-features-snapshot"))
    dokka(project(":agents:agents-features:agents-features-trace"))
    dokka(project(":agents:agents-features:agents-features-tokenizer"))
    dokka(project(":agents:agents-mcp"))
    dokka(project(":agents:agents-test"))
    dokka(project(":agents:agents-tools"))
    dokka(project(":agents:agents-utils"))
    dokka(project(":agents:agents-ext"))
    dokka(project(":embeddings:embeddings-base"))
    dokka(project(":embeddings:embeddings-llm"))
    dokka(project(":prompt:prompt-cache:prompt-cache-files"))
    dokka(project(":prompt:prompt-cache:prompt-cache-model"))
    dokka(project(":prompt:prompt-cache:prompt-cache-redis"))
    dokka(project(":prompt:prompt-executor:prompt-executor-cached"))
    dokka(project(":prompt:prompt-executor:prompt-executor-clients"))
    dokka(project(":prompt:prompt-executor:prompt-executor-clients:prompt-executor-anthropic-client"))
    dokka(project(":prompt:prompt-executor:prompt-executor-clients:prompt-executor-bedrock-client"))
    dokka(project(":prompt:prompt-executor:prompt-executor-clients:prompt-executor-deepseek-client"))
    dokka(project(":prompt:prompt-executor:prompt-executor-clients:prompt-executor-google-client"))
    dokka(project(":prompt:prompt-executor:prompt-executor-clients:prompt-executor-ollama-client"))
    dokka(project(":prompt:prompt-executor:prompt-executor-clients:prompt-executor-openai-client"))
    dokka(project(":prompt:prompt-executor:prompt-executor-clients:prompt-executor-openai-model"))
    dokka(project(":prompt:prompt-executor:prompt-executor-clients:prompt-executor-openrouter-client"))
    dokka(project(":prompt:prompt-executor:prompt-executor-llms"))
    dokka(project(":prompt:prompt-executor:prompt-executor-llms-all"))
    dokka(project(":prompt:prompt-executor:prompt-executor-model"))
    dokka(project(":prompt:prompt-llm"))
    dokka(project(":prompt:prompt-markdown"))
    dokka(project(":prompt:prompt-model"))
    dokka(project(":prompt:prompt-structure"))
    dokka(project(":prompt:prompt-tokenizer"))
    dokka(project(":prompt:prompt-xml"))
    dokka(project(":koog-spring-boot-starter"))
    dokka(project(":koog-ktor"))
    dokka(project(":rag:rag-base"))
    dokka(project(":rag:vector-storage"))
}

val knitProperties: Provider<Properties> =
    providers.fileContents(layout.projectDirectory.file("knit.properties"))
        .asText
        .map { text ->
            Properties().apply {
                text.reader().use { load(it) }
            }
        }

val knitDir: Provider<String> =
    knitProperties.map { props ->
        requireNotNull(props.getProperty("knit.dir")) {
            "Missing 'knit.dir' in knit.properties"
        }
    }

ktlint {
    filter {
        exclude { it.file.path.contains("/docs/${knitDir.get()}/") }
    }
}

knit {
    rootDir = project.rootDir
    files = fileTree("docs/") {
        include("**/*.md")
    }
    moduleDocs = "docs/modules.md"
    siteRoot = "https://docs.koog.ai/"
}

tasks.register<Delete>("knitClean") {
    delete(
        fileTree(project.rootDir) {
            include("**/docs/${knitDir.get()}/**")
        }
    )
}

tasks.named("clean") {
    dependsOn("knitClean")
}

tasks.register<Delete>("knitAssemble") {
    dependsOn("knitClean", "knit", "assemble")
}

tasks.named("knit").configure { mustRunAfter("knitClean") }
tasks.named("assemble").configure { mustRunAfter("knit") }
