## Release guide

### Check README.md and build.gradle

Be sure that [build.gradle](build.gradle) and [Readme](../README.md) are both updated and contain new, to be released version.

### Run static checkers and tests
`./gradlew clean build checkstyle pmd findbugs test`

### Run gradle task

`./gradlew clean build javadocs assemble`

`./gradlew bintrayUpload -Pbintray_username={yourBintrayUsername} -Pbintray_api_key={bintrayApiKey}`

Replace `{yourBintrayUsername}` with your username on Bintray and `{bintrayApiKey}` with Bintray api key. Api key is available under Edit Profile -> API Key

### Bintray publish

Manually go to [Bintray page](https://bintray.com/infinum/android), check that all 8 files are there and publish them if everything is OK.