Copyright 2018 - The LineageOS Project

Device configuration for Motorola SDM660 Devices
================================================

Common tree for the Motorola sdm630/sdm636/sdm660 devices on the sdm660
4.19 kernel (github.com/moto-sdm660/android_kernel_motorola_sdm660).

Devices:

* Motorola moto x4 (payton): supported
* Motorola moto g7 plus (lake): work in progress
* Motorola moto g6 plus (evert): testing
* Motorola moto z3 play (beckham): planned
* Motorola one power (chef): planned

Each device tree provides what depends on its SoC or its blobs:

* configs/powerhint.json: the libperfmgr power HAL boosts, tuned for the
  SoC's CPU clusters (sdm630 and sdm636 differ).
* The Vulkan renderers (TARGET_USES_VULKAN, debug.renderengine.backend),
  only on devices whose vendor blobs carry a Vulkan driver.
* The boot control HAL, when the QTI one does not fit the device
  (payton uses its own AIDL HAL over the legacy bootctrl.qcom code).
