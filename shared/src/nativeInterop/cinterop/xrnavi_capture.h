#import <AVFoundation/AVFoundation.h>

static inline AVAuthorizationStatus xrnaviAuthorizationStatus(AVMediaType mediaType) {
    return [AVCaptureDevice authorizationStatusForMediaType:mediaType];
}

static inline void xrnaviRequestAccess(AVMediaType mediaType, void (^ _Nonnull handler)(BOOL granted)) {
    [AVCaptureDevice requestAccessForMediaType:mediaType completionHandler:handler];
}

static inline void xrnaviEnableDistortionCorrection(AVCaptureDevice * _Nonnull device) {
    if (device.isGeometricDistortionCorrectionSupported) {
        device.geometricDistortionCorrectionEnabled = YES;
    }
}
