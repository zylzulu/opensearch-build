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

        String getPackageName() {
            String packagePrefix = [
                this.getFilename(),
                this.version,
                this.platform,
                this.architecture,
            ].join('-')
            return packagePrefix + '.tar.gz'
        }
    }

    class Components extends ArrayList {
        Map artifacts

        Components(ArrayList data) {
            this.artifacts = data[0].artifacts
        }

    }

    class Component implements Serializable{
        String dist

        Component(Map data) {
            this.dist = data.dist
        }

        String getDistPackageLocation(){
            try {
                if (this.dist.size() == 1 && this.dist[0].length() != 0){
                    return this.dist[0]
                }
            } catch (Exception ex){
                throw new Exception("The number of items found in dist is either 0 or more than one", ex.printStackTrace())
            }
        }

    }

    Build build
    Components components
    Component component

    BuildManifest(Map data) {
        this.build = new BuildManifest.Build(data.build)
        this.components = new BuildManifest.Components(data.components)
        this.component = new BuildManifest.Component(this.components.artifacts)
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
}
