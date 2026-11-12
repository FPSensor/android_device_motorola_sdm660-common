/*
 * SPDX-FileCopyrightText: 2019-2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

#define LOG_TAG "vendor.lineage.touch-service.sdm660"

#include <android-base/logging.h>
#include <android/binder_manager.h>
#include <android/binder_process.h>

#include "KeyDisabler.h"

using aidl::vendor::lineage::touch::KeyDisabler;

int main() {
    ABinderProcess_setThreadPoolMaxThreadCount(0);

    std::shared_ptr<KeyDisabler> keyDisabler = ndk::SharedRefBase::make<KeyDisabler>();
    const std::string instance = std::string(KeyDisabler::descriptor) + "/default";
    binder_status_t status =
            AServiceManager_addService(keyDisabler->asBinder().get(), instance.c_str());
    CHECK_EQ(status, STATUS_OK) << "Failed to add service " << instance;

    ABinderProcess_joinThreadPool();
    return EXIT_FAILURE;  // should not reach
}
