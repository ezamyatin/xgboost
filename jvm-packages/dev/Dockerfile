#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#
FROM centos:7.9.2009

RUN sed -i 's/mirrorlist/#mirrorlist/g' /etc/yum.repos.d/CentOS-*
RUN sed -i 's|#baseurl=http://mirror.centos.org|baseurl=http://vault.centos.org|g' /etc/yum.repos.d/CentOS-*
RUN \
echo "" >> /etc/yum.repos.d/CentOS-Base.repo && \
echo "[centos-sclo-rh]" >> /etc/yum.repos.d/CentOS-Base.repo && \
echo "name=CentOS-$releasever - SCLo rh" >> /etc/yum.repos.d/CentOS-Base.repo && \
echo "baseurl=http://vault.centos.org/altarch/$releasever/sclo/$basearch/rh" >> /etc/yum.repos.d/CentOS-Base.repo && \
echo "gpgcheck=1" >> /etc/yum.repos.d/CentOS-Base.repo && \
echo "enabled=0" >> /etc/yum.repos.d/CentOS-Base.repo && \
echo "gpgkey=file:///etc/pki/rpm-gpg/RPM-GPG-KEY-CentOS-7" >> /etc/yum.repos.d/CentOS-Base.repo && \
echo "       file:///etc/pki/rpm-gpg/RPM-GPG-KEY-CentOS-7-aarch64" >> /etc/yum.repos.d/CentOS-Base.repo && \
echo "       " >> /etc/yum.repos.d/CentOS-Base.repo && \
echo "[centos-sclo-sclo]" >> /etc/yum.repos.d/CentOS-Base.repo && \
echo "name=CentOS-$releasever - SCLo sclo" >> /etc/yum.repos.d/CentOS-Base.repo && \
echo "baseurl=http://vault.centos.org/altarch/$releasever/sclo/$basearch/sclo" >> /etc/yum.repos.d/CentOS-Base.repo && \
echo "gpgcheck=1" >> /etc/yum.repos.d/CentOS-Base.repo && \
echo "enabled=0" >> /etc/yum.repos.d/CentOS-Base.repo && \
echo "gpgkey=file:///etc/pki/rpm-gpg/RPM-GPG-KEY-CentOS-7" >> /etc/yum.repos.d/CentOS-Base.repo && \
echo "       file:///etc/pki/rpm-gpg/RPM-GPG-KEY-CentOS-7-aarch64" >> /etc/yum.repos.d/CentOS-Base.repo && \
echo "" >> /etc/yum.repos.d/CentOS-Base.repo

RUN yum clean all && yum makecache

# Install all basic requirements
RUN yum -y update
RUN yum install -y make bzip2 tar unzip wget xz git centos-release-scl yum-utils java-1.8.0-openjdk-devel
RUN yum-config-manager --enable centos-sclo-rh-testing --enablerepo='centos-sclo-rh' --enablerepo='centos-sclo-sclo' 
#RUN yum -y update
RUN yum install -y devtoolset-7-gcc devtoolset-7-binutils devtoolset-7-gcc-c++
RUN yum install -y blas-devel lapack-devel

    # Python
RUN wget https://repo.continuum.io/miniconda/Miniconda3-4.5.12-Linux-x86_64.sh
RUN bash Miniconda3-4.5.12-Linux-x86_64.sh -b -p /opt/python
    # CMake
RUN wget -nv -nc https://cmake.org/files/v3.13/cmake-3.13.0-Linux-x86_64.sh --no-check-certificate
RUN bash cmake-3.13.0-Linux-x86_64.sh --skip-license --prefix=/usr
    # Maven
RUN wget https://archive.apache.org/dist/maven/maven-3/3.6.1/binaries/apache-maven-3.6.1-bin.tar.gz
RUN tar xvf apache-maven-3.6.1-bin.tar.gz -C /opt
RUN ln -s /opt/apache-maven-3.6.1/ /opt/maven

# Set the required environment variables
ENV PATH=/opt/python/bin:/opt/maven/bin:$PATH
ENV CC=/opt/rh/devtoolset-7/root/usr/bin/gcc
ENV CXX=/opt/rh/devtoolset-7/root/usr/bin/c++
ENV CPP=/opt/rh/devtoolset-7/root/usr/bin/cpp
ENV JAVA_HOME=/usr/lib/jvm/java

# Install Python packages
# RUN pip install numpy pytest scipy scikit-learn wheel kubernetes urllib3==1.22 awscli

WORKDIR /xgboost
