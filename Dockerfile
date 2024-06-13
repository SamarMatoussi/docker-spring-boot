FROM jenkins/jenkins:latest

# Switch to root user to perform installations
USER root

# Préparation de Jenkins
ENV JAVA_OPTS -Djenkins.install.runSetupWizard=false
ENV CASC_JENKINS_CONFIG /var/jenkins_home/casc.yaml

# Installation des plugins Jenkins à partir du fichier plugins.txt
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli -f /usr/share/jenkins/ref/plugins.txt

# Copie du script de pipeline Groovy
COPY pipeline.groovy /var/jenkins_home/pipeline.groovy

# Installation de Maven
ENV MAVEN_VERSION=3.6.3
ENV MAVEN_HOME=/usr/share/maven
ENV PATH=${MAVEN_HOME}/bin:${PATH}

RUN mkdir -p /usr/share/maven \
    && curl -fsSL https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz | tar -xzC ${MAVEN_HOME} --strip-components=1

# Vérification de l'installation de Maven
RUN mvn --version

# Copie du fichier de configuration CASC
COPY casc.yaml /var/jenkins_home/casc.yaml
