/*
 * SPDX-FileCopyrightText: 2019-2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

#pragma once

#include <aidl/vendor/lineage/touch/BnKeyDisabler.h>
#include <com/fingerprints/extension/1.0/IFingerprintNavigation.h>

namespace aidl {
namespace vendor {
namespace lineage {
namespace touch {

class KeyDisabler : public BnKeyDisabler {
  public:
    KeyDisabler();

    ndk::ScopedAStatus getEnabled(bool* _aidl_return) override;
    ndk::ScopedAStatus setEnabled(bool enabled) override;

  private:
    android::sp<::com::fingerprints::extension::V1_0::IFingerprintNavigation>
            mFingerprintNavigation;
};

}  // namespace touch
}  // namespace lineage
}  // namespace vendor
}  // namespace aidl
