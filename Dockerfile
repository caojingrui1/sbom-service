FROM openeuler/openeuler:22.03-lts@sha256:a96e504086acb8cd22d551d28252658e4440a4dae4bdecb3fed524deeb74ea75 AS build

RUN yum update -y && yum install -y \
    git \
    java-17-openjdk \
    python3-pip \
    && rm -rf /var/cache/yum \
    && pip3 install virtualenv

#install RASP
ARG PUBLIC_USER
ARG PUBLIC_PASSWORD
RUN git clone https://$PUBLIC_USER:$PUBLIC_PASSWORD@github.com/Open-Infra-Ops/plugins  /opt/plugins \
    && cp /opt/plugins/armorrasp/rasp.tgz /opt \
    && tar -zxf /opt/rasp.tgz \
    && chown -R root:root /opt/rasp.tgz && chmod 755 -R /opt/rasp.tgz \
    && rm -rf /opt/plugins 

WORKDIR /opt
RUN git clone --recurse-submodules https://github.com/opensourceways/sbom-service.git
WORKDIR /opt/sbom-service
RUN /bin/bash gradlew bootWar

ENTRYPOINT ["/bin/bash", "/opt/sbom-service/docker-entrypoint.sh"]

