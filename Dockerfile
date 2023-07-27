FROM openeuler/openeuler:22.03-lts@sha256:a96e504086acb8cd22d551d28252658e4440a4dae4bdecb3fed524deeb74ea75 AS build

RUN yum update -y && yum install -y \
    git \
    java-17-openjdk \
    python3-pip \
    && rm -rf /var/cache/yum \
    && pip3 install virtualenv

WORKDIR /opt
RUN git clone --recurse-submodules https://github.com/opensourceways/sbom-service.git
WORKDIR /opt/sbom-service
RUN /bin/bash gradlew bootWar

#install RASP
ARG PUBLIC_USER
ARG PUBLIC_PASSWORD
RUN git clone https://$PUBLIC_USER:$PUBLIC_PASSWORD@github.com/Open-Infra-Ops/plugins \
    && cp plugins/armorrasp/rasp.tgz . \
    && tar -zxf rasp.tgz \
    && chown -R root:root rasp && chmod 755 -R rasp \
    && rm -rf plugins  

ENTRYPOINT ["/bin/bash", "/opt/sbom-service/docker-entrypoint.sh"]
