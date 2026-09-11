from app.models.enums import VerificationType
from app.models.request import VerificationRequest
from app.models.bidder import BidderVerificationRequest


def _checks(data: BidderVerificationRequest):
    mapping = [
        (VerificationType.PAN, data.pan),
        (VerificationType.GST, data.gstin),
        (VerificationType.UDYAM, data.udyam),
        (VerificationType.EPFO, data.epfo),
        (VerificationType.ESIC, data.esic),
        (VerificationType.STARTUP_INDIA, data.startup_india),
        (VerificationType.NSIC, data.nsic),
        (VerificationType.OEM, data.oem),
        (VerificationType.BLACKLIST, data.blacklist or data.pan),
    ]
    return [
        VerificationRequest(
            bidder_id=data.bidder_id,
            verification_type=vtype,
            identifier=identifier,
            company_name=data.company_name,
            pan=data.pan,
            gstin=data.gstin,
            aadhaar=data.aadhaar,
        )
        for vtype, identifier in mapping
        if identifier
    ]


def clean_bidder() -> list[VerificationRequest]:
    # Each identifier is a known positive synthetic record. The scenario
    # intentionally omits company_name so provider-level identity matching
    # is not confused with the cross-provider compliance demo.
    return _checks(BidderVerificationRequest(
        bidder_id="DEMO-CLEAN-001",
        pan="IXUWX1025G",
        gstin="21XDDMN1036R9Z1",
        udyam="UDYAM-OD-06-1000000",
        epfo="EPFO-2000",
        esic="ESIC-3001",
        startup_india="DIPP-50000",
        nsic="NSIC-4001",
        oem="OEM-5002",
    ))


def incomplete_bidder() -> list[VerificationRequest]:
    return _checks(BidderVerificationRequest(
        bidder_id="DEMO-INCOMPLETE-001",
        company_name="Northern Enterprises Pvt Ltd",
        pan="IXUWX1025G",
        gstin="NOT-A-REAL-GST",
    ))


def suspicious_bidder() -> list[VerificationRequest]:
    return _checks(BidderVerificationRequest(
        bidder_id="DEMO-SUSPICIOUS-001",
        company_name="National Foods & Co",
        pan="DGNKJ1080X",
    ))


SCENARIOS = {
    "clean": clean_bidder,
    "incomplete": incomplete_bidder,
    "suspicious": suspicious_bidder,
}
