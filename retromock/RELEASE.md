## Release guide

### Check README.md and build.gradle

Be sure that [build.gradle](build.gradle), [Changelog](../CHANGELOG.md) and [Readme](../README.md) are updated and contain new, to be released version.

### Run static checkers and tests
`./gradlew clean build checkstyleMain checkstyleTest pmdMain pmdTest spotBugsMain test`

### Run gradle task

`./gradlew clean build javadoc jar sourceJar`

`./gradlew publish -PsonatypeUsername={yourMavenUsername} -PsonatypePassword={yourMavenPassword}`

Replace `{sonatypeUsername}` with your username on Maven Central and `{yourMavenPassword}` with Maven Central password.

### Maven Central publish

Manually go to [Repository](https://oss.sonatype.org/#stagingRepositories), check that all 8 files are there and publish them if everything is OK.
