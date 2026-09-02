# Add-on execution

This repository starts inactive and stock-safe. Implement only the smallest
observed Lootr rendering defect before staging.

Before running Gradle gates, activate a Python 3.11 or newer virtual
environment, initialize both pinned source submodules, and install the exact
development-only toolkit into it:

```bash
git submodule update --init --recursive -- \
  tooling/bluemap-addon-toolkit modules/bluemap-addon-adapter-api
python -m pip install --disable-pip-version-check --no-deps \
  --require-hashes --only-binary=:all: \
  --requirement requirements/toolkit.txt
```

## Prototype

Acquire and verify the exact candidate JARs outside Git. Their Gradle
properties are:

- `-PlootrJar=/path/to/lootr-neoforge-1.21.1-1.11.37.122.jar`

Then run:

```bash
gradle --no-daemon -PbluemapSourcePath=/path/to/BlueMap-at-7e07f4e7 \
  <exact-candidate-properties> clean prototypeCheck build
bash gallery/package.sh /tmp/lootr-gallery.zip
```

Deploy that JAR and gallery only to disposable staging, verify the intended
BlueMap link loads, and compare it with the matching client. Iterate from
observed defects until the owner explicitly accepts one exact staging JAR.

## Acceptance and release

The migration candidate records the production JAR, sources JAR, POM, and
Gradle module identities under `candidate_artifacts`. After visual acceptance,
change the provenance status to `owner-accepted-release-candidate` and record
the exact integration manifest, source candidate, and staged activation overlay
under `combined_integration_acceptance`.

Promote `addon_version` through a pull request, remove every unresolved
placeholder marker, and run with all exact candidate properties:

```bash
gradle --no-daemon -PbluemapSourcePath=/path/to/BlueMap-at-7e07f4e7 \
  <exact-candidate-properties> -PreleaseTag=v0.1.0-alpha.2 \
  clean build generatePomFileForAddonPublication \
  generateMetadataFileForAddonPublication verifyReleaseCandidate
```

Merge only after owner acceptance and final-head CI pass this gate. Create an annotated
`v<version>` tag at reviewed `main`; the release workflow independently checks
the tag, exact BlueMap checkout, accepted bytes and draft assets before making
the prerelease public. Publication never deploys to production.
