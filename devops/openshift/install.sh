#!/usr/bin/env bash

set -e

vagrant up
# TODO check for include in ~/.ssh/config
# Include config-vnet
vagrant ssh-config > ~/.ssh/config-vnet
# only install if Openshift is not installed
if [ -d openshift-ansible ]
then
  git clone https://github.com/openshift/openshift-ansible.git
  cd openshift-ansible && git checkout openshift-ansible-3.10.49-1 && cd ..
  ansible-playbook openshift-ansible/playbooks/prerequisites.yml -i openshift-inventory
  ansible-playbook openshift-ansible/playbooks/deploy-cluster.yml -i openshift-inventory
  # Fix broken DNS
  ansible-playbook fix.yml -i openshift-inventory
  # Run OC tasks as cluster-admin
  # Enables NFS-Volume
  ansible-playbook ansible-after-install.yml -i openshift-inventory
fi


