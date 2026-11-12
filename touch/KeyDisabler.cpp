/*
 * SPDX-FileCopyrightText: 2019-2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

#include "KeyDisabler.h"

using ::com::fingerprints::extension::V1_0::IFingerprintNavigation;

namespace aidl {
namespace vendor {
namespace lineage {
namespace touch {

KeyDisabler::KeyDisabler() : mFingerprintNavigation(IFingerprintNavigation::getService()) {}

ndk::ScopedAStatus KeyDisabler::getEnabled(bool* _aidl_return) {
    if (mFingerprintNavigation == nullptr) {
        return ndk::ScopedAStatus::fromExceptionCode(EX_UNSUPPORTED_OPERATION);
    }

    *_aidl_return = mFingerprintNavigation->isEnabled();
    return ndk::ScopedAStatus::ok();
}

ndk::ScopedAStatus KeyDisabler::setEnabled(bool enabled) {
    if (mFingerprintNavigation == nullptr) {
        return ndk::ScopedAStatus::fromExceptionCode(EX_UNSUPPORTED_OPERATION);
    }

    mFingerprintNavigation->setNavigation(!enabled);
    return ndk::ScopedAStatus::ok();
}

}  // namespace touch
}  // namespace lineage
}  // namespace vendor
}  // namespace aidl
