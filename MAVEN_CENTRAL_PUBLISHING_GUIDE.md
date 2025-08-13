# Maven Central Publishing Guide - Cloudinary Java SDK

This guide documents the complete process for publishing the Cloudinary Java SDK to Maven Central using the new Central Portal (central.sonatype.com), replacing the deprecated OSSRH system.

## 🎯 **Overview**

- **Old System:** `oss.sonatype.org` (dead, returns 401 errors)
- **New System:** `central.sonatype.com` with manual bundle upload
- **Method:** Manual bundle creation and upload (not automated plugin publishing)
- **Requirements:** Complete artifacts with checksums and GPG signatures
- **Current Version:** 2.3.1 → Next version (e.g., 2.3.2)

## 📋 **Prerequisites**

1. **Credentials:**
   - `centralUsername` and `centralPassword` for central.sonatype.com
   - Legacy `ossrhToken` and `ossrhTokenPassword` (if available)

2. **GPG Setup:**
   - GPG key imported: `6B42474E50D0D89A01B40AC225FE63F85DCB788F`
   - Private key available in repository: `private-key.asc`
   - Password: `nwov0aaStnO4`

3. **Java Version:**
   - **Java 8+** (current project targets Java 8)
   - Verify with: `java -version`

## 🔧 **Configuration Changes Required**

### 1. Update Root `build.gradle`

```gradle
plugins {
    id 'maven-publish'
    // Remove the old nexus plugin: id 'io.github.gradle-nexus.publish-plugin' version '1.0.0'
}

allprojects {
    repositories {
        mavenCentral()
    }
    project.ext.set("publishGroupId", group)
}

// Remove the old nexusPublishing block - we'll create bundles manually for Central Portal

tasks.create('createTestSubAccount') {
    doFirst {
        println("Task createTestSubAccount called with module $moduleName")
        def cloudinaryUrl = ""
        
        // core does not use test clouds, skip (keep empty file for a more readable generic travis test script)
        if (moduleName != "core") {
            println "Creating test cloud..."
            def baseUrl = new URL('https://sub-account-testing.cloudinary.com/create_sub_account')
            def connection = baseUrl.openConnection()
            connection.with {
                doOutput = true
                requestMethod = 'POST'
                def json = new JsonSlurper().parseText(content.text)
                def cloud = json["payload"]["cloudName"]
                def key = json["payload"]["cloudApiKey"]
                def secret = json["payload"]["cloudApiSecret"]
                cloudinaryUrl = "CLOUDINARY_URL=cloudinary://$key:$secret@$cloud"
            }
        }

        def dir = new File("${projectDir.path}${File.separator}tools")
        dir.mkdir()
        def file = new File(dir, "cloudinary_url.txt")
        file.createNewFile()
        file.text = cloudinaryUrl

        println("Test sub-account created successfully!")
    }
}
```

### 2. Create New `publish.gradle` for Modules

```gradle
apply plugin: 'maven-publish'
apply plugin: 'signing'

// Simple module-level publishing for manual upload to Central Portal
if (hasProperty("ossrhTokenPassword") || hasProperty("centralPassword")) {
    
    publishing {
        publications {
            mavenJava(MavenPublication) {
                // Set coordinates from gradle.properties
                groupId = project.ext.publishGroupId
                artifactId = project.name
                version = project.version
                
                // Include JAR artifacts and components for Java
                from components.java
                artifact sourcesJar
                artifact javadocJar
                
                pom {
                    name = getModuleName(project.name)
                    packaging = 'jar'
                    description = publishDescription
                    url = githubUrl
                    
                    licenses {
                        license {
                            name = licenseName
                            url = licenseUrl
                        }
                    }
                    
                    developers {
                        developer {
                            id = developerId
                            name = developerName
                            email = developerEmail
                        }
                    }
                    
                    scm {
                        connection = scmConnection
                        developerConnection = scmDeveloperConnection
                        url = scmUrl
                    }
                }
            }
        }
    }
    
    signing {
        // Configure GPG signing
        useGpgCmd()
        sign publishing.publications.mavenJava
    }
}

// Helper function to get proper module names
def getModuleName(artifactId) {
    switch(artifactId) {
        case 'cloudinary-core':
            return 'Cloudinary Core Library'
        case 'cloudinary-http5':
            return 'Cloudinary Apache HTTP 5 Library'
        case 'cloudinary-taglib':
            return 'Cloudinary Taglib Library'
        case 'cloudinary-test-common':
            return 'Cloudinary Test Common Library'
        default:
            return 'Cloudinary Java Library'
    }
}
```

### 3. Update Module `build.gradle` Files

For each module (cloudinary-core, cloudinary-http5, cloudinary-taglib, cloudinary-test-common), replace the publishing section:

```gradle
plugins {
    id 'java-library'
    // Remove: id 'signing'
    // Remove: id 'maven-publish' 
    // Remove: id 'io.codearte.nexus-staging' version '0.21.1'
}

apply from: "../java_shared.gradle"
apply from: "../publish.gradle"  // Apply our new simplified publishing

// Remove the entire old publishing block with nexusStaging
// The new publish.gradle handles everything
```

### 4. Update `gradle.properties`

```properties
# Update URLs to point to new system (for documentation)
publishRepo=https://central.sonatype.com/
snapshotRepo=https://central.sonatype.com/
publishDescription=Cloudinary is a cloud service that offers a solution to a web application's entire image management pipeline. Upload images to the cloud. Automatically perform smart image resizing, cropping and conversion without installing any complex software. Integrate Facebook or Twitter profile image extraction in a snap, in any dimension and style to match your website's graphics requirements. Images are seamlessly delivered through a fast CDN, and much much more. This Java library allows to easily integrate with Cloudinary in Java applications.
githubUrl=http://github.com/cloudinary/cloudinary_java
scmConnection=scm:git:git://github.com/cloudinary/cloudinary_java.git
scmDeveloperConnection=scm:git:git@github.com:cloudinary/cloudinary_java.git
scmUrl=http://github.com/cloudinary/cloudinary_java
licenseName=MIT
licenseUrl=http://opensource.org/licenses/MIT
developerId=cloudinary
developerName=Cloudinary
developerEmail=info@cloudinary.com

# Update version for next release
group=com.cloudinary
version=2.3.2

gnsp.disableApplyOnlyOnRootProjectEnforcement=true

# see https://github.com/gradle/gradle/issues/11308
systemProp.org.gradle.internal.publish.checksums.insecure=true
```

## 🚀 **Step-by-Step Publishing Process**

### Step 1: Environment Setup

```bash
# Navigate to project
cd /Users/adimizrahi/Development/Java/cloudinary_java

# Verify Java version (should be Java 8+)
java -version
javac -version

# Set GPG environment for batch signing
export GPG_TTY=$(tty)
```

### Step 2: Clean and Build All Artifacts

```bash
# Clean previous builds and generate all artifacts
./gradlew clean publishToMavenLocal
```

**Expected Output:** 
- JAR files for each module (cloudinary-core, cloudinary-http5, cloudinary-taglib, cloudinary-test-common)
- Sources JARs (`-sources.jar`)
- Javadoc JARs (`-javadoc.jar`)  
- POM files with correct XML structure
- All artifacts signed with GPG (`.asc` files)

### Step 3: Verify Artifacts Generated

```bash
# Check that all 4 modules have complete artifacts (should be 7 files each)
for module in ~/.m2/repository/com/cloudinary/cloudinary-*; do
    if [[ -d "$module" ]]; then
        echo "--- $(basename $module) ---"
        ls -1 $module/2.3.2/ 2>/dev/null | grep -E "\.(jar|pom|asc)$" | wc -l
    fi
done
```

**Expected:** Each module should show `7` files:
- `cloudinary-module-2.3.2.jar` + `.asc`
- `cloudinary-module-2.3.2-sources.jar` + `.asc` 
- `cloudinary-module-2.3.2-javadoc.jar` + `.asc`
- `cloudinary-module-2.3.2.pom` + `.asc`

### Step 4: Verify POM Files Are Valid

```bash
# Check that POM files have proper metadata
for pom in ~/.m2/repository/com/cloudinary/cloudinary-*/2.3.2/*.pom; do
    if [[ -f "$pom" ]]; then
        echo "--- $(basename $pom) ---"
        echo "Name tags: $(grep -c "<name>" "$pom")"
        echo "Description: $(grep -c "<description>" "$pom")"
        echo "License: $(grep -c "<license>" "$pom")"
        echo "Developer: $(grep -c "<developer>" "$pom")"
        echo "SCM: $(grep -c "<scm>" "$pom")"
    fi
done
```

**Expected:** Each POM should have all required metadata elements.

### Step 5: Generate Additional Checksums

```bash
cd ~/.m2/repository

# Generate MD5 and SHA1 checksums for all artifacts (Central Portal requires these)
find com/cloudinary/cloudinary-* -name "*.jar" -o -name "*.pom" | while read file; do
    if [[ -f "$file" ]]; then
        echo "Processing $file"
        md5sum "$file" | awk '{print $1}' > "$file.md5"
        sha1sum "$file" | awk '{print $1}' > "$file.sha1"
    fi
done
```

### Step 6: Verify Complete File Set

```bash
cd ~/.m2/repository

echo "=== FINAL FILE COUNT CHECK ==="
echo "JAR/POM files:" && find com/cloudinary/cloudinary-* -name "*.jar" -o -name "*.pom" | wc -l
echo "GPG signatures:" && find com/cloudinary/cloudinary-* -name "*.asc" | wc -l
echo "MD5 checksums:" && find com/cloudinary/cloudinary-* -name "*.md5" | wc -l  
echo "SHA1 checksums:" && find com/cloudinary/cloudinary-* -name "*.sha1" | wc -l
```

**Expected File Count:**
- 4 modules × 4 artifacts each = **16 original files**
- **16 GPG signatures** (`.asc`)
- **16 MD5 checksums** (`.md5`)
- **16 SHA1 checksums** (`.sha1`)
- **Total: 64 files**

### Step 7: Create Final Bundle

```bash
cd ~/.m2/repository

# Create the complete bundle for Central Portal upload
BUNDLE_NAME="cloudinary-java-$(grep '^version=' ~/Development/Java/cloudinary_java/gradle.properties | cut -d'=' -f2)-bundle-COMPLETE.tar.gz"

tar -czf ~/"$BUNDLE_NAME" \
$(find com/cloudinary/cloudinary-* \
  -name "*.pom" -o -name "*.jar" \
  -o -name "*.md5" -o -name "*.sha1" -o -name "*.asc" | \
  grep -v maven-metadata | sort)
```

### Step 8: Verify Final Bundle

```bash
cd ~/

# Check bundle size and contents
ls -lh cloudinary-java-*-bundle-COMPLETE.tar.gz
echo "--- File count ---"
tar -tzf cloudinary-java-*-bundle-COMPLETE.tar.gz | wc -l
echo "--- Sample contents ---" 
tar -tzf cloudinary-java-*-bundle-COMPLETE.tar.gz | head -16
echo "--- Module breakdown ---"
tar -tzf cloudinary-java-*-bundle-COMPLETE.tar.gz | grep -E "(core|http5|taglib|test-common)" | cut -d'/' -f3 | sort | uniq -c
```

**Expected:**
- **Size:** ~1-2MB (smaller than Android due to fewer dependencies)
- **Files:** 64 total
- **Modules:** 4 modules with 16 files each
- **Contents:** Each module should have JARs, POMs, and all checksums/signatures

## 📤 **Upload to Central Portal**

### Manual Upload Process

1. **Login:** Go to https://central.sonatype.com/
2. **Credentials:** Use `centralUsername` and `centralPassword`
3. **Upload:** Navigate to "Upload Component" or "Publish"
4. **Bundle:** Select the `.tar.gz` file created in Step 7
5. **Publishing Type:** Choose "USER_MANAGED"
6. **Publication Name:** "Cloudinary Java SDK v{version}"

### Expected Validation

The Central Portal will validate:
- ✅ **POM structure** (proper XML with required metadata)
- ✅ **Artifact integrity** (MD5/SHA1 checksums match)
- ✅ **Signatures** (GPG signatures valid)
- ✅ **Completeness** (all required files present)
- ✅ **Java compatibility** (JAR files are valid)

## 🛠 **Troubleshooting**

### Common Issues & Solutions

1. **GPG Signing Issues:**
   - **Cause:** TTY or batch mode problems
   - **Solution:** `export GPG_TTY=$(tty)` and use `--batch --yes` flags
   - **Alternative:** Use `signing { useGpgCmd() }` in Gradle

2. **Missing Dependencies in POM:**
   - **Cause:** Gradle not including transitive dependencies
   - **Solution:** Verify `from components.java` includes dependencies
   - **Check:** Examine generated POM files for `<dependencies>` section

3. **Version Conflicts:**
   - **Cause:** Old artifacts in local repository
   - **Solution:** `./gradlew clean` and delete `~/.m2/repository/com/cloudinary/`

4. **Module Configuration Issues:**
   - **Cause:** Inconsistent `build.gradle` files between modules
   - **Solution:** Ensure all modules apply `publish.gradle` consistently

5. **Bundle Upload Failures:**
   - **Cause:** Missing or corrupted files in bundle
   - **Solution:** Verify all 64 files present and re-create bundle

## 📋 **Module-Specific Information**

### Cloudinary Core (`cloudinary-core`)
- **Artifact ID:** `cloudinary-core`
- **Description:** Core Cloudinary functionality
- **Dependencies:** Minimal (mostly standard Java libraries)

### Cloudinary HTTP5 (`cloudinary-http5`)
- **Artifact ID:** `cloudinary-http5`
- **Description:** Apache HTTP Client 5 implementation
- **Dependencies:** `cloudinary-core`, Apache HTTP Components

### Cloudinary Taglib (`cloudinary-taglib`)
- **Artifact ID:** `cloudinary-taglib`
- **Description:** JSP Taglib for Cloudinary
- **Dependencies:** `cloudinary-core`, Servlet API

### Cloudinary Test Common (`cloudinary-test-common`)
- **Artifact ID:** `cloudinary-test-common`  
- **Description:** Shared test utilities
- **Dependencies:** `cloudinary-core`, JUnit, test frameworks

## 📝 **Version Update Checklist**

For publishing a new version:

- [ ] Update `version` in `gradle.properties`
- [ ] Update this guide with new version number
- [ ] Run complete publishing process (Steps 1-8)
- [ ] Verify all 64 files in final bundle (4 modules × 16 files)
- [ ] Upload to Central Portal
- [ ] Verify publication appears on Maven Central
- [ ] Update GitHub releases and tags
- [ ] Test artifacts can be consumed by dependent projects

## 🔗 **References**

- **Central Portal:** https://central.sonatype.com/
- **Migration Guide:** https://central.sonatype.org/publish/publish-guide/
- **Gradle Publishing:** https://docs.gradle.org/current/userguide/publishing_maven.html

---

**Last Updated:** [Current Date]  
**Tested Version:** 2.3.2  
**Success Rate:** ✅ To be tested with this process 

## 🚨 **Key Differences from Android SDK**

1. **No AAR files** - Uses JAR files instead
2. **Java components** - Uses `components.java` instead of `components.release`
3. **Simpler setup** - No Android-specific build tools required
4. **Standard Maven structure** - Follows typical Java library patterns
5. **Fewer files per module** - 16 files per module vs 24 for Android modules
