from .schemas import Bidder


def create_demo_bidders():

    # -----------------------------------------------------
    # B1 - B2: STRONG CONNECTION
    # -----------------------------------------------------

    bidder_1 = Bidder(
        bidder_id="BIDDER-001",
        company_name="Alpha Infrastructure Pvt Ltd",
        pan="ABCDE1234F",
        gstin="19ABCDE1234F1Z5",
        udyam="UDYAM-WB-01-000001",
        address="12 Park Street, Kolkata",
        phone="9876543210",
        email="alpha@example.com",
        authorized_person="RAHUL SHARMA",
        bank_account="1234567890",
        document_hashes=[
            "HASH_DOCUMENT_001"
        ]
    )

    bidder_2 = Bidder(
        bidder_id="BIDDER-002",
        company_name="Alpha Engineering Solutions",
        pan="ABCDE1234F",
        gstin="19ABCDE1234F1Z5",
        udyam="UDYAM-WB-02-000002",
        address="12 Park Street, Kolkata",
        phone="9876543210",
        email="different@example.com",
        authorized_person="RAHUL SHARMA",
        bank_account="1234567890",
        document_hashes=[
            "HASH_DOCUMENT_001"
        ]
    )

    # -----------------------------------------------------
    # B3: WEAKER CONNECTION WITH B1
    # -----------------------------------------------------

    bidder_3 = Bidder(
        bidder_id="BIDDER-003",
        company_name="Beta Construction Works",
        pan="BBBBB2222B",
        gstin="27BBBBB2222B1Z2",
        udyam="UDYAM-MH-03-000003",
        address="Mumbai",
        phone="9876543210",
        email="beta@example.com",
        authorized_person="AMIT PATEL",
        bank_account="2222222222",
        document_hashes=[
            "HASH_DOCUMENT_003"
        ]
    )

    # -----------------------------------------------------
    # B4: COMPLETELY INDEPENDENT
    # -----------------------------------------------------

    bidder_4 = Bidder(
        bidder_id="BIDDER-004",
        company_name="Bharat Foods Ltd",
        pan="CCCCC3333C",
        gstin="29CCCCC3333C1Z3",
        udyam="UDYAM-KA-04-000004",
        address="Bangalore",
        phone="9999999999",
        email="bharat@example.com",
        authorized_person="SURESH KUMAR",
        bank_account="3333333333",
        document_hashes=[
            "HASH_DOCUMENT_004"
        ]
    )

    # -----------------------------------------------------
    # B5: DOCUMENT REUSE WITH B3
    # -----------------------------------------------------

    bidder_5 = Bidder(
        bidder_id="BIDDER-005",
        company_name="Gamma Industrial Systems",
        pan="DDDDD4444D",
        gstin="33DDDDD4444D1Z4",
        udyam="UDYAM-TN-05-000005",
        address="Chennai",
        phone="8888888888",
        email="gamma@example.com",
        authorized_person="VIJAY SINGH",
        bank_account="4444444444",
        document_hashes=[
            "HASH_DOCUMENT_003"
        ]
    )

    return [
        bidder_1,
        bidder_2,
        bidder_3,
        bidder_4,
        bidder_5
    ]