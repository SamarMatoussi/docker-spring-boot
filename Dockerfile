# Start with the latest Jenkins image
FROM jenkins/jenkins:latest AS jenkins

# Switch to root user to perform installations
USER root

# Set environment variables
ENV JAVA_OPTS -Djenkins.install.runSetupWizard=false
ENV CASC_JENKINS_CONFIG /var/jenkins_home/casc.yaml

# Copy necessary files
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
COPY pipeline.groovy /var/jenkins_home/pipeline.groovy

# Install Jenkins plugins
RUN jenkins-plugin-cli -f /usr/share/jenkins/ref/plugins.txt

# Copy Configuration as Code (CasC) file
COPY casc.yaml /var/jenkins_home/casc.yaml

# Use OpenJDK 8 as the base for the final image
FROM openjdk:8-jdk

# Set environment variables for Maven
ENV MAVEN_VERSION=3.6.3
ENV MAVEN_HOME=/usr/share/maven
ENV PATH=${MAVEN_HOME}/bin:${PATH}

# Copy Jenkins from the first stage
COPY --from=jenkins /var/jenkins_home /var/jenkins_home
COPY --from=jenkins /usr/share/jenkins/ref/plugins.txt /usr/share/jenkins/ref/plugins.txt

# Download and install Maven
RUN mkdir -p /usr/share/maven \
    && curl -fsSL https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz | tar -xzC /usr/share/maven --strip-components=1

# Verify Maven installation
RUN mvn --version

# Set the default working directory
WORKDIR /var/jenkins_home

# Set the default command to start Jenkins
CMD ["java", "-jar", "/usr/share/jenkins/jenkins.war"]
