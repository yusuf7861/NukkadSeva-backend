## Plan: Provider Trust Overhaul

The main goal is to turn the provider experience from “generic profile card” into a trust-first marketplace surface. The backend already has some strong signals (`profilePicture`, `averageRating`, `reviewCount`, `serviceArea`, `availability`, `createdAt`), so the plan is to expose them consistently, add a few missing trust fields, and make sure the public listing/detail UI can render them as badges, proof, and social validation.

### Steps
1. Audit the provider trust data already available in `Provider`, `ProviderProfileResponseDto`, and `DashboardProviderDto`.
2. Enrich the public marketplace payload in `PublicController#getProviders` and `ProviderServiceImpl#searchProviders` with visible trust signals.
3. Expand provider detail responses via `ProviderMapper#toProviderDetailDto` and `AdminController#getProviderDetails` for full trust disclosure.
4. Add public-facing review content by reusing `ReviewRepository` and separating it from provider-only `ReviewController#getProviderReviews`.
5. Model missing trust fields such as verified badges, response time, guarantees, and portfolio support if the UI needs them.
6. Update the marketplace/provider page layout to show avatar, business + owner name, badges, jobs completed, reviews, availability, and service area prominently.

### Further Considerations
1. The current repo is backend-only, so the frontend component/template that renders the provider card will need the same trust fields wired in separately.
2. Decide whether verification badges should be fully shown to customers or partially masked for privacy/compliance.
3. If portfolio/gallery data doesn’t already exist, choose between a lightweight image list on the provider entity or a separate gallery model.

