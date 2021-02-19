plugins {
    java
    kotlin("jvm") version "1.4.30"
}

group = "com.chutneytesting"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.chutneytesting:chutney-kotlin-dsl:0.1.3")
}
