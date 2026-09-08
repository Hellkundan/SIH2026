from enum import Enum

class VerificationType(str, Enum):
    GST = "GST"
    UDYAM = "UDYAM"
    PAN = "PAN"
    EPFO = "EPFO"
    ESIC = "ESIC"
    STARTUP_INDIA = "STARTUP_INDIA"
    NSIC = "NSIC"
    OEM = "OEM"
    BLACKLIST = "BLACKLIST"

class VerificationStatus(str, Enum):
    VERIFIED = "VERIFIED"
    NOT_FOUND = "NOT_FOUND"
    FAILED = "FAILED"
    PENDING = "PENDING"
    MANUAL_REVIEW = "MANUAL_REVIEW"

class ErrorState(str, Enum):
    NONE = "NONE"
    PROVIDER_UNAVAILABLE = "PROVIDER_UNAVAILABLE"
    TIMEOUT = "TIMEOUT"
    INVALID_REQUEST = "INVALID_REQUEST"
    INTERNAL_ERROR = "INTERNAL_ERROR"
