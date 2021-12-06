# SPDX-License-Identifier: Apache-2.0
#
# The OpenSearch Contributors require contributions made to
# this file be licensed under the Apache-2.0 license or a
# compatible open source license.

import errno
import logging
import os
import shutil
import subprocess
import tarfile
import zipfile
from abc import ABC, abstractmethod
from assemble_workflow.fpm_builder import FpmBuilder

from system.zip_file import ZipFile


class Dist(ABC):
    def __init__(self, name, path):
        self.name = name
        self.path = path

    @abstractmethod
    def __extract__(self, dest):
        pass

    @property
    def distribution(self):
        pass

    def extract(self, dest):
        self.__extract__(dest)

        # OpenSearch & Dashboard tars will include only a single folder at the top level of the tar.

        for file in os.scandir(dest):
            if file.is_dir():
                self.archive_path = file.path
                return self.archive_path

        raise FileNotFoundError(errno.ENOENT, os.strerror(errno.ENOENT), os.path.join(dest, "*"))

    def build(self, bundle_recorder, dest):
        name = bundle_recorder.package_name
        self.__build__(bundle_recorder, dest)
        path = os.path.join(dest, name)
        shutil.copyfile(name, path)
        logging.info(f"Published {path}.")


class DistZip(Dist):
    def __extract__(self, dest):
        with ZipFile(self.path, "r") as zip:
            zip.extractall(dest)

    def __build__(self, bundle_recorder, dest):
        with ZipFile(bundle_recorder.package_name, "w", zipfile.ZIP_DEFLATED) as zip:
            rootlen = len(self.archive_path) + 1
            for base, dirs, files in os.walk(self.archive_path):
                for file in files:
                    fn = os.path.join(base, file)
                    zip.write(fn, fn[rootlen:])

    @property
    def distribution(self):
        return "zip"


class DistTar(Dist):
    def __extract__(self, dest):
        with tarfile.open(self.path, "r:gz") as tar:
            tar.extractall(dest)

    def __build__(self, bundle_recorder, dest):
        with tarfile.open(bundle_recorder.package_name, "w:gz") as tar:
            tar.add(self.archive_path, arcname=os.path.basename(self.archive_path))

    @property
    def distribution(self):
        return "tar"


class DistRpm(Dist):
    def __extract__(self, dest):
        with tarfile.open(self.path, "r:gz") as tar:
            tar.extractall(dest)

    @property
    def distribution(self):
        return "rpm"

    def __build__(self, bundle_recorder, dest):
        logging.info("build for rpm distribution.")
        FpmBuilder().build(bundle_recorder, self.archive_path, self.distribution)
