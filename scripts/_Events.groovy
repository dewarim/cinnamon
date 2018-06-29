eventCompileStart = { kind ->
    def buildNumber = metadata.'app.buildNumber'

    if (!buildNumber)
        buildNumber = 1
    else
        buildNumber = Integer.valueOf(buildNumber) + 1

    metadata.'app.buildNumber' = buildNumber.toString()

    metadata.persist()

    println "**** Compile Starting on Build #${buildNumber}"
}