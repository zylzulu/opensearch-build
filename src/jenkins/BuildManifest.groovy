/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

package jenkins

class BuildManifest implements Serializable {
    class Build implements Serializable {
        String name
        String version
        String platform
        String architecture

        Build(Map data) {
            this.name = data.name
            this.version = data.version
            this.platform = data.platform
            this.architecture = data.architecture
        }

        String getFilename() {
            return this.name.toLowerCase().replaceAll(' ', '-')
        }
    }

    class Components implements Serializable {
        String dist

        Components(Map data) {
            this.dist = data.artifacts.dist
        }

        String getDistPackageLocation() {
            try {
                return this.dist[0]
            } catch (Exception e) {
                echo "Exception: ${e}"
            }
            
        }
    }

    Build build
    Components components

    BuildManifest(Map data) {
        this.build = new BuildManifest.Build(data.build)
        this.components = new BuildManifest.Components(data.components)
    }

    public String getArtifactRoot(String jobName, String buildNumber) {
        return [
            jobName,
            this.build.version,
            buildNumber,
            this.build.platform,
            this.build.architecture
        ].join("/")
    }

    public String getArtifactRootUrl(String publicArtifactUrl = 'https://ci.opensearch.org/ci/dbc', String jobName, String buildNumber) {
        return [
            publicArtifactUrl,
            this.getArtifactRoot(jobName, buildNumber)
        ].join('/')
    }

    public String getPackageName() {
        String packagePrefix = [
            this.buid.getFilename(),
            this.build.version,
            this.build.platform,
            this.build.architecture,       
        ].join('-')
        return packagePrefix + '.tar.gz'
    }
}
