#!/usr/bin/env bash

set -e

#packer
curl -fSL "https://releases.hashicorp.com/packer/1.1.3/packer_1.1.3_linux_amd64.zip" -o packer.zip
unzip packer.zip -d /opt/packer
sudo ln -s /opt/packer/packer /usr/bin/packer
rm -f packer.zip

#ansible
sudo apt-get -y install ansible
