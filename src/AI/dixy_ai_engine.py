
import re
from rapidfuzz import fuzz


# ============================================================
# TEXT NORMALIZATION
# ============================================================

def normalize_text(value):

    if value is None:
        return ""

    value = str(value).upper().strip()

    value = re.sub(r'[^A-Z0-9\s]', ' ', value)

    value = re.sub(r'\bPRIVATE LIMITED\b', ' ', value)
    value = re.sub(r'\bPRIVATE LTD\b', ' ', value)
    value = re.sub(r'\bPVT LIMITED\b', ' ', value)
    value = re.sub(r'\bPVT LTD\b', ' ', value)
    value = re.sub(r'\bLIMITED\b', ' ', value)
    value = re.sub(r'\bLTD\b', ' ', value)
    value = re.sub(r'\bPVT\b', ' ', value)

    value = re.sub(r'\s+', ' ', value).strip()

    return value


def text_similarity(a, b):

    a = normalize_text(a)
    b = normalize_text(b)

    if not a or not b:
        return 0.0

    return fuzz.ratio(a, b) / 100.0


# ============================================================
# IDENTITY EXTRACTION
# ============================================================


# ============================================================
# DYNAMIC IDENTITY EXTRACTION
# ============================================================

def extract_identity_data(bidder_json):

    """
    Extract identity-related information from the OCR JSON.

    The engine supports the currently known documents while
    preserving additional document information for future
    identity-graph checks.

    Missing documents are treated as unavailable evidence,
    NOT as automatic failures.
    """

    identity = {}

    # --------------------------------------------------------
    # PAN
    # --------------------------------------------------------

    pan_data = bidder_json.get("pan", {})

    if isinstance(pan_data, dict):
        identity["pan"] = {
            "number": pan_data.get("pan"),
            "name": pan_data.get("name"),
            "address": pan_data.get("address")
        }

    # --------------------------------------------------------
    # GST
    # --------------------------------------------------------

    gst_data = bidder_json.get("gst", {})

    if isinstance(gst_data, dict):
        identity["gst"] = {
            "gstin": gst_data.get("gstin"),
            "name": gst_data.get("legal_name"),
            "trade_name": gst_data.get("trade_name"),
            "address": gst_data.get("address"),
            "state": gst_data.get("state")
        }

    # --------------------------------------------------------
    # UDYAM
    # --------------------------------------------------------

    udyam_data = bidder_json.get("udyam", {})

    if isinstance(udyam_data, dict):
        identity["udyam"] = {
            "number": udyam_data.get("udyam_number"),
            "name": udyam_data.get("enterprise_name"),
            "pan": udyam_data.get("pan"),
            "gstin": udyam_data.get("gstin"),
            "address": udyam_data.get("address")
        }

    # --------------------------------------------------------
    # MCA / CIN
    # --------------------------------------------------------

    mca_data = bidder_json.get("mca", {})

    if isinstance(mca_data, dict):
        identity["mca"] = {
            "cin": mca_data.get("cin"),
            "name": mca_data.get("company_name"),
            "address": mca_data.get("registered_office_address"),
            "state": mca_data.get("registered_state"),
            "status": mca_data.get("company_status")
        }

    # --------------------------------------------------------
    # CIN directly supplied by OCR
    # --------------------------------------------------------

    cin_data = bidder_json.get("cin", {})

    if isinstance(cin_data, dict):
        identity["cin"] = {
            "number": cin_data.get("cin"),
            "name": cin_data.get("company_name")
        }

    # --------------------------------------------------------
    # ITR
    # --------------------------------------------------------

    itr_data = bidder_json.get("itr", {})

    if isinstance(itr_data, dict):
        identity["itr"] = {
            "pan": itr_data.get("pan"),
            "name": itr_data.get("name"),
            "acknowledgement_number": itr_data.get(
                "acknowledgement_number"
            ),
            "assessment_year": itr_data.get(
                "assessment_year"
            ),
            "turnover": itr_data.get("turnover"),
            "address": itr_data.get("address")
        }

    # --------------------------------------------------------
    # AUTHORIZED PERSON
    # --------------------------------------------------------

    authorized_data = bidder_json.get(
        "authorized_person",
        bidder_json.get("authorized_signatory", {})
    )

    if isinstance(authorized_data, dict):
        identity["authorized_person"] = {
            "name": authorized_data.get("name"),
            "pan": authorized_data.get("pan"),
            "aadhaar": authorized_data.get("aadhaar"),
            "designation": authorized_data.get("designation")
        }

    # --------------------------------------------------------
    # ADDRESS
    # --------------------------------------------------------

    address_data = bidder_json.get("address", {})

    if isinstance(address_data, dict):
        identity["address"] = {
            "registered": address_data.get("registered"),
            "business": address_data.get("business"),
            "state": address_data.get("state")
        }

    # --------------------------------------------------------
    # PRESERVE OTHER DOCUMENT TYPES
    # --------------------------------------------------------

    known_documents = {
        "pan",
        "gst",
        "udyam",
        "mca",
        "cin",
        "itr",
        "authorized_person",
        "authorized_signatory",
        "address"
    }

    identity["available_documents"] = [
        key for key in bidder_json.keys()
        if key not in known_documents
    ]

    return identity


# ============================================================
# CROSS DOCUMENT CONSISTENCY
# ============================================================

def check_cross_document_consistency(identity):

    results = []

    pan_name = identity["pan"].get("name")
    pan_number = identity["pan"].get("number")

    gst_name = identity["gst"].get("name")
    gstin = identity["gst"].get("gstin")

    udyam_name = identity["udyam"].get("name")


    if pan_name and gst_name:

        score = text_similarity(
            pan_name,
            gst_name
        ) * 100

        if score >= 85:
            status = "CONSISTENT"

        elif score >= 70:
            status = "REVIEW"

        else:
            status = "DISCREPANCY"

        results.append({

            "check": "PAN name vs GST legal name",

            "pan_name": pan_name,

            "gst_name": gst_name,

            "similarity": round(score, 2),

            "status": status

        })


    if pan_name and udyam_name:

        score = text_similarity(
            pan_name,
            udyam_name
        ) * 100

        if score >= 85:
            status = "CONSISTENT"

        elif score >= 70:
            status = "REVIEW"

        else:
            status = "DISCREPANCY"

        results.append({

            "check": "PAN name vs Udyam enterprise name",

            "pan_name": pan_name,

            "udyam_name": udyam_name,

            "similarity": round(score, 2),

            "status": status

        })


    if gst_name and udyam_name:

        score = text_similarity(
            gst_name,
            udyam_name
        ) * 100

        if score >= 85:
            status = "CONSISTENT"

        elif score >= 70:
            status = "REVIEW"

        else:
            status = "DISCREPANCY"

        results.append({

            "check": "GST legal name vs Udyam enterprise name",

            "gst_name": gst_name,

            "udyam_name": udyam_name,

            "similarity": round(score, 2),

            "status": status

        })


    if pan_number:

        pan_valid = bool(
            re.fullmatch(
                r"[A-Z]{5}[0-9]{4}[A-Z]",
                str(pan_number).upper()
            )
        )

        results.append({

            "check": "PAN format",

            "value": pan_number,

            "status":
                "VALID_FORMAT"
                if pan_valid
                else "INVALID_FORMAT"

        })


    if gstin:

        gstin_valid = bool(
            re.fullmatch(
                r"[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][0-9A-Z]Z[0-9A-Z]",
                str(gstin).upper()
            )
        )

        results.append({

            "check": "GSTIN format",

            "value": gstin,

            "status":
                "VALID_FORMAT"
                if gstin_valid
                else "INVALID_FORMAT"

        })


    return results


# ============================================================
# IMPORTANT IDENTIFIER CHECKS
# ============================================================

def check_important_identifiers(identity):

    results = []

    pan_number = identity["pan"].get("number")
    gstin = identity["gst"].get("gstin")

    if pan_number:
        pan_number = str(
            pan_number
        ).upper().strip()

    if gstin:
        gstin = str(
            gstin
        ).upper().strip()


    if pan_number and gstin:

        if len(gstin) == 15:

            gst_pan = gstin[2:12]

            if gst_pan == pan_number:

                results.append({

                    "check": "PAN ↔ GSTIN PAN",

                    "status": "CONSISTENT",

                    "pan": pan_number,

                    "gstin": gstin,

                    "gstin_embedded_pan": gst_pan,

                    "severity": "NONE",

                    "reason":
                        "PAN matches the PAN embedded in the GSTIN."

                })

            else:

                results.append({

                    "check": "PAN ↔ GSTIN PAN",

                    "status": "DISCREPANCY",

                    "pan": pan_number,

                    "gstin": gstin,

                    "gstin_embedded_pan": gst_pan,

                    "severity": "CRITICAL",

                    "reason":
                        "PAN does not match the PAN embedded in the GSTIN."

                })


    return results



# ============================================================
# IDENTITY GRAPH / CROSS-DOCUMENT RELATIONSHIP CHECKS
# ============================================================

def check_identity_graph(identity):

    """
    Analyze relationships between identity information
    extracted from multiple bidder documents.

    This is deterministic consistency analysis.
    It does not perform authoritative government verification.
    """

    results = []

    pan_data = identity.get("pan", {})
    gst_data = identity.get("gst", {})
    udyam_data = identity.get("udyam", {})
    mca_data = identity.get("mca", {})
    itr_data = identity.get("itr", {})

    pan = pan_data.get("number")
    pan_name = pan_data.get("name")

    gstin = gst_data.get("gstin")
    gst_name = gst_data.get("name")
    gst_state = gst_data.get("state")

    udyam_pan = udyam_data.get("pan")
    udyam_gstin = udyam_data.get("gstin")
    udyam_name = udyam_data.get("name")

    mca_name = mca_data.get("name")
    mca_state = mca_data.get("state")
    mca_status = mca_data.get("status")

    itr_pan = itr_data.get("pan")
    itr_name = itr_data.get("name")

    # --------------------------------------------------------
    # PAN ↔ UDYAM PAN
    # --------------------------------------------------------

    if pan and udyam_pan:

        pan_clean = str(pan).upper().strip()
        udyam_pan_clean = str(udyam_pan).upper().strip()

        if pan_clean == udyam_pan_clean:

            results.append({
                "check": "PAN ↔ UDYAM PAN",
                "status": "CONSISTENT",
                "severity": "NONE",
                "reason": "PAN matches the PAN reported in Udyam data."
            })

        else:

            results.append({
                "check": "PAN ↔ UDYAM PAN",
                "status": "DISCREPANCY",
                "severity": "CRITICAL",
                "reason": "PAN does not match the PAN reported in Udyam data."
            })

    # --------------------------------------------------------
    # PAN ↔ ITR PAN
    # --------------------------------------------------------

    if pan and itr_pan:

        pan_clean = str(pan).upper().strip()
        itr_pan_clean = str(itr_pan).upper().strip()

        if pan_clean == itr_pan_clean:

            results.append({
                "check": "PAN ↔ ITR PAN",
                "status": "CONSISTENT",
                "severity": "NONE",
                "reason": "PAN matches the PAN reported in ITR data."
            })

        else:

            results.append({
                "check": "PAN ↔ ITR PAN",
                "status": "DISCREPANCY",
                "severity": "CRITICAL",
                "reason": "PAN does not match the PAN reported in ITR data."
            })

    # --------------------------------------------------------
    # GSTIN ↔ UDYAM GSTIN
    # --------------------------------------------------------

    if gstin and udyam_gstin:

        gst_clean = str(gstin).upper().strip()
        udyam_gst_clean = str(udyam_gstin).upper().strip()

        if gst_clean == udyam_gst_clean:

            results.append({
                "check": "GSTIN ↔ UDYAM GSTIN",
                "status": "CONSISTENT",
                "severity": "NONE",
                "reason": "GSTIN matches the GSTIN reported in Udyam data."
            })

        else:

            results.append({
                "check": "GSTIN ↔ UDYAM GSTIN",
                "status": "DISCREPANCY",
                "severity": "CRITICAL",
                "reason": "GSTIN does not match the GSTIN reported in Udyam data."
            })

    # --------------------------------------------------------
    # PAN ↔ MCA COMPANY NAME
    # --------------------------------------------------------

    if pan_name and mca_name:

        score = text_similarity(
            pan_name,
            mca_name
        ) * 100

        if score >= 85:
            status = "CONSISTENT"
            severity = "NONE"

        elif score >= 70:
            status = "REVIEW"
            severity = "MEDIUM"

        else:
            status = "DISCREPANCY"
            severity = "HIGH"

        results.append({
            "check": "PAN name ↔ MCA company name",
            "status": status,
            "severity": severity,
            "similarity": round(score, 2),
            "reason": "PAN-holder name and MCA company name were compared for identity consistency."
        })

    # --------------------------------------------------------
    # GST ↔ MCA COMPANY NAME
    # --------------------------------------------------------

    if gst_name and mca_name:

        score = text_similarity(
            gst_name,
            mca_name
        ) * 100

        if score >= 85:
            status = "CONSISTENT"
            severity = "NONE"

        elif score >= 70:
            status = "REVIEW"
            severity = "MEDIUM"

        else:
            status = "DISCREPANCY"
            severity = "HIGH"

        results.append({
            "check": "GST legal name ↔ MCA company name",
            "status": status,
            "severity": severity,
            "similarity": round(score, 2),
            "reason": "GST legal name and MCA company name were compared for identity consistency."
        })

    # --------------------------------------------------------
    # UDYAM ↔ MCA COMPANY NAME
    # --------------------------------------------------------

    if udyam_name and mca_name:

        score = text_similarity(
            udyam_name,
            mca_name
        ) * 100

        if score >= 85:
            status = "CONSISTENT"
            severity = "NONE"

        elif score >= 70:
            status = "REVIEW"
            severity = "MEDIUM"

        else:
            status = "DISCREPANCY"
            severity = "HIGH"

        results.append({
            "check": "Udyam enterprise name ↔ MCA company name",
            "status": status,
            "severity": severity,
            "similarity": round(score, 2),
            "reason": "Udyam enterprise name and MCA company name were compared for identity consistency."
        })

    # --------------------------------------------------------
    # ITR ↔ PAN NAME
    # --------------------------------------------------------

    if pan_name and itr_name:

        score = text_similarity(
            pan_name,
            itr_name
        ) * 100

        if score >= 85:
            status = "CONSISTENT"
            severity = "NONE"

        elif score >= 70:
            status = "REVIEW"
            severity = "MEDIUM"

        else:
            status = "DISCREPANCY"
            severity = "HIGH"

        results.append({
            "check": "PAN name ↔ ITR name",
            "status": status,
            "severity": severity,
            "similarity": round(score, 2),
            "reason": "PAN name and ITR name were compared for identity consistency."
        })

    # --------------------------------------------------------
    # GST STATE ↔ MCA STATE
    # --------------------------------------------------------

    if gst_state and mca_state:

        gst_state_clean = normalize_text(gst_state)
        mca_state_clean = normalize_text(mca_state)

        if gst_state_clean == mca_state_clean:

            results.append({
                "check": "GST state ↔ MCA registered state",
                "status": "CONSISTENT",
                "severity": "NONE",
                "reason": "GST state matches the MCA registered state."
            })

        else:

            results.append({
                "check": "GST state ↔ MCA registered state",
                "status": "REVIEW",
                "severity": "MEDIUM",
                "reason": "GST state differs from the MCA registered state and should be reviewed."
            })

    # --------------------------------------------------------
    # MCA COMPANY STATUS
    # --------------------------------------------------------

    if mca_status:

        status_clean = str(mca_status).upper().strip()

        if status_clean in {
            "ACTIVE",
            "ACTV",
            "ACTIVE COMPLIANT"
        }:

            results.append({
                "check": "MCA company status",
                "status": "ACTIVE",
                "severity": "NONE",
                "reason": "MCA data indicates that the company is active."
            })

        else:

            results.append({
                "check": "MCA company status",
                "status": "REVIEW",
                "severity": "MEDIUM",
                "reason": "MCA company status is not an active status and should be reviewed."
            })

    return results


# ============================================================
# WEIGHTED RISK
# ============================================================

def calculate_weighted_risk(
    consistency_results,
    identifier_results,
    identity_graph_results=None
):

    risk_points = 0

    findings = []

    name_scores = []


    for result in identifier_results:

        status = result.get("status")

        severity = result.get("severity")

        if status == "DISCREPANCY":

            if severity == "CRITICAL":
                risk_points += 60

            elif severity == "HIGH":
                risk_points += 40

            else:
                risk_points += 20

            findings.append({

                "check": result.get("check"),

                "type": "IDENTIFIER_CONFLICT",

                "severity": severity,

                "reason": result.get("reason")

            })


    for result in consistency_results:

        if "similarity" not in result:
            continue

        similarity = result["similarity"]

        name_scores.append(similarity)


        if result["status"] == "DISCREPANCY":

            risk_points += 20

            findings.append({

                "check": result.get("check"),

                "type": "NAME_CONFLICT",

                "severity": "HIGH",

                "reason":
                    "Company identity names have low similarity."

            })


        elif result["status"] == "REVIEW":

            risk_points += 10

            findings.append({

                "check": result.get("check"),

                "type": "NAME_REVIEW",

                "severity": "MEDIUM",

                "reason":
                    "Company identity names require review."

            })


    for result in consistency_results:

        if result.get("status") == "INVALID_FORMAT":

            risk_points += 15

            findings.append({

                "check": result.get("check"),

                "type": "INVALID_FORMAT",

                "severity": "HIGH",

                "reason":
                    "Identifier format is invalid."

            })



    # --------------------------------------------------------
    # IDENTITY GRAPH FINDINGS
    # --------------------------------------------------------

    if identity_graph_results:

        for result in identity_graph_results:

            status = result.get("status")
            severity = result.get("severity")

            if status == "DISCREPANCY":

                if severity == "CRITICAL":
                    points = 60

                elif severity == "HIGH":
                    points = 40

                else:
                    points = 20

                risk_points += points

                findings.append({

                    "check":
                        result.get("check"),

                    "type":
                        "IDENTITY_GRAPH_CONFLICT",

                    "severity":
                        severity,

                    "points":
                        points,

                    "reason":
                        result.get("reason"),

                    "similarity":
                        result.get("similarity")

                })

            elif status == "REVIEW":

                risk_points += 10

                findings.append({

                    "check":
                        result.get("check"),

                    "type":
                        "IDENTITY_GRAPH_REVIEW",

                    "severity":
                        "MEDIUM",

                    "points":
                        10,

                    "reason":
                        result.get("reason"),

                    "similarity":
                        result.get("similarity")

                })


    if name_scores:

        average_name_score = (
            sum(name_scores) /
            len(name_scores)
        )

    else:

        average_name_score = 0


    consistency_score = round(
        average_name_score,
        2
    )


    if risk_points >= 60:
        risk_level = "CRITICAL"

    elif risk_points >= 40:
        risk_level = "HIGH"

    elif risk_points >= 20:
        risk_level = "MEDIUM"

    else:
        risk_level = "LOW"


    return {

        "identity_consistency_score":
            consistency_score,

        "risk_points":
            risk_points,

        "risk_level":
            risk_level,

        "findings":
            findings

    }


# ============================================================
# ANOMALY DETECTION
# ============================================================

def dixy_anomaly_check(
    bidder_json,
    identity,
    consistency_results,
    identifier_results,
    identity_graph_results=None
):

    anomalies = []

    anomaly_points = 0


    for result in identifier_results:

        if result.get("status") == "DISCREPANCY":

            severity = result.get(
                "severity",
                "MEDIUM"
            )

            if severity == "CRITICAL":
                points = 50

            elif severity == "HIGH":
                points = 30

            else:
                points = 15

            anomaly_points += points

            anomalies.append({

                "type":
                    "IDENTIFIER_CONFLICT",

                "severity":
                    severity,

                "check":
                    result.get("check"),

                "points":
                    points,

                "reason":
                    result.get("reason")

            })


    for result in consistency_results:

        if "similarity" not in result:
            continue

        similarity = result["similarity"]


        if similarity < 70:

            anomaly_points += 20

            anomalies.append({

                "type":
                    "NAME_MISMATCH",

                "severity":
                    "HIGH",

                "check":
                    result.get("check"),

                "points":
                    20,

                "similarity":
                    similarity,

                "reason":
                    "Names across submitted documents have low similarity."

            })


        elif similarity < 85:

            anomaly_points += 8

            anomalies.append({

                "type":
                    "NAME_VARIATION",

                "severity":
                    "MEDIUM",

                "check":
                    result.get("check"),

                "points":
                    8,

                "similarity":
                    similarity,

                "reason":
                    "Names differ enough to warrant review."

            })


    for result in consistency_results:

        if result.get("status") == "INVALID_FORMAT":

            anomaly_points += 20

            anomalies.append({

                "type":
                    "INVALID_IDENTIFIER",

                "severity":
                    "HIGH",

                "check":
                    result.get("check"),

                "points":
                    20,

                "reason":
                    "Identifier does not match the expected format."

            })



    # --------------------------------------------------------
    # IDENTITY GRAPH ANOMALIES
    # --------------------------------------------------------

    if identity_graph_results:

        for result in identity_graph_results:

            status = result.get("status")
            severity = result.get("severity")

            if status == "DISCREPANCY":

                if severity == "CRITICAL":
                    points = 50

                elif severity == "HIGH":
                    points = 30

                else:
                    points = 15

                anomaly_points += points

                anomalies.append({

                    "type":
                        "IDENTITY_GRAPH_CONFLICT",

                    "severity":
                        severity,

                    "check":
                        result.get("check"),

                    "points":
                        points,

                    "reason":
                        result.get("reason"),

                    "similarity":
                        result.get("similarity")

                })


            elif status == "REVIEW":

                anomaly_points += 8

                anomalies.append({

                    "type":
                        "IDENTITY_GRAPH_REVIEW",

                    "severity":
                        "MEDIUM",

                    "check":
                        result.get("check"),

                    "points":
                        8,

                    "reason":
                        result.get("reason"),

                    "similarity":
                        result.get("similarity")

                })


    pan_data = bidder_json.get(
        "pan",
        {}
    )

    gst_data = bidder_json.get(
        "gst",
        {}
    )


    missing_fields = []


    if not pan_data.get("pan"):
        missing_fields.append("PAN")


    if not gst_data.get("gstin"):
        missing_fields.append("GSTIN")


    if missing_fields:

        anomaly_points += 5

        anomalies.append({

            "type":
                "MISSING_IDENTITY_DATA",

            "severity":
                "LOW",

            "check":
                "Required identity fields",

            "points":
                5,

            "fields":
                missing_fields,

            "reason":
                "Some expected identity information was not available."

        })


    if anomaly_points >= 50:
        anomaly_level = "HIGH"

    elif anomaly_points >= 20:
        anomaly_level = "MEDIUM"

    elif anomaly_points > 0:
        anomaly_level = "LOW"

    else:
        anomaly_level = "NONE"


    return {

        "anomaly_score":
            anomaly_points,

        "anomaly_level":
            anomaly_level,

        "anomalies":
            anomalies

    }


# ============================================================
# EVIDENCE COVERAGE
# ============================================================

def calculate_evidence_summary(
    bidder_json,
    identity
):

    available_documents = []

    missing_documents = []


    if (
        identity["pan"].get("number")
        or
        identity["pan"].get("name")
    ):

        available_documents.append("PAN")

    else:

        missing_documents.append("PAN")


    if (
        identity["gst"].get("gstin")
        or
        identity["gst"].get("name")
    ):

        available_documents.append("GST")

    else:

        missing_documents.append("GST")


    if identity["udyam"].get("name"):

        available_documents.append("UDYAM")

    else:

        missing_documents.append("UDYAM")


    return {

        "available_documents":
            available_documents,

        "missing_documents":
            missing_documents,

        "evidence_count":
            len(available_documents),

        "evidence_coverage":
            round(
                len(available_documents) / 3 * 100,
                2
            )

    }


# ============================================================
# VERIFICATION ANALYSIS
# ============================================================

def analyze_verification_results(
    verification_results
):

    if not verification_results:

        return {

            "verification_available":
                False,

            "verification_status":
                "NOT_AVAILABLE",

            "verification_findings":
                []

        }


    findings = []


    for field, result in verification_results.items():

        if not isinstance(result, dict):
            continue


        status = str(
            result.get(
                "status",
                "UNKNOWN"
            )
        ).upper()


        if status in [
            "FAILED",
            "INVALID",
            "NOT_FOUND",
            "MISMATCH"
        ]:

            findings.append({

                "field":
                    field,

                "status":
                    status,

                "severity":
                    "HIGH",

                "reason":
                    result.get(
                        "reason",
                        "Authoritative verification reported a problem."
                    ),

                "source":
                    result.get("source")

            })


        elif status == "VERIFIED":

            findings.append({

                "field":
                    field,

                "status":
                    "VERIFIED",

                "severity":
                    "NONE",

                "reason":
                    "Authoritative verification succeeded.",

                "source":
                    result.get("source")

            })


    return {

        "verification_available":
            True,

        "verification_status":
            "AVAILABLE",

        "verification_findings":
            findings

    }


# ============================================================
# MASTER DIXY ANALYZER
# ============================================================

def dixy_analyze(
    bidder_json,
    verification_results=None
):

    identity = extract_identity_data(
        bidder_json
    )


    # Identity Graph analysis
    identity_graph_results = (
        check_identity_graph(
            identity
        )
    )


    consistency_results = (
        check_cross_document_consistency(
            identity
        )
    )


    identifier_results = (
        check_important_identifiers(
            identity
        )
    )


    risk_result = (
        calculate_weighted_risk(
            consistency_results,
            identifier_results,
            identity_graph_results
        )
    )


    anomaly_result = (
        dixy_anomaly_check(
            bidder_json,
            identity,
            consistency_results,
            identifier_results,
            identity_graph_results
        )
    )


    evidence_result = (
        calculate_evidence_summary(
            bidder_json,
            identity
        )
    )


    verification_analysis = (
        analyze_verification_results(
            verification_results
        )
    )


    verification_risk_points = 0


    for finding in verification_analysis[
        "verification_findings"
    ]:

        if finding["status"] in [
            "FAILED",
            "INVALID",
            "NOT_FOUND",
            "MISMATCH"
        ]:

            verification_risk_points += 50


    total_risk_points = (
        risk_result["risk_points"]
        +
        verification_risk_points
    )


    if total_risk_points >= 60:

        final_risk_level = "CRITICAL"

    elif total_risk_points >= 40:

        final_risk_level = "HIGH"

    elif total_risk_points >= 20:

        final_risk_level = "MEDIUM"

    else:

        final_risk_level = "LOW"


    critical_internal_conflict = any(

        item.get("status") == "DISCREPANCY"
        and
        item.get("severity") == "CRITICAL"

        for item in identifier_results

    )


    critical_identity_graph_conflict = any(

        item.get("status") == "DISCREPANCY"
        and
        item.get("severity") == "CRITICAL"

        for item in identity_graph_results

    )


    failed_authoritative_verification = any(

        item["status"] in [
            "FAILED",
            "INVALID",
            "NOT_FOUND",
            "MISMATCH"
        ]

        for item in verification_analysis[
            "verification_findings"
        ]

    )


    if (
        critical_internal_conflict
        or
        critical_identity_graph_conflict
        or
        failed_authoritative_verification
    ):

        final_status = "REVIEW"

        final_recommendation = (
            "MANUAL VERIFICATION REQUIRED"
        )

        final_explanation = (
            "A significant identity conflict was detected "
            "either within the submitted documents or in "
            "authoritative verification results. "
            "Procurement-officer review is required."
        )


    elif final_risk_level == "HIGH":

        final_status = "REVIEW"

        final_recommendation = (
            "MANUAL REVIEW REQUIRED"
        )

        final_explanation = (
            "Significant identity risk signals were detected "
            "across the available bidder evidence."
        )


    elif final_risk_level == "MEDIUM":

        final_status = "REVIEW"

        final_recommendation = (
            "REVIEW BEFORE ACCEPTANCE"
        )

        final_explanation = (
            "The available evidence is mostly consistent, "
            "but additional review is recommended."
        )


    else:

        final_status = "CONSISTENT"

        final_recommendation = (
            "IDENTITY CONSISTENT"
        )

        final_explanation = (
            "No significant identity conflicts were detected "
            "in the available evidence."
        )


    return {

        "status":
            final_status,

        "identity_consistency_score":
            risk_result[
                "identity_consistency_score"
            ],

        "evidence_coverage":
            evidence_result[
                "evidence_coverage"
            ],

        "available_documents":
            evidence_result[
                "available_documents"
            ],

        "missing_documents":
            evidence_result[
                "missing_documents"
            ],

        "risk_points":
            total_risk_points,

        "risk_level":
            final_risk_level,

        "consistency_checks":
            consistency_results,

        "identifier_checks":
            identifier_results,

        "identity_graph":
            identity_graph_results,

        "verification_analysis":
            verification_analysis,

        "anomaly_analysis":
            anomaly_result,

        "findings":
            risk_result["findings"],

        "recommendation":
            final_recommendation,

        "explanation":
            final_explanation
    }
