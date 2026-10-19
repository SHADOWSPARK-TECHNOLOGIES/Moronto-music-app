# MORONTO MORNICA — Commercial Validation, Pilot Plan & Play Store Compliance (Prompt F)

## 1. Demand Research & Honest Epistemic Framing

Third-party **vidIQ Nigerian monthly YouTube search estimates** provide directional evidence of strong organic search behavior for Afrobeats and Amapiano on YouTube:

- **Asake**: `207,295` est. monthly searches (`INFERRED — third-party estimate`)
- **Davido**: `123,964` est. monthly searches (`INFERRED — third-party estimate`)
- **Wizkid**: `94,555` est. monthly searches (`INFERRED — third-party estimate`)
- **Burna Boy**: `94,014` est. monthly searches (`INFERRED — third-party estimate`)
- **Rema**: `82,179` est. monthly searches (`INFERRED — third-party estimate`)
- **Omah Lay**: `30,694` est. monthly searches (`INFERRED — third-party estimate`)
- **Amapiano**: `25,401` est. monthly searches (`INFERRED — third-party estimate`)

**Commercial Interpretation**: Listeners already search heavily for major Afrobeats catalogs and Amapiano mixes on YouTube while keeping personal MP3/WhatsApp/device audio collections offline. Moronto Mornica bridges discovery and offline library organization without charging users for YouTube access.

---

## 2. Monetization Boundary & Independent Creator Launch Package

### What Is Always Free (Never Monetized)
- Searching and watching official YouTube music videos and channel shelves.
- Scanning, organizing, queueing, and playing user-owned offline device audio (`MP3`, `FLAC`, `M4A`, `WAV`, `OGG`).

### Commercial Offer (Hypothesis to Validate in 5-Artist Pilot)
- **Product**: **Moronto Verified Artist Launch Package**
  - Verified Creator Profile badge (`LICENSED` / `DEMO` provenance attestation).
  - Up to 3 rights-cleared demo tracks bundled in the Discovery Spotlight shelf.
  - Direct fan opt-in CTA (WhatsApp / email / social deep-links) + weekly click/play summary report.
- **Pilot Pricing Hypothesis**:
  - **One-Time Launch Setup**: `₦25,000 – ₦50,000` (approx. `$15 – $30 USD` equivalent) via manual invoice/receipt after rights verification (`INFERRED — pricing hypothesis, not yet validated by paid transactions`).

---

## 3. Five-Artist Lagos / Pan-African Pilot Plan

| Step | Action | Target Metric / Gate |
| :--- | :--- | :--- |
| **1. Outreach** | Invite 5 independent Afrobeats / Street-Pop / Amapiano artists or managers to test the Creator Launch Hub in Moronto Mornica. | `5 / 5` completed onboarding interviews |
| **2. Rights Vetting** | Collect signed/checked ownership attestation (`rightsAttested = true`) and up to 3 cleared audio previews per artist. | `100%` provenance compliance (`0` unverified uploads) |
| **3. Pilot Conversion** | Offer the Verified Artist Launch Package via manual invoice after a 7-day preview period. | `>= 2 / 5` paid pilot conversions before building automated checkout |
| **4. Unit Economics** | `Net Contribution = Pilot Fee - Payment Processing - Manual Onboarding Time (<= 1.5 hrs/artist) - Cloud/API Overhead ($0 on free tier)`. | Positive net margin per onboarded creator |

---

## 4. Google Play Store Readiness & Data Safety Summary

### Data Safety Questionnaire Mapping
- **Audio Files (`READ_MEDIA_AUDIO`)**: Processed **locally on-device only** to populate the user's "My Music" tabs (Songs, Folders, Artists, Albums). User audio files are **never uploaded** to ShadowSpark servers.
- **Creator Launch Inquiries**: Voluntary user-submitted form fields (`Artist Name`, `Contact Email / WhatsApp`, `Track Title`, `Genre`, `Rights Attestation`) stored locally on device in MVP and transmitted only when the user explicitly submits an inquiry.
- **YouTube Embedded Player**: Loads official `https://www.youtube.com/embed/{videoId}` over HTTPS; subject to Google/YouTube standard privacy policy disclosed in the in-app Privacy Notice.
