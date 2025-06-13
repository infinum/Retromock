## Release guide

### Check README.md and build.gradle

Be sure that [build.gradle](build.gradle), [Changelog](../CHANGELOG.md) and [Readme](../README.md) are updated and contain new, to be released version.

### Run static checkers and tests
`./gradlew clean build checkstyleMain checkstyleTest pmdMain pmdTest findbugsMain findbugsTest test`

### Run gradle task

`./gradlew clean :library:publishMavenPublicationToMavenCentralRepository`

Check [docs](https://vanniktech.github.io/gradle-maven-publish-plugin/central) to see if you have complete setup for publishing plugin on your computer.

### Maven Central publish

Manually go to [Maven Central](https://central.sonatype.com/publishing/deployments), check that all files are there and publish them if everything is OK.
